package com.omigost.localaws.budgets.repository;

import com.omigost.localaws.budgets.model.Notification;
import com.omigost.localaws.budgets.model.NotificationSubscriber;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationSubscriberRepository extends JpaRepository<NotificationSubscriber, Integer> {

}
