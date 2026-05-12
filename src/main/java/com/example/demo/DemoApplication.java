package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.example.smart_notification_system")
@org.springframework.boot.autoconfigure.domain.EntityScan("com.example.smart_notification_system.entity")
@org.springframework.data.jpa.repository.config.EnableJpaRepositories("com.example.smart_notification_system.repository")
@org.springframework.retry.annotation.EnableRetry
@org.springframework.scheduling.annotation.EnableAsync
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

}
