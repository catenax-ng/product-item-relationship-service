//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.aaswrapper.registry.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.catenax.irs.component.assetadministrationshell.AssetAdministrationShellDescriptor;
import net.catenax.irs.component.assetadministrationshell.IdentifierKeyValuePair;
import net.catenax.irs.component.assetadministrationshell.SubmodelDescriptor;
import net.catenax.irs.component.enums.AspectType;
import net.catenax.irs.dto.JobParameter;
import org.springframework.stereotype.Service;

/**
 * Public API Facade for digital twin registry domain
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DigitalTwinRegistryFacade {

    private final DigitalTwinRegistryClient digitalTwinRegistryClient;

    /**
     * Combines required data from Digital Twin Registry Service
     *
     * @param globalAssetId The Asset Administration Shell's global id
     * @param jobData       the job data parameters
     * @return list of submodel addresses
     */
    public AssetAdministrationShellDescriptor getAAShellDescriptor(final String globalAssetId,
            final JobParameter jobData) {
        final String aaShellIdentification = getAAShellIdentificationOrGlobalAssetId(globalAssetId);
        log.info("Retrieved AAS Identification {} for globalAssetId {}", aaShellIdentification, globalAssetId);

        final AssetAdministrationShellDescriptor assetAdministrationShellDescriptor = digitalTwinRegistryClient.getAssetAdministrationShellDescriptor(aaShellIdentification);
        final List<SubmodelDescriptor> submodelDescriptors = filterByAspectType(
                assetAdministrationShellDescriptor, jobData.getAspectTypes());
        return assetAdministrationShellDescriptor.toBuilder().submodelDescriptors(submodelDescriptors).build();
    }

    private String getAAShellIdentificationOrGlobalAssetId(final String globalAssetId) {
        final IdentifierKeyValuePair identifierKeyValuePair = IdentifierKeyValuePair.builder()
                                                                           .key("globalAssetId")
                                                                           .value(globalAssetId)
                                                                           .build();

        final List<String> allAssetAdministrationShellIdsByAssetLink = digitalTwinRegistryClient.getAllAssetAdministrationShellIdsByAssetLink(
                Collections.singletonList(identifierKeyValuePair));

        return allAssetAdministrationShellIdsByAssetLink.stream().findFirst().orElse(globalAssetId);
    }

    /**
     * @param aspectTypes        the aspect types which should be filtered by
     * @return True, if the aspect type of the submodelDescriptor is part of
     * the given consumer aspectTypes
     */
    private List<SubmodelDescriptor> filterByAspectType(final AssetAdministrationShellDescriptor assetAdministrationShellDescriptor,
            final List<String> aspectTypes) {

        final List<String> filterAspectTypes = new ArrayList<>(aspectTypes);

        if (notContainsAssemblyPartRelationship(filterAspectTypes)) {
            filterAspectTypes.add(AspectType.ASSEMBLY_PART_RELATIONSHIP.toString());
            log.info("Adjusted Aspect Type Filter '{}'", filterAspectTypes);
        }

        return assetAdministrationShellDescriptor.filterDescriptorsByAspectTypes(filterAspectTypes);
    }

    private boolean notContainsAssemblyPartRelationship(final List<String> filterAspectTypes) {
        return !filterAspectTypes.contains(AspectType.ASSEMBLY_PART_RELATIONSHIP.toString());
    }

}
