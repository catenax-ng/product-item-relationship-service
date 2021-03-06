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

import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import net.catenax.irs.component.enums.BomLifecycle;

/*** API type for ChildItem name/url entry. */
@Schema(description = "Set of child parts the parent object is assembled by (one structural level down).")
@Value
@Builder(toBuilder = true)
@JsonDeserialize(builder = ChildItem.ChildItemBuilder.class)
@AllArgsConstructor
public class ChildItem {

    @Schema(description = "Quantity component.", implementation = Quantity.class)
    private Quantity quantity;

    @Schema(description = "The lifecycle context in which the child part was assembled into the parent part.",
            implementation = BomLifecycle.class)
    private BomLifecycle lifecycleContext;

    @Schema(description = "Datetime of assembly.", implementation = ZonedDateTime.class)
    private ZonedDateTime assembledOn;

    @Schema(description = "Last datetime item was modified.", implementation = ZonedDateTime.class)
    private ZonedDateTime lastModifiedOn;

    @Schema(description = "CatenaX child Id.", implementation = GlobalAssetIdentification.class)
    @JsonUnwrapped
    private GlobalAssetIdentification childCatenaXId;

    /**
     * Builder for ChildItem class
     */
    @Schema(description = "Builder to to build child items")
    @JsonPOJOBuilder(withPrefix = "")
    public static class ChildItemBuilder {
    }
}
