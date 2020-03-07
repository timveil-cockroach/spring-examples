package io.crdb.spring;

import com.github.javafaker.Faker;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Locale;

@SpringBootApplication
public class DatasourceApplication {
    public static void main(String[] args) {
        SpringApplication.run(DatasourceApplication.class, args);
    }

    @Bean
    public Faker faker() {
        return new Faker(new Locale("en-US"));
    }
}
