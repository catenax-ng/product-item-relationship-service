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

package org.eclipse.tractusx.component;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import org.eclipse.tractusx.component.enums.NodeType;

/**
 * Tombstone with information about request failure
 */
@Getter
@Builder
@Jacksonized
public class Tombstone {
    private static final NodeType NODE_TYPE = NodeType.TOMBSTONE;
    private final String catenaXId;
    private final String endpointURL;
    private final ProcessingError processingError;

    public static Tombstone from(final String catenaXId, final String endpointURL, final Exception exception,
            final int retryCount) {
        final ProcessingError processingError = ProcessingError.builder()
                                                               .withRetryCounter(retryCount)
                                                               .withLastAttempt(ZonedDateTime.now(ZoneOffset.UTC))
                                                               .withErrorDetail(exception.getMessage())
                                                               .build();
        return Tombstone.builder()
                        .endpointURL(endpointURL)
                        .catenaXId(catenaXId)
                        .processingError(processingError)
                        .build();
    }
}
