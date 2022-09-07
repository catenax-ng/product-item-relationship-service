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
package org.eclipse.tractusx.irs.dto.assetadministrationshell;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.tractusx.irs.util.TestMother.shellDescriptor;
import static org.eclipse.tractusx.irs.util.TestMother.submodelDescriptor;
import static org.eclipse.tractusx.irs.util.TestMother.submodelDescriptorWithoutEndpoint;

import java.util.List;

import org.eclipse.tractusx.irs.component.assetadministrationshell.AssetAdministrationShellDescriptor;
import org.eclipse.tractusx.irs.component.assetadministrationshell.SubmodelDescriptor;
import org.junit.jupiter.api.Test;

class AssetAdministrationShellDescriptorTest {

    final String assemblyPartRelationshipId = "urn:bamm:com.catenax.assembly_part_relationship:1.0.0";
    final String assemblyPartRelationshipIdWithAspectName = "urn:bamm:com.catenax.assembly_part_relationship:1.0.0#AssemblyPartRelationship";
    final String serialPartTypizationId = "urn:bamm:com.catenax.serial_part_typization:1.0.0";
    final String serialPartTypizationIdWithAspectName = "urn:bamm:com.catenax.serial_part_typization:1.0.0#SerialPartTypization";

    @Test
    void shouldFilterByAssemblyPartRelationshipWhenEndingWithAspectName() {
        // Arrange
        final AssetAdministrationShellDescriptor shellDescriptor = shellDescriptor(
                List.of(submodelDescriptorWithoutEndpoint(assemblyPartRelationshipIdWithAspectName)));
        // Act
        final List<SubmodelDescriptor> result = shellDescriptor.withFilteredSubmodelDescriptors(List.of()).getSubmodelDescriptors();

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSemanticId().getValue().get(0)).isEqualTo(assemblyPartRelationshipIdWithAspectName);
    }

    @Test
    void shouldFilterByAssemblyPartRelationshipWhenNotEndingWithAspectName() {
        // Arrange
        final AssetAdministrationShellDescriptor shellDescriptor = shellDescriptor(
                List.of(submodelDescriptorWithoutEndpoint(assemblyPartRelationshipId)));
        // Act
        final List<SubmodelDescriptor> result = shellDescriptor.withFilteredSubmodelDescriptors(List.of()).getSubmodelDescriptors();

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSemanticId().getValue().get(0)).isEqualTo(assemblyPartRelationshipId);
    }

    @Test
    void shouldFilterByAspectTypeWhenEndingWithAspectName() {
        // Arrange
        final AssetAdministrationShellDescriptor shellDescriptor = shellDescriptor(
                List.of(submodelDescriptorWithoutEndpoint(assemblyPartRelationshipIdWithAspectName)));
        final List<String> aspectTypeFilter = List.of("AssemblyPartRelationship");

        // Act
        final List<SubmodelDescriptor> result = shellDescriptor.filterDescriptorsByAspectTypes(aspectTypeFilter);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSemanticId().getValue().get(0)).isEqualTo(assemblyPartRelationshipIdWithAspectName);
    }

    @Test
    void shouldFilterByAspectTypeWhenNotEndingWithAspectName() {
        // Arrange
        final AssetAdministrationShellDescriptor shellDescriptor = shellDescriptor(
                List.of(submodelDescriptorWithoutEndpoint(serialPartTypizationId)));
        final List<String> aspectTypeFilter = List.of("SerialPartTypization");

        // Act
        final List<SubmodelDescriptor> result = shellDescriptor.filterDescriptorsByAspectTypes(aspectTypeFilter);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSemanticId().getValue().get(0)).isEqualTo(serialPartTypizationId);
    }

    @Test
    void shouldFilterByAspectTypeWhenWithDifferentAspects() {
        // Arrange
        final AssetAdministrationShellDescriptor shellDescriptor = shellDescriptor(
                List.of(submodelDescriptorWithoutEndpoint(serialPartTypizationIdWithAspectName),
                        submodelDescriptorWithoutEndpoint(assemblyPartRelationshipId)));

        final List<String> aspectTypeFilter = List.of("SerialPartTypization");

        // Act
        final List<SubmodelDescriptor> result = shellDescriptor.filterDescriptorsByAspectTypes(aspectTypeFilter);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSemanticId().getValue().get(0)).isEqualTo(serialPartTypizationIdWithAspectName);
    }

    @Test
    void shouldReturnEndpointAddressesForSubmodelDescriptors() {
        // Arrange
        final AssetAdministrationShellDescriptor shellDescriptor = shellDescriptor(
                List.of(submodelDescriptor(serialPartTypizationIdWithAspectName,
                                "testSerialPartTypizationEndpoint"),
                        submodelDescriptor(assemblyPartRelationshipIdWithAspectName,
                                "testAssemblyPartRelationshipEndpoint")));

        // Act
        final List<String> result = shellDescriptor.findAssemblyPartRelationshipEndpointAddresses();

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo("testAssemblyPartRelationshipEndpoint");
    }


}