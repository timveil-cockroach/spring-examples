package io.crdb.spring;

import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public Iterable<User> findAll() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Iterable<User> findAll(Iterable<UUID> ids) {
        return userRepository.findAllById(ids);
    }

    @Transactional(readOnly = true)
    public Optional<User> find(UUID id) {
        return userRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public boolean exists(UUID id) {
        return userRepository.existsById(id);
    }

    @Transactional(readOnly = true)
    public long count() {
        return userRepository.count();
    }

    @Transactional
    @Retryable(exceptionExpression="@exceptionChecker.shouldRetry(#root)")
    public Iterable<User> saveAll(List<User> users) {
        return userRepository.saveAll(users);
    }

    @Transactional
    @Retryable(exceptionExpression="@exceptionChecker.shouldRetry(#root)")
    public Iterable<User> saveAll(Iterable<User> users) {
        return userRepository.saveAll(users);
    }

    @Transactional
    @Retryable(exceptionExpression="@exceptionChecker.shouldRetry(#root)")
    public User save(User user) {
        return userRepository.save(user);
    }

    @Transactional
    @Retryable(exceptionExpression="@exceptionChecker.shouldRetry(#root)")
    public void deleteAll() {
        userRepository.deleteAll();
    }

    @Transactional
    @Retryable(exceptionExpression="@exceptionChecker.shouldRetry(#root)")
    public void deleteAll(Iterable<User> users) {
        userRepository.deleteAll(users);
    }

    @Transactional
    @Retryable(exceptionExpression="@exceptionChecker.shouldRetry(#root)")
    public void delete(UUID id) {
        userRepository.deleteById(id);
    }

    @Transactional
    @Retryable(exceptionExpression="@exceptionChecker.shouldRetry(#root)")
    public void delete(User user) {
        userRepository.delete(user);
    }

}
