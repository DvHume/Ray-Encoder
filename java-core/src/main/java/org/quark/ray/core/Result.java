package org.quark.ray.core;


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
