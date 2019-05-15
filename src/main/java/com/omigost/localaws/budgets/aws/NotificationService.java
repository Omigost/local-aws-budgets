package com.omigost.localaws.budgets.aws;

import com.amazonaws.services.budgets.model.CreateNotificationResult;
import com.amazonaws.services.sns.AmazonSNS;
import com.omigost.localaws.budgets.aws.util.ShorthandParser;
import com.omigost.localaws.budgets.model.Budget;
import com.omigost.localaws.budgets.model.Notification;
import com.omigost.localaws.budgets.model.NotificationSubscriber;
import com.omigost.localaws.budgets.repository.BudgetRepository;
import com.omigost.localaws.budgets.repository.NotificationRepository;
import com.omigost.localaws.budgets.repository.NotificationSubscriberRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class NotificationService {

    @Autowired
    private BudgetRepository budgets;

    @Autowired
    private NotificationRepository notifications;

    @Autowired
    private NotificationSubscriberRepository subscribers;

    @Autowired
    private AmazonSNS amazonSNS;

    public void sendNotification(final String accountId, final String budgetName) {
        log.info("Sending notification for account "+accountId+" and budget "+budgetName);

        final Budget budget = budgets.getBudgetByLabel(Budget.generateLabel(accountId, budgetName));
        final List<Notification> notificationsForBudget = notifications.findAllByBudget(budget);
        for (final Notification notification : notificationsForBudget) {
            for(final NotificationSubscriber sub: notification.getSubscribers()) {
                if (sub.getType().equals("SNS")) {
                    log.debug("Send SNS notification to the endpoint ["+sub.getAddress()+"]");
                    amazonSNS.publish(sub.getAddress(), "budget violated");
                } else {
                    throw new RuntimeException("Delivery method not yet supported: "+sub.getType());
                }
            }
        }

        log.debug("Notification processing was finished.");
    }

    @Transactional
    public CreateNotificationResult createNotification(final String accountId, final String budgetName, final String notificationSpecString, final String notificationSubscribersSpecString) {
        Map<String, String> specs = ShorthandParser.parse(notificationSpecString);
        final com.amazonaws.services.budgets.model.Notification awsNotification = new com.amazonaws.services.budgets.model.Notification();

        if (specs.containsKey("NotificationType")) {
            awsNotification.setNotificationType(specs.get("NotificationType"));
        }

        if (specs.containsKey("ComparisonOperator")) {
            awsNotification.setComparisonOperator(specs.get("ComparisonOperator"));
        }

        if (specs.containsKey("Threshold")) {
            awsNotification.setThreshold(Double.parseDouble(specs.get("Threshold")));
        }

        if (specs.containsKey("ThresholdType")) {
            awsNotification.setThresholdType(specs.get("ThresholdType"));
        }

        if (specs.containsKey("NotificationState")) {
            awsNotification.setNotificationState(specs.get("TNotificationState"));
        }


        Map<String, String> subscribersSpecs = ShorthandParser.parse(notificationSubscribersSpecString);
        Set<NotificationSubscriber> subscribersForNotification = new HashSet<>();
        for(String subSpecString : subscribersSpecs.values()) {
            Map<String, String> subSpec = ShorthandParser.parse(subSpecString);

            final NotificationSubscriber sub = new NotificationSubscriber();
            sub.setAddress(subSpec.get("Address"));
            sub.setType(subSpec.get("SubscriptionType"));
            subscribers.save(sub);

            subscribersForNotification.add(sub);
        }

        log.debug("Creating new notification for budget with name ["+budgetName+"]");

        final Budget budget = budgets.getBudgetByLabel(Budget.generateLabel(accountId, budgetName));
        Notification notification = new Notification(accountId, budget, awsNotification, subscribersForNotification);
        notifications.save(notification);

        log.debug("Notification was created.");

        return new CreateNotificationResult();
    }
}
