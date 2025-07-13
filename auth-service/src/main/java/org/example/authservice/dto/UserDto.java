package org.example.authservice.dto;

import lombok.Getter;
import lombok.Setter;
import org.example.authservice.model.User;

@Getter
@Setter
public class UserDto {

    private Long id;

    private String login;

    private String password;

    private String email;

    public static UserDto fromEntity(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setLogin(user.getLogin());
        dto.setEmail(user.getEmail());
        return dto;
    }
}
