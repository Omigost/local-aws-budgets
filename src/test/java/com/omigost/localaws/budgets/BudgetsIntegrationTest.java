package com.omigost.localaws.budgets;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.budgets.AWSBudgets;
import com.amazonaws.services.budgets.AWSBudgetsClientBuilder;
import com.amazonaws.services.budgets.model.*;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.AddPermissionRequest;
import com.amazonaws.services.sns.model.CreateTopicRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@EnableAutoConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class BudgetsIntegrationTest {

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    AmazonSNS amazonSNS;

    AWSCredentialsProvider credentials() {
        return new CustomAWSCredentialsProvider();
    }

    public AwsClientBuilder.EndpointConfiguration getEndpointConfiguration() {
        return new AwsClientBuilder.EndpointConfiguration(
                "http://" +
                        "localhost" +
                        ":" +
                        "5000", "us-east-1");
    }

    @Test
    public void someTestMethod() {
        final AWSBudgets client = AWSBudgetsClientBuilder.standard()
                .withCredentials(credentials())
                .withEndpointConfiguration(getEndpointConfiguration())
                .build();

        String testArn = amazonSNS.createTopic(
                new CreateTopicRequest()
                    .withName("test-topic")
        ).getTopicArn();

        amazonSNS.subscribe(testArn, "https", "https://976a76db.ngrok.io/notification/receive");

        final Budget b = new Budget()
                .withBudgetName("ala ma kota")
                .withBudgetLimit(
                        new Spend()
                            .withAmount(BigDecimal.valueOf(1500))
                            .withUnit("USD")
                )
                .withCostFilters(new HashMap<String, List<String>>(){{
                    put("KEY_A", new ArrayList<String>(){{ add("X"); }});
                }})
                .withCalculatedSpend(
                        new CalculatedSpend()
                                .withActualSpend(new Spend().withAmount(new BigDecimal(22)).withUnit("USD"))
                );

        client.createBudget(
                new CreateBudgetRequest()
                        .withBudget(b)
                        .withAccountId("abc")
                .withNotificationsWithSubscribers(
                        new NotificationWithSubscribers()
                            .withNotification(
                                    new Notification()
                                            .withNotificationType("ACTUAL")
                                            .withComparisonOperator("GREATER_THAN")
                                            .withThreshold(10.0d)
                                            .withThresholdType("ABSOLUTE_VALUE")
                                            .withNotificationState("OK")
                            )
                            .withSubscribers(new Subscriber().withAddress(testArn).withSubscriptionType("SNS"))
                )
        );

        final Budget storedBudget = client.describeBudget(new DescribeBudgetRequest().withBudgetName("ala ma kota").withAccountId("abc")).getBudget();

        assert (storedBudget.getBudgetName().equals(b.getBudgetName()));
        assert (storedBudget.getCostFilters().containsKey("KEY_A"));
        assert (storedBudget.getCostFilters().get("KEY_A").stream().filter(x -> x.equals("X")).count() == 1);

        try {
            Thread.sleep(100000000);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private class CustomAWSCredentialsProvider implements AWSCredentialsProvider {

        @Override
        public AWSCredentials getCredentials() {
            return new BasicAWSCredentials("key", "secret");
        }

        @Override
        public void refresh() {
        }
    }

}
