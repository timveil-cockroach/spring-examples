package io.crdb.spring.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.classify.Classifier;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.policy.NeverRetryPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;

import java.sql.SQLException;

public class PostgresRetryClassifier implements Classifier<Throwable, RetryPolicy> {

    private static final Logger logger = LoggerFactory.getLogger(PostgresRetryClassifier.class);
    private static final String POSTGRES_SERIALIZATION_FAILURE = "40001";


    @Override
    public RetryPolicy classify(Throwable classifiable) {
        if (isRetryable(classifiable)) {
            return new SimpleRetryPolicy(3);
        }
        return new NeverRetryPolicy();
    }

    private boolean isRetryable(Throwable ex) {

        if (ex == null) {
            return false;
        }

        if (ex instanceof SQLException) {

            String sqlState = ((SQLException) ex).getSQLState();
            int errorCode = ((SQLException) ex).getErrorCode();

            logger.debug("sql state [{}] and error code [{}]", sqlState, errorCode);

            // ------------------
            // POSTGRES: https://www.postgresql.org/docs/current/errcodes-appendix.html
            // ------------------

            return errorCode == 0 && POSTGRES_SERIALIZATION_FAILURE.equals(sqlState);
        }

        return false;
    }
}
