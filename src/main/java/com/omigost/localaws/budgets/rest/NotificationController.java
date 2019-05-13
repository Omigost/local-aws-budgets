package com.omigost.localaws.budgets.rest;

import com.omigost.localaws.budgets.ServerApplication;
import com.omigost.localaws.budgets.aws.BudgetService;
import com.omigost.localaws.budgets.aws.NotificationService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/notification")
@Slf4j
public class NotificationController {

    @PostMapping("/receive")
    public String receiveNotification() throws IOException {
        log.info("Received notification from endpoint");
        return "OK";
    }

}
