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
package org.eclipse.tractusx.services;

import java.util.List;
import java.util.UUID;

import lombok.NonNull;
import org.eclipse.tractusx.component.Job;
import org.eclipse.tractusx.component.JobHandle;
import org.eclipse.tractusx.component.JobStatusResult;
import org.eclipse.tractusx.component.Jobs;
import org.eclipse.tractusx.component.RegisterJob;
import org.eclipse.tractusx.component.enums.JobState;

/**
 * IIrsItemGraphQueryService interface
 */
public interface IIrsItemGraphQueryService {

    JobHandle registerItemJob(@NonNull RegisterJob request);

    List<JobStatusResult> getJobsByJobState(@NonNull List<JobState> jobStates);

    Job cancelJobById(@NonNull UUID jobId);

    Jobs getJobForJobId(UUID jobId, boolean includePartialResults);
}
