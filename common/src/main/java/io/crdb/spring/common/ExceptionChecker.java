package io.crdb.spring.common;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.sql.SQLException;

@Component
public class ExceptionChecker {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionChecker.class);
    private static final String POSTGRES_SERIALIZATION_FAILURE = "40001";


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

        // ------------------
        // POSTGRES: https://www.postgresql.org/docs/current/errcodes-appendix.html
        // ------------------

        boolean retryable = errorCode == 0 && POSTGRES_SERIALIZATION_FAILURE.equals(sqlState);

        logger.debug("SQLException is retryable? {} : sql state [{}] and error code [{}]", retryable, sqlState, errorCode);

        return retryable;
    }
}
