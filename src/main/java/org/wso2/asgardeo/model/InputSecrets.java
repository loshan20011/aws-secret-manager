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
