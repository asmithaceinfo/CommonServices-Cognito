package com.aceinfo.commonservices.security.api.cognito.config;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSSessionCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClientBuilder;

@Configuration
public class CognitoConfig {
	private static final Logger logger = LoggerFactory.getLogger(CognitoConfig.class);

	@Value("${AWS_ACCESS_KEY_ID}")
	private String								awsAccessKey;
	@Value("${AWS_SECRET_ACCESS_KEY}")
	private String								awsSecretKey;
	@Value("${aws_region}")
	private String								awsRegion;

	
	
	@Bean
	public AWSCognitoIdentityProvider identityProviderFactory() {
				
		BasicAWSCredentials credChain = new BasicAWSCredentials(awsAccessKey, awsSecretKey);
		
		AWSCognitoIdentityProvider	cognitoClient	= AWSCognitoIdentityProviderClientBuilder.standard()
				 .withCredentials(new AWSStaticCredentialsProvider(credChain))
				 .withRegion(awsRegion)
				 .build();//.defaultClient();
		
		return cognitoClient;
	}

	
}
