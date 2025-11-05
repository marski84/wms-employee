package auth.service;

import auth.dto.login.TokenResponseDto;

public interface IAuthenticationService {
    TokenResponseDto handleApiLogin(String email, String password);
}
