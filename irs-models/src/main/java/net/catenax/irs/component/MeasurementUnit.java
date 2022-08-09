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

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * MeasurementUnit
 */
@Value
@Jacksonized
@Builder(toBuilder = true)
public class MeasurementUnit {
    /**
     * dataTypeURI
     */
    private String datatypeURI;

    /**
     * lexicalValue
     */
    private String lexicalValue;

}
