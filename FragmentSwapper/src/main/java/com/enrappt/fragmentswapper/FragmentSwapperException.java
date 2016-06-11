package com.enrappt.fragmentswapper;

public class FragmentSwapperException extends RuntimeException {

    public FragmentSwapperException(String detailMessage) {
        super(detailMessage);
    }

    public FragmentSwapperException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }
}
