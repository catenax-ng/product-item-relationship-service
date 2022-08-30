package org.eclipse.tractusx.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import org.eclipse.tractusx.aaswrapper.job.AASTransferProcess;
import org.eclipse.tractusx.aaswrapper.job.ItemContainer;
import org.eclipse.tractusx.component.Job;
import org.eclipse.tractusx.component.Jobs;
import org.eclipse.tractusx.component.Relationship;
import org.eclipse.tractusx.component.enums.JobState;
import org.eclipse.tractusx.connector.job.JobStore;
import org.eclipse.tractusx.connector.job.MultiTransferJob;
import org.eclipse.tractusx.exceptions.EntityNotFoundException;
import org.eclipse.tractusx.persistence.BlobPersistence;
import org.eclipse.tractusx.persistence.BlobPersistenceException;
import org.eclipse.tractusx.util.JsonUtil;
import org.eclipse.tractusx.util.TestMother;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IrsItemGraphQueryServiceTest {

    private final UUID jobId = UUID.randomUUID();
    private final TestMother generate = new TestMother();

    @Mock
    private JobStore jobStore;

    @Mock
    private BlobPersistence blobStore;

    @InjectMocks
    private IrsItemGraphQueryService testee;

    @Test
    void registerItemJobWithoutDepthShouldBuildFullTree() throws Exception {
        // given
        final var jobId = UUID.randomUUID();
        final AASTransferProcess transfer1 = generate.aasTransferProcess();
        givenTransferResultIsStored(transfer1);

        final AASTransferProcess transfer2 = generate.aasTransferProcess();
        givenTransferResultIsStored(transfer2);

        givenRunningJobHasFinishedTransfers(jobId, transfer1, transfer2);

        // when
        final Jobs jobs = testee.getJobForJobId(jobId, true);

        // then
        assertThat(jobs.getRelationships()).hasSize(2);
    }

    private void givenRunningJobHasFinishedTransfers(final UUID jobId, final AASTransferProcess... transfers) {
        final MultiTransferJob job = MultiTransferJob.builder()
                                                     .completedTransfers(Arrays.asList(transfers))
                                                     .job(generate.fakeJob(JobState.RUNNING))
                                                     .build();
        when(jobStore.find(jobId.toString())).thenReturn(Optional.of(job));
    }

    private void givenTransferResultIsStored(final AASTransferProcess transfer1) throws BlobPersistenceException {
        final Relationship relationship1 = generate.relationship();
        final ItemContainer itemContainer1 = ItemContainer.builder().relationship(relationship1).build();
        when(blobStore.getBlob(transfer1.getId())).thenReturn(Optional.of(toBlob(itemContainer1)));
    }

    private byte[] toBlob(final Object transfer) {
        return new JsonUtil().asString(transfer).getBytes(StandardCharsets.UTF_8);
    }

    @Test
    void cancelJobById() {
        final Job job = generate.fakeJob(JobState.CANCELED);

        final MultiTransferJob multiTransferJob = MultiTransferJob.builder().job(job).build();

        when(jobStore.cancelJob(jobId.toString())).thenReturn(Optional.ofNullable(multiTransferJob));
        final Job canceledJob = testee.cancelJobById(jobId);

        assertNotNull(canceledJob);
        Assertions.assertEquals(canceledJob.getJobState().name(), JobState.CANCELED.name());
    }

    @Test
    void cancelJobById_throwEntityNotFoundException() {
        when(jobStore.cancelJob(jobId.toString())).thenThrow(
                new EntityNotFoundException("No job exists with id " + jobId));

        assertThrows(EntityNotFoundException.class, () -> testee.cancelJobById(jobId));
    }

}