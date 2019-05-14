package com.omigost.localaws.budgets.collector.sources;

import com.amazonaws.services.budgets.model.CalculatedSpend;
import com.amazonaws.services.budgets.model.Spend;
import com.omigost.localaws.budgets.aws.BudgetService;
import com.omigost.localaws.budgets.collector.DataCollectorService;
import com.omigost.localaws.budgets.collector.DataSource;
import com.omigost.localaws.budgets.collector.DataSourcePublisher;
import com.omigost.localaws.budgets.collector.model.BudgetCostMeasurement;
import com.omigost.localaws.budgets.model.Budget;
import com.omigost.localaws.budgets.repository.BudgetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Random;

@Component
public class FakeSource implements DataSource {
    private String name;

    @Autowired
    DataCollectorService dataCollectorService;

    @Autowired
    BudgetService budgetService;

    @Autowired
    private BudgetRepository budgets;

    private Random random;

    public FakeSource() {
        random = new Random();
    }

    @Override
    public void onDataFetch(final DataSourcePublisher publisher) {
        for(Budget b : budgets.findAllBy()) {
            final double spend = random.nextDouble()*2*b.getBudgetLimit().getAmount().doubleValue();
            final double forecastedSpend = random.nextDouble()*2*b.getBudgetLimit().getAmount().doubleValue();

            publisher.publishPoint(
                    BudgetCostMeasurement
                            .builder()
                            .budgetName(b.getName())
                            .actualSpend(spend)
                            .forecastedSpend(forecastedSpend)
                            .build()
            );

            b.setCalculatedSpend(
                new CalculatedSpend()
                    .withActualSpend(
                            new Spend()
                                    .withAmount(BigDecimal.valueOf(spend))
                                    .withUnit("USD")
                    )
                    .withForecastedSpend(
                            new Spend()
                                    .withAmount(BigDecimal.valueOf(forecastedSpend))
                                    .withUnit("USD")
                    )
            );
            budgets.save(b);
        }
    }

    @Override
    public void setBeanName(final String name) {
        this.name = name;
    }
}
