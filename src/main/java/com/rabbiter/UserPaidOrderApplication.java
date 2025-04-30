package com.rabbiter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class UserPaidOrderApplication {
	public static void main(String[] args) {
		SpringApplication.run(UserPaidOrderApplication.class, args);
	}

}
