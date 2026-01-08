package com.tcs.transactionService.feignClient;

import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "NOTIFICATION-SERVICE")
public interface NotificationClient {

    @PostMapping("/notification/inapp")
    void sendInApp(@RequestBody Map<String,String> message);

    @PostMapping("/notification/email")
    void sendEmail(@RequestBody Map<String,String> message);
}
