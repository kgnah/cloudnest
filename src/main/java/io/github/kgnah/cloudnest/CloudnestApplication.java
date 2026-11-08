package io.github.kgnah.cloudnest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CloudnestApplication {

	public static void main(String[] args) {
        SpringApplication.run(CloudnestApplication.class, args);
	}
}
