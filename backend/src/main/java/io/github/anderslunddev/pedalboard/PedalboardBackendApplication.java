package io.github.anderslunddev.pedalboard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class PedalboardBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(PedalboardBackendApplication.class, args);
	}
}
