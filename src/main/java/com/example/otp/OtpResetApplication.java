package com.example.otp;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.TopicBuilder;

@SpringBootApplication
public class OtpResetApplication {

    public static void main(String[] args) {
        SpringApplication.run(OtpResetApplication.class, args);
    }

    @Bean
    public NewTopic otpTopic() {
        return TopicBuilder.name("password-reset-otp")
                .partitions(1)
                .replicas(1)
                .build();
    }
}
