# HTML5 EXPENSE - OAUTH #

## Overview ##

The OAuth app is used to authenticate the client. It is deployed separately from the API app.

## Project Structure ##

the web application

* src - all source code
* main/java - backend java source code
* main/webapp/assets - static sources including css, javascript, and image files
* main/webapp/WEB-INF/views - template sources that generic dynamic views
* test/ - test code

## Build the App ##

To build a deployable web app archive (.war):

    ./gradlew build

See build/libs/oauth.war

## Deploy to Cloud Foundry ##

    vmc push <app_name> --path build/libs

The OAuth app requires a PostgreSQL service to be installed in Cloud Foundry. When pushing this to a new application in Cloud Foundry, add a PostgreSQL service and name it "tokendb".