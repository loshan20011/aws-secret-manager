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

package org.wso2.asgardeo.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.asgardeo.exception.EncryptionException;
import org.wso2.asgardeo.model.InputSecrets;
import org.wso2.asgardeo.model.Secret;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;
import software.amazon.awssdk.services.secretsmanager.model.ResourceNotFoundException;
import software.amazon.awssdk.services.secretsmanager.model.UpdateSecretRequest;
import software.amazon.awssdk.services.secretsmanager.model.SecretsManagerException;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AwsSecretsManagerUtils {

    private static final Logger log = LoggerFactory.getLogger(AwsSecretsManagerUtils.class);

    /**
     * Creates an AWS Secrets Manager client.
     *
     * @return Configured SecretsManagerClient.
     * @throws EncryptionException if the AWS region is not configured.
     */
    public static SecretsManagerClient createSecretsManagerClient() {
        String regionName = System.getProperty(Constants.AWS_REGION_SYS_PROPERTY_KEY);
        if (regionName == null || regionName.trim().isEmpty()) {
            throw new EncryptionException("AWS Region not configured. Please set the system property: " + Constants.AWS_REGION_SYS_PROPERTY_KEY);
        }
        try {
            Region region = Region.of(regionName);
             log.info("Creating AWS Secrets Manager client for region: {}", region);
            return SecretsManagerClient.builder()
                    .region(region)
                    .build();
        } catch (IllegalArgumentException e) {
             log.error("Invalid AWS Region name specified: {}", regionName, e);
             throw new EncryptionException("Invalid AWS Region specified: " + regionName, e);
        } catch (Exception e) {
             log.error("Failed to create AWS Secrets Manager client: {}", e.getMessage(), e);
             throw new EncryptionException("Could not create AWS Secrets Manager client", e);
        }
    }

    /**
     * Retrieve the defined plain text passwords from AWS Secrets Manager.
     * Secret names are defined in the secrets.json file.
     *
     * @param inputSecrets Input secret names.
     * @param client       AWS Secrets Manager client.
     * @return List of Secrets with identifiers and plain text passwords.
     * @throws EncryptionException if secrets cannot be retrieved.
     */
    public static List<Secret> retrievePlainTextSecrets(InputSecrets inputSecrets, SecretsManagerClient client) {
        Objects.requireNonNull(inputSecrets, "InputSecrets cannot be null");
        Objects.requireNonNull(client, "SecretsManagerClient cannot be null");

        List<String> secretNames = inputSecrets.getSecrets();
        if (secretNames == null || secretNames.isEmpty()) {
            log.warn("No secret names provided in input secrets file.");
            return new ArrayList<>();
        }

        log.info("Retrieving {} plain text secrets from AWS Secrets Manager...", secretNames.size());
        List<Secret> secrets = new ArrayList<>();
        for (String secretName : secretNames) {
            log.debug("Retrieving secret: {}", secretName);
            try {
                GetSecretValueRequest valueRequest = GetSecretValueRequest.builder()
                        .secretId(secretName)
                        .build();
                GetSecretValueResponse valueResponse = client.getSecretValue(valueRequest);

                String plainTextPassword = valueResponse.secretString();
                if (plainTextPassword == null) {
                     log.warn("Retrieved null secretString for secret '{}'. Treating as empty.", secretName);
                     plainTextPassword = "";
                }
                 secrets.add(new Secret(secretName, plainTextPassword));
                 log.debug("Successfully retrieved secret: {}", secretName);

            } catch (ResourceNotFoundException e) {
                log.error("Secret '{}' not found in AWS Secrets Manager.", secretName);
                throw new EncryptionException("Secret not found: " + secretName, e);
            } catch (SecretsManagerException e) {
                log.error("Error retrieving secret '{}' from AWS Secrets Manager: {}", secretName, e.awsErrorDetails().errorMessage(), e);
                throw new EncryptionException("AWS error retrieving secret: " + secretName, e);
            } catch (Exception e) {
                 log.error("Unexpected error retrieving secret '{}': {}", secretName, e.getMessage(), e);
                 throw new EncryptionException("Unexpected error retrieving secret: " + secretName, e);
            }
        }
        log.info("Successfully retrieved {} plain text secrets.", secrets.size());
        return secrets;
    }

     /**
     * Retrieves the secret string value for a given secret name.
     *
     * @param client     AWS Secrets Manager client.
     * @param secretName The exact name or ARN of the secret.
     * @return The secret string value.
     * @throws EncryptionException if the secret is not found or retrieval fails.
     */
    public static String getSecretStringValue(SecretsManagerClient client, String secretName) {
        Objects.requireNonNull(client, "SecretsManagerClient cannot be null");
        if (secretName == null || secretName.trim().isEmpty()) {
            throw new IllegalArgumentException("Secret name cannot be null or empty.");
        }

        log.info("Retrieving secret string value for: {}", secretName);
        try {
            GetSecretValueRequest valueRequest = GetSecretValueRequest.builder()
                    .secretId(secretName)
                    .build();
            GetSecretValueResponse valueResponse = client.getSecretValue(valueRequest);

            String secretValue = valueResponse.secretString();
            if (secretValue == null) {
                // This might happen if it's stored as binary, which shouldn't be the case now
                log.error("Secret '{}' does not contain a string value (secretString is null).", secretName);
                throw new EncryptionException("Secret '" + secretName + "' is not a string secret.");
            }
            log.info("Successfully retrieved secret string for: {}", secretName);
            return secretValue;

        } catch (ResourceNotFoundException e) {
            log.error("Secret '{}' not found in AWS Secrets Manager.", secretName);
            throw new EncryptionException("Secret not found: " + secretName, e);
        } catch (SecretsManagerException e) {
            log.error("Error retrieving secret '{}' from AWS Secrets Manager: {}", secretName, e.awsErrorDetails().errorMessage(), e);
            throw new EncryptionException("AWS error retrieving secret: " + secretName, e);
        } catch (Exception e) {
             log.error("Unexpected error retrieving secret '{}': {}", secretName, e.getMessage(), e);
             throw new EncryptionException("Unexpected error retrieving secret: " + secretName, e);
        }
    }


    /**
     * Updates secrets in AWS Secrets Manager with their encrypted values.
     * WARNING: This overwrites the existing secret value.
     *
     * @param secrets List of Secret objects with encrypted passwords populated.
     * @param client  AWS Secrets Manager client.
     * @throws EncryptionException if update fails.
     */
    public static void updateSecretsWithEncryptedValue(List<Secret> secrets, SecretsManagerClient client) {
        Objects.requireNonNull(secrets, "Secrets list cannot be null");
        Objects.requireNonNull(client, "SecretsManagerClient cannot be null");

        if (secrets.isEmpty()) {
             log.warn("No secrets provided to update in AWS Secrets Manager.");
             return;
        }

        log.warn("Starting update process. This will OVERWRITE existing secrets in AWS Secrets Manager.");
        int updateCount = 0;
        for (Secret secret : secrets) {
            if (secret.getEncryptedPassword() == null) {
                 log.warn("Secret '{}' has null encrypted password. Skipping update.", secret.getIdentifier());
                 continue;
            }
            log.debug("Updating secret: {}", secret.getIdentifier());
            try {
                UpdateSecretRequest updateRequest = UpdateSecretRequest.builder()
                        .secretId(secret.getIdentifier())
                        .secretString(secret.getEncryptedPassword())
                        .build();

                client.updateSecret(updateRequest);
                log.debug("Successfully updated secret: {}", secret.getIdentifier());
                updateCount++;
            } catch (ResourceNotFoundException e) {
                log.error("Cannot update secret '{}' as it was not found (unexpected).", secret.getIdentifier());
                throw new EncryptionException("Cannot update non-existent secret: " + secret.getIdentifier(), e);
            } catch (SecretsManagerException e) {
                log.error("Error updating secret '{}' in AWS Secrets Manager: {}", secret.getIdentifier(), e.awsErrorDetails().errorMessage(), e);
                throw new EncryptionException("AWS error updating secret: " + secret.getIdentifier(), e);
            } catch (Exception e) {
                 log.error("Unexpected error updating secret '{}': {}", secret.getIdentifier(), e.getMessage(), e);
                 throw new EncryptionException("Unexpected error updating secret: " + secret.getIdentifier(), e);
            }
        }
         log.warn("Completed update process. Successfully updated {} secrets.", updateCount);
    }
}
