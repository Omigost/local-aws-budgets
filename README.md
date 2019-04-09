# Omigost AWS Budgets Mock

## Description

This project provides simple mocking feature for AWS Budgets.
It's designed to use with *Docker* (for example *Testcontainers*)

## Building

To build Docker image please type the following commands:
```bash
  $ ./gradlew docke         # To build Docker image
  $ ./gradlew dockerPush    # To push the image to the repository (you must be logged in with docker login command)
```

## Running locally

To run the AWS Budgets mock locally please type the following commands into your terminal:
```bash
  $ ./gradlew bootRun
```

The budgets server will be available on port `5000`.
