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

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * A service class that handles Digital Twin Registry cases.
 */
@Service
@Slf4j
public class DigitalTwinRegistryService {
    private final DigitalTwinRegistryClientAsync asyncClient;

    public DigitalTwinRegistryService(final DigitalTwinRegistryClientAsync client) {
        this.asyncClient = client;
    }

    public CompletableFuture<AssetAdministrationShellDescriptor> getAASDescriptorAsync(final String aasIdentifier) {
        return this.asyncClient.getAASDescriptorAsync(aasIdentifier);
    }

}
