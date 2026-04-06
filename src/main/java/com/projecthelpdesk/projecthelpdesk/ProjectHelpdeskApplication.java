package com.projecthelpdesk.projecthelpdesk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ProjectHelpdeskApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProjectHelpdeskApplication.class, args);
	}
}
