/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 */

package org.wso2.asgardio.exception;

/**
 * This Java Class defines a custom Java {@link Exception} used to wrap an actual exception with a custom message.
 */
public class EncryptionException extends RuntimeException {

    public EncryptionException(String msg) {

        super(msg);
    }

    public EncryptionException(String msg, Throwable exception) {

        super(msg, exception);
    }
}
