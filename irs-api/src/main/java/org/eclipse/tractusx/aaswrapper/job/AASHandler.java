/********************************************************************************
 * Copyright (c) 2021,2022
 *       2022: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 * Copyright (c) 2021,2022 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0. *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/
package org.eclipse.tractusx.aaswrapper.job;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import io.github.resilience4j.retry.RetryRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.aaswrapper.registry.domain.DigitalTwinRegistryFacade;
import org.eclipse.tractusx.aaswrapper.submodel.domain.SubmodelFacade;
import org.eclipse.tractusx.bpdm.BpdmFacade;
import org.eclipse.tractusx.component.Bpn;
import org.eclipse.tractusx.component.GlobalAssetIdentification;
import org.eclipse.tractusx.component.LinkedItem;
import org.eclipse.tractusx.component.Relationship;
import org.eclipse.tractusx.component.Submodel;
import org.eclipse.tractusx.component.Tombstone;
import org.eclipse.tractusx.component.assetadministrationshell.AssetAdministrationShellDescriptor;
import org.eclipse.tractusx.component.assetadministrationshell.Endpoint;
import org.eclipse.tractusx.component.assetadministrationshell.SubmodelDescriptor;
import org.eclipse.tractusx.dto.JobParameter;
import org.eclipse.tractusx.exceptions.JsonParseException;
import org.eclipse.tractusx.semanticshub.SemanticsHubFacade;
import org.eclipse.tractusx.services.validation.InvalidSchemaException;
import org.eclipse.tractusx.services.validation.JsonValidatorService;
import org.eclipse.tractusx.services.validation.ValidationResult;
import org.eclipse.tractusx.util.JsonUtil;
import org.springframework.web.client.RestClientException;

/**
 * Class to process Shells and Submodels and fill a ItemContainer with Relationships, Shells,
 * Tombstones and Submodels.
 */
@Slf4j
@RequiredArgsConstructor
public class AASHandler {

    private final DigitalTwinRegistryFacade registryFacade;
    private final SubmodelFacade submodelFacade;
    private final SemanticsHubFacade semanticsHubFacade;
    private final BpdmFacade bpdmFacade;
    private final JsonValidatorService jsonValidatorService;
    private final JsonUtil jsonUtil;

    /**
     * @param jobData            The job parameters used for filtering
     * @param aasTransferProcess The transfer process which will be filled with childIds for
     *                           further processing
     * @param itemId             The id of the current item
     * @return The ItemContainer filled with Relationships, Shells, Submodels (if requested in jobData)
     * and Tombstones (if requests fail).
     */
    public ItemContainer collectShellAndSubmodels(final JobParameter jobData,
            final AASTransferProcess aasTransferProcess, final String itemId) {

        final ItemContainer.ItemContainerBuilder itemContainerBuilder = ItemContainer.builder();
        final int retryCount = RetryRegistry.ofDefaults().getDefaultConfig().getMaxAttempts();

        try {
            final AssetAdministrationShellDescriptor aasShell = registryFacade.getAAShellDescriptor(itemId, jobData);
            final List<SubmodelDescriptor> aasSubmodelDescriptors = aasShell.getSubmodelDescriptors();

            log.info("Retrieved {} SubmodelDescriptor for itemId {}", aasSubmodelDescriptors.size(), itemId);

            aasShell.findAssemblyPartRelationshipEndpointAddresses().forEach(address -> {
                try {
                    final List<Relationship> relationships = submodelFacade.getRelationships(address, jobData);
                    processEndpoint(aasTransferProcess, itemContainerBuilder, relationships);
                } catch (RestClientException | IllegalArgumentException e) {
                    log.info("Submodel Endpoint could not be retrieved for Endpoint: {}. Creating Tombstone.", address);
                    itemContainerBuilder.tombstone(Tombstone.from(itemId, address, e, retryCount));
                } catch (JsonParseException e) {
                    log.info("Submodel payload did not match the expected AspectType. Creating Tombstone.");
                    itemContainerBuilder.tombstone(Tombstone.from(itemId, address, e, retryCount));
                }
            });
            final List<SubmodelDescriptor> filteredSubmodelDescriptorsByAspectType = aasShell.filterDescriptorsByAspectTypes(
                    jobData.getAspectTypes());

            if (jobData.isCollectAspects()) {
                log.info("Collecting Submodels.");
                collectSubmodels(filteredSubmodelDescriptorsByAspectType, itemContainerBuilder, itemId);
            }
            log.debug("Unfiltered SubmodelDescriptor: {}", aasSubmodelDescriptors);
            log.debug("Filtered SubmodelDescriptor: {}", filteredSubmodelDescriptorsByAspectType);

            itemContainerBuilder.shell(
                    aasShell.toBuilder().submodelDescriptors(filteredSubmodelDescriptorsByAspectType).build());

            aasShell.findManufacturerId().ifPresent(manufacturerId -> {
                final Optional<String> manufacturerName = bpdmFacade.findManufacturerName(manufacturerId);
                manufacturerName.ifPresent(name -> itemContainerBuilder.bpn(Bpn.of(manufacturerId, name)));
            });

        } catch (RestClientException e) {
            log.info("Shell Endpoint could not be retrieved for Item: {}. Creating Tombstone.", itemId);
            itemContainerBuilder.tombstone(Tombstone.from(itemId, null, e, retryCount));
        }
        return itemContainerBuilder.build();
    }

