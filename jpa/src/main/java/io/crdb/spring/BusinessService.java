package io.crdb.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BusinessService {

    private static final Logger logger = LoggerFactory.getLogger(BusinessService.class);

    private final UserRepository userRepository;

    public BusinessService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    @Retryable(exceptionExpression = "@exceptionChecker.shouldRetry(#root)")
    public void someComplexService(User user, boolean forceRetry) {

        logger.debug("*********************************** saving user ***********************************");

        User newUser = userRepository.save(user);

        newUser.setStateCode("GA");

        logger.debug("*********************************** updating user ***********************************");

        User gaUser = userRepository.save(newUser);

        if (forceRetry) {
            logger.debug("*********************************** forcing retry ***********************************");
            userRepository.forceRetry();
        }

    }

}
