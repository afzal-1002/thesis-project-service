package com.pw.edu.pl.master.thesis.ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class AiIntegrationApplication {

	public static void main(String[] args) {
        SpringApplication.run(AiIntegrationApplication.class, args);
	}

}
