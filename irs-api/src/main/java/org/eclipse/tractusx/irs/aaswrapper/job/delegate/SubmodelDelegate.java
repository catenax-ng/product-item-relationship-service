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
package org.eclipse.tractusx.irs.aaswrapper.job.delegate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.github.resilience4j.retry.RetryRegistry;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.irs.aaswrapper.job.AASTransferProcess;
import org.eclipse.tractusx.irs.aaswrapper.job.ItemContainer;
import org.eclipse.tractusx.irs.aaswrapper.submodel.domain.SubmodelFacade;
import org.eclipse.tractusx.irs.component.Submodel;
import org.eclipse.tractusx.irs.component.Tombstone;
import org.eclipse.tractusx.irs.component.assetadministrationshell.Endpoint;
import org.eclipse.tractusx.irs.component.assetadministrationshell.SubmodelDescriptor;
import org.eclipse.tractusx.irs.dto.JobParameter;
import org.eclipse.tractusx.irs.exceptions.JsonParseException;
import org.eclipse.tractusx.irs.semanticshub.SemanticsHubFacade;
import org.eclipse.tractusx.irs.services.validation.InvalidSchemaException;
import org.eclipse.tractusx.irs.services.validation.JsonValidatorService;
import org.eclipse.tractusx.irs.services.validation.ValidationResult;
import org.eclipse.tractusx.irs.util.JsonUtil;
import org.springframework.web.client.RestClientException;

/**
 * Builds submodels array for AAShell from previous steps.
 * All submodels are being retrieved from EDC's components.
 * Additionally submodel descriptors from shell are being filtered to requested aspect types.
 */
@Slf4j
public class SubmodelDelegate extends AbstractDelegate {

    private final SubmodelFacade submodelFacade;
    private final SemanticsHubFacade semanticsHubFacade;
    private final JsonValidatorService jsonValidatorService;
    private final JsonUtil jsonUtil;

    public SubmodelDelegate(final AbstractDelegate nextStep,
            final SubmodelFacade submodelFacade,
            final SemanticsHubFacade semanticsHubFacade,
            final JsonValidatorService jsonValidatorService,
            final JsonUtil jsonUtil) {
        super(nextStep);
        this.submodelFacade = submodelFacade;
        this.semanticsHubFacade = semanticsHubFacade;
        this.jsonValidatorService = jsonValidatorService;
        this.jsonUtil = jsonUtil;
    }

    @Override
    public ItemContainer process(final ItemContainer.ItemContainerBuilder itemContainerBuilder, final JobParameter jobData,
            final AASTransferProcess aasTransferProcess, final String itemId) {

        itemContainerBuilder.build().getShells().stream().findFirst().ifPresent(
            shell -> {
                try {
                    final List<SubmodelDescriptor> aasSubmodelDescriptors = shell.getSubmodelDescriptors();
                    log.info("Retrieved {} SubmodelDescriptor for itemId {}", aasSubmodelDescriptors.size(), itemId);

                    final List<SubmodelDescriptor> filteredSubmodelDescriptorsByAspectType = shell.filterDescriptorsByAspectTypes(
                            jobData.getAspectTypes());

                    if (jobData.isCollectAspects()) {
                        log.info("Collecting Submodels.");
                        filteredSubmodelDescriptorsByAspectType.forEach(submodelDescriptor -> itemContainerBuilder.submodels(
                                getSubmodels(submodelDescriptor, itemContainerBuilder, itemId)));
                    }
                    log.debug("Unfiltered SubmodelDescriptor: {}", aasSubmodelDescriptors);
                    log.debug("Filtered SubmodelDescriptor: {}", filteredSubmodelDescriptorsByAspectType);

                    shell.setSubmodelDescriptors(filteredSubmodelDescriptorsByAspectType);

                } catch (RestClientException e) {
                    log.info("Shell Endpoint could not be retrieved for Item: {}. Creating Tombstone.", itemId);
                    itemContainerBuilder.tombstone(Tombstone.from(itemId, null, e, retryCount));
                }
            }
        );

        return next(itemContainerBuilder, jobData, aasTransferProcess, itemId);
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
}
