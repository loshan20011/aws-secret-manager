/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 */

package org.wso2.asgardio.model;

/**
 * This Java Class represents a secret instance.
 * This type represents a secret instance during the course of runtime of this application.
 */
public class Secret {

    private String identifier;
    private String password;
    private String encryptedPassword;

    public Secret(String identifier, String password) {

        this.identifier = identifier;
        this.password = password;
    }

    public String getPassword() {

        return password;
    }

    public String getIdentifier() {

        return identifier;
    }

    public String getEncryptedPassword() {

        return encryptedPassword;
    }

    public void setEncryptedPassword(String encryptedPassword) {

        this.encryptedPassword = encryptedPassword;
    }
}
