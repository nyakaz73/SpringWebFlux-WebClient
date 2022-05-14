package com.stackdev.springwebclient.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Users {
    private String name;
    private String surname;
    private String email;
    private String username;
    private String password;
}
