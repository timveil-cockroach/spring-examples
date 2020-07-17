package io.crdb.spring;

import com.github.javafaker.Faker;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

import java.util.Locale;

@SpringBootApplication
public class JpaApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext ctx = SpringApplication.run(JpaApplication.class, args);
		SpringApplication.exit(ctx, () -> 0);
	}

	@Bean
	public Faker faker() {
		return new Faker(Locale.US);
	}
}