    private void collectSubmodels(final List<SubmodelDescriptor> submodelDescriptors,
            final ItemContainer.ItemContainerBuilder itemContainerBuilder, final String itemId) {
        submodelDescriptors.forEach(submodelDescriptor -> itemContainerBuilder.submodels(
                getSubmodels(submodelDescriptor, itemContainerBuilder, itemId)));
    }

    private List<Submodel> getSubmodels(final SubmodelDescriptor submodelDescriptor,
            final ItemContainer.ItemContainerBuilder itemContainerBuilder, final String itemId) {
        final List<Submodel> submodels = new ArrayList<>();
        submodelDescriptor.getEndpoints().forEach(endpoint -> {
            try {
                final String jsonSchema = semanticsHubFacade.getModelJsonSchema(submodelDescriptor.getAspectType());
                final String submodelRawPayload = requestSubmodelAsString(endpoint);

                final ValidationResult validationResult = jsonValidatorService.validate(jsonSchema, submodelRawPayload);

                if (validationResult.isValid()) {
                    final Submodel submodel = Submodel.from(submodelDescriptor.getIdentification(),
                            submodelDescriptor.getAspectType(), jsonUtil.fromString(submodelRawPayload, Map.class));
                    submodels.add(submodel);
                } else {
                    final String errors = String.join(", ", validationResult.getValidationErrors());
                    itemContainerBuilder.tombstone(
                            Tombstone.from(itemId, endpoint.getProtocolInformation().getEndpointAddress(),
                                    new IllegalArgumentException("Submodel payload validation failed. " + errors), 0));
                }
            } catch (JsonParseException e) {
                itemContainerBuilder.tombstone(
                        Tombstone.from(itemId, endpoint.getProtocolInformation().getEndpointAddress(), e,
                                RetryRegistry.ofDefaults().getDefaultConfig().getMaxAttempts()));
                log.info("Submodel payload did not match the expected AspectType. Creating Tombstone.");
            } catch (InvalidSchemaException | RestClientException e) {
                itemContainerBuilder.tombstone(
                        Tombstone.from(itemId, endpoint.getProtocolInformation().getEndpointAddress(), e, 0));
                log.info("Cannot load JSON schema for validation. Creating Tombstone.");
            }
        });
        return submodels;
    }

    private String requestSubmodelAsString(final Endpoint endpoint) {
        return submodelFacade.getSubmodelRawPayload(endpoint.getProtocolInformation().getEndpointAddress());
    }

    private void processEndpoint(final AASTransferProcess aasTransferProcess,
            final ItemContainer.ItemContainerBuilder itemContainer, final List<Relationship> relationships) {
        final List<String> childIds = relationships.stream()
                                                   .map(Relationship::getLinkedItem)
                                                   .map(LinkedItem::getChildCatenaXId)
                                                   .map(GlobalAssetIdentification::getGlobalAssetId)
                                                   .collect(Collectors.toList());
        log.info("Processing Relationships with {} children", childIds.size());

        aasTransferProcess.addIdsToProcess(childIds);
        itemContainer.relationships(relationships);
    }
}
