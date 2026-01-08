package com.tcs.notificationService.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.tcs.notificationService.bean.Notification;
import com.tcs.notificationService.dto.NotificationDto;
import com.tcs.notificationService.repository.NotificationRepository;


@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationDto send(NotificationDto req) {

        Notification notification = new Notification();
        notification.setUserId(req.getUserId());
        notification.setType(req.getType());
        notification.setTitle(req.getTitle());
        notification.setMessage(req.getMessage());

        notificationRepository.save(notification);

        req.setStatus("SUCCESS");
        return req;
    }
}
