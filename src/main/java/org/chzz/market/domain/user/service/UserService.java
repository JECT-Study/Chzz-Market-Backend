package org.chzz.market.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.chzz.market.domain.user.dto.UpdateProfileResponse;
import org.chzz.market.domain.user.dto.UpdateUserProfileRequest;
import org.chzz.market.domain.user.entity.User;
import org.chzz.market.domain.user.error.exception.UserException;
import org.chzz.market.domain.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.chzz.market.domain.user.error.UserErrorCode.USER_NOT_FOUND;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    @Transactional
    public UpdateProfileResponse updateUserProfile(String nickname, UpdateUserProfileRequest request){
        logger.info("유저 닉네임이 {}인 유저에 대한 프로필 정보 업데이트를 시작합니다.", nickname);
        // 유저 유효성 검사
        User existingUser = userRepository.findByNickname(nickname)
                .orElseThrow(() -> new UserException(USER_NOT_FOUND));

        // 프로필 정보 업데이트
        existingUser.updateProfile(
                request.getNickname(),
                request.getDescription(),
                request.getRegion(),
                request.getUrl()
        );

        logger.info("유저 닉네임이 {}인 유저에 대한 프로필 정보 업데이트를 완료했습니다.", nickname);
        return UpdateProfileResponse.from(existingUser);
    }

}
