package com.omigost.localaws.budgets;

import com.amazonaws.services.budgets.model.*;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.budgets.AWSBudgets;
import com.amazonaws.services.budgets.AWSBudgetsClientBuilder;
import com.omigost.localaws.budgets.aws.BudgetService;
import com.omigost.localaws.budgets.config.AppConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;

@RunWith(SpringJUnit4ClassRunner.class)
@EnableAutoConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class BudgetsIntegrationTest {

    @Autowired
    private WebApplicationContext wac;

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

        final Budget b = new Budget()
                .withBudgetName("ala ma kota")
                .withCalculatedSpend(
                        new CalculatedSpend()
                        .withActualSpend(new Spend().withAmount(new BigDecimal(22)).withUnit("USD"))
                );

        client.createBudget(
            new CreateBudgetRequest()
                .withBudget(b)
                .withAccountId("abc")
        );

        assert(client.describeBudget(new DescribeBudgetRequest().withBudgetName("ala ma kota").withAccountId("abc")).getBudget().equals(b));
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
