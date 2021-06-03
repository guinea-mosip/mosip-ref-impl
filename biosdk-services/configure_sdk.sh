#!/bin/bash
# Built by MOSIP team
# Script to configure the BioSDK
set -e

echo "Configuring SDK"
mkdir -p /biosdk
cd /biosdk

echo "Download the biosdk from $biosdk_zip_file_path"
wget $biosdk_zip_file_path -O biosdk.zip
echo "Downloaded $biosdk_zip_file_path"

unzip biosdk.zip

exec "$@"