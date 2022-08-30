/*
 * Copyright (c) 2022. Copyright Holder (Catena-X Consortium)
 *
 * See the AUTHORS file(s) distributed with this work for additional
 * information regarding authorship.
 *
 * See the LICENSE file(s) distributed with this work for
 * additional information regarding license terms.
 *
 */
package org.eclipse.tractusx.aaswrapper.submodel.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.List;
import java.util.Set;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClientException;

class SubmodelTestdataCreatorTest {

    private final SubmodelTestdataCreator submodelTestdataCreator = new SubmodelTestdataCreator();

    @Test
    void shouldReturnAssemblyPartRelationshipWithoutChildrenWhenRequestingWithTestId() {
        final AssemblyPartRelationship dummyAssemblyPartRelationshipForId = submodelTestdataCreator.createDummyAssemblyPartRelationshipForId(
                "test");

        assertThat(dummyAssemblyPartRelationshipForId.getCatenaXId()).isEqualTo("test");
        assertThat(dummyAssemblyPartRelationshipForId.getChildParts()).isEmpty();
    }

    @Test
    void shouldReturnAssemblyPartRelationshipWithPreDefinedChildrenWhenRequestingWithCatenaXId() {
        final String catenaXId = "urn:uuid:8a61c8db-561e-4db0-84ec-a693fc5ffdf6";
        final AssemblyPartRelationship assemblyPartRelationship = submodelTestdataCreator.createDummyAssemblyPartRelationshipForId(
                catenaXId);

        final Set<ChildData> childParts = assemblyPartRelationship.getChildParts();
        Assertions.assertThat(childParts).hasSize(3);
        final List<String> childIDs = List.of("urn:uuid:5ce49656-5156-4c8a-b93e-19422a49c0bc",
                "urn:uuid:09b48bcc-8993-4379-a14d-a7740e1c61d4", "urn:uuid:9ea14fbe-0401-4ad0-93b6-dad46b5b6e3d");
        childParts.forEach(childData -> assertThat(childIDs).contains(childData.getChildCatenaXId()));
    }

    @Test
    void shouldReturnAssemblyPartRelationshipWithCustomChildrenWhenRequestingWithCatenaXId() {
        final String catenaXId = "urn:uuid:8a61c8db-561e-4db0-84ec-a693fc5ffdf6";

        final List<String> children = List.of("abc", "def", "ghi");

        final AssemblyPartRelationship assemblyPartRelationship = submodelTestdataCreator.getDummyAssemblyPartRelationshipWithChildren(
                catenaXId, children);

        final Set<ChildData> childParts = assemblyPartRelationship.getChildParts();
        Assertions.assertThat(childParts).hasSize(3);
        final List<String> childIDs = List.of("abc", "def", "ghi");
        childParts.forEach(childData -> assertThat(childIDs).contains(childData.getChildCatenaXId()));
    }

    @Test
    void shouldThrowErrorWhenCallingTestId() {
        final String catenaXId = "urn:uuid:c35ee875-5443-4a2d-bc14-fdacd64b9446";
        final SubmodelClientLocalStub client = new SubmodelClientLocalStub();

        assertThatExceptionOfType(RestClientException.class).isThrownBy(
                () -> client.getSubmodel(catenaXId, AssemblyPartRelationship.class));
    }
}