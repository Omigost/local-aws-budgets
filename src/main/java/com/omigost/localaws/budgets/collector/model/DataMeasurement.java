package com.omigost.localaws.budgets.collector.model;

import java.util.Map;

public interface DataMeasurement {
    public Map<String, Object> toMap();
    public String getName();
}
