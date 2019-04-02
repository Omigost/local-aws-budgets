package com.omigost.localaws.budgets.rest;

import com.amazonaws.services.budgets.model.*;
import com.omigost.localaws.budgets.ServerApplication;
import com.omigost.localaws.budgets.aws.BudgetService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.bind.annotation.*;


import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@RestController
@RequestMapping("")
public class BudgetController {
    @Autowired
    private BudgetService budgetService;

	private Jackson2ObjectMapperBuilder mapperBuilder = ServerApplication.jacksonBuilder();

    private static final String PARAM_AWS_ACCOUNT_ID  = "AccountId";
	private static final String PARAM_AWS_BUDGET_NAME = "BudgetName";
	private static final String PARAM_AWS_BUDGET = "Budget";

	@RequiredArgsConstructor
	@Getter
	@FieldDefaults(makeFinal = true)
	public enum Command {
		DESCRIBE_BUDGETS("DescribeBudgets"),
		DELETE_BUDGET("DeleteBudget"),
		DESCRIBE_BUDGET("DescribeBudget"),
		CREATE_BUDGET("CreateBudget");

		String awsApiCommand;
		private static final String awsPrefix = "AWSBudgetServiceGateway.";

		public static Command stringToCommand(final String input) {
			final String opName = input.replaceAll(Pattern.quote(awsPrefix), "");

			for(Command command : Command.values()) {
				if (command.awsApiCommand.equals(opName)) {
					return command;
				}
			}
			throw new RuntimeException("Operation \"" + opName + "\" is not yet supported by the budgets server.");
		}
	}

    @GetMapping("/health")
    public String hello() {
        return "OK";
    }
    
    private Object getEndpointResponse(final String amzTarget, final Map<String, Object> requestParameters) {
		String accountId = null;
		String budgetName = null;
		String budgetJSON = null;

		if (requestParameters != null) {
			if (requestParameters.containsKey(PARAM_AWS_ACCOUNT_ID)) {
				accountId = requestParameters.get(PARAM_AWS_ACCOUNT_ID).toString();
			}
			if (requestParameters.containsKey(PARAM_AWS_BUDGET_NAME)) {
				budgetName = requestParameters.get(PARAM_AWS_BUDGET_NAME).toString();
			}
			if (requestParameters.containsKey(PARAM_AWS_BUDGET)) {
				budgetJSON = requestParameters.get(PARAM_AWS_BUDGET).toString();
			}
		}

		switch(Command.stringToCommand(amzTarget)) {
			case DESCRIBE_BUDGETS:
				return budgetService.describeBudgets(accountId);
			case DELETE_BUDGET:
				return budgetService.deleteBudget(accountId, budgetName);
			case DESCRIBE_BUDGET:
				return budgetService.describeBudget(accountId, budgetName);
			case CREATE_BUDGET:
				return budgetService.createBudget(accountId, budgetJSON);
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
}
