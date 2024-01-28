package junit.service;

import by.youngliqui.dto.User;
import by.youngliqui.service.UserService;
import org.junit.jupiter.api.*;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("user")
@Tag("fast")
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class UserServiceTest {
    private static final User IVAN = User.of(1, "Ivan", "2223");
    private static final User MAXIM = User.of(2, "Maxim", "1212");

    private UserService userService;

    @BeforeAll
    static void init() {
        System.out.println("Before all: ");
    }

    @BeforeEach
    void prepare() {
        System.out.println("Before each: " + this);
        userService = new UserService();
    }

    @Test
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
    @Tag("login")
    void loginSuccessIfUserExists() {
        userService.add(IVAN);
        Optional<User> maybeUser = userService.login(IVAN.getUsername(), IVAN.getPassword());

        assertThat(maybeUser).isPresent();
        maybeUser.ifPresent(user -> assertThat(user).isEqualTo(IVAN));
    }

    @Test
    @Tag("login")
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
    @Tag("login")
    void loginFailIfPasswordIsNotCorrect() {
        userService.add(IVAN);

        Optional<User> maybeUser = userService.login(IVAN.getUsername(), "dummy");

        assertThat(maybeUser).isEmpty();
    }

    @Test
    @Tag("login")
    void loginFailIfUserDoesNotExist() {
        userService.add(IVAN);

        var maybeUser = userService.login("dummy", IVAN.getPassword());

        assertThat(maybeUser).isEmpty();
    }

    @AfterEach
    void deleteDataFromDatabase() {
        System.out.println("After each: " + this);
    }

    @AfterAll
    static void closeConnectionPool() {
        System.out.println("After all: ");
    }
}
