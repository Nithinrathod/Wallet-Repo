package com.tcs.notificationService.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import com.tcs.notificationService.dto.NotificationDto;
import com.tcs.notificationService.service.NotificationService;

@RestController
@RequestMapping("/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/send")
    public NotificationDto send(@RequestBody NotificationDto dto) {
        return notificationService.send(dto);
    }
}
