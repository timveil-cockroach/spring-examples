package io.crdb.spring.common;

import org.springframework.classify.Classifier;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.policy.NeverRetryPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.stereotype.Component;

@Component
public class PostgresRetryClassifier implements Classifier<Throwable, RetryPolicy> {

    private final ExceptionChecker exceptionChecker;

    public PostgresRetryClassifier(ExceptionChecker exceptionChecker) {
        this.exceptionChecker = exceptionChecker;
    }

    @Override
    public RetryPolicy classify(Throwable classifiable) {
        if (exceptionChecker.shouldRetry(classifiable)) {
            return new SimpleRetryPolicy(3);
        }
        return new NeverRetryPolicy();
    }
}
