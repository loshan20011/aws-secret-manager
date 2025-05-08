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

package org.wso2.asgardeo.model;

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
