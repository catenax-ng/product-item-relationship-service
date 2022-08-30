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

package org.eclipse.tractusx.configuration;

import java.util.concurrent.Executors;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import org.eclipse.tractusx.aaswrapper.job.AASHandler;
import org.eclipse.tractusx.aaswrapper.job.AASRecursiveJobHandler;
import org.eclipse.tractusx.aaswrapper.job.AASTransferProcess;
import org.eclipse.tractusx.aaswrapper.job.AASTransferProcessManager;
import org.eclipse.tractusx.aaswrapper.job.ItemDataRequest;
import org.eclipse.tractusx.aaswrapper.job.ItemTreesAssembler;
import org.eclipse.tractusx.aaswrapper.job.TreeRecursiveLogic;
import org.eclipse.tractusx.aaswrapper.registry.domain.DigitalTwinRegistryFacade;
import org.eclipse.tractusx.aaswrapper.submodel.domain.SubmodelFacade;
import org.eclipse.tractusx.bpdm.BpdmFacade;
import org.eclipse.tractusx.connector.job.JobOrchestrator;
import org.eclipse.tractusx.connector.job.JobStore;
import org.eclipse.tractusx.persistence.BlobPersistence;
import org.eclipse.tractusx.persistence.BlobPersistenceException;
import org.eclipse.tractusx.persistence.MinioBlobPersistence;
import org.eclipse.tractusx.semanticshub.SemanticsHubFacade;
import org.eclipse.tractusx.services.MeterRegistryService;
import org.eclipse.tractusx.services.validation.JsonValidatorService;
import org.eclipse.tractusx.util.JsonUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Spring configuration for job-related beans.
 */
@Configuration
public class JobConfiguration {

    @Bean
    public JobOrchestrator<ItemDataRequest, AASTransferProcess> jobOrchestrator(
            final DigitalTwinRegistryFacade registryFacade, final SubmodelFacade submodelFacade,
            final SemanticsHubFacade semanticsHubFacade, final BpdmFacade bpdmFacade,
            final JsonValidatorService jsonValidatorService, final BlobPersistence blobStore, final JobStore jobStore,
            final MeterRegistryService meterService) {

        final var aasHandler = new AASHandler(registryFacade, submodelFacade, semanticsHubFacade, bpdmFacade,
                jsonValidatorService, jsonUtil());
        final var manager = new AASTransferProcessManager(aasHandler, Executors.newCachedThreadPool(), blobStore);
        final var logic = new TreeRecursiveLogic(blobStore, new JsonUtil(), new ItemTreesAssembler());
        final var handler = new AASRecursiveJobHandler(logic);

        return new JobOrchestrator<>(manager, jobStore, handler, meterService);
    }

    @Profile("!test")
    @Bean
    public BlobPersistence blobStore(final BlobstoreConfiguration config) throws BlobPersistenceException {
        return new MinioBlobPersistence(config.getEndpoint(), config.getAccessKey(), config.getSecretKey(),
                config.getBucketName());
    }

    @Bean
    public JsonUtil jsonUtil() {
        return new JsonUtil();
    }

    @Bean
    public TimedAspect timedAspect(final MeterRegistry registry) {
        return new TimedAspect(registry);
    }
}
