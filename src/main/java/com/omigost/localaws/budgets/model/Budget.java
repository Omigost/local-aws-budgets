package com.omigost.localaws.budgets.model;

import com.amazonaws.services.budgets.model.CalculatedSpend;
import com.amazonaws.services.budgets.model.CostTypes;
import com.amazonaws.services.budgets.model.Spend;
import com.amazonaws.services.budgets.model.TimePeriod;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.util.Pair;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Data
@Entity(name = "budget_entry")
@EqualsAndHashCode
public class Budget {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @NotNull
    @Column(unique = true)
    String label;

    @NotNull
    String name;

    @NotNull
    String accountId;

    @Column
    @Lob
    Spend budgetLimit;

    @Column
    @Lob
    CostTypes costTypes;

    @Column
    String timeUnit;

    @Column
    String budgetType;

    @ManyToMany(cascade = CascadeType.ALL)
    Map<String, FilterCollection> filters;

    @Column
    @Lob
    Date lastUpdatedTime;

    @Column
    @Lob
    TimePeriod timePeriod;

    @Column
    @Lob
    CalculatedSpend calculatedSpend;

    public Budget() {
    }

    public Budget(String accountId, String name) {
        this.label = generateLabel(accountId, name);
        this.name = name;
        this.accountId = accountId;
    }

    public Budget(String accountId, final com.amazonaws.services.budgets.model.Budget awsBudget) {
        this.label = generateLabel(accountId, awsBudget.getBudgetName());
        this.name = awsBudget.getBudgetName();
        this.accountId = accountId;

        this.budgetLimit = awsBudget.getBudgetLimit();
        this.costTypes = awsBudget.getCostTypes();
        this.timeUnit = awsBudget.getTimeUnit();
        this.budgetType = awsBudget.getBudgetType();
        this.lastUpdatedTime = awsBudget.getLastUpdatedTime();
        this.timePeriod = awsBudget.getTimePeriod();
        this.calculatedSpend = awsBudget.getCalculatedSpend();
        this.filters = awsBudget
                .getCostFilters()
                .entrySet()
                .stream()
                .map(e -> FilterCollection.builder()
                    .values(e.getValue())
                    .key(e.getKey())
                    .build()
                )
                .collect(Collectors.toMap(FilterCollection::getKey, Function.identity())
        );
    }

    public static String generateLabel(final String accountId, final String name) {
        if (accountId == null) {
            throw new RuntimeException("Generate label executed for null AccountId");
        }
        if (name == null) {
            throw new RuntimeException("Generate label executed for null BudgetName");
        }
        return accountId + "___" + name;
    }

    public com.amazonaws.services.budgets.model.Budget toAwsBudget() {
        return new com.amazonaws.services.budgets.model.Budget()
                .withBudgetName(getName())
                .withBudgetLimit(getBudgetLimit())
                .withTimeUnit(getTimeUnit())
                .withBudgetType(getBudgetType())
                .withLastUpdatedTime(getLastUpdatedTime())
                .withTimePeriod(getTimePeriod())
                .withCalculatedSpend(getCalculatedSpend())
                .withCostFilters(
                    getFilters()
                        .entrySet()
                        .stream()
                        .map(e -> Pair.of(e.getKey(), e.getValue().values))
                        .collect(Collectors.toMap(Pair::getFirst, Pair::getSecond))
                );
    }
}

