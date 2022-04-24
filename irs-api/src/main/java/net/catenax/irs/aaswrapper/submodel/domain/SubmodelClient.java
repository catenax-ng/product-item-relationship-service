//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.aaswrapper.submodel.domain;

import java.util.concurrent.CompletableFuture;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Submodel client
 */
interface SubmodelClient {

    /**
     * @return Returns the Submodel
     */
    @Async("asyncExecutor")
    <T> CompletableFuture<T> getSubmodel(String submodelEndpointAddress, Class<T> submodelClass);

}

/**
 * Digital Twin Registry Rest Client Stub used in local environment
 */
@Service
class SubmodelClientLocalStub implements SubmodelClient {

    @Override
    public <T> CompletableFuture<T> getSubmodel(final String submodelEndpointAddress, final Class<T> submodelClass) {
        final SubmodelTestdataCreator submodelTestdataCreator = new SubmodelTestdataCreator();

        final T result = (T) submodelTestdataCreator.createDummyAssemblyPartRelationshipForId(submodelEndpointAddress);

        return CompletableFuture.completedFuture(result);
    }

}

/**
 * Submodel Rest Client Implementation
 */
class SubmodelClientImpl implements SubmodelClient {
    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public <T> CompletableFuture<T> getSubmodel(final String submodelEndpointAddress, final Class<T> submodelClass) {
        return CompletableFuture.supplyAsync(() -> restTemplate.getForEntity(submodelEndpointAddress, submodelClass).getBody());
    }
}
