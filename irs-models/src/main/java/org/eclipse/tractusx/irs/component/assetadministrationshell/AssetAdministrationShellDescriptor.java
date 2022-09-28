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
package org.eclipse.tractusx.irs.component.assetadministrationshell;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.irs.component.enums.AspectType;

/**
 * AssetAdministrationShellDescriptor
 */
@Data
@Jacksonized
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Slf4j
public class AssetAdministrationShellDescriptor {

    /**
     * administration
     */
    private AdministrativeInformation administration;
    /**
     * description
     */
    private List<LangString> description;
    /**
     * globalAssetId
     */
    private Reference globalAssetId;
    /**
     * idShort
     */
    private String idShort;
    /**
     * identification
     */
    private String identification;
    /**
     * specificAssetIds
     */
    private List<IdentifierKeyValuePair> specificAssetIds;
    /**
     * submodelDescriptors
     */
    private List<SubmodelDescriptor> submodelDescriptors;

    /**
     * @return ManufacturerId value from Specific Asset Ids
     */
    public Optional<String> findManufacturerId() {
        return this.specificAssetIds.stream().filter(assetId -> "ManufacturerId".equals(assetId.getKey())).map(IdentifierKeyValuePair::getValue).findFirst();
    }

    /**
     * @param aspectTypes        the aspect types which should be filtered by
     * @return AssetAdministrationShellDescriptor with filtered submodel descriptors
     */
    public AssetAdministrationShellDescriptor withFilteredSubmodelDescriptors(final List<String> aspectTypes) {
        final List<String> filterAspectTypes = new ArrayList<>(aspectTypes);

        if (notContainsAssemblyPartRelationship(filterAspectTypes)) {
            filterAspectTypes.add(AspectType.ASSEMBLY_PART_RELATIONSHIP.toString());
            log.info("Adjusted Aspect Type Filter '{}'", filterAspectTypes);
        }

        this.setSubmodelDescriptors(this.filterDescriptorsByAspectTypes(filterAspectTypes));
        return this;
    }

    /**
     * @return The filtered list containing only SubmodelDescriptors which are AssemblyPartRelationship
     */
    public List<String> findAssemblyPartRelationshipEndpointAddresses() {
        final List<SubmodelDescriptor> filteredSubmodelDescriptors = filterDescriptorsByAssemblyPartRelationship();
        return filteredSubmodelDescriptors.stream()
                                          .map(SubmodelDescriptor::getEndpoints)
                                          .flatMap(endpoints -> endpoints.stream()
                                                                         .map(Endpoint::getProtocolInformation)
                                                                         .map(ProtocolInformation::getEndpointAddress))
                                          .collect(Collectors.toList());
    }

    /**
     * @param aspectTypes The AspectTypes for which should be filtered
     * @return The filtered list containing only SubmodelDescriptors which are provided as AspectTypes
     */
    public List<SubmodelDescriptor> filterDescriptorsByAspectTypes(final List<String> aspectTypes) {
        log.info("Filtering for Aspect Types '{}'", aspectTypes);
        return this.submodelDescriptors.stream()
                                       .filter(submodelDescriptor -> aspectTypes.stream()
                                                                                .anyMatch(type -> isMatching(
                                                                                        submodelDescriptor, type)))

                                       .collect(Collectors.toList());
    }

    /**
     * @return The SubmodelDescriptors which are of AspectType AssemblyPartRelationship
     */
    private List<SubmodelDescriptor> filterDescriptorsByAssemblyPartRelationship() {
        return filterDescriptorsByAspectTypes(List.of(AspectType.ASSEMBLY_PART_RELATIONSHIP.toString()));
    }

    private boolean isMatching(final SubmodelDescriptor submodelDescriptor, final String aspectTypeFilter) {
        final Optional<String> submodelAspectType = submodelDescriptor.getSemanticId().getValue().stream().findFirst();
        return submodelAspectType.map(semanticId -> semanticId.endsWith("#" + aspectTypeFilter) || contains(semanticId, aspectTypeFilter))
                                 .orElse(false);
    }

    private boolean contains(final String semanticId, final String aspectTypeFilter) {
        // https://stackoverflow.com/a/3752693
        final String[] split = aspectTypeFilter.split("(?=\\p{Lu})");
        final String join = String.join("_", split).toLowerCase(Locale.ROOT);
        log.debug("lower case aspect: '{}'", join);
        return semanticId.contains(join);
    }

    private boolean notContainsAssemblyPartRelationship(final List<String> filterAspectTypes) {
        return !filterAspectTypes.contains(AspectType.ASSEMBLY_PART_RELATIONSHIP.toString());
    }
}
