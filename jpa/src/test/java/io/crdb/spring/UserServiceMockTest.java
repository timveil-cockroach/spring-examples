package io.crdb.spring;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.sql.SQLException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;

@SpringBootTest
@ActiveProfiles("test")
class UserServiceMockTest {

    @Autowired
    private UserBuilder userBuilder;

    @Autowired
    private UserService userService;

    @MockBean
    private UserRepository userRepository;

    @Test
    void insertUser() {
        User user = userBuilder.buildUser();

        doReturn(user).when(userRepository).save(any());

        User returnedUser = userService.save(user);

        Assertions.assertNotNull(returnedUser, "The saved user should not be null");
        Assertions.assertEquals(user.getId(), returnedUser.getId(), "The id should be the same");


        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                throw new SQLException("mock retry", "40001", 0);
            }
        }).when(userRepository).save(any());

        User otherUser = userService.save(user);
    }


}