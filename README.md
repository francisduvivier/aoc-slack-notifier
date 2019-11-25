# Advent of Code Slack Notifier

## What?

This project is aimed at those participating in https://adventofcode.com/ and who are member of one ore
more private leaderboards.

This application will poll the AOC API, store the data in a DynamoDB and notify a Slack channel of your choice
if there has been a change in the leaderboard. 

`<TODO Screenshot of bot message>`

## How it works

This projects runs on AWS and uses the following component:

* A Lambda to poll adventofcode.com API
* DynamoDB to store historical data
* A Lambda to compare the latest two polling data
* An SQS queue to pass data between the two lambdas

![Aws architecture](Aoc-slack-notifier.svg?raw=true "Aws Architecture")

This project also uses the following frameworks:

* [Serverless](https://github.com/serverless/serverless) to deploy and manage the AWS resources
* [Spring Boot](https://spring.io/projects/spring-boot) to bootstrap the Java code
* [Spring Cloud Function](https://spring.io/projects/spring-cloud-function) to integrate between Serverless and Spring
* [jslack](https://github.com/seratch/jslack) to talk to the slack API

## Prerequisites

To run this project you need to do the following:

* Create an AWS account and and IAM user configured in your `~/.aws/credentials` file. See [AWS tutorial](https://docs.aws.amazon.com/cli/latest/userguide/cli-chap-install.html) for help
* Install Serverless via npm as explained [here](https://github.com/serverless/serverless#quick-start)
* [Create a Slack Incoming Webhook](https://api.slack.com/messaging/webhooks) in your Slack environment and make sure you know the token.
* You are member of a AOC leaderboard and you know the id, typically `https://adventofcode.com/2019/leaderboard/private/view/<id>`
* You are logged in adventofcode.com and you know your sessionId (to get it just inspect the network and look at the `cookie` header: e.g. `session=123...`)

## How to run

In the folder where you have checked out this project run the following command:

```%shell script
serverless deploy --leaderboardid <leaderboardId> --year <year e.g '2018'> --slackToken <slackToken> --sessionid <aocSessionId>
```

## TODO

* write more tests
* compute hash of AOC response and don't save new dynamo DB event if there was no change
* handle multiple (year,leaderboardId) pairs
* Ability to customise Slack message with env variable
* Use block kit to write slack message
* Could add more complex logic in Slack by getting info from the
* Process dynamoDB data to draw interesting graph of ranks/stars in time 