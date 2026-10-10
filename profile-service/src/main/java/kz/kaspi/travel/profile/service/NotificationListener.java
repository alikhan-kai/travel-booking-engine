package kz.kaspi.travel.profile.service;

import org.springframework.stereotype.Service;
import org.springframework.kafka.annotation.KafkaListener;

@Service
public class NotificationListener {
    @KafkaListener(topics = "payment-events", groupId = "notification-group")
    public void sendSms(String ev) { 
        System.out.println("SMS Sent: Your ticket has been issued successfully."); 
    }
}
