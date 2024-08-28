package org.chzz.market.domain.user.controller;

import lombok.RequiredArgsConstructor;
import org.chzz.market.domain.user.dto.UserProfileResponse;
import org.chzz.market.domain.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @GetMapping("/{nickname}")
    public ResponseEntity<UserProfileResponse> getUserProfile(@PathVariable String nickname){
        UserProfileResponse response = userService.getUserProfile(nickname);
        return ResponseEntity.ok(response);
    }
}
