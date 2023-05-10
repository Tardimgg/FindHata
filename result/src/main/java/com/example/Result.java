package com.example;


import lombok.NonNull;

public class Result <T, E> {

    public enum Val {
        Ok, Err
    }

    public final Val type;
    public final T ok;
    public final E err;

    private Result(T ok, E err) {
        type = ok != null ? Val.Ok : Val.Err;
        this.ok = ok;
        this.err = err;
    }

    public static <T1, E1> Result<T1, E1> ok(@NonNull T1 ok) {
        return new Result<>(ok, null);
    }

    public static <T1, E1> Result<T1, E1> err(@NonNull E1 err) {
        return new Result<>(null, err);
    }
}
