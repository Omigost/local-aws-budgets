package com.omigost.localaws.budgets.collector;

import org.springframework.beans.factory.BeanNameAware;

public interface DataSource extends BeanNameAware {

    public void onDataFetch(final DataSourcePublisher publisher);

}
