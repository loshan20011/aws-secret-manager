# AWS Secrets Manager Asymmetric Key Encryption Tool

This Java application is designed to encrypt secrets stored in AWS Secrets Manager using an asymmetric encryption approach with a public certificate.

**Note: This tool overwrites existing plain text secrets in AWS Secrets Manager with their encrypted values.**

## Purpose

The primary goal of this tool is to enhance the security of sensitive information stored in AWS Secrets Manager by encrypting the plain text values using a public key. The corresponding private key (which this tool does not handle or require) would be needed to decrypt these secrets.

## Prerequisites

* **Java 8 or higher:** Ensure you have a compatible Java Development Kit (JDK) installed on your system. You can check your Java version by running `java -version` in your terminal.
* **Maven:** This project uses Maven for building. Make sure Maven is installed. You can verify by running `mvn -v`.
* **AWS Credentials Configured:** The application relies on the AWS SDK for Java to interact with AWS Secrets Manager. Ensure your AWS credentials are properly configured. This can be through environment variables (`AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`, `AWS_SESSION_TOKEN`), your `~/.aws/credentials` file, or an IAM role if running on an EC2 instance.
* **Secrets in AWS Secrets Manager:** The secrets you intend to encrypt must already exist in the AWS region you specify. The identifiers (names or ARNs) of these secrets are configured in `src/main/resources/secrets.json`.
* **Public Key Certificate in AWS Secrets Manager:** A public key certificate in PEM format must be stored as a string value in an AWS Secrets Manager secret. You will need to provide the name of this secret when running the tool.

## Setup and Usage

1.  **Clone the Repository:**
    ```bash
    git clone <repository_url>
    cd aws-encryption-manager
    ```

2.  **Review Configuration:**
    * **`src/main/resources/secrets.json`:** This file contains a JSON array of strings, where each string is the name or ARN of a secret in AWS Secrets Manager that you want to encrypt. **Edit this file to list the secrets you wish to process.**
        ```json
        {
          "secrets": [
            "your-secret-name-1",
            "..."
          ]
        }
        ```
    * **`src/main/resources/log4j.properties`:** This file configures the logging behavior of the application. You can adjust the logging levels as needed.

3.  **Build the Application:**
    Use Maven to build the JAR file:
    ```bash
    mvn clean package
    ```
    This command will download dependencies, compile the Java code, and create the executable JAR file (`aws-encryption-manager.jar`) in the `target` directory.

4.  **Run the Encryption Tool:**
    Execute the `encrypt.sh` script, providing the required AWS region and the name of the secret containing the PEM formatted public certificate:
    ```bash
    ./encrypt.sh --aws-region <your_aws_region> --pem-cert-secret-name <name_of_pem_cert_secret>
    ```
    Replace:
    * `<your_aws_region>`: The AWS region where your secrets and the certificate secret are located (e.g., `us-east-1`, `ap-southeast-2`).
    * `<name_of_pem_cert_secret>`: The exact name of the AWS Secrets Manager secret that holds the **full PEM-encoded public key certificate as a string**.

    **Example:**
    ```bash
    ./encrypt.sh --aws-region us-west-2 --pem-cert-secret-name internal/public-encryption-cert
    ```

    The script will then:
    * Validate the provided arguments.
    * Execute the Java application, passing the AWS region and certificate secret name as system properties.
    * The Java application will:
        * Connect to AWS Secrets Manager.
        * Load the list of secrets to encrypt from `secrets.json`.
        * Retrieve the plain text values of these secrets.
        * Retrieve the PEM certificate string.
        * Extract the clean PEM block from the certificate string.
        * Encrypt each plain text secret using the public key from the certificate.
        * Overwrite the original secrets in AWS Secrets Manager with their encrypted values.
        * Log the progress and any errors.

## Project Structure

```plaintext

.
├── encrypt.sh
├── pom.xml
├── README.md
├── src
│   └── main
│       ├── java
│       │   └── org
│       │       └── wso2
│       │           └── asgardeo
│       │               ├── encrypt
│       │               │   └── PasswordEncryptor.java
│       │               ├── exception
│       │               │   └── EncryptionException.java
│       │               ├── model
│       │               │   ├── InputSecrets.java
│       │               │   └── Secret.java
│       │               └── utils
│       │                   ├── AwsSecretsManagerUtils.java
│       │                   ├── Constants.java
│       │                   └── EncryptionUtils.java
│       └── resources
│           ├── log4j.properties
│           └── secrets.json
└── target
    ├── archive-tmp
    ├── aws-encryption-manager-1.0-SNAPSHOT.jar
    ├── aws-encryption-manager.jar
    ├── classes
    │   ├── log4j.properties
    │   ├── org
    │   │   └── wso2
    │   │       └── asgardeo
    │   │           ├── encrypt
    │   │           │   └── PasswordEncryptor.class
    │   │           ├── exception
    │   │           │   └── EncryptionException.class
    │   │           ├── model
    │   │           │   ├── InputSecrets.class
    │   │           │   └── Secret.class
    │   │           └── utils
    │   │               ├── AwsSecretsManagerUtils.class
    │   │               ├── Constants.class
    │   │               └── EncryptionUtils.class
    │   └── secrets.json
    ├── generated-sources
    │   └── annotations
    ├── maven-archiver
    │   └── pom.properties
    └── maven-status
        └── maven-compiler-plugin
            └── compile
                └── default-compile
                    ├── createdFiles.lst
                    └── inputFiles.lst

```

## Contributing

Please follow the guidelines below:

1. Fork the repository.

2. Create a new branch for your changes.
   ```bash
   git checkout -b new-branch
   ```
3. Make your changes and commit them.
   ```bash
   git commit -m "Description of your changes"
   ```

4. Push your changes to your fork.
   ```bash
   git push origin new-branch
   ```

5. Open a pull request using the provided template.
