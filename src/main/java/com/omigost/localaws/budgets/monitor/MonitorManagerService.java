package com.omigost.localaws.budgets.monitor;

import com.omigost.localaws.budgets.monitor.implementation.DefaultMonitor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MonitorManagerService {

    @Autowired
    private DefaultMonitor defaultMonitor;

}
