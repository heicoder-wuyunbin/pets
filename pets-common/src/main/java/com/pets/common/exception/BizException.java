package com.pets.common.exception;

public class BizException extends RuntimeException {
    private final int code;
    private final String msg;

    public BizException(int code, String msg) {
        super(msg);
        this.code = code;
        this.msg = msg;
    }

    public BizException(String msg) {
        this(-1, msg);
    }

    public int getCode() { return code; }
    public String getMsg() { return msg; }
}
