package io.crdb.spring;

import org.springframework.retry.RetryCallback;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RetryTemplate retryTemplate;

    public UserService(UserRepository userRepository, RetryTemplate retryTemplate) {
        this.userRepository = userRepository;
        this.retryTemplate = retryTemplate;
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Iterable<User> saveAll(List<User> users) {
        return retryTemplate.execute(context -> userRepository.saveAll(users));
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Iterable<User> saveAll(Iterable<User> users) {
        return retryTemplate.execute(context -> userRepository.saveAll(users));
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public User save(User user) {
        return retryTemplate.execute(context -> userRepository.save(user));
    }

    @Transactional(isolation = Isolation.SERIALIZABLE, readOnly = true)
    public Iterable<User> findAll() {
        return userRepository.findAll();
    }

    @Transactional(isolation = Isolation.SERIALIZABLE, readOnly = true)
    public Iterable<User> findAll(Iterable<UUID> ids) {
        return userRepository.findAllById(ids);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE, readOnly = true)
    public Optional<User> find(UUID id) {
        return userRepository.findById(id);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE, readOnly = true)
    public boolean exists(UUID id) {
        return userRepository.existsById(id);
    }


    @Transactional(isolation = Isolation.SERIALIZABLE, readOnly = true)
    public long count() {
        return userRepository.count();
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void deleteAll() {
        retryTemplate.execute((RetryCallback<Void, RuntimeException>) context -> {
            userRepository.deleteAll();
            return null;
        });
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void deleteAll(Iterable<User> users) {
        retryTemplate.execute((RetryCallback<Void, RuntimeException>) context -> {
            userRepository.deleteAll(users);
            return null;
        });
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void delete(UUID id) {
        retryTemplate.execute((RetryCallback<Void, RuntimeException>) context -> {
            userRepository.deleteById(id);
            return null;
        });
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void delete(User user) {
        retryTemplate.execute((RetryCallback<Void, RuntimeException>) context -> {
            userRepository.delete(user);
            return null;
        });
    }

    // custom methods

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public int updateUsers() {
        return retryTemplate.execute(context -> userRepository.updateTimestamp(ZonedDateTime.now()));
    }


    @Transactional(isolation = Isolation.SERIALIZABLE, readOnly = true)
    public Iterable<User> findUsersWithNullTimestamp() {
        return userRepository.findByUpdatedTimestampIsNull();
    }

}
