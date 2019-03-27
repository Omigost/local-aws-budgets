package com.omigost.localaws.budgets.rest;

import com.amazonaws.services.budgets.model.Budget;
import com.amazonaws.services.budgets.model.Spend;
import com.omigost.localaws.budgets.aws.BudgetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("")
public class BudgetController {
    @Autowired
    private BudgetService budgetService;

    @GetMapping("/hello")
    public String hello() {
        return budgetService.hello();
    }

    @GetMapping("/describe-budgets")
    public List<Budget> describeBudgets() {
        return new ArrayList<Budget>() {{
            add(
                new Budget()
                    .withBudgetName("test-budget")
                    .withBudgetLimit(new Spend().withUnit("USD").withAmount(new BigDecimal(100)))
            );
        }};
    }

    @PostMapping("/get-budget")
    public Budget createBudeget() {
        return new Budget();
    }
}
