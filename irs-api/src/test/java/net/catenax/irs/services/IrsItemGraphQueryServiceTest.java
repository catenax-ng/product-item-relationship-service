package net.catenax.irs.services;

import java.util.Optional;
import java.util.UUID;

import net.catenax.irs.TestConfig;
import net.catenax.irs.component.Job;
import net.catenax.irs.connector.job.JobState;
import net.catenax.irs.connector.job.JobStore;
import net.catenax.irs.connector.job.MultiTransferJob;
import net.catenax.irs.exceptions.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = { "test" })
@Import(TestConfig.class)
class IrsItemGraphQueryServiceTest {

    private final UUID jobId = UUID.randomUUID();

    @MockBean
    private JobStore jobStore;

    @Autowired
    private IrsItemGraphQueryService service;

    @Test
    void registerItemJob() {
        assertTrue(true);
    }

    @Test
    void jobLifecycle() {
        assertTrue(true);
    }

    @Test
    void getJobsByProcessingState() {
        assertTrue(true);
    }

    @Test
    void cancelJobById() {
        final MultiTransferJob multiTransferJob = MultiTransferJob.builder()
                                                                  .jobId(jobId.toString())
                                                                  .state(JobState.CANCELED)
                                                                  .errorDetail("Job should be canceled")
                                                                  .build();

        when(jobStore.cancelJob(jobId.toString())).thenReturn(Optional.ofNullable(multiTransferJob));
        final Job job = service.cancelJobById(jobId);

        assertNotNull(job);
        assertEquals(job.getJobId(), jobId);
        assertEquals(job.getJobState().name(), JobState.CANCELED.name());
    }

    @Test
    void cancelJobById_throwEntityNotFoundException() {
        when(jobStore.cancelJob(jobId.toString()))
                .thenThrow(new EntityNotFoundException("No job exists with id " + jobId));

        assertThrows(EntityNotFoundException.class, () -> service.cancelJobById(jobId));
    }
}