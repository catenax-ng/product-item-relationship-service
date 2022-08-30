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

package org.eclipse.tractusx.semanticshub;

import org.eclipse.tractusx.configuration.RestTemplateConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Semantics Hub Rest Client
 */
interface SemanticsHubClient {

    /**
     * Return Json Schema of requsted model by urn
     *
     * @param urn of the model
     * @return Json Schema
     */
    String getModelJsonSchema(String urn);

}

/**
 * Semantics Hub Rest Client Stub used in local environment
 */
@Service
@Profile({ "local",
           "test"
})
class SemanticsHubClientLocalStub implements SemanticsHubClient {

    @Override
    public String getModelJsonSchema(final String urn) {
        return "{" + "  \"$schema\": \"http://json-schema.org/draft-07/schema#\"," + "  \"type\": \"integer\"" + "}";
    }
}

/**
 * Semantics Hub Rest Client Implementation
 */
@Service
@Profile({ "!local && !test" })
class SemanticsHubClientImpl implements SemanticsHubClient {

    private final RestTemplate restTemplate;
    private final String semanticsHubUrl;

    /* package */ SemanticsHubClientImpl(@Qualifier(RestTemplateConfig.OAUTH_REST_TEMPLATE) final RestTemplate restTemplate,
            @Value("${semanticsHub.url:}") final String semanticsHubUrl) {
        this.restTemplate = restTemplate;
        this.semanticsHubUrl = semanticsHubUrl;
    }

    @Override
    public String getModelJsonSchema(final String urn) {
        final UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(semanticsHubUrl);
        uriBuilder.path("/models/").path(urn).path("/json-schema");

        return restTemplate.getForObject(uriBuilder.build().toUri(), String.class);
    }
}
