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

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;

/**
 * Initializing Semantics Hub cache with values
 */
@Service
@Slf4j
class SemanticsHubCacheInitializer {

    private final SemanticsHubFacade semanticsHubFacade;
    private final List<String> defaultUrns;

    /* package */ SemanticsHubCacheInitializer(final SemanticsHubFacade semanticsHubFacade,
            @Value("${semanticsHub.defaultUrns:}") final List<String> defaultUrns) {
        this.semanticsHubFacade = semanticsHubFacade;
        this.defaultUrns = defaultUrns;
    }

    /**
     * Initializing Semantics Hub cache with values, initially after application starts.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void initializeCacheValues() {
        log.debug("Initializing Semantics Hub Cache with values.");

        try {
            defaultUrns.forEach(semanticsHubFacade::getModelJsonSchema);
        } catch (final HttpServerErrorException ex) {
            log.error("Initialization of Semantics Hub Cache failed", ex);
        }
    }

    /**
     * Cleaning up Semantics Hub cache after scheduled time, and reinitializing it once again.
     */
    @Scheduled(cron = "${semanticsHub.cleanup.scheduler}")
    /* package */ void reinitializeAllCacheInterval() {
        log.debug("Reinitializing Semantics Hub Cache with new values.");

        semanticsHubFacade.evictAllCacheValues();
        initializeCacheValues();
    }

}
