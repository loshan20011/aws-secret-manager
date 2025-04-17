/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 */

package org.wso2.asgardio.utils;

import com.google.gson.Gson;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.asgardio.exception.EncryptionException;
import org.wso2.asgardio.model.InputSecrets;
import org.wso2.asgardio.model.Secret;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.List;
import java.util.Base64;

public class EncryptionUtils {

    private static final Logger log = LoggerFactory.getLogger(EncryptionUtils.class);
    private static final String DEFAULT_TRANSFORMATION = "RSA/ECB/PKCS1Padding"; 
    private static final String BOUNCY_CASTLE_PROVIDER = BouncyCastleProvider.PROVIDER_NAME;

    static {
        if (Security.getProvider(BOUNCY_CASTLE_PROVIDER) == null) {
            log.info("Adding BouncyCastleProvider.");
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    public static void encryptPlainTextSecretsUsingCertString(List<Secret> secrets, String certificateString) {
        if (secrets == null || secrets.isEmpty()) {
            log.warn("No input secrets provided to encrypt.");
            return;
        }
        if (certificateString == null || certificateString.isEmpty()) {
             throw new EncryptionException("Certificate string provided for encryption is null or empty.");
        }

        log.info("Initializing cipher for encryption...");
        Cipher cipherForEncryption = initializeCipherUsingCertString(certificateString);
        log.info("Cipher initialized successfully.");

        secrets.forEach(secret -> {
            if (secret.getPassword() != null && !secret.getPassword().isEmpty()) {
                log.debug("Encrypting secret: {}", secret.getIdentifier());
                secret.setEncryptedPassword(performEncryption(cipherForEncryption, secret.getPassword()));
                log.debug("Successfully encrypted secret: {}", secret.getIdentifier());
            } else {
                 log.warn("Plain text password for secret '{}' is null or empty. Skipping encryption.", secret.getIdentifier());
                 secret.setEncryptedPassword("");
            }
        });
        log.info("Completed encryption for {} secrets.", secrets.size());
    }

    private static String performEncryption(Cipher cipher, String plainTextPassword) {
        if (plainTextPassword == null || plainTextPassword.isEmpty()) {
            log.warn("Attempted to encrypt a null or empty password.");
            return "";
        }

        String encodedValue;
        try {
            byte[] plainTextBytes = plainTextPassword.getBytes(StandardCharsets.UTF_8);
            byte[] encryptedPasswordBytes;
            synchronized (cipher) {
                 encryptedPasswordBytes = cipher.doFinal(plainTextBytes);
            }
            encodedValue = Base64.getEncoder().encodeToString(encryptedPasswordBytes);
        } catch (BadPaddingException | IllegalBlockSizeException exception) {
            log.error("Error during encryption: {}", exception.getMessage(), exception);
            throw new EncryptionException("Error when encrypting the provided password", exception);
        }
        return encodedValue;
    }

    private static Cipher initializeCipherUsingCertString(String certificateString) {

        String transformation = System.getProperty(Constants.CIPHER_TRANSFORMATION_SYSTEM_PROPERTY, DEFAULT_TRANSFORMATION);
        log.info("Using cipher transformation: {}", transformation);

        try (InputStream stream = new ByteArrayInputStream(certificateString.getBytes(StandardCharsets.UTF_8))) {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509", BOUNCY_CASTLE_PROVIDER);
            Certificate certificate = certificateFactory.generateCertificate(stream);

            Cipher cipher = Cipher.getInstance(transformation, BOUNCY_CASTLE_PROVIDER);
            synchronized(cipher) {
                cipher.init(Cipher.ENCRYPT_MODE, certificate.getPublicKey());
            }
            return cipher;
        } catch (CertificateException | NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | NoSuchProviderException exception) {
             log.error("Error initializing cipher: {}", exception.getMessage(), exception);
            throw new EncryptionException("Error when initializing the cipher", exception);
        } catch (java.io.IOException e) {
            log.error("IOException during cipher initialization (unexpected): {}", e.getMessage(), e);
            throw new EncryptionException("Unexpected IO error initializing cipher", e);
        }
    }

    public static InputSecrets loadInputSecrets() {
         log.info("Loading input secret identifiers from {}...", Constants.INPUT_SECRETS_SOURCE_FILE);
         Gson gson = new Gson();
         try (InputStream inputStream = loadResourceFileAsStream(Constants.INPUT_SECRETS_SOURCE_FILE);
              BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

             InputSecrets inputSecrets = gson.fromJson(reader, InputSecrets.class);
             if (inputSecrets == null || inputSecrets.getSecrets() == null || inputSecrets.getSecrets().isEmpty()) {
                log.warn("Loaded secrets file, but it contained no secret identifiers.");
                // Return empty list instead of null
                 return new InputSecrets();
             }
             log.info("Successfully loaded {} secret identifiers.", inputSecrets.getSecrets().size());
             return inputSecrets;

         } catch (com.google.gson.JsonSyntaxException e) {
             log.error("Error parsing JSON from {}: {}", Constants.INPUT_SECRETS_SOURCE_FILE, e.getMessage(), e);
             throw new EncryptionException("Invalid JSON format in " + Constants.INPUT_SECRETS_SOURCE_FILE, e);
         } catch (Exception e) {
             log.error("Failed to load or read {}: {}", Constants.INPUT_SECRETS_SOURCE_FILE, e.getMessage(), e);
             throw new EncryptionException("Could not load or read input secrets file: " + Constants.INPUT_SECRETS_SOURCE_FILE, e);
         }
     }

     private static InputStream loadResourceFileAsStream(String resourceName) {
        ClassLoader classLoader = EncryptionUtils.class.getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(resourceName);

        if (inputStream == null) {
            log.error("Resource file '{}' not found in classpath.", resourceName);
            throw new IllegalArgumentException("Resource file " + resourceName + " not found!");
        } else {
            return inputStream;
        }
    }
}
