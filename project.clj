(defproject site-contact-us-lambda "0.1.0-SNAPSHOT"
  :description "Lambda function to support the EPX Labs Contact Us form"
  :url "https://github.com/epxlabs/site-contact-us-lambda"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/data.json "0.2.6"]
                 [com.amazonaws/aws-java-sdk-ses "1.11.0"]
                 [com.amazonaws/aws-lambda-java-core "1.1.0"]]
  :java-source-paths ["src/java"]
  :aot :all)
