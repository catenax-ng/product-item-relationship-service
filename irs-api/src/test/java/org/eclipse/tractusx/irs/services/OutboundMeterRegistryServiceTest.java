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
package org.eclipse.tractusx.irs.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.vavr.collection.Array;
import io.vavr.collection.Seq;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OutboundMeterRegistryServiceTest {

    private final MeterRegistry meterRegistry = new SimpleMeterRegistry();

    @Mock
    private RetryRegistry retryRegistry;
    private OutboundMeterRegistryService testee;

    @BeforeEach
    void setUp() {
        final Retry retry = mock(Retry.class);
        when(retryRegistry.retry(any())).thenReturn(retry);
        final Retry.EventPublisher publisher = mock(Retry.EventPublisher.class);
        when(retry.getEventPublisher()).thenReturn(publisher);

        final Seq<Retry> seq = Array.empty();
        when(retryRegistry.getAllRetries()).thenReturn(seq);
        testee = new OutboundMeterRegistryService(meterRegistry, retryRegistry);

    }

    @Test
    void incrementRegistryTimeoutCounter() {
        testee.incrementRegistryTimeoutCounter();
        assertThat(testee.getCounterTimeoutsRegistry().count()).isOne();
    }

    @Test
    void incrementSubmodelTimeoutCounter() {
        testee.incrementSubmodelTimeoutCounter("testTarget");

        assertThat(testee.getCounterTimeoutsSubmodel().get("testTarget").count()).isOne();
    }

    @Test
    void incrementSubmodelRetryCounter() {
        testee.incrementSubmodelRetryCounter("testTarget");

        assertThat(testee.getCounterRetriesSubmodel().get("testTarget").count()).isOne();
    }
}