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


/**
 * Helper class contains all Api Errors as reusable dtos.
 */
@SuppressWarnings({ "PMD.CommentRequired" })
public class ApiErrorsConstants {
    public static final String INVALID_ARGUMENTS = "Invalid Arguments.";
    public static final String INVALID_DEPTH = "Invalid Depth.";
    public static final String ITEM_VIEW_NOT_NULL = "Must not be null or provide  AS_BUILT.";
    public static final String ITEM_VIEW_MUST_MATCH_ENUM = "Must be AS_BUILT. ";
    public static final String VEHICLE_NOT_FOUND_BY_VIN = "Vehicle not found by VIN {0}";
    public static final String ITEM_MIN_DEPTH = "Depth should be at least 1.";
    public static final String ITEM_MAX_DEPTH = "Depth should not be more than 100";
    public static final String NOT_BLANK = "Must not be blank.";
    public static final String ASPECT_NOT_SUPPORTED = "AspectType not supported";
}
