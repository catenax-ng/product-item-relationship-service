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
package org.eclipse.tractusx.irs.configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.minio.MinioClient;
import org.eclipse.tractusx.irs.persistence.BlobPersistence;
import org.eclipse.tractusx.irs.persistence.MinioBlobPersistence;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

class MinioHealthIndicatorTest {

    @Test
    void shouldReturnStatusUpWhenMinioBlobPersistencePresentAndBucketExists() throws Exception {
        // given
        final MinioClient minioClient = mock(MinioClient.class);
        final BlobstoreConfiguration blobstoreConfiguration = mock(BlobstoreConfiguration.class);
        when(blobstoreConfiguration.getBucketName()).thenReturn("bucket-name");
        when(minioClient.bucketExists(any())).thenReturn(Boolean.TRUE);

        final MinioBlobPersistence blobPersistence = new MinioBlobPersistence("bucket-name", minioClient);
        final MinioHealthIndicator minioHealthIndicator = new MinioHealthIndicator(blobPersistence, blobstoreConfiguration);

        // when
        final Health health = minioHealthIndicator.health();

        // then
        assertThat(health.getStatus()).isEqualTo(Status.UP);
    }

    @Test
    void shouldReturnStatusDownWhenBlobStorageIsNotMinio() {
        // given
        final BlobPersistence blobPersistence = mock(BlobPersistence.class);
        final BlobstoreConfiguration blobstoreConfiguration = mock(BlobstoreConfiguration.class);

        final MinioHealthIndicator minioHealthIndicator = new MinioHealthIndicator(blobPersistence, blobstoreConfiguration);

        // when
        final Health health = minioHealthIndicator.health();

        // then
        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
    }
}
