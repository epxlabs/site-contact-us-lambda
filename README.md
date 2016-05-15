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

## API Gateway settings for application/x-www-form-urlencoded

API Gateway is very picky about what you send it. It seems to strongly prefer application/json. So in order to get it to accept form data we must use a mapping.

This mapping came in very handy: https://forums.aws.amazon.com/thread.jspa?messageID=673012&tstart=0#673012

Below is basically the same with a minor tweak to allow empty params.

```
## convert HTML POST data or HTTP GET query string to JSON
 
## get the raw post data from the AWS built-in variable and give it a nicer name
#if ($context.httpMethod == "POST")
 #set($rawAPIData = $input.path('$'))
#elseif ($context.httpMethod == "GET")
 #set($rawAPIData = $input.params().querystring)
 #set($rawAPIData = $rawAPIData.toString())
 #set($rawAPIDataLength = $rawAPIData.length() - 1)
 #set($rawAPIData = $rawAPIData.substring(1, $rawAPIDataLength))
 #set($rawAPIData = $rawAPIData.replace(", ", "&"))
#else
 #set($rawAPIData = "")
#end
 
## first we get the number of "&" in the string, this tells us if there is more than one key value pair
#set($countAmpersands = $rawAPIData.length() - $rawAPIData.replace("&", "").length())
 
## if there are no "&" at all then we have only one key value pair.
## we append an ampersand to the string so that we can tokenise it the same way as multiple kv pairs.
## the "empty" kv pair to the right of the ampersand will be ignored anyway.
#if ($countAmpersands == 0)
 #set($rawPostData = $rawAPIData + "&")
#end
 
## now we tokenise using the ampersand(s)
#set($tokenisedAmpersand = $rawAPIData.split("&"))
 
## we set up a variable to hold the valid key value pairs
#set($tokenisedEquals = [])
 
## now we set up a loop to find the valid key value pairs, which must contain only one "="
#foreach( $kvPair in $tokenisedAmpersand )
 #set($countEquals = $kvPair.length() - $kvPair.replace("=", "").length())
 #if ($countEquals == 1)
  #set($kvTokenised = $kvPair.split("="))
  #if ($kvTokenised[0].length() > 0)
   ## we found a valid key value pair. add it to the list.
   #set($devNull = $tokenisedEquals.add($kvPair))
  #end
 #end
#end
 
## next we set up our loop inside the output structure "{" and "}"
{
#foreach( $kvPair in $tokenisedEquals )
  ## finally we output the JSON for this pair and append a comma if this isn't the last pair
  #set($kvTokenised = $kvPair.split("="))
 "$util.urlDecode($kvTokenised[0])" : #if($kvTokenised.size() > 1 && $kvTokenised[1].length() > 0)"$util.urlDecode($kvTokenised[1])"#{else}""#end#if( $foreach.hasNext ),#end
#end
}
```

## Test API Gateway using cURL

On your local machine execute:

```shell
curl -X POST -v -d 'name=Gilligan+Dilbert&email=wheres%40my.money&phone=&message=All+revolutions+start+with+one+man+and+a+computer' https://stagingsiteapi.epxlabs.com/contact-us --header "Content-Type:application/x-www-form-urlencoded; charset=UTF-8"
```

## License

Copyright © 2016 EPX Labs, Inc.
