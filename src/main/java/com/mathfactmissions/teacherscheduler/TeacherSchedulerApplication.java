package com.mathfactmissions.teacherscheduler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class TeacherSchedulerApplication {

	public static void main(String[] args) {
		SpringApplication.run(TeacherSchedulerApplication.class, args);
	}

}
