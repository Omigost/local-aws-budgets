package com.omigost.localaws.budgets.aws;

import com.amazonaws.services.budgets.model.*;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.omigost.localaws.budgets.ServerApplication;
import com.omigost.localaws.budgets.model.Budget;
import com.omigost.localaws.budgets.repository.BudgetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BudgetService {

    @Autowired
    private BudgetRepository budgets;

    public DescribeBudgetsResult describeBudgets(final String accountId) {
        return new DescribeBudgetsResult()
            .withBudgets(
                budgets.findAll().stream().map(Budget::toAwsBudget).collect(Collectors.toList())
            );
    }

    public DeleteBudgetResult deleteBudget(final String accountId, final String budgetName) {
        budgets.deleteBudgetByLabel(Budget.generateLabel(accountId, budgetName));
        return new DeleteBudgetResult();
    }

    public DescribeBudgetResult describeBudget(final String accountId, final String budgetName) {
        final Budget b = budgets.getBudgetByLabel(Budget.generateLabel(accountId, budgetName));
        if (b == null) {
            return new DescribeBudgetResult();
        }
        return new DescribeBudgetResult()
            .withBudget(
                b.toAwsBudget()
            );
    }

    private static Map<String, String> shorthandToMap(final String specs) {
        String specsString = specs;

        if (specsString.startsWith("{")) {
            specsString = specsString.substring(1, specsString.length()-1);
        }

        specsString = specsString + ",";

        HashMap<String, String> m = new HashMap<>();

        final int len = specsString.length();
        int level = 0;
        int lastCutoffPos = 0;

        String keyAcc = "";

        for(int i=0;i<len;++i) {
            if (specsString.charAt(i) == '=' && level == 0) {
                keyAcc = specsString.substring(lastCutoffPos, i).trim();
                lastCutoffPos = i+1;
            } else if (specsString.charAt(i) == ',' && level == 0) {
                m.put(keyAcc, specsString.substring(lastCutoffPos, i));
                lastCutoffPos = i+1;
            } else if (specsString.charAt(i) == '{') {
                ++level;
            } else if (specsString.charAt(i) == '}') {
                --level;
            }
        }

        return m;
    }

    public CreateBudgetResult createBudget(final String accountId, final String budgetSpecString) {

        Map<String, String> specs = shorthandToMap(budgetSpecString);

        final com.amazonaws.services.budgets.model.Budget awsBudget = new com.amazonaws.services.budgets.model.Budget();

        if (specs.containsKey("BudgetName")) {
           awsBudget.setBudgetName(specs.get("BudgetName"));
        }

        if (specs.containsKey("BudgetLimit")) {
            final Map<String, String> limitSpecs = shorthandToMap(specs.get("BudgetLimit"));

            Spend s = new Spend();

            if (limitSpecs.containsKey("Amount")) {
                s.setAmount(new BigDecimal(limitSpecs.get("Amount")));
            }

            if (limitSpecs.containsKey("Unit")) {
                s.setUnit(limitSpecs.get("Unit"));
            }

            awsBudget.setBudgetLimit(s);
        }

        /* Todo: Cost filters */
        // CostFilters={KeyName1=string,string,KeyName2=string,string},

        if (specs.containsKey("CostTypes")) {
            final Map<String, String> costTypesSpecs = shorthandToMap(specs.get("CostTypes"));
            CostTypes c = new CostTypes();

            if (costTypesSpecs.containsKey("IncludeTax")) {
                c.setIncludeTax(Boolean.parseBoolean(costTypesSpecs.get("IncludeTax")));
            }

            if (costTypesSpecs.containsKey("IncludeSubscription")) {
                c.setIncludeSubscription(Boolean.parseBoolean(costTypesSpecs.get("IncludeSubscription")));
            }

            if (costTypesSpecs.containsKey("UseBlended")) {
                c.setUseBlended(Boolean.parseBoolean(costTypesSpecs.get("UseBlended")));
            }

            if (costTypesSpecs.containsKey("IncludeRefund")) {
                c.setIncludeRefund(Boolean.parseBoolean(costTypesSpecs.get("IncludeRefund")));
            }

            if (costTypesSpecs.containsKey("IncludeCredit")) {
                c.setIncludeRefund(Boolean.parseBoolean(costTypesSpecs.get("IncludeCredit")));
            }

            if (costTypesSpecs.containsKey("IncludeUpfront")) {
                c.setIncludeUpfront(Boolean.parseBoolean(costTypesSpecs.get("IncludeUpfront")));
            }

            if (costTypesSpecs.containsKey("IncludeRecurring")) {
                c.setIncludeUpfront(Boolean.parseBoolean(costTypesSpecs.get("IncludeRecurring")));
            }

            if (costTypesSpecs.containsKey("IncludeOtherSubscription")) {
                c.setIncludeOtherSubscription(Boolean.parseBoolean(costTypesSpecs.get("IncludeOtherSubscription")));
            }

            if (costTypesSpecs.containsKey("IncludeSupport")) {
                c.setIncludeSupport(Boolean.parseBoolean(costTypesSpecs.get("IncludeSupport")));
            }

            if (costTypesSpecs.containsKey("IncludeDiscount")) {
                c.setIncludeSupport(Boolean.parseBoolean(costTypesSpecs.get("IncludeDiscount")));
            }

            if (costTypesSpecs.containsKey("UseAmortized")) {
                c.setIncludeSupport(Boolean.parseBoolean(costTypesSpecs.get("UseAmortized")));
            }

            awsBudget.setCostTypes(c);
        }

        if (specs.containsKey("TimeUnit")) {
            awsBudget.setTimeUnit(specs.get("TimeUnit"));
        }

        if (specs.containsKey("BudgetType")) {
            awsBudget.setBudgetType(specs.get("BudgetType"));
        }

        if (specs.containsKey("LastUpdatedTime")) {
            awsBudget.setLastUpdatedTime(new Date(Long.parseLong(specs.get("LastUpdatedTime"))));
        }

        if (specs.containsKey("TimePeriod")) {
            final Map<String, String> periodSpecs = shorthandToMap(specs.get("TimePeriod"));
            TimePeriod t = new TimePeriod();

            if (periodSpecs.containsKey("Start")) {
                t.setStart(new Date(Long.parseLong(periodSpecs.get("Start"))));
            }

            if (periodSpecs.containsKey("End")) {
                t.setStart(new Date(Long.parseLong(periodSpecs.get("End"))));
            }

            awsBudget.setTimePeriod(t);
        }

        if (specs.containsKey("CalculatedSpend")) {
            final Map<String, String> spendSpecs = shorthandToMap(specs.get("CalculatedSpend"));
            CalculatedSpend cs = new CalculatedSpend();

            if (spendSpecs.containsKey("ActualSpend")) {
                final Map<String, String> spend = shorthandToMap(spendSpecs.get("ActualSpend"));
                Spend s = new Spend();

                if (spend.containsKey("Amount")) {
                    s.setAmount(new BigDecimal(spend.get("Amount")));
                }

                if (spend.containsKey("Unit")) {
                    s.setUnit(spend.get("Unit"));
                }

                cs.setActualSpend(s);
            }

            if (spendSpecs.containsKey("ForecastedSpend")) {
                final Map<String, String> spend = shorthandToMap(spendSpecs.get("ForecastedSpend"));
                Spend s = new Spend();

                if (spend.containsKey("Amount")) {
                    s.setAmount(new BigDecimal(spend.get("Amount")));
                }

                if (spend.containsKey("Unit")) {
                    s.setUnit(spend.get("Unit"));
                }

                cs.setForecastedSpend(s);
            }

            awsBudget.setCalculatedSpend(cs);
        }

        Budget budget = new Budget(accountId, awsBudget);
        budgets.save(budget);

        return new CreateBudgetResult();
    }
}
