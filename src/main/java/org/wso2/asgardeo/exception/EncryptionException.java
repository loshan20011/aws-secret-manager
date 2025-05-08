/*
* -------------------------------------------------------------------------------------
*
* Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
*
* This software is the property of WSO2 LLC. and its suppliers, if any.
* Dissemination of any information or reproduction of any material contained
* herein in any form is strictly forbidden, unless permitted by WSO2 expressly.
* You may not alter or remove any copyright or other notice from copies of this content.
*
* --------------------------------------------------------------------------------------
*/

package org.wso2.asgardeo.exception;

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
