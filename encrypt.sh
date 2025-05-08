#!/usr/bin/env bash

# -------------------------------------------------------------------------------------
#
# Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com). All Rights Reserved.
#
# This software is the property of WSO2 LLC. and its suppliers, if any.
# Dissemination of any information or reproduction of any material contained
# herein in any form is strictly forbidden, unless permitted by WSO2 expressly.
# You may not alter or remove any copyright or other notice from copies of this content.
#
# --------------------------------------------------------------------------------------

# -------------------------------------------------------------------------------------
# Script to run the AWS Encryption Manager
# Overwrites plain text secrets in AWS Secrets Manager with encrypted values.
# --------------------------------------------------------------------------------------

echo ""
echo "--------------------------------------------------------"
echo "| AWS Secrets Manager Asymmetric Key Encryption        |"
echo "--------------------------------------------------------"

function print_usage {
    echo -e "\nUsage: $0 [options]\n";
    echo -e "Options:\n"
    echo -e "  --aws-region <region>         - AWS Region where secrets are stored (e.g., us-east-1)";
    echo -e "  --pem-cert-secret-name <name> - Name of the STRING secret in Secrets Manager holding the full PEM public certificate";
    echo -e "\nExample:\n"
    echo -e "  $0 --aws-region us-west-2 --pem-cert-secret-name wso2is/internal-cert-pem\n"
    echo -e "Prerequisites:"
    echo -e "  - Java 8+ installed"
    echo -e "  - Maven installed (for building)"
    echo -e "  - AWS credentials configured (environment variables, ~/.aws/credentials, or IAM role)"
    echo -e "  - Secrets listed in src/main/resources/secrets.json must exist in the specified AWS region."
    echo -e "  - The certificate secret must exist and contain the public key certificate binary (PEM format recommended)."
    exit 1;
}

# Global variables
aws_region=""
pem_cert_secret_name=""

# Parse arguments
while [[ $# -gt 0 ]]; do
    key="$1"
    case $key in
        --aws-region)
        aws_region="$2"
        shift 2 ;;
        # *** CHANGED Argument Parsing ***
        --pem-cert-secret-name)
        pem_cert_secret_name="$2"
        shift 2 ;;
        *) echo "Unknown option: $1"; print_usage ;;
    esac
done

# Check if required arguments are provided
if [[ -z "${aws_region}" ]] || [[ -z "${pem_cert_secret_name}" ]]; then
    echo "Error: Missing required arguments."
    print_usage
fi

echo ""
echo "Configuration:"
echo "  AWS Region             : ${aws_region}"
echo "  PEM Cert Secret Name   : ${pem_cert_secret_name}"
echo ""

# --- Set paths ---
readonly SCRIPT_DIR=$(dirname "$0")
readonly JAR_FILE="${SCRIPT_DIR}/target/aws-encryption-manager.jar"
readonly JAVA_CMD=$(which java)

# --- Logging functions ---
function log_info() {
    local msg=$*
    echo "[INFO] ${msg}"
}

function log_error() {
    local msg=$*
    echo "[ERROR] ${msg}. Exiting." >&2
    exit 1
}

# --- Pre-execution Checks ---
if [[ ! -f "${JAR_FILE}" ]]; then
    log_error "JAR file not found: ${JAR_FILE}. Build the project first using 'mvn clean package'."
fi

if [[ -z "${JAVA_CMD}" ]]; then
    log_error "Java command not found. Make sure JDK is installed and in your PATH."
fi

# Check AWS credentials - basic check for common env vars or CLI config
if [[ -z "$AWS_ACCESS_KEY_ID" && -z "$AWS_SECRET_ACCESS_KEY" && -z "$AWS_SESSION_TOKEN" && ! -f ~/.aws/credentials && -z "$AWS_ROLE_ARN" ]]; then
   log_info "AWS credentials environment variables not detected. Assuming credentials configured via CLI, instance profile, or other means."
   # Note: This is not a foolproof check. The Java SDK handles the full chain.
fi


# --- Execute Application ---
log_info "Executing Encryption Manager JAR..."

if ! "${JAVA_CMD}" \
    -Dorg.wso2.asgardio.aws.region="${aws_region}" \
    -Dorg.wso2.asgardio.aws.pem.cert.secret.name="${pem_cert_secret_name}" \
    -jar "${JAR_FILE}"
then
    log_error "Failed to execute the encryption manager JAR."
else
    log_info "Encryption manager process finished."
fi

exit 0
