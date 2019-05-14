package com.omigost.localaws.budgets.collector;

import com.omigost.localaws.budgets.collector.model.DataMeasurement;
import lombok.AllArgsConstructor;
import org.influxdb.dto.Point;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@AllArgsConstructor
public abstract class DataSourcePublisher {

    private final DataCollectorService collector;
    private final DataSource source;

    public abstract void publishPoint(final DataMeasurement measurement);

}
