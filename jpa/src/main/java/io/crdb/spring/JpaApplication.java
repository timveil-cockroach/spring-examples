package io.crdb.spring;

import com.github.javafaker.Faker;
import io.crdb.spring.common.ExceptionChecker;
import io.crdb.spring.common.PostgresRetryClassifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.policy.ExceptionClassifierRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.util.Locale;

@SpringBootApplication
@EnableRetry
public class JpaApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext ctx = SpringApplication.run(JpaApplication.class, args);
		SpringApplication.exit(ctx, () -> 0);
	}

	@Bean
	public Faker faker() {
		return new Faker(Locale.US);
	}

	// not used by @Transactional.  To use it must be called directly
	@Bean
	public RetryTemplate retryTemplate(ExceptionChecker exceptionChecker) {
		ExceptionClassifierRetryPolicy policy = new ExceptionClassifierRetryPolicy();
		policy.setExceptionClassifier(new PostgresRetryClassifier(exceptionChecker));

		return RetryTemplate.builder()
				.customPolicy(policy)
				.fixedBackoff(2)
				.build();
	}
}
