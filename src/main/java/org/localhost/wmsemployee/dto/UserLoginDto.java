package org.localhost.wmsemployee.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserLoginDto {
    private Long userId;
    private String firstName;
    private String lastName;
    private String password;
}
