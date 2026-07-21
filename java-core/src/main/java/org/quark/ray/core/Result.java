package org.quark.ray.core;

/*
 * Copyright (c) 2026 DvHume
 *
 * Licensed under the MIT License.
 * See the LICENSE file in the project root for license information
 */

/**
 * Contains execution status summary data and a localized msg to the user
 */
public record Result(
        Status status,
        String data,
        String msg
) {
    public boolean success() {
        return status == Status.SUCCESS;
    }

    /**
     * Successful outcome with data
     */
    public static Result ok(String data, String msg) {
        return new Result(Status.SUCCESS, data, msg);
    }

    /**
     * Result with error
     */
    public static Result fail(Status status, String msg) {
        return new Result(status, null, msg);
    }
}
