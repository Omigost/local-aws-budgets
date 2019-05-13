package com.omigost.localaws.budgets.config;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Slf4j
public class AWSConfig {

    @Getter
    @Value("${aws.sns.ip:#{environment.AWS_SNS_IP}}")
    private String awsSnsIP;

    @Getter
    @Value("${aws.sns.port:#{environment.AWS_SNS_PORT}}")
    private String awsSnsPort;

    @Autowired
    private AWSCredentials creds;

    @Bean
    public AmazonSNS amazonSNS() {
        return AmazonSNSClientBuilder
                .standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("http://" + awsSnsIP + ":" + awsSnsPort, creds.getAwsRegion()))
                .withCredentials(creds.getProvider())
                .build();
    }
}
