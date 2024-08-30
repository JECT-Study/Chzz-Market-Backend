package org.chzz.market.domain.like.service;

import org.chzz.market.domain.like.dto.LikeResponse;
import org.chzz.market.domain.like.entity.Like;
import org.chzz.market.domain.like.repository.LikeRepository;
import org.chzz.market.domain.product.entity.Product;
import org.chzz.market.domain.product.repository.ProductRepository;
import org.chzz.market.domain.user.entity.User;
import org.chzz.market.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LikeServiceTest {

    @Mock
    private LikeRepository likeRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private LikeService likeService;

    private User user, user2;
    private Product product;
    private Like like;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .build();

        user2 = User.builder()
                .id(2L)
                .build();

        product = Product.builder()
                .id(1L)
                .build();

        like = Like.builder()
                .product(product)
                .user(user)
                .id(1L)
                .build();
    }

    @Nested
    @DisplayName("좋아요 토글 테스트")
    class ToggleLikeTest {

        @Test
        @DisplayName("1. 좋아요 없을 때 좋아요 생성")
        void createNewLikeWhenNotExists() {
            // given
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(productRepository.findPreOrder(1L)).thenReturn(Optional.of(product));
            when(likeRepository.existsByUserIdAndProductId(1L, 1L)).thenReturn(false);

            // when
            LikeResponse response = likeService.toggleLike(1L, 1L);

            // then
            assertTrue(response.isLiked());
            assertEquals(1, product.getLikeCount());
            verify(likeRepository).save(any(Like.class));
        }

        @Test
        @DisplayName("2. 좋아요 있을 때 좋아요 삭제")
        void removeLikeWhenExists() {
            // then
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(productRepository.findPreOrder(1L)).thenReturn(Optional.of(product));
            when(likeRepository.existsByUserIdAndProductId(1L, 1L)).thenReturn(true);
            when(likeRepository.findByUserAndProduct(user, product)).thenReturn(Optional.of(like));

            // when
            LikeResponse response = likeService.toggleLike(1L, 1L);

            // then
            assertFalse(response.isLiked());
            assertEquals(0, product.getLikeCount());
            verify(likeRepository).delete(any(Like.class));
        }

        @Test
        @DisplayName("3. 여러 사용자가 좋아요 누르면 좋아요 수가 증가")
        void increaseLikeCountWhenMultipleUsersLike() {
            // given
            when(userRepository.findById(anyLong())).thenReturn(Optional.of(user), Optional.of(user2));
            when(productRepository.findPreOrder(1L)).thenReturn(Optional.of(product));
            when(likeRepository.existsByUserIdAndProductId(anyLong(), anyLong())).thenReturn(false);

            // when
            likeService.toggleLike(1L, 1L);
            LikeResponse response = likeService.toggleLike(2L, 1L);

            // then
            assertTrue(response.isLiked());
            assertEquals(2, product.getLikeCount());
            verify(likeRepository, times(2)).save(any(Like.class));
        }
    }
}