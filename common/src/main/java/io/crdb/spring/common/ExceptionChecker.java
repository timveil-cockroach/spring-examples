package io.crdb.spring.common;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.sql.SQLException;

@Component
public class ExceptionChecker {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionChecker.class);

    // this is thrown when CRDB needs the client to retry
    private static final String POSTGRES_SERIALIZATION_FAILURE = "40001";

    // the following codes are often encountered when nodes become unavailable during processing
    private static final String POSTGRES_STATEMENT_COMPLETION_UNKNOWN = "40003";
    private static final String POSTGRES_CONNECTION_DOES_NOT_EXIST = "08003";
    private static final String POSTGRES_CONNECTION_FAILURE = "08006";

    public boolean shouldRetry(Throwable ex) {

        if (ex == null) {
            return false;
        }

        SQLException sqlException = ExceptionUtils.throwableOfType(ex, SQLException.class);

        if (sqlException != null) {
            return shouldRetry(sqlException);
        }

        logger.warn("Exception is not a SQLException.  Will not be retried.  Class is {}.", ex.getClass());

        return false;
    }

    private boolean shouldRetry(SQLException ex) {
        String sqlState = ex.getSQLState();
        int errorCode = ex.getErrorCode();

        if (errorCode != 0) {
            return false;
        }

        if (sqlState == null) {
            return false;
        }

        boolean retryable = isRetryableState(sqlState);

        logger.debug("SQLException is retryable? {} : sql state [{}], error code [{}], message [{}]", retryable, sqlState, errorCode, ex.getMessage());

        return retryable;
    }

    private boolean isRetryableState(String sqlState) {
        // ------------------
        // POSTGRES: https://www.postgresql.org/docs/current/errcodes-appendix.html
        // ------------------

        return POSTGRES_SERIALIZATION_FAILURE.equals(sqlState)
                || POSTGRES_STATEMENT_COMPLETION_UNKNOWN.equals(sqlState)
                || POSTGRES_CONNECTION_FAILURE.equals(sqlState)
                || POSTGRES_CONNECTION_DOES_NOT_EXIST.equals(sqlState);
    }
}
