package com.mathfactmissions.teacherscheduler;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.security.Security;

@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
public class TeacherSchedulerApplication {

    static {
        // Register the Bouncy Castle provider as soon as the class is loaded
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

	public static void main(String[] args) {
		SpringApplication.run(TeacherSchedulerApplication.class, args);
	}

}
