package org.eclipse.tractusx.services;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.tractusx.component.enums.JobState;
import org.eclipse.tractusx.util.TestMother;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class MeterRegistryServiceTest {

    static MeterRegistryService meterRegistryService; // = TestMother.fakeMeterRegistryService();

    @BeforeAll
    static void setup() {
        meterRegistryService = TestMother.simpleMeterRegistryService();
    }

    @Test
    void checkJobStateMetricsIfCorrectlyIncremented() {
        meterRegistryService.recordJobStateMetric(JobState.RUNNING);
        Assertions.assertThat(meterRegistryService.getJobMetric().getJobRunning().count()).isEqualTo(1);

        meterRegistryService.recordJobStateMetric(JobState.TRANSFERS_FINISHED);
        Assertions.assertThat(meterRegistryService.getJobMetric().getJobProcessed().count()).isEqualTo(1);

        meterRegistryService.recordJobStateMetric(JobState.COMPLETED);
        Assertions.assertThat(meterRegistryService.getJobMetric().getJobSuccessful().count()).isEqualTo(1);

        meterRegistryService.recordJobStateMetric(JobState.RUNNING);
        Assertions.assertThat(meterRegistryService.getJobMetric().getJobRunning().count()).isEqualTo(2);

        meterRegistryService.recordJobStateMetric(JobState.ERROR);
        Assertions.assertThat(meterRegistryService.getJobMetric().getJobProcessed().count()).isEqualTo(1);

        meterRegistryService.recordJobStateMetric(JobState.CANCELED);
        Assertions.assertThat(meterRegistryService.getJobMetric().getJobCancelled().count()).isEqualTo(1);

        meterRegistryService.setNumberOfJobsInJobStore(10L);
        Assertions.assertThat(meterRegistryService.getJobMetric().getJobInJobStore().value()).isEqualTo(10);

        meterRegistryService.setNumberOfJobsInJobStore(9L);
        meterRegistryService.setNumberOfJobsInJobStore(6L);
        Assertions.assertThat(meterRegistryService.getJobMetric().getJobInJobStore().value()).isEqualTo(6);

    }

}