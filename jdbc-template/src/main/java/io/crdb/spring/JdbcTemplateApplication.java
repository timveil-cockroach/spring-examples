package io.crdb.spring;

import com.github.javafaker.Faker;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Locale;

@SpringBootApplication
public class JdbcTemplateApplication {

	public static void main(String[] args) {
		SpringApplication.run(JdbcTemplateApplication.class, args);
	}

	@Bean
	public Faker faker() {
		return new Faker(new Locale("en-US"));
	}
}
