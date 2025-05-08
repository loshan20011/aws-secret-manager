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

package org.wso2.asgardio.encrypt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.asgardio.exception.EncryptionException;
import org.wso2.asgardio.model.InputSecrets;
import org.wso2.asgardio.model.Secret;
import org.wso2.asgardio.utils.AwsSecretsManagerUtils;
import org.wso2.asgardio.utils.EncryptionUtils;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;

import java.nio.charset.StandardCharsets;
import java.util.List;
import org.wso2.asgardio.utils.Constants;

public class PasswordEncryptor {

    private static final Logger log = LoggerFactory.getLogger(PasswordEncryptor.class);
    private static final String PEM_BEGIN_MARKER = "-----BEGIN CERTIFICATE-----";
    private static final String PEM_END_MARKER = "-----END CERTIFICATE-----";


    public static void main(String... args) {
        log.info("Starting AWS Encryption Manager process...");

        SecretsManagerClient client = null;
        try {
            // 1. Create AWS Client
            client = AwsSecretsManagerUtils.createSecretsManagerClient();

            // 2. Load Secret Identifiers
            InputSecrets inputSecrets = EncryptionUtils.loadInputSecrets();
            // ... (check if inputSecrets is empty) ...

            // 3. Retrieve Plain Text Secrets
            List<Secret> secrets = AwsSecretsManagerUtils.retrievePlainTextSecrets(inputSecrets, client);
            // ... (check if secrets is empty) ...

            // 4. Retrieve the *full* Certificate String Secret
            String pemCertSecretName = System.getProperty(Constants.PUBLIC_PEM_CERT_SECRET_NAME_SYS_PROPERTY_KEY);
            if (pemCertSecretName == null || pemCertSecretName.trim().isEmpty()) {
                throw new EncryptionException("Public PEM certificate secret name not configured. Set system property: " + Constants.PUBLIC_PEM_CERT_SECRET_NAME_SYS_PROPERTY_KEY);
            }
            // Retrieve the potentially "dirty" string containing extra metadata
            String fullCertificateStringFromSecret = AwsSecretsManagerUtils.getSecretStringValue(client, pemCertSecretName);
            log.debug("Retrieved full certificate string from secret '{}'. Length: {}", pemCertSecretName, fullCertificateStringFromSecret.length());

            // 5. *** NEW: Extract the Clean PEM Block from the retrieved string ***
            int beginIndex = fullCertificateStringFromSecret.indexOf(PEM_BEGIN_MARKER);
            int endIndex = fullCertificateStringFromSecret.indexOf(PEM_END_MARKER);

            if (beginIndex == -1) {
                 log.error("Could not find '{}' marker in the certificate string retrieved from secret '{}'.", PEM_BEGIN_MARKER, pemCertSecretName);
                 throw new EncryptionException("Certificate data from secret " + pemCertSecretName + " is missing the BEGIN marker.");
            }
            if (endIndex == -1) {
                 log.error("Could not find '{}' marker in the certificate string retrieved from secret '{}'.", PEM_END_MARKER, pemCertSecretName);
                 throw new EncryptionException("Certificate data from secret " + pemCertSecretName + " is missing the END marker.");
            }

            // Extract the substring including the markers
            String cleanCertificatePem = fullCertificateStringFromSecret.substring(beginIndex, endIndex + PEM_END_MARKER.length());
            log.info("Successfully extracted clean PEM certificate block. Length: {}", cleanCertificatePem.length());


            // 6. Encrypt Plain Text Secrets using the *Cleaned* Certificate String
            // Pass the extracted 'cleanCertificatePem' string
            EncryptionUtils.encryptPlainTextSecretsUsingCertString(secrets, cleanCertificatePem);

            // 7. Update Secrets in AWS Secrets Manager
            AwsSecretsManagerUtils.updateSecretsWithEncryptedValue(secrets, client);

            log.info("AWS Encryption Manager process completed successfully.");

        } catch (EncryptionException e) {
            log.error("Process failed due to EncryptionException: {}", e.getMessage(), e.getCause() != null ? e.getCause() : e);
            System.exit(1);
        } catch (Exception e) {
             log.error("An unexpected error occurred during the process: {}", e.getMessage(), e);
             System.exit(1);
        } finally {
            // 8. Close AWS Client
            if (client != null) {
                log.info("Closing AWS Secrets Manager client.");
                client.close();
            }
        }
    }
}
