package com.omigost.localaws.budgets.repository;

import com.omigost.localaws.budgets.model.Budget;
import com.omigost.localaws.budgets.model.Notification;
import com.omigost.localaws.budgets.model.NotificationSubscriber;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    List<Notification> findAllByBudget(final Budget budget);
}
