package com.omigost.localaws.budgets.monitor;

import com.omigost.localaws.budgets.monitor.implementation.FakeMonitor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MonitorManagerService {

    @Autowired
    private FakeMonitor fakeMonitor;

}
