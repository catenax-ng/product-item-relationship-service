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

package net.catenax.irs.aaswrapper.submodel.domain;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import net.catenax.irs.dto.AssemblyPartRelationshipDTO;
import net.catenax.irs.dto.ChildDataDTO;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@Slf4j
class SubmodelFacadeTest {

    private static final String CATENA_X = "8a61c8db-561e-4db0-84ec-a693fc5ffdf6";

    @Test
    void shouldReturnAssemblyPartRelationshipWithChildDataWhenRequestingWithCatenaXId() {
        final SubmodelClientLocalStub submodelClient = new SubmodelClientLocalStub();
        final SubmodelFacade submodelFacade = new SubmodelFacade(submodelClient);

        final AssemblyPartRelationshipDTO submodel = submodelFacade.getSubmodel(CATENA_X);
        assertThat(submodel.getCatenaXId()).isEqualTo(CATENA_X);
        final Set<ChildDataDTO> childParts = submodel.getChildParts();
        assertThat(childParts).hasSize(3);
        final List<String> childIds = childParts.stream()
                                                .map(ChildDataDTO::getChildCatenaXId)
                                                .collect(Collectors.toList());
        assertThat(childIds).containsAnyOf("09b48bcc-8993-4379-a14d-a7740e1c61d4",
                                           "5ce49656-5156-4c8a-b93e-19422a49c0bc",
                                           "9ea14fbe-0401-4ad0-93b6-dad46b5b6e3d");
    }

    @Test
    void asyncEndpointShouldReturnObjectWithCompletableFuture() {
        final SubmodelClient asyncClient = Mockito.mock(SubmodelClient.class);
        final SubmodelFacade facade = new SubmodelFacade(asyncClient);
        when(asyncClient.getSubmodel(anyString(), any())).thenReturn(this.processRequest());

        final AssemblyPartRelationshipDTO submodel = facade.getSubmodel(CATENA_X);
        assertThat(submodel.getCatenaXId()).isEqualTo(CATENA_X);
    }

    // Invoke AAS wrapper via the submodel rest client
    private CompletableFuture<Object> processRequest() {
        final SubmodelTestdataCreator submodelCreator = new SubmodelTestdataCreator();
        final AssemblyPartRelationship response =
                submodelCreator.createDummyAssemblyPartRelationshipForId(SubmodelFacadeTest.CATENA_X);

        return CompletableFuture.completedFuture(response);
    }
}