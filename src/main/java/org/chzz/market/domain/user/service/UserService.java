package org.chzz.market.domain.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chzz.market.domain.user.dto.request.UserCreateRequest;
import org.chzz.market.domain.user.dto.response.NicknameAvailabilityResponse;
import org.chzz.market.domain.user.dto.UpdateProfileResponse;
import org.chzz.market.domain.user.dto.UpdateUserProfileRequest;
import org.chzz.market.domain.user.entity.User;
import org.chzz.market.domain.user.error.exception.UserException;
import org.chzz.market.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.chzz.market.domain.user.error.UserErrorCode.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public User completeUserRegistration(Long userId, UserCreateRequest userCreateRequest) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserException(USER_NOT_FOUND));
        if (userRepository.findByNickname(userCreateRequest.getNickname()).isPresent()) {
            throw new UserException(NICKNAME_DUPLICATION);
        }
        user.createUser(userCreateRequest);
        user.addBankAccount(userCreateRequest.toBankAccount());
        return user;
    }

    public NicknameAvailabilityResponse checkNickname(String nickname) {
        return new NicknameAvailabilityResponse(userRepository.findByNickname(nickname).isEmpty());
    }

    @Transactional
    public UpdateProfileResponse updateUserProfile(String nickname, Long userId, UpdateUserProfileRequest request){
        log.info("유저 닉네임이 {}인 유저에 대한 프로필 정보 업데이트를 시작합니다.", nickname);
        // 유저 유효성 검사
        User existingUser = userRepository.findByNickname(nickname)
                .orElseThrow(() -> new UserException(USER_NOT_FOUND));

        // 로그인 유저와 프로필 소유자 같은지 유효성 검사
        if (!existingUser.getId().equals(userId)) {
            throw new UserException(UNAUTHORIZED_USER);
        }

        // 닉네임 중복 검사
        if (!nickname.equals(request.getNickname())) {
            userRepository.findByNickname(request.getNickname()).ifPresent(user -> {
                throw new UserException(NICKNAME_DUPLICATION);
            });
        }

        // 프로필 정보 업데이트
        existingUser.updateProfile(
                request.getNickname(),
                request.getBio(),
                request.getLink()
        );

        log.info("유저 닉네임이 {}인 유저에 대한 프로필 정보 업데이트를 완료했습니다.", nickname);
        return UpdateProfileResponse.from(existingUser);
    }
}
