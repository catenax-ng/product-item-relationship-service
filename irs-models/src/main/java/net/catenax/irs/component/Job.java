//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.component;

import static net.catenax.irs.dtos.ValidationConstants.INPUT_FIELD_MIN_LENGTH;
import static net.catenax.irs.dtos.ValidationConstants.JOB_ID_FIELD_MAX_LENGTH;

import java.net.URL;
import java.time.Instant;
import java.util.UUID;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.ToString;
import lombok.Value;
import net.catenax.irs.component.enums.JobState;

/**
 * A job to retrieve item relationship data.
 */
@Value
@Builder(toBuilder = true)
@JsonDeserialize(builder = Job.JobBuilder.class)
@AllArgsConstructor
@ToString
@SuppressWarnings("PMD.ShortClassName")
public class Job {

    /**
     * jobId
     */
    @NotNull
    @Size(min = INPUT_FIELD_MIN_LENGTH, max = JOB_ID_FIELD_MAX_LENGTH)
    @Schema(description = "JobId of the job.", minLength = INPUT_FIELD_MIN_LENGTH,
            maxLength = JOB_ID_FIELD_MAX_LENGTH, implementation = UUID.class)
    private UUID jobId;

    /**
     * globalAssetId
     */
    @NotNull
    @Size(min = INPUT_FIELD_MIN_LENGTH, max = JOB_ID_FIELD_MAX_LENGTH)
    @Schema(description = "Part global unique Id", minLength = INPUT_FIELD_MIN_LENGTH,
            maxLength = JOB_ID_FIELD_MAX_LENGTH, implementation = GlobalAssetIdentification.class)
    @JsonUnwrapped
    private GlobalAssetIdentification globalAssetId;

    @NotBlank
    @Schema()
    private JobState jobState;

    @Schema(description = "Job error details.", implementation = JobErrorDetails.class)
    private JobErrorDetails exception;

    /**
     * Timestamp when the job was created
     */
    @Schema(implementation = Instant.class)
    private Instant createdOn;

    /**
     * Timestamp when the job was started
     */
    @Schema(implementation = Instant.class)
    private Instant startedOn;

    /**
     * Last time job was modified
     */
    @Schema(implementation = Instant.class)
    private Instant lastModifiedOn;

    /**
     * Mark the time the was completed
     */
    @Schema(implementation = Instant.class)
    private Instant jobCompleted;

    /**
     * Url of request that resulted to this job
     */
    @Schema(implementation = URL.class)
    private URL requestUrl;

    /**
     * Http method, only GET is supported
     */
    @Schema(description = "HTTP verbs used by request.")
    private String action;

    /**
     * Owner of the job
     */
    @Schema(description = "The IRS api consumer.")
    private String owner;

    @Schema(description = "Summary of the job with statistics of the job processing.", implementation = Summary.class)
    private Summary summary;

    @Schema(description = "The passed query parameters", implementation = QueryParameter.class)
    private QueryParameter queryParameter;

    /**
     * Builder class
     */
    @JsonPOJOBuilder(withPrefix = "")
    public static class JobBuilder {
    }

}
