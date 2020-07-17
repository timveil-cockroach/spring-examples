package io.crdb.spring;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Iterable<User> insertUsers(List<User> users) {
        return userRepository.saveAll(users);
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
        return userRepository.updateTimestamp(ZonedDateTime.now());
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void deleteUsers() {
        userRepository.deleteAll();
    }

}
