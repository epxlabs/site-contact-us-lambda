(ns site-contact-us-lambda.core
  (:gen-class
   :implements [com.amazonaws.services.lambda.runtime.RequestStreamHandler])
  (:require [clojure.data.json :as json]
            [clojure.java.io :as io])
  (:import (com.amazonaws.services.simpleemail AmazonSimpleEmailServiceClient)
           (com.amazonaws.services.simpleemail.model SendEmailRequest Content Message Body Destination)
           #_(com.amazonaws.auth BasicAWSCredentials DefaultAWSCredentialsProviderChain AWSCredentialsProvider AWSCredentials)
           #_(com.amazonaws.regions Region Regions)))

#_(. Regions US_EAST_1)

(def ses-client ^AmazonSimpleEmailServiceClient (AmazonSimpleEmailServiceClient.))

(defn compose-body [details]
  (str "FROM STAGING: Congratulations! Someone actually wants to talk to you:\n\n"
       ;; We like Clojure so just send us the map literal!
       (with-out-str
         (clojure.pprint/pprint details))))

(defn send-email [details]
  (let [to-addresses (.withToAddresses (Destination.) ["prachetas@epxlabs.com" "evan@epxlabs.com"])
        body (Body. (Content. (compose-body details)))
        message (Message. (Content. "A Wild Contact Us Submission Appears!") body)]
    (.sendEmail
     ses-client
     (SendEmailRequest. "Contact Us <prachetas@epxlabs.com>" to-addresses message))
    {:success true}))

(defn -handleRequest [this is os context]
  (let [w (io/writer os)]
    (-> (io/reader is)
        (json/read :key-fn keyword)
        (send-email)
        ;; Write JSON to the writer
        (json/write w))
    (.flush w)))
