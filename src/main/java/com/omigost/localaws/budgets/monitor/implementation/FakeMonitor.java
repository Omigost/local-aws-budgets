package com.omigost.localaws.budgets.monitor.implementation;

import com.omigost.localaws.budgets.aws.BudgetService;
import com.omigost.localaws.budgets.aws.NotificationService;
import com.omigost.localaws.budgets.model.Budget;
import com.omigost.localaws.budgets.monitor.Monitor;
import com.omigost.localaws.budgets.repository.BudgetRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class FakeMonitor implements Monitor {

    @Autowired
    NotificationService notificationService;

    @Autowired
    private BudgetRepository budgets;

    @Override
    @Scheduled(fixedRate = 5000)
    public void onMonitoringRequest() {
        log.info("Sending notifications to all registered budgets");
        for(Budget budget : budgets.findAllBy()) {
            notificationService.sendNotification(budget.getAccountId(), budget.getName());
        }
    }
}
