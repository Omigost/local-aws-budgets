package com.omigost.localaws.budgets.repository;

import com.omigost.localaws.budgets.model.Budget;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BudgetRepository extends JpaRepository<Budget, Integer> {
    Budget getBudgetByName(String name);
    void deleteBudgetByName(String name);
}
