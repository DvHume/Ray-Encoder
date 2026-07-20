package org.quark.ray.core;

/*
 * Copyright (c) 2026 DvHume
 *
 * Licensed under the MIT License.
 * See the LICENSE file in the project root for license information
 */
public record Result(
        Status status,
        String data,
        String msg
) {
    public boolean success() {
        return status == Status.SUCCESS;
    }
}
