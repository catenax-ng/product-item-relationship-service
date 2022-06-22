//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.controllers;

import static io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.List;
import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.Size;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.Explode;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.catenax.irs.IrsApplication;
import net.catenax.irs.annotations.ExcludeFromCodeCoverageGeneratedReport;
import net.catenax.irs.component.Job;
import net.catenax.irs.component.JobHandle;
import net.catenax.irs.component.Jobs;
import net.catenax.irs.component.RegisterJob;
import net.catenax.irs.component.enums.JobState;
import net.catenax.irs.component.JobStatusResult;
import net.catenax.irs.dtos.ErrorResponse;
import net.catenax.irs.services.IrsItemGraphQueryService;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Application REST controller.
 */
@Slf4j
@RestController
@RequestMapping(IrsApplication.API_PREFIX)
@RequiredArgsConstructor
@ExcludeFromCodeCoverageGeneratedReport
@SuppressWarnings({ "PMD.AvoidDuplicateLiterals",
                    "PMD.ExcessiveImports"
})
public class IrsController {

    private final IrsItemGraphQueryService itemJobService;

    @Operation(operationId = "registerJobForGlobalAssetId",
               summary = "Register an IRS job to retrieve an item graph for given {globalAssetId}.",
               security = @SecurityRequirement(name = "OAuth2", scopes = "write"),
               tags = { "Item Relationship Service" },
               description = "Register an IRS job to retrieve an item graph for given {globalAssetId}.")
    @ApiResponses(value = { @ApiResponse(responseCode = "201", description = "Returns jobId of registered job.",
                                         content = { @Content(mediaType = APPLICATION_JSON_VALUE,
                                                              schema = @Schema(implementation = JobHandle.class),
                                                              examples = { @ExampleObject(name = "complete",
                                                                                          ref = "#/components/examples/job-handle")
                                                              })
                                         }),
                            @ApiResponse(responseCode = "400", description = "Job registration failed.",
                                         content = { @Content(mediaType = APPLICATION_JSON_VALUE,
                                                              schema = @Schema(implementation = ErrorResponse.class),
                                                              examples = @ExampleObject(name = "complete",
                                                                                        ref = "#/components/examples/error-response"))
                                         }),
    })
    @PostMapping("/jobs")
    @ResponseStatus(HttpStatus.CREATED)
    public JobHandle registerJobForGlobalAssetId(final @Valid @RequestBody RegisterJob request) {
        return itemJobService.registerItemJob(request);
    }

    @Operation(description = "Return job with optional item graph result for requested jobId.",
               operationId = "getJobForJobId",
               summary = "Return job with optional item graph result for requested jobId.",
               security = @SecurityRequirement(name = "OAuth2", scopes = "read"),
               tags = { "Item Relationship Service" })
    @ApiResponses(value = { @ApiResponse(responseCode = "200",
                                         description = "Return job with item graph for the requested jobId.",
                                         content = { @Content(mediaType = APPLICATION_JSON_VALUE,
                                                              schema = @Schema(implementation = Jobs.class),
                                                              examples = @ExampleObject(name = "complete",
                                                                                        ref = "#/components/examples/complete-job-result"))
                                         }),
                            @ApiResponse(responseCode = "206",
                                         description = "Return job with current processed item graph for the requested jobId.",
                                         content = { @Content(mediaType = APPLICATION_JSON_VALUE,
                                                              schema = @Schema(implementation = Jobs.class),
                                                              examples = @ExampleObject(name = "complete",
                                                                                        ref = "#/components/examples/partial-job-result"))
                                         }),
                            @ApiResponse(responseCode = "404", description = "Job with the requested jobId not found.",
                                         content = { @Content(mediaType = APPLICATION_JSON_VALUE,
                                                              schema = @Schema(implementation = ErrorResponse.class),
                                                              examples = @ExampleObject(name = "complete",
                                                                                        ref = "#/components/examples/error-response"))
                                         }),
    })
    @GetMapping("/jobs/{jobId}")
    public Jobs getJobById(
            @Parameter(description = "JobId of the job.", schema = @Schema(implementation = UUID.class), name = "jobId",
                       example = "6c311d29-5753-46d4-b32c-19b918ea93b0") @Size(min = IrsApiConstants.JOB_ID_SIZE,
                                                                               max = IrsApiConstants.JOB_ID_SIZE) @Valid @PathVariable final UUID jobId,
            @Parameter(
                    description = "<true> Return job with current processed item graph. <false> Return job with item graph if job is in state <COMPLETED>, otherwise job.") @Schema(
                    implementation = Boolean.class, defaultValue = "true") @RequestParam(value = "returnUncompletedJob",
                                                                                         required = false) final boolean returnUncompletedJob) {
        return itemJobService.getJobForJobId(jobId, returnUncompletedJob);
    }

    @Operation(description = "Cancel job for requested jobId.", operationId = "cancelJobByJobId",
               summary = "Cancel job for requested jobId.",
               security = @SecurityRequirement(name = "OAuth2", scopes = "write"),
               tags = { "Item Relationship Service" })
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Job with requested jobId canceled."),
                            @ApiResponse(responseCode = "404", description = "Job for requested jobId not found.",
                                         content = { @Content(mediaType = APPLICATION_JSON_VALUE,
                                                              schema = @Schema(implementation = ErrorResponse.class),
                                                              examples = @ExampleObject(name = "complete",
                                                                                        ref = "#/components/examples/error-response"))
                                         }),
    })
    @PutMapping("/jobs/{jobId}")
    public Job cancelJobByJobId(
            @Parameter(description = "JobId of the job.", schema = @Schema(implementation = UUID.class), name = "jobId",
                       example = "6c311d29-5753-46d4-b32c-19b918ea93b0") @Size(min = IrsApiConstants.JOB_ID_SIZE,
                                                                               max = IrsApiConstants.JOB_ID_SIZE) @Valid @PathVariable final UUID jobId) {

        return this.itemJobService.cancelJobById(jobId);
    }

    @Operation(description = "Returns jobIds for requested job states.", operationId = "getJobIdsByJobStates",
               summary = "Returns jobIds for requested job states.",
               security = @SecurityRequirement(name = "OAuth2", scopes = "read"),
               tags = { "Item Relationship Service" })
    @ApiResponses(value = { @ApiResponse(responseCode = "200",
                                         description = "List of job ids and status for requested job states.",
                                         content = { @Content(mediaType = APPLICATION_JSON_VALUE, array = @ArraySchema(
                                                 schema = @Schema(implementation = JobStatusResult.class)),
                                                              examples = @ExampleObject(name = "complete",
                                                                                        ref = "#/components/examples/complete-job-list-processing-state"))
                                         }),
                            @ApiResponse(responseCode = "404",
                                         description = "No jobIds found for requested job states.",
                                         content = { @Content(mediaType = APPLICATION_JSON_VALUE,
                                                              schema = @Schema(implementation = ErrorResponse.class),
                                                              examples = @ExampleObject(name = "complete",
                                                                                        ref = "#/components/examples/error-response"))
                                         }),
    })
    @GetMapping("/jobs")
    public List<JobStatusResult> getJobsByJobState(
            @Valid @ParameterObject @Parameter(description = "Requested job states.", in = QUERY,
                                               explode = Explode.FALSE, array = @ArraySchema(
                    schema = @Schema(implementation = JobState.class))) @RequestParam(value = "jobStates",
                                                                                      required = false,
                                                                                      defaultValue = "") final List<JobState> jobStates) {
        return itemJobService.getJobsByJobState(jobStates);
    }

}
