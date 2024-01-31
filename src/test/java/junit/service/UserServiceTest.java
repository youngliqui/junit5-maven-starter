package junit.service;

import by.youngliqui.dao.UserDao;
import by.youngliqui.dto.User;
import by.youngliqui.service.UserService;
import junit.extension.ConditionalExtension;
import junit.extension.GlobalExtension;
import junit.extension.ThrowableException;
import junit.extension.UserServiceParamResolver;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Tag("user")
@Tag("fast")
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@ExtendWith({
        UserServiceParamResolver.class,
        GlobalExtension.class,
        ConditionalExtension.class,
//        ThrowableException.class
})
public class UserServiceTest {
    private static final User IVAN = User.of(1, "Ivan", "2223");
    private static final User MAXIM = User.of(2, "Maxim", "1212");

    private UserDao userDao;
    private UserService userService;

    UserServiceTest(TestInfo testInfo) {
        System.out.println();
    }

    @BeforeAll
    static void init() {
        System.out.println("Before all: ");
    }

    @BeforeEach
    void prepare() {
        System.out.println("Before each: " + this);
        this.userDao = Mockito.mock(UserDao.class);
        this.userService = new UserService(userDao);
    }

    @Test
    @DisplayName("users will be empty if no user added")
    void usersEmptyIfNoUserAdded() {
        System.out.println("Test 1: " + this);
        var users = userService.getAll();

        assertThat(users).isEmpty();
    }

    @Test
    void usersSizeIfUserAdded() {
        System.out.println("Test 2: " + this);
        userService.add(IVAN);
        userService.add(MAXIM);

        var users = userService.getAll();

        assertThat(users).hasSize(2);
    }

    @Test
    void usersConvertedToMapById() {
        userService.add(IVAN, MAXIM);

        Map<Integer, User> users = userService.getAllConvertedById();

        assertAll(
                () -> assertThat(users).containsKeys(IVAN.getId(), MAXIM.getId()),
                () -> assertThat(users).containsValues(IVAN, MAXIM)
        );
    }

    @Test
    void shouldDeleteExistedUser() {
        userService.add(IVAN);
//        Mockito.doReturn(true).when(userDao).delete(IVAN.getId());
//        Mockito.doReturn(true).when(userDao).delete(Mockito.any());

        Mockito.when(userDao.delete(IVAN.getId()))
                .thenReturn(true)
                .thenReturn(false);

        var deleteResult = userService.delete(IVAN.getId());
        assertThat(deleteResult).isTrue();
        System.out.println(userService.delete(IVAN.getId()));
        System.out.println(userService.delete(IVAN.getId()));
    }

    @AfterEach
    void deleteDataFromDatabase() {
        System.out.println("After each: " + this);
    }

    @AfterAll
    static void closeConnectionPool() {
        System.out.println("After all: ");
    }

    @Nested
    @DisplayName("test user login functionality")
    @Tag("login")
    class LoginTest {
        @Test
        void loginSuccessIfUserExists() {
            userService.add(IVAN);
            Optional<User> maybeUser = userService.login(IVAN.getUsername(), IVAN.getPassword());

            assertThat(maybeUser).isPresent();
            maybeUser.ifPresent(user -> assertThat(user).isEqualTo(IVAN));
        }

        @Test
        void throwExceptionIfUsernameOrPasswordIsNull() {
            assertAll(
                    () -> {
                        var exception = assertThrows(
                                IllegalArgumentException.class, () -> userService.login(null, "dummy")
                        );
                        assertThat(exception.getMessage()).isEqualTo("username or password is null");
                    },
                    () -> {
                        var exception = assertThrows(
                                IllegalArgumentException.class, () -> userService.login("dummy", null)
                        );
                        assertThat(exception.getMessage()).isEqualTo("username or password is null");
                    }
            );
        }

        @Test
        void loginFailIfPasswordIsNotCorrect() {
            userService.add(IVAN);

            Optional<User> maybeUser = userService.login(IVAN.getUsername(), "dummy");

            assertThat(maybeUser).isEmpty();
        }

        @Test
        void checkLoginFunctionalityPerformance() {
            var result = assertTimeout(Duration.ofMillis(400L), () -> {
                Thread.sleep(500L);
                return userService.login("dummy", IVAN.getPassword());
            });
        }


        //        @Test
        @RepeatedTest(value = 5, name = RepeatedTest.LONG_DISPLAY_NAME)
        void loginFailIfUserDoesNotExist() {
            userService.add(IVAN);

            var maybeUser = userService.login("dummy", IVAN.getPassword());

            assertThat(maybeUser).isEmpty();
        }

        @ParameterizedTest(name = "{arguments} test")
//    @ArgumentsSource()
//        @NullSource
//        @EmptySource
//    @NullAndEmptySource
//        @ValueSource(strings = {
//                "Ivan", "Maxim"
//        })
//    @EnumSource
        @MethodSource("junit.service.UserServiceTest#getArgumentsForLoginTest")
//        @CsvFileSource(resources = "/login-test-data.csv", delimiter = ',', numLinesToSkip = 1)
//        @CsvSource({
//                "Ivan,2223",
//                "Maxim,1212"
//        })
        @DisplayName("login param test")
        void loginParametrizedTest(String username, String password, Optional<User> user) {
            userService.add(IVAN, MAXIM);

            var maybeUser = userService.login(username, password);
            assertThat(maybeUser).isEqualTo(user);
        }
    }

    static Stream<Arguments> getArgumentsForLoginTest() {
        return Stream.of(
                Arguments.of("Ivan", "2223", Optional.of(IVAN)),
                Arguments.of("Maxim", "1212", Optional.of(MAXIM)),
                Arguments.of("Petr", "dummy", Optional.empty()),
                Arguments.of("dummy", "1212", Optional.empty())
        );
    }
}
