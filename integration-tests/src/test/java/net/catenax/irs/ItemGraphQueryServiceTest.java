package net.catenax.irs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.catenax.irs.component.Job;
import net.catenax.irs.component.JobHandle;
import net.catenax.irs.component.Jobs;
import net.catenax.irs.component.RegisterJob;
import net.catenax.irs.component.enums.AspectType;
import net.catenax.irs.component.enums.JobState;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = { "test" })
@Import(IntTestConfig.class)
public class ItemGraphQueryServiceTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String GLOBAL_ASSET_ID = "urn:uuid:2ddcc6be-67e7-4518-8b15-161b4fb21d75";
    private static int TREE_DEPTH = 5;
    private static List<AspectType> ASPECTS = List.of(AspectType.ASSEMBLY_PART_RELATIONSHIP);


    @Test
    void initiateJobForGlobalAssetId() throws Exception {
        // Integration tests Scenario 2 STEP 1
        final ResultActions resultInitiateJobForGlobalAssetId = this.mvc.perform(post("/irs/jobs")
                                                        .contentType(MediaType.APPLICATION_JSON)
                                                        .content(new ObjectMapper().writeValueAsString(registerJob())))
                                                    .andExpect(status().isCreated());

        // Deserialize response
        final MvcResult result1 = resultInitiateJobForGlobalAssetId.andReturn();
        String contentAsString1 = result1.getResponse().getContentAsString();

        final JobHandle returnedJob = objectMapper.readValue(contentAsString1, JobHandle.class);

        // jobId UUID for registered job processing globalAssetId
        final UUID jobId = returnedJob.getJobId();

        assertThat(returnedJob).isNotNull();
        assertThat(jobId).isNotNull();

        // Integration tests Scenario 2 STEP 2
        final ResultActions resultGetJobById = this.mvc.perform(post("/irs/jobs/" + jobId)
                                                           .contentType(MediaType.APPLICATION_JSON))
                                                       .andExpect(status().is(206));

        // Deserialize response
        final MvcResult result2 = resultGetJobById.andReturn();
        String contentAsString2 = result2.getResponse().getContentAsString();

        final Jobs partialJob = objectMapper.readValue(contentAsString2, Jobs.class);

        assertThat(partialJob).isNotNull();
        assertThat(partialJob.getRelationships()).isEmpty();
        assertThat(partialJob.getShells()).isEmpty();
        assertThat(partialJob.getTombstones()).isEmpty();

        final Job job = this.getJob(partialJob);
        assertThat(job).isNotNull();
        assertThat(job.getGlobalAssetId().getGlobalAssetId()).isEqualTo(GLOBAL_ASSET_ID);
        assertThat(job.getJobState()).isEqualTo(JobState.RUNNING);
    }

    private static RegisterJob registerJob() {
        final RegisterJob registerJob = new RegisterJob();
        registerJob.setGlobalAssetId(GLOBAL_ASSET_ID);
        registerJob.setDepth(TREE_DEPTH);
        registerJob.setAspects(ASPECTS);

        return registerJob;
    }

    private Job getJob(final Jobs jobs) {
        return jobs.getJob();
    }
}
