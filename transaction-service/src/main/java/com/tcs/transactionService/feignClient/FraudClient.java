package com.tcs.transactionService.feignClient;

import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "FRAUD-SERVICE")
public interface FraudClient {

    @PostMapping("/fraud/check")
    Map<String,String> check(@RequestBody Map<String,Object> req);
}
