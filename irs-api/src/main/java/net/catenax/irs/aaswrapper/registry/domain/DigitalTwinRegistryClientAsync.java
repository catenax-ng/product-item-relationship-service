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

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * The digital twin registry rest client in an asynchronous mode
 */
@Service
public class DigitalTwinRegistryClientAsync {
    private final DigitalTwinRegistryClient client;

    public DigitalTwinRegistryClientAsync(final DigitalTwinRegistryClient client) {
        this.client = client;
    }

    /**
     * Retrieve an Asset Administration Shell by id
     *
     * @param aasIdentifier The Asset Administration Shell’s unique id
     * @return A specific Asset Administration Shell Descriptor
     */
    @Async
    public CompletableFuture<AssetAdministrationShellDescriptor> getAASDescriptorAsync(final String aasIdentifier) {
        final AssetAdministrationShellDescriptor result = this.client.getAssetAdministrationShellDescriptor(
                aasIdentifier);

        return CompletableFuture.completedFuture(result);
    }
}
