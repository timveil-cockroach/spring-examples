package io.crdb.spring;

import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.ZonedDateTime;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void insertUsers(List<User> users) {
        userRepository.saveAll(users);
    }

    public List<User> selectUsers() {
        return userRepository.findByUpdatedTimestampIsNull();
    }

    @Transactional
    public int updateUsers() {
        return userRepository.updateTimestamp(ZonedDateTime.now());
    }

    @Transactional
    public void deleteUsers() {
        userRepository.deleteAll();
    }

}
