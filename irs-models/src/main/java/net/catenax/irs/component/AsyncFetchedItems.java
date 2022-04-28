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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;
import net.catenax.irs.annotations.ExcludeFromCodeCoverageGeneratedReport;

/**
 * Describe the state of the fetched items
 */
@Schema(description = "State of the Item fetch.")
@Value
@Builder(toBuilder = true)
@JsonDeserialize(builder = AsyncFetchedItems.AsyncFetchedItemsBuilder.class)
@ExcludeFromCodeCoverageGeneratedReport
public class AsyncFetchedItems {

    @Schema(description = "No of job with the globalAssetId on the queue.", implementation = Integer.class)
    private Integer queue;

    @Schema(description = "Summary of running job with the globalAssetId.", implementation = Integer.class)
    private Integer running;

    @Schema(description = "Summary of completed job with the globalAssetId.", implementation = Integer.class)
    private Integer complete;

    @Schema(description = "Summary of failed job with the globalAssetId.", implementation = Integer.class)
    private Integer failed;

    /**
     * User to build async fetched items
     */
    @Schema(description = "User to build async fetched items")
    @JsonPOJOBuilder(withPrefix = "")
    public static class AsyncFetchedItemsBuilder {
    }
}
