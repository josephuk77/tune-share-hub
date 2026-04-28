package com.tunesharehub.controller;

import com.tunesharehub.auth.AuthUser;
import com.tunesharehub.auth.CurrentUser;
import com.tunesharehub.dto.UserSummaryResponse;
import com.tunesharehub.service.AuthService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/session")
public class SessionController {

    private final AuthService authService;

    public SessionController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/me")
    public UserSummaryResponse getCurrentUser(@CurrentUser AuthUser authUser) {
        return authService.getCurrentUser(authUser.userId());
    }
}
