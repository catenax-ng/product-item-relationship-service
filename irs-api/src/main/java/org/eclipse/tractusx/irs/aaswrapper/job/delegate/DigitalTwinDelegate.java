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
package org.eclipse.tractusx.irs.aaswrapper.job.delegate;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.irs.aaswrapper.job.AASTransferProcess;
import org.eclipse.tractusx.irs.aaswrapper.job.ItemContainer;
import org.eclipse.tractusx.irs.aaswrapper.registry.domain.DigitalTwinRegistryFacade;
import org.eclipse.tractusx.irs.component.Tombstone;
import org.eclipse.tractusx.irs.component.assetadministrationshell.AssetAdministrationShellDescriptor;
import org.eclipse.tractusx.irs.dto.JobParameter;
import org.springframework.web.client.RestClientException;

/**
 * Retrieves AAShell from Digital Twin Registry service and storing it inside {@link ItemContainer}.
 * This shell is later used in further processing by other delegates.
 */
@Slf4j
public class DigitalTwinDelegate extends AbstractDelegate {

    private final DigitalTwinRegistryFacade digitalTwinRegistryFacade;

    public DigitalTwinDelegate(final AbstractDelegate nextStep,
            final DigitalTwinRegistryFacade digitalTwinRegistryFacade) {
        super(nextStep);
        this.digitalTwinRegistryFacade = digitalTwinRegistryFacade;
    }

    @Override
    public ItemContainer process(final ItemContainer.ItemContainerBuilder itemContainerBuilder, final JobParameter jobData,
            final AASTransferProcess aasTransferProcess, final String itemId) {

        try {
            final AssetAdministrationShellDescriptor aaShell = digitalTwinRegistryFacade.getAAShellDescriptor(itemId);

            itemContainerBuilder.shell(aaShell);
        } catch (RestClientException e) {
            log.info("Shell Endpoint could not be retrieved for Item: {}. Creating Tombstone.", itemId);
            itemContainerBuilder.tombstone(Tombstone.from(itemId, null, e, retryCount));
        }

        return next(itemContainerBuilder, jobData, aasTransferProcess, itemId);
    }

}
