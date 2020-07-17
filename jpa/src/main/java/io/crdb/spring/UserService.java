package io.crdb.spring;

import org.springframework.retry.RetryCallback;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RetryTemplate retryTemplate;

    public UserService(UserRepository userRepository, RetryTemplate retryTemplate) {
        this.userRepository = userRepository;
        this.retryTemplate = retryTemplate;
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Iterable<User> insertUsers(List<User> users) {
        return retryTemplate.execute(context -> userRepository.saveAll(users));
    }

    @Transactional(isolation = Isolation.SERIALIZABLE, readOnly = true)
    public List<User> selectUsers() {
        return userRepository.findByUpdatedTimestampIsNull();
    }

    @Transactional(isolation = Isolation.SERIALIZABLE, readOnly = true)
    public long countUsers() {
        return userRepository.count();
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public int updateUsers() {
        return retryTemplate.execute(context -> userRepository.updateTimestamp(ZonedDateTime.now()));
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void deleteUsers() {
        retryTemplate.execute((RetryCallback<Void, RuntimeException>) context -> {
            userRepository.deleteAll();
            return null;
        });
    }

}
