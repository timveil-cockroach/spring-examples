package io.crdb.spring;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public class BusinessServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(BusinessServiceTest.class);

    private final BusinessService businessService;
    private final UserBuilder userBuilder;

    @Autowired
    public BusinessServiceTest(BusinessService businessService, UserBuilder userBuilder) {
        this.businessService = businessService;
        this.userBuilder = userBuilder;
    }

    @Test
    void retryWithRetry() {
        User test = userBuilder.buildUser();

        logger.debug(test.toString());

        businessService.someComplexService(test, true);

    }

    @Test
    void retryWithoutRetry() {
        User test = userBuilder.buildUser();

        logger.debug(test.toString());

        businessService.someComplexService(test, false);
    }
}
