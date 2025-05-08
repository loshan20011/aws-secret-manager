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

package org.wso2.asgardio.utils;

public class Constants {

    // Name of the JSON file containing the secret identifiers
    static final String INPUT_SECRETS_SOURCE_FILE = "secrets.json";
    static final String CIPHER_TRANSFORMATION_SYSTEM_PROPERTY = "org.wso2.asgardio.cipher.transformation";

    // --- AWS Configuration System Property Keys ---
    static final String AWS_REGION_SYS_PROPERTY_KEY = "org.wso2.asgardio.aws.region";
    // *** CHANGED: Key for the NAME of the STRING secret holding the full PEM certificate ***
    public static final String PUBLIC_PEM_CERT_SECRET_NAME_SYS_PROPERTY_KEY = "org.wso2.asgardio.aws.pem.cert.secret.name"; // Renamed for clarity

    private Constants() {}
}
