package com.omigost.localaws.budgets.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity(name = "budget_entry")
@EqualsAndHashCode
public class Budget {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @NotNull
    String name;
    
    public Budget() {}

    public Budget(String name) {
        this.name = name;
    }
}

