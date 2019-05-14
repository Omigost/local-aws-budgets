package com.omigost.localaws.budgets.model;

import com.amazonaws.services.budgets.model.CalculatedSpend;
import com.amazonaws.services.budgets.model.CostTypes;
import com.amazonaws.services.budgets.model.Spend;
import com.amazonaws.services.budgets.model.TimePeriod;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Data
@Entity(name = "notification_entry")
@EqualsAndHashCode
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @NotNull
    @Column
    String accountId;

    @ManyToOne
    Budget budget;

    @Column
    ComparisonType comparisonType;

    @Column
    ThresholdType thresholdType;

    @Column
    NotificationState notificationState;

    @Column
    NotificationType notificationType;

    @Column
    double threshold;

    @Column
    @NotNull
    @OneToMany
    Set<NotificationSubscriber> subscribers;

    @AllArgsConstructor
    public enum ComparisonType {
        GT("GREATER_THAN"),
        LT("LESS_THAN"),
        EQ("EQUAL");

        @Getter
        private String token;
    }

    @AllArgsConstructor
    public enum ThresholdType {
        PERCENTAGE("PERCENTAGE"),
        ABSOLUTE("ABSOLUTE_VALUE");

        @Getter
        private String token;
    }

    @AllArgsConstructor
    public enum NotificationState {
        OK("OK"),
        ALARM("ALARM");

        @Getter
        private String token;
    }

    @AllArgsConstructor
    public enum NotificationType {
        ACTUAL("ACTUAL"),
        FORECASTED("FORECASTED");

        @Getter
        private String token;
    }

    public Notification() {}

    public Notification(String accountId, final Budget budget, final com.amazonaws.services.budgets.model.Notification awsNotification, final Set<NotificationSubscriber> subscribers) {
        this.accountId = accountId;
        this.budget = budget;

        this.threshold = awsNotification.getThreshold();
        this.subscribers = subscribers;

        this.comparisonType = null;
        if (awsNotification.getComparisonOperator() != null) {
            for (ComparisonType cmpType : ComparisonType.values()) {
                if (awsNotification.getComparisonOperator().equals(cmpType.getToken())) {
                    this.comparisonType = cmpType;
                    break;
                }
            }
        }

        this.thresholdType = null;
        if (awsNotification.getThresholdType() != null) {
            for (ThresholdType thrType : ThresholdType.values()) {
                if (awsNotification.getThresholdType().equals(thrType.getToken())) {
                    this.thresholdType = thrType;
                    break;
                }
            }
        }

        this.notificationState = null;
        if (awsNotification.getNotificationState() != null) {
            for (NotificationState notifState : NotificationState.values()) {
                if (awsNotification.getNotificationState().equals(notifState.getToken())) {
                    this.notificationState = notifState;
                    break;
                }
            }
        }

        this.notificationType = null;
        if (awsNotification.getNotificationType() != null) {
            for (NotificationType notifType : NotificationType.values()) {
                if (awsNotification.getNotificationType().equals(notifType.getToken())) {
                    this.notificationType = notifType;
                    break;
                }
            }
        }
    }

    public com.amazonaws.services.budgets.model.Notification toAwsNotification() {
        return new com.amazonaws.services.budgets.model.Notification();
    }
}

