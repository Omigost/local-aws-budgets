package com.omigost.localaws.budgets.rest;

import com.omigost.localaws.budgets.ServerApplication;
import com.omigost.localaws.budgets.aws.BudgetService;
import com.omigost.localaws.budgets.aws.NotificationService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;
import java.util.regex.Pattern;

@RestController
@RequestMapping("")
@Slf4j
public class BudgetController {
    private static final String PARAM_AWS_ACCOUNT_ID = "AccountId";
    private static final String PARAM_AWS_BUDGET_NAME = "BudgetName";
    private static final String PARAM_AWS_BUDGET = "Budget";
    private static final String PARAM_AWS_NOTIFICATION = "Notification";
    private static final String PARAM_AWS_SUBSCRIBERS = "Subscribers";
    private static final String PARAM_AWS_NOTIFICATIONS_WITH_SUBSCRIBERS = "NotificationsWithSubscribers";

    @Autowired
    private BudgetService budgetService;

    @Autowired
    private NotificationService notificationService;

    private Jackson2ObjectMapperBuilder mapperBuilder = ServerApplication.jacksonBuilder();

    @GetMapping("/health")
    public String hello() {
        return "OK";
    }

    private String retrieveKey(final Map<String, Object> requestParameters, final String keyName) {
        if (requestParameters == null) {
            return null;
        }
        if (requestParameters.containsKey(keyName)) {
            return requestParameters.get(keyName).toString();
        }
        return null;
    }

    private Object getEndpointResponse(final String amzTarget, final Map<String, Object> requestParameters) {
        log.debug("Received new request. Printing all request variables:");
        if (log.isDebugEnabled()){
            if (requestParameters == null) {
                log.debug("  --- None ---");
            } else {
                for (final String key : requestParameters.keySet()) {
                    log.debug("   [" + key + "] = " + requestParameters.get(key).toString());
                }
            }
        }
        log.debug("End of request print. Now it will be dispatched.");

        switch (Command.stringToCommand(amzTarget)) {
            case DESCRIBE_BUDGETS:
                return budgetService.describeBudgets(
                    retrieveKey(requestParameters, PARAM_AWS_ACCOUNT_ID)
                );
            case DELETE_BUDGET:
                return budgetService.deleteBudget(
                    retrieveKey(requestParameters, PARAM_AWS_ACCOUNT_ID),
                    retrieveKey(requestParameters, PARAM_AWS_BUDGET_NAME)
                );
            case DESCRIBE_BUDGET:
                return budgetService.describeBudget(
                        retrieveKey(requestParameters, PARAM_AWS_ACCOUNT_ID),
                        retrieveKey(requestParameters, PARAM_AWS_BUDGET_NAME)
                );
            case CREATE_BUDGET:
                return budgetService.createBudget(
                        retrieveKey(requestParameters, PARAM_AWS_ACCOUNT_ID),
                        retrieveKey(requestParameters, PARAM_AWS_BUDGET),
                        retrieveKey(requestParameters, PARAM_AWS_NOTIFICATIONS_WITH_SUBSCRIBERS)
                );
            case CREATE_NOTIFICATION:
                return notificationService.createNotification(
                        retrieveKey(requestParameters, PARAM_AWS_ACCOUNT_ID),
                        retrieveKey(requestParameters, PARAM_AWS_BUDGET_NAME),
                        retrieveKey(requestParameters, PARAM_AWS_NOTIFICATION),
                        retrieveKey(requestParameters, PARAM_AWS_SUBSCRIBERS)
                 );
            default:
                return null;
        }
    }

    @RequestMapping(
            value = "/",
            method = RequestMethod.POST,
            consumes = MediaType.ALL_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Object describeBudgetsGet(
            @RequestHeader("X-Amz-Target") String amzTarget,
            HttpEntity<String> httpEntity
    ) throws IOException {
        return getEndpointResponse(amzTarget, mapperBuilder.build().readValue(httpEntity.getBody(), Map.class));
    }

    @RequestMapping(
            value = "/",
            method = RequestMethod.GET,
            consumes = MediaType.ALL_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Object describeBudgetsPost(
            @RequestHeader("X-Amz-Target") String amzTarget,
            HttpEntity<String> httpEntity
    ) throws IOException {
        return getEndpointResponse(amzTarget, mapperBuilder.build().readValue(httpEntity.getBody(), Map.class));
    }

    @RequiredArgsConstructor
    @Getter
    @FieldDefaults(makeFinal = true)
    public enum Command {
        DESCRIBE_BUDGETS("DescribeBudgets"),
        DELETE_BUDGET("DeleteBudget"),
        DESCRIBE_BUDGET("DescribeBudget"),
        CREATE_BUDGET("CreateBudget"),
        CREATE_NOTIFICATION("CreateNotification");

        private static final String awsPrefix = "AWSBudgetServiceGateway.";
        String awsApiCommand;

        public static Command stringToCommand(final String input) {
            final String opName = input.replaceAll(Pattern.quote(awsPrefix), "");

            for (Command command : Command.values()) {
                if (command.awsApiCommand.equals(opName)) {
                    return command;
                }
            }
            throw new RuntimeException("Operation \"" + opName + "\" is not yet supported by the budgets server.");
        }
    }
}
