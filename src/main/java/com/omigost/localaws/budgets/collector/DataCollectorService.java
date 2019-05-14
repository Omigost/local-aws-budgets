package com.omigost.localaws.budgets.collector;

import com.omigost.localaws.budgets.collector.model.DataMeasurement;
import lombok.extern.slf4j.Slf4j;
import org.influxdb.InfluxDB;
import org.influxdb.dto.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class DataCollectorService {

    @Autowired
    InfluxDB influxDB;

    @Autowired
    List<DataSource> dataSources;

    @Scheduled(fixedRateString = "${collector.interval:10000}")
    public void collectData() {
        log.info("Collecting data from sources...");
        for(DataSource source : dataSources) {
            source.onDataFetch(new DataSourcePublisher(this, source){
                public void publishPoint(final DataMeasurement measurement) {
                    if (influxDB == null) return;
                    influxDB.write(
                            Point.measurement(measurement.getName())
                                    .fields(measurement.toMap())
                                    .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                                    .build()
                    );
                }

            });
        }
    }

}
