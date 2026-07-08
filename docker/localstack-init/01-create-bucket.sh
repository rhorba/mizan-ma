#!/bin/sh
# Local-dev stand-in for Cloudflare R2 (S3-compatible) — creates the contracts bucket
# in LocalStack so contracts-service can run without real R2 credentials.
awslocal s3 mb "s3://${R2_BUCKET_NAME}"
