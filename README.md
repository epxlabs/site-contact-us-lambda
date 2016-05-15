# site-contact-us-lambda

A Clojure Lambda designed to receive a JSON message from the Contact Us form on the site and send an email to EPX Labs.

## AWS IAM Role

```json
  {
    "Version": "2012-10-17",
    "Statement": [
     {
       "Effect": "Allow",
       "Action": ["ses:SendEmail", "ses:SendRawEmail"],
       "Resource":"*",
           "Condition": {
             "StringEquals": {
               "ses:FromAddress": "prachetas@epxlabs.com"
             }
         }
       }
    ] 
  }
```

Also the lambda_logging policy is attached

## Uberjar Creation

To create the JAR

```shell
lein uberjar
```

Verify that site-contact-us-lambda-0.1.0-SNAPSHOT-standalone.jar exists in ./target/

## Lambda Creation

To create the lambda run

```sh
aws lambda create-function \
--function-name site-contact-us \
--handler 'site_contact_us_lambda.core' \
--runtime java8 \
--memory 192 \
--timeout 20 \
--role 'arn:aws:iam::807976332278:role/lambda_siteapi_contact_us' \
--zip-file 'fileb://./target/site-contact-us-lambda-0.1.0-SNAPSHOT-standalone.jar'
```

This will return something like:

```json
{
    "CodeSha256": "aUDD2xOT2VvCE4/y49ntBK7uNIi9gP5HZzB87ZA0jko=", 
    "FunctionName": "site-contact-us", 
    "CodeSize": 8414199, 
    "MemorySize": 192, 
    "FunctionArn": "arn:aws:lambda:us-east-1:807976332278:function:site-contact-us", 
    "Version": "$LATEST", 
    "Role": "arn:aws:iam::807976332278:role/lambda_siteapi_contact_us", 
    "Timeout": 10, 
    "LastModified": "2016-05-15T00:22:05.873+0000", 
    "Handler": "site_contact_us_lambda.core", 
    "Runtime": "java8", 
    "Description": ""
}
```

Take note of the `FunctionArn` you will need it for API Gateway.

## Lambda Testing

To test the contact us email sending we must use the test data in `resources/test/input/sample_contact_data.json`.

To invoke the function we run:

```shell
aws lambda invoke \
 --invocation-type RequestResponse \
 --function-name site-contact-us \
 --payload 'file://./resources/test/input/sample_contact_data.json' \
 resources/test/output/contact_output.json
```

## Deploy New Lambda Code

Don't forget to run `lein uberjar` before deploying!

To deploy new code run:

```shell
aws lambda update-function-code \
 --function-name site-contact-us \
 --zip-file 'fileb://./target/site-contact-us-lambda-0.1.0-SNAPSHOT-standalone.jar'
```

## Update Lambda Configuration

To update configuration as opposed to code run:

```shell
aws lambda update-function-configuration \
 --function-name site-contact-us \
 --memory 192 \
 --timeout 20
```

## License

Copyright © 2016 EPX Labs, Inc.
