package net.catenax.irs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.catenax.irs.component.Job;
import net.catenax.irs.component.JobHandle;
import net.catenax.irs.component.Jobs;
import net.catenax.irs.component.RegisterJob;
import net.catenax.irs.component.enums.AspectType;
import net.catenax.irs.component.enums.BomLifecycle;
import net.catenax.irs.component.enums.JobState;
import net.catenax.irs.controllers.IrsController;
import net.catenax.irs.services.IrsItemGraphQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = { "test" })
@Import(IntTestConfig.class)
public class ItemGraphIntegrationTest {

    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private IrsItemGraphQueryService service;

    @Autowired
    private WebApplicationContext applicationContext;

    private static final String GLOBAL_ASSET_ID = "urn:uuid:2ddcc6be-67e7-4518-8b15-161b4fb21d75";
    private static final int TREE_DEPTH = 5;
    private static final List<AspectType> ASPECTS = List.of(AspectType.fromValue("AssemblyPartRelationship"));

    @BeforeEach
    void setup() {
        this.mvc = MockMvcBuilders.webAppContextSetup(applicationContext).build();
    }

    @Test
    void initiateJobForGlobalAssetId() throws Exception {
        // Integration test Scenario 2 STEP 1
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

        // Integration test Scenario 2 STEP 2
        final ResultActions resultGetJobById = this.mvc.perform(get("/irs/jobs/" + jobId)
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

        // Integration test Scenario 2 STEP 3

    }

    private static RegisterJob registerJob() {
        final RegisterJob registerJob = new RegisterJob();
        registerJob.setGlobalAssetId(GLOBAL_ASSET_ID);
        registerJob.setDepth(TREE_DEPTH);
        registerJob.setAspects(ASPECTS);
        registerJob.setBomLifecycle(BomLifecycle.fromLifecycleContextCharacteristic("AsBuilt"));

        return registerJob;
    }

    private Job getJob(final Jobs jobs) {
        return jobs.getJob();
    }
}
