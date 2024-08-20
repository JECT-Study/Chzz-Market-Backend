package org.chzz.market.util;

import org.chzz.market.domain.user.entity.User;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Constructor;

public class UserTestFactory {
    public static User createUser(Long id, String nickname, String email) {
        try {
            Constructor<User> constructor = User.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            User user = constructor.newInstance();

            ReflectionTestUtils.setField(user, "id", id);
            ReflectionTestUtils.setField(user, "nickname", nickname);
            ReflectionTestUtils.setField(user, "email", email);
            ReflectionTestUtils.setField(user, "providerId", "testProviderId");
            ReflectionTestUtils.setField(user, "userRole", User.UserRole.USER);
            ReflectionTestUtils.setField(user, "providerType", User.ProviderType.LOCAL);

            return user;
        } catch (Exception e) {
            throw new RuntimeException("테스트를 위한 사용자 인스턴스 생성에 실패했습니다.", e);
        }
    }

    public static User createUserWithRole(Long id, String nickname, String email, User.UserRole userRole, User.ProviderType providerType) {
        User user = createUser(id, nickname, email);
        ReflectionTestUtils.setField(user, "userRole", userRole);
        ReflectionTestUtils.setField(user, "providerType", providerType);
        return user;
    }
}
