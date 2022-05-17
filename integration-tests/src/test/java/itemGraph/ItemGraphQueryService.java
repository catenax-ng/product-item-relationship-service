package itemGraph;

import static org.awaitility.Awaitility.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.catenax.irs.component.JobHandle;
import net.catenax.irs.component.RegisterJob;
import net.catenax.irs.component.enums.AspectType;
import net.catenax.irs.controllers.IrsController;
import net.catenax.irs.services.IrsItemGraphQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

public class ItemGraphQueryService {

    @Autowired
    private MockMvc mockMvc;

    private IrsItemGraphQueryService service;
    private IrsController controller;
    private RegisterJob registerJob;

    private static final String GLOBAL_ASSET_ID = "urn:uuid:8a61c8db-561e-4db0-84ec-a693fc5ffdf6";
    private static int TREE_DEPTH = 5;
    private static List<AspectType> ASPECTS = List.of(AspectType.ASSEMBLY_PART_RELATIONSHIP);

    @BeforeEach
    void setUp() {
        controller = new IrsController(service);
        registerJob = registerJob();
    }

    @Test
    void initiateJobForGlobalAssetId() throws Exception {
        final UUID returnedJob = UUID.randomUUID();
        when(service.registerItemJob(any())).thenReturn(JobHandle.builder().jobId(returnedJob).build());

        this.mockMvc.perform(post("/irs/jobs")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(new ObjectMapper().writeValueAsString(registerJob())))
                    .andExpect(status().isCreated())
                    .andExpect(content().string(containsString(returnedJob.toString())));
    }

    @Test
    void registerItemJob() {
        final int expectedRelationshipsSizeFullTree = 6;
        final JobHandle registeredJob = controller.initiateJobForGlobalAssetId(registerJob);

        // then
        given().await()
               .atMost(10, TimeUnit.SECONDS)
               .until(() -> getRelationshipsSize(registeredJob.getJobId()), equalTo(expectedRelationshipsSizeFullTree));


    }

    private static RegisterJob registerJob() {
        final RegisterJob registerJob = new RegisterJob();
        registerJob.setGlobalAssetId(GLOBAL_ASSET_ID);
        registerJob.setDepth(TREE_DEPTH);
        registerJob.setAspects(ASPECTS);

        return registerJob;
    }

    private int getRelationshipsSize(final UUID jobId) {
        return service.getJobForJobId(jobId, false)
                      .getRelationships()
                      .size();
    }
}
