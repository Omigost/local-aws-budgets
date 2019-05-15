package com.omigost.localaws.budgets.aws;

import com.amazonaws.services.budgets.model.*;
import com.amazonaws.services.sns.AmazonSNS;
import com.omigost.localaws.budgets.aws.util.ShorthandParser;
import com.omigost.localaws.budgets.model.Budget;
import com.omigost.localaws.budgets.repository.BudgetRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class BudgetService {

    @Autowired
    private BudgetRepository budgets;

    @Autowired
    private NotificationService notificationService;

    public DescribeBudgetsResult describeBudgets() {
        return new DescribeBudgetsResult()
                .withBudgets(
                        budgets.findAll().stream().map(Budget::toAwsBudget).collect(Collectors.toList())
                );
    }

    @Transactional
    public DescribeBudgetsResult describeBudgets(final String accountId) {
        return new DescribeBudgetsResult()
                .withBudgets(
                        budgets.findAll().stream().map(Budget::toAwsBudget).collect(Collectors.toList())
                );
    }

    @Transactional
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

    @Transactional
    public CreateBudgetResult createBudget(final String accountId, final String budgetSpecString) {
        return createBudget(accountId, budgetSpecString, null);
    }

    @Transactional
    public CreateBudgetResult createBudget(final String accountId, final String budgetSpecString, final String notificationsWithSubscribersSpecs) {

        Map<String, String> specs = ShorthandParser.parse(budgetSpecString);

        final com.amazonaws.services.budgets.model.Budget awsBudget = new com.amazonaws.services.budgets.model.Budget();

        if (specs.containsKey("BudgetName")) {
            awsBudget.setBudgetName(specs.get("BudgetName"));
        }

        if (specs.containsKey("BudgetLimit")) {
            final Map<String, String> limitSpecs = ShorthandParser.parse(specs.get("BudgetLimit"));

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
            final Map<String, String> costTypesSpecs = ShorthandParser.parse(specs.get("CostTypes"));
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
            final Map<String, String> periodSpecs = ShorthandParser.parse(specs.get("TimePeriod"));
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
            final Map<String, String> spendSpecs = ShorthandParser.parse(specs.get("CalculatedSpend"));
            CalculatedSpend cs = new CalculatedSpend();

            if (spendSpecs.containsKey("ActualSpend")) {
                final Map<String, String> spend = ShorthandParser.parse(spendSpecs.get("ActualSpend"));
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
                final Map<String, String> spend = ShorthandParser.parse(spendSpecs.get("ForecastedSpend"));
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

        log.debug("Creating budget with name ["+awsBudget.getBudgetName()+"]");

        Budget budget = new Budget(accountId, awsBudget);
        budgets.save(budget);

        /* Automatically register all notifications with subscribers */
        if (notificationsWithSubscribersSpecs != null) {
            log.debug("Creating notifications for budget, because notificationsWithSubscribersSpecs was specified.");

            final Map<String, String> notifs = ShorthandParser.parse(notificationsWithSubscribersSpecs);
            for (final String notifSpecs : notifs.values()) {
                final Map<String, String> notifData = ShorthandParser.parse(notifSpecs);
                notificationService.createNotification(accountId, budget.getName(), notifData.get("Notification"), notifData.get("Subscribers"));
            }
        } else {
            log.debug("No notifications were automatically created.");
        }

        log.debug("New budget was created.");

        return new CreateBudgetResult();
    }
}
