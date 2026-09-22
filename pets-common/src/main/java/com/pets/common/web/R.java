package com.pets.common.web;

public record R<T>(int code, String msg, T data, PageResult page) {
    public static <T> R<T> ok(T data) { return new R<>(0, "成功", data, null); }
    public static <T> R<T> ok() { return new R<>(0, "成功", null, null); }
    public static <T> R<T> fail(int code, String msg) { return new R<>(code, msg, null, null); }
    public static <T> R<T> fail(String msg) { return new R<>(-1, msg, null, null); }
}
