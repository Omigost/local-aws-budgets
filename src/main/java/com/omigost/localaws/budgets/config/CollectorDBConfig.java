package com.omigost.localaws.budgets.config;

import lombok.extern.slf4j.Slf4j;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.testcontainers.containers.InfluxDBContainer;

import java.util.List;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Configuration
@Slf4j
public class CollectorDBConfig {

    private InfluxDBContainer influxDbContainer = null;

    @Value("${collector.influx.enable:false}")
    private boolean influxDBEnable;

    @Value("${collector.influx.dbName:awsCosts}")
    private String influxDBName;

    @Value("${collector.influx.remote.use:false}")
    private boolean useExternalDB;

    @Value("${collector.influx.remote.url:}")
    private String influxDBRemoteUrl;

    @Value("${collector.influx.remote.username:}")
    private String influxDBRemoteUsername;

    @Value("${collector.influx.remote.password:}")
    private String influxDBRemotePassword;

    @Bean
    InfluxDB influxDB() {
        InfluxDB influxDB = new InfluxDB() {
            @Override
            public InfluxDB setLogLevel(LogLevel logLevel) {
                return null;
            }

            @Override
            public InfluxDB enableGzip() {
                return null;
            }

            @Override
            public InfluxDB disableGzip() {
                return null;
            }

            @Override
            public boolean isGzipEnabled() {
                return false;
            }

            @Override
            public InfluxDB enableBatch(int actions, int flushDuration, TimeUnit flushDurationTimeUnit) {
                return null;
            }

            @Override
            public InfluxDB enableBatch(int actions, int flushDuration, TimeUnit flushDurationTimeUnit, ThreadFactory threadFactory) {
                return null;
            }

            @Override
            public InfluxDB enableBatch(int actions, int flushDuration, TimeUnit flushDurationTimeUnit, ThreadFactory threadFactory, BiConsumer<Iterable<Point>, Throwable> exceptionHandler) {
                return null;
            }

            @Override
            public void disableBatch() {

            }

            @Override
            public boolean isBatchEnabled() {
                return false;
            }

            @Override
            public Pong ping() {
                return null;
            }

            @Override
            public String version() {
                return null;
            }

            @Override
            public void write(Point point) {

            }

            @Override
            public void write(String records) {

            }

            @Override
            public void write(List<String> records) {

            }

            @Override
            public void write(String database, String retentionPolicy, Point point) {

            }

            @Override
            public void write(int udpPort, Point point) {

            }

            @Override
            public void write(BatchPoints batchPoints) {

            }

            @Override
            public void write(String database, String retentionPolicy, ConsistencyLevel consistency, String records) {

            }

            @Override
            public void write(String database, String retentionPolicy, ConsistencyLevel consistency, List<String> records) {

            }

            @Override
            public void write(int udpPort, String records) {

            }

            @Override
            public void write(int udpPort, List<String> records) {

            }

            @Override
            public QueryResult query(Query query) {
                return null;
            }

            @Override
            public void query(Query query, Consumer<QueryResult> onSuccess, Consumer<Throwable> onFailure) {

            }

            @Override
            public void query(Query query, int chunkSize, Consumer<QueryResult> consumer) {

            }

            @Override
            public QueryResult query(Query query, TimeUnit timeUnit) {
                return null;
            }

            @Override
            public void createDatabase(String name) {

            }

            @Override
            public void deleteDatabase(String name) {

            }

            @Override
            public List<String> describeDatabases() {
                return null;
            }

            @Override
            public boolean databaseExists(String name) {
                return false;
            }

            @Override
            public void flush() {

            }

            @Override
            public void close() {

            }

            @Override
            public InfluxDB setConsistency(ConsistencyLevel consistency) {
                return null;
            }

            @Override
            public InfluxDB setDatabase(String database) {
                return null;
            }

            @Override
            public InfluxDB setRetentionPolicy(String retentionPolicy) {
                return null;
            }

            @Override
            public void createRetentionPolicy(String rpName, String database, String duration, String shardDuration, int replicationFactor, boolean isDefault) {

            }

            @Override
            public void createRetentionPolicy(String rpName, String database, String duration, int replicationFactor, boolean isDefault) {

            }

            @Override
            public void createRetentionPolicy(String rpName, String database, String duration, String shardDuration, int replicationFactor) {

            }

            @Override
            public void dropRetentionPolicy(String rpName, String database) {

            }
        };

        if (!influxDBEnable) {
            return influxDB;
        }

        if (!useExternalDB) {
            if (influxDbContainer == null){
                influxDbContainer = new InfluxDBContainer();
                influxDbContainer.start();
            }

            influxDB = influxDbContainer.getNewInfluxDB().setLogLevel(InfluxDB.LogLevel.FULL);
        } else {
            influxDB = InfluxDBFactory.connect(influxDBRemoteUrl, influxDBRemoteUsername, influxDBRemotePassword);
        }

        Pong response = influxDB.ping();
        if (response.getVersion().equalsIgnoreCase("unknown")) {
            log.error("Error pinging server.");
            return null;
        }

        if (!influxDB.databaseExists(influxDBName)) {
            influxDB.createDatabase(influxDBName);
            influxDB.createRetentionPolicy("defaultPolicy", influxDBName, "30d", 1, true);
        }

        return influxDB
                .setDatabase(influxDBName)
                .setLogLevel(InfluxDB.LogLevel.BASIC);
    }

}
