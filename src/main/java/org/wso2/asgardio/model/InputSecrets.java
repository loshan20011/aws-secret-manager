/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 */

package org.wso2.asgardio.model;

import java.util.List;

/**
 * This Java Class represents a list of secret used as the input to this application.
 */
public class InputSecrets {

    private List<String> secrets;

    public List<String> getSecrets() {

        return secrets;
    }

    public void setSecrets(List<String> secrets) {

        this.secrets = secrets;
    }
}
