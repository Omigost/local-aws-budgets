package com.omigost.localaws.budgets.collector.model;

import lombok.Builder;
import org.influxdb.annotation.Column;
import org.influxdb.annotation.Measurement;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Measurement(name = "aws_budget_cost")
@Builder
public class BudgetCostMeasurement implements DataMeasurement {

    @Column(name = "time")
    private Instant time;

    @Column(name = "budgetName")
    private String budgetName;

    @Column(name = "actualSpend")
    private double actualSpend;

    @Column(name = "forecastedSpend")
    private double forecastedSpend;

    public String getName() {
        return "aws_budget_cost";
    }

    public Map<String, Object> toMap() {
        return new HashMap<String, Object>(){{
            put("budgetName", budgetName);
        }};
    }
}
