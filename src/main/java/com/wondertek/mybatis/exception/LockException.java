package com.wondertek.mybatis.exception;

import java.io.Serializable;

/**
 * @Author zbc
 * @Date 20:30-2019/2/19
 */
public class LockException extends RuntimeException implements Serializable {

    private static final long serialVersionUID = 2328044648861336437L;

    public LockException() {
    }

    public LockException(String message) {
        super(message);
    }

    public LockException(String message, Throwable cause) {
        super(message, cause);
    }

    public LockException(Throwable cause) {
        super(cause);
    }

}
