package com.nimbusframework.nimbusaws

import com.amazonaws.services.apigatewaymanagementapi.AmazonApiGatewayManagementApiClientBuilder
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.amazonaws.services.lambda.AWSLambda
import com.amazonaws.services.lambda.AWSLambdaClientBuilder
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.sns.AmazonSNS
import com.amazonaws.services.sns.AmazonSNSClientBuilder
import com.amazonaws.services.sqs.AmazonSQS
import com.amazonaws.services.sqs.AmazonSQSClientBuilder
import com.google.inject.AbstractModule
import com.google.inject.assistedinject.FactoryModuleBuilder
import com.nimbusframework.nimbusaws.clients.dynamo.DynamoClient

class AwsClientModule: AbstractModule() {

    override fun configure() {
        bind(AmazonDynamoDB::class.java).toInstance(AmazonDynamoDBClientBuilder.defaultClient())
        bind(AmazonSQS::class.java).toInstance(AmazonSQSClientBuilder.defaultClient())
        bind(AmazonSNS::class.java).toInstance(AmazonSNSClientBuilder.defaultClient())
        bind(AWSLambda::class.java).toInstance(AWSLambdaClientBuilder.defaultClient())
        bind(AmazonS3::class.java).toInstance(AmazonS3ClientBuilder.defaultClient())
        bind(AmazonApiGatewayManagementApiClientBuilder::class.java).toInstance(AmazonApiGatewayManagementApiClientBuilder.standard())
        install(FactoryModuleBuilder().build(DynamoClient.DynamoClientFactory::class.java))
    }

}