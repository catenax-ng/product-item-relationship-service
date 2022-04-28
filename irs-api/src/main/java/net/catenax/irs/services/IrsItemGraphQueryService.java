//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.services;

import static net.catenax.irs.dtos.IrsCommonConstants.DEPTH_ID_KEY;
import static net.catenax.irs.dtos.IrsCommonConstants.ROOT_ITEM_ID_KEY;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.catenax.irs.aaswrapper.job.AASTransferProcess;
import net.catenax.irs.aaswrapper.job.ItemContainer;
import net.catenax.irs.aaswrapper.job.ItemDataRequest;
import net.catenax.irs.annotations.ExcludeFromCodeCoverageGeneratedReport;
import net.catenax.irs.component.ChildItem;
import net.catenax.irs.component.GlobalAssetIdentification;
import net.catenax.irs.component.Job;
import net.catenax.irs.component.JobHandle;
import net.catenax.irs.component.Jobs;
import net.catenax.irs.component.RegisterJob;
import net.catenax.irs.component.Relationship;
import net.catenax.irs.component.enums.BomLifecycle;
import net.catenax.irs.component.enums.JobState;
import net.catenax.irs.connector.job.JobInitiateResponse;
import net.catenax.irs.connector.job.JobOrchestrator;
import net.catenax.irs.connector.job.JobStore;
import net.catenax.irs.connector.job.MultiTransferJob;
import net.catenax.irs.connector.job.ResponseStatus;
import net.catenax.irs.connector.job.TransferProcess;
import net.catenax.irs.controllers.IrsApiConstants;
import net.catenax.irs.dto.AssemblyPartRelationshipDTO;
import net.catenax.irs.exceptions.EntityNotFoundException;
import net.catenax.irs.persistence.BlobPersistence;
import net.catenax.irs.persistence.BlobPersistenceException;
import net.catenax.irs.util.JsonUtil;
import org.springframework.stereotype.Service;

/**
 * Service for retrieving parts tree.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ExcludeFromCodeCoverageGeneratedReport
@SuppressWarnings("PMD.ExcessiveImports")
public class IrsItemGraphQueryService implements IIrsItemGraphQueryService {

    private final JobOrchestrator<ItemDataRequest, AASTransferProcess> orchestrator;

    private final JobStore jobStore;

    private final BlobPersistence blobStore;

    @Override
    public JobHandle registerItemJob(final @NonNull RegisterJob request) {
        final String uuid = request.getGlobalAssetId().substring(IrsApiConstants.URN_PREFIX_SIZE);
        final var params = Map.of(ROOT_ITEM_ID_KEY, uuid, DEPTH_ID_KEY, String.valueOf(request.getDepth()));
        final JobInitiateResponse jobInitiateResponse = orchestrator.startJob(params);

        if (jobInitiateResponse.getStatus().equals(ResponseStatus.OK)) {
            final String jobId = jobInitiateResponse.getJobId();
            return JobHandle.builder().jobId(UUID.fromString(jobId)).build();
        } else {
            // TODO (jkreutzfeld) Improve with better response (proper exception for error responses?)
            throw new IllegalArgumentException("Could not start job: " + jobInitiateResponse.getError());
        }
    }

    @Override
    public Jobs jobLifecycle(final @NonNull String jobId) {
        return null;
    }

    @Override
    public List<UUID> getJobsByJobState(final @NonNull List<JobState> jobStates) {
        final List<MultiTransferJob> jobs = jobStore.findByStates(jobStates);

        return jobs.stream().map(x -> x.getJob().getJobId()).collect(Collectors.toList());
    }

    @Override
    public Job cancelJobById(final @NonNull UUID jobId) {
        final String idAsString = String.valueOf(jobId);

        final Optional<MultiTransferJob> canceled = this.jobStore.cancelJob(idAsString);
        if (canceled.isPresent()) {
            final MultiTransferJob job = canceled.get();

            return job.getJob();
        } else {
            throw new EntityNotFoundException("No job exists with id " + jobId);
        }
    }

    @Override
    public Jobs getJobForJobId(final UUID jobId, final boolean includePartialResults) {
        final Optional<MultiTransferJob> multiTransferJob = jobStore.find(jobId.toString());
        if (multiTransferJob.isPresent()) {
            final MultiTransferJob multiJob = multiTransferJob.get();

            final var relationships = new ArrayList<Relationship>();

            if (jobIsCompleted(multiJob)) {
                relationships.addAll(retrieveJobResultRelationships(multiJob));
            } else if (includePartialResults) {
                relationships.addAll(retrievePartialResults(multiJob));
            }

            return Jobs.builder().job(multiJob.getJob()).relationships(relationships).build();
        } else {
            throw new EntityNotFoundException("No job exists with id " + jobId);
        }
    }

    private Collection<Relationship> retrievePartialResults(final MultiTransferJob multiJob) {
        final List<TransferProcess> completedTransfers = multiJob.getCompletedTransfers();
        final List<String> transferIds = completedTransfers.stream()
                                                           .map(TransferProcess::getId)
                                                           .collect(Collectors.toList());

        final var relationships = new ArrayList<Relationship>();
        for (final String id : transferIds) {
            try {
                final Optional<byte[]> blob = blobStore.getBlob(id);
                blob.ifPresent(bytes -> relationships.addAll(toRelationships(bytes)));
            } catch (BlobPersistenceException e) {
                log.error("Unable to read transfer result", e);
            }
        }
        return relationships;
    }

    private List<AssemblyPartRelationshipDTO> toRelationshipDTOs(final byte[] blob) {
        final ItemContainer itemContainer = new JsonUtil().fromString(
                new String(blob, StandardCharsets.UTF_8), ItemContainer.class);
        return itemContainer.getAssemblyPartRelationships();
    }

    private Collection<Relationship> retrieveJobResultRelationships(final MultiTransferJob multiJob) {
        try {
            final String jobId = multiJob.getJob().getJobId().toString();
            final Optional<byte[]> blob = blobStore.getBlob(jobId);
            final byte[] bytes = blob.orElseThrow(
                    () -> new EntityNotFoundException("Could not find stored data for multiJob with id " + jobId));
            return toRelationships(bytes);
        } catch (BlobPersistenceException e) {
            log.error("Unable to read blob", e);
            return Collections.emptyList();
        }
    }

    private Collection<Relationship> toRelationships(final byte[] bytes) {
        final List<AssemblyPartRelationshipDTO> assemblyPartRelationships = toRelationshipDTOs(bytes);
        return convert(assemblyPartRelationships);
    }

    private boolean jobIsCompleted(final MultiTransferJob multiJob) {
        return multiJob.getJob().getJobState().equals(JobState.COMPLETED);
    }

    private Collection<Relationship> convert(final Collection<AssemblyPartRelationshipDTO> assemblyPartRelationships) {
        return assemblyPartRelationships.stream().flatMap(this::convert).collect(Collectors.toList());
    }

    private Stream<Relationship> convert(final AssemblyPartRelationshipDTO dto) {
        return dto.getChildParts()
                  .stream()
                  .map(child -> Relationship.builder()
                                            .catenaXId(GlobalAssetIdentification.builder()
                                                                                .globalAssetId(dto.getCatenaXId())
                                                                                .build())
                                            .childItem(ChildItem.builder()
                                                                .childCatenaXId(GlobalAssetIdentification.builder()
                                                                                                         .globalAssetId(
                                                                                                                 child.getChildCatenaXId())
                                                                                                         .build())
                                                                .lifecycleContext(
                                                                        BomLifecycle.fromLifecycleContextCharacteristic(
                                                                                child.getLifecycleContext()))
                                                                .build())
                                            .build());
    }
}
