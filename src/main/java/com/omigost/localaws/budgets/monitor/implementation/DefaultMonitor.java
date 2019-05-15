package com.omigost.localaws.budgets.monitor.implementation;

import com.omigost.localaws.budgets.aws.BudgetService;
import com.omigost.localaws.budgets.aws.NotificationService;
import com.omigost.localaws.budgets.model.Budget;
import com.omigost.localaws.budgets.model.Notification;
import com.omigost.localaws.budgets.monitor.Monitor;
import com.omigost.localaws.budgets.repository.BudgetRepository;
import com.omigost.localaws.budgets.repository.NotificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@Slf4j
public class DefaultMonitor implements Monitor {

    @Autowired
    NotificationService notificationService;

    @Autowired
    private BudgetRepository budgets;

    @Autowired
    private NotificationRepository notifications;

    @Override
    @Scheduled(fixedRateString = "${monitor.interval:10000}")
    public void onMonitoringRequest() {
        log.info("Checking notifications for budgets");
        for (Budget budget : budgets.findAllBy()) {
            final List<Notification> notificationsForBudget = notifications.findAllByBudget(budget);
            if (budget.getCalculatedSpend() != null && budget.getBudgetLimit() != null) {
                log.info("Look for any suitable notifications to launch...");
                for (final Notification notification : notificationsForBudget) {
                    log.info("Check notification status");

                    final BigDecimal act = budget.getCalculatedSpend().getActualSpend().getAmount();
                    final BigDecimal limit = budget.getBudgetLimit().getAmount();

                    Notification.ComparisonType type = notification.getComparisonType();
                    boolean willTrigger;

                    if (type == null) {
                        willTrigger = true;
                    } else {
                        switch (type) {
                            case GT:
                                willTrigger = limit.compareTo(act) < 0;
                                break;
                            case LT:
                                willTrigger = limit.compareTo(act) > 0;
                                break;
                            case EQ:
                                willTrigger = limit.compareTo(act) == 0;
                                break;
                            default:
                                willTrigger = false;
                                break;
                        }
                    }

                    if (willTrigger) {
                        notificationService.sendNotification(budget.getAccountId(), budget.getName());
                    }
                }
            }
        }
    }
}
