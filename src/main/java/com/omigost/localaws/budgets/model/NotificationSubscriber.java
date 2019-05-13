package com.omigost.localaws.budgets.model;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Data
@Entity(name = "notification_subscriber_entry")
@EqualsAndHashCode
public class NotificationSubscriber {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @NotNull
    @Column
    private String address;

    @NotNull
    @Column
    private String type;
}
