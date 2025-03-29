package com.example.charger_management_system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ChargerManagementSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChargerManagementSystemApplication.class, args);
	}

}
