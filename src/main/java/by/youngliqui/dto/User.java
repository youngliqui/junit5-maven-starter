package by.youngliqui.dto;

import lombok.Value;

@Value(staticConstructor = "of")
public class User {
    private int id;
    private String username;
    private String password;
}
