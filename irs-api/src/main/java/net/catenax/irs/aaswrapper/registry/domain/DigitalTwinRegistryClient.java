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

import java.util.concurrent.CompletableFuture;

import net.catenax.irs.annotations.ExcludeFromCodeCoverageGeneratedReport;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Digital Twin Registry Rest Client
 */
@Profile("prod")
@FeignClient(contextId = "digitalTwinRegistryClientContextId", value = "digitalTwinRegistryClient",
             url = "${feign.client.config.digitalTwinRegistry.url}",
             configuration = DigitalTwinRegistryClientConfiguration.class)
interface DigitalTwinRegistryClient {

    /**
     * Retrieve asynchronously an Asset Administration Shell by id
     *
     * @param aasIdentifier The Asset Administration Shell’s unique id
     * @return A specific Asset Administration Shell Descriptor
     */
    @Async
    @GetMapping(value = "/registry/shell-descriptors/{aasIdentifier}", consumes = APPLICATION_JSON_VALUE)
    CompletableFuture<AssetAdministrationShellDescriptor> getAssetAdministrationShellDescriptor(
            @PathVariable("aasIdentifier") String aasIdentifier);

}

/**
 * Digital Twin Registry Rest Client Stub used in local environment
 */
@Service
@ExcludeFromCodeCoverageGeneratedReport
class DigitalTwinRegistryClientLocalStub implements DigitalTwinRegistryClient {

    @Override
    public CompletableFuture<AssetAdministrationShellDescriptor> getAssetAdministrationShellDescriptor(
            final String aasIdentifier) {
        final AssetAdministrationShellTestdataCreator testdataCreator = new AssetAdministrationShellTestdataCreator();

        return CompletableFuture.completedFuture(
                testdataCreator.createDummyAssetAdministrationShellDescriptorForId(aasIdentifier));
    }
}
