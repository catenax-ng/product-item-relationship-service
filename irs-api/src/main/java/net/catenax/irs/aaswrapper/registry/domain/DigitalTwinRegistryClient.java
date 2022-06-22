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

import static net.catenax.irs.configuration.RestTemplateConfig.OAUTH_REST_TEMPLATE;

import java.util.Collections;
import java.util.List;

import net.catenax.irs.component.assetadministrationshell.AssetAdministrationShellDescriptor;
import net.catenax.irs.component.assetadministrationshell.IdentifierKeyValuePair;
import net.catenax.irs.util.JsonUtil;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Digital Twin Registry Rest Client
 */
interface DigitalTwinRegistryClient {

    /**
     * @param aasIdentifier The Asset Administration Shell’s unique id
     * @return Returns a specific Asset Administration Shell Descriptor
     */
    AssetAdministrationShellDescriptor getAssetAdministrationShellDescriptor(String aasIdentifier);

    /**
     * Returns a list of Asset Administration Shell ids based on Asset identifier key-value-pairs.
     * Only the Shell ids are returned when all provided key-value pairs match.
     * @param assetIds The key-value-pair of an Asset identifier
     * @return urn uuid string list
     */
    List<String> getAllAssetAdministrationShellIdsByAssetLink(List<IdentifierKeyValuePair> assetIds);

}

/**
 * Digital Twin Registry Rest Client Stub used in local environment
 */
@Service
@Profile({"local", "test"})
class DigitalTwinRegistryClientLocalStub implements DigitalTwinRegistryClient {

    private final AssetAdministrationShellTestdataCreator testdataCreator = new AssetAdministrationShellTestdataCreator();

    @Override
    public AssetAdministrationShellDescriptor getAssetAdministrationShellDescriptor(final String aasIdentifier) {
        if ("urn:uuid:9ea14fbe-0401-4ad0-93b6-dad46b5b6e3d".equals(aasIdentifier)) {
            throw new RestClientException("Dummy Exception");
        }
        return testdataCreator.createDummyAssetAdministrationShellDescriptorForId(aasIdentifier);
    }

    @Override
    public List<String> getAllAssetAdministrationShellIdsByAssetLink(final List<IdentifierKeyValuePair> assetIds) {
        return Collections.emptyList();
    }
}


/**
 * Digital Twin Registry Rest Client Implementation
 */
@Service
@Profile({"!local && !test"})
class DigitalTwinRegistryClientImpl implements DigitalTwinRegistryClient {

    private final RestTemplate restTemplate;
    private final String digitalTwinRegistryUrl;

    /* package */ DigitalTwinRegistryClientImpl(@Qualifier(OAUTH_REST_TEMPLATE) final RestTemplate restTemplate, @Value("${digitalTwinRegistry.url:}") final String digitalTwinRegistryUrl) {
        this.restTemplate = restTemplate;
        this.digitalTwinRegistryUrl = digitalTwinRegistryUrl;
    }

    @Override
    public AssetAdministrationShellDescriptor getAssetAdministrationShellDescriptor(final String aasIdentifier) {
        final UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(digitalTwinRegistryUrl);
        uriBuilder.path("/registry/shell-descriptors/").path(aasIdentifier);

        return restTemplate.getForObject(uriBuilder.build().toUri(), AssetAdministrationShellDescriptor.class);
    }

    @Override
    public List<String> getAllAssetAdministrationShellIdsByAssetLink(final List<IdentifierKeyValuePair> assetIds) {
        final UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(digitalTwinRegistryUrl);
        uriBuilder.path("/lookup/shells").queryParam("assetIds", new JsonUtil().asString(assetIds));

        return restTemplate.getForObject(uriBuilder.build().toUri(), List.class);
    }


}
