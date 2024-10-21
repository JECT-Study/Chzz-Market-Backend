package org.chzz.market.domain.address;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.chzz.market.domain.address.dto.DeliveryRequest;
import org.chzz.market.domain.address.dto.DeliveryResponse;
import org.chzz.market.domain.address.entity.Address;
import org.chzz.market.domain.address.error.AddressException;
import org.chzz.market.domain.address.repository.AddressRepository;
import org.chzz.market.domain.address.service.AddressService;
import org.chzz.market.domain.user.entity.User;
import org.chzz.market.domain.user.error.exception.UserException;
import org.chzz.market.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

@ExtendWith(MockitoExtension.class)
public class AddressServiceTest {

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AddressService addressService;

    private User testUser;
    private DeliveryRequest testDeliveryRequest;
    private Address testAddress, address1, address2, address3;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .build();

        testAddress = Address.builder()
                .id(1L)
                .user(testUser)
                .roadAddress("서울시 강남구")
                .jibun("강남대로 123")
                .zipcode("12345")
                .detailAddress("상세주소")
                .isDefault(true)
                .build();

        address1 = Address.builder()
                .id(1L)
                .user(testUser)
                .roadAddress("Address 1")
                .detailAddress("Detail 1")
                .zipcode("12345")
                .isDefault(true)
                .build();

        address2 = Address.builder()
                .id(2L)
                .user(testUser)
                .roadAddress("Address 2")
                .detailAddress("Detail 2")
                .zipcode("23456")
                .isDefault(false)
                .build();

        address3 = Address.builder()
                .id(3L)
                .user(testUser)
                .roadAddress("Address 3")
                .detailAddress("Detail 3")
                .zipcode("34567")
                .isDefault(false)
                .build();

        testDeliveryRequest = new DeliveryRequest(
                "서울시 강남구",
                "강남대로 123",
                "12345",
                "상세주소",
                "홍길동",
                "01012345678",
                true
        );
    }

    @Test
    @DisplayName("배송지 목록 조회 성공")
    void getAddresses_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Address> addressePage = new PageImpl<>(Collections.singletonList(testAddress));
        when(addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(eq(1L), any(Pageable.class))).thenReturn(
                addressePage);

        Page<DeliveryResponse> result = addressService.getAddresses(1L, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    @DisplayName("배송지 목록 조회 정렬 확인")
    void getAddresses_SortCorrectly() {
        Pageable pageable = PageRequest.of(0, 10);

        List<Address> addresses = Arrays.asList(address1, address2, address3);
        Page<Address> addressPage = new PageImpl<>(addresses);

        when(addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(eq(1L), any(Pageable.class))).thenReturn(
                addressPage);

        Page<DeliveryResponse> result = addressService.getAddresses(1L, pageable);

        assertNotNull(result);
        assertEquals(3, result.getContent().size());

        // 정렬 순서 확인
        assertTrue(result.getContent().get(0).isDefault());
        assertFalse(result.getContent().get(1).isDefault());
        assertFalse(result.getContent().get(2).isDefault());

        assertEquals("Address 1", result.getContent().get(0).roadAddress());
    }

    @Test
    @DisplayName("기본 배송지만 조회 성공 확인")
    void getAddresses_ShouldReturnOnlyDefaultAddressWhenSizeIsOne() {
        Long userId = 1L;
        Address defaultAddress = Address.builder()
                .id(1L)
                .roadAddress("서울시 강남구")
                .jibun("강남대로 123")
                .zipcode("12345")
                .detailAddress("상세주소")
                .recipientName("홍길동")
                .phoneNumber("01012345678")
                .isDefault(true)
                .build();

        Page<Address> addressPage = new PageImpl<>(Collections.singletonList(defaultAddress));

        Pageable pageable = PageRequest.of(0, 1, Sort.by(Direction.DESC, "isDefault", "createdAt"));

        when(addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(eq(userId),
                any(Pageable.class))).thenReturn(
                addressPage);

        Page<DeliveryResponse> result = addressService.getAddresses(userId, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        DeliveryResponse response = result.getContent().get(0);
        assertTrue(response.isDefault());
        assertEquals("서울시 강남구", response.roadAddress());
        assertEquals("강남대로 123", response.jibun());
        assertEquals("12345", response.zipcode());
        assertEquals("상세주소", response.detailAddress());
        assertEquals("홍길동", response.recipientName());
        assertEquals("01012345678", response.phoneNumber());

    }

    @Test
    @DisplayName("배송지 추가 성공")
    void addDelivery_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(addressRepository.save(any(Address.class))).thenReturn(testAddress);

        assertDoesNotThrow(() -> addressService.addDelivery(1L, testDeliveryRequest));

        verify(addressRepository).findByUserIdAndIsDefaultTrue(1L);
        verify(addressRepository).save(any(Address.class));
    }

    @Test
    @DisplayName("배송지 추가 실패 - 사용자를 찾을 수 없음")
    void addDelivery_UserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UserException.class, () -> addressService.addDelivery(1L, testDeliveryRequest));
    }

    @Test
    @DisplayName("배송지 수정 성공")
    void updateDelivery_Success() {
        Address addressToUpdate = Address.builder()
                .id(1L)
                .user(testUser)
                .roadAddress("기존 주소")
                .jibun("기존 지번")
                .zipcode("12345")
                .detailAddress("기존 상세주소")
                .recipientName("기존 수령인")
                .phoneNumber("01012345678")
                .isDefault(false)
                .build();

        when(addressRepository.findById(1L)).thenReturn(Optional.of(addressToUpdate));

        DeliveryRequest updateRequest = new DeliveryRequest(
                "새로운 주소", "새로운 지번", "54321", "새로운 상세주소",
                "새로운 수령인", "01087654321", true);

        addressService.updateDelivery(1L, 1L, updateRequest);

        assertEquals("새로운 주소", addressToUpdate.getRoadAddress());
        assertEquals("새로운 지번", addressToUpdate.getJibun());
        assertEquals("54321", addressToUpdate.getZipcode());
        assertEquals("새로운 상세주소", addressToUpdate.getDetailAddress());
        assertEquals("새로운 수령인", addressToUpdate.getRecipientName());
        assertEquals("01087654321", addressToUpdate.getPhoneNumber());
        assertTrue(addressToUpdate.isDefault());

        verify(addressRepository).findById(1L);
        verify(addressRepository).findByUserIdAndIsDefaultTrue(1L);
    }

    @Test
    @DisplayName("배송지 수정 실패 - 배송지를 찾을 수 없음")
    void updateDelivery_AddressNotFound() {
        when(addressRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(AddressException.class, () -> addressService.updateDelivery(1L, 1L, testDeliveryRequest));
    }

    @Test
    @DisplayName("배송지 삭제 성공")
    void deleteDelivery_Success() {
        Address nonDefaultAddress = Address.builder()
                .id(2L)
                .user(testUser)
                .roadAddress("서울시 강남구")
                .jibun("강남대로 123")
                .zipcode("12345")
                .detailAddress("상세주소")
                .recipientName("홍길동")
                .phoneNumber("01012345678")
                .isDefault(false)
                .build();

        when(addressRepository.findById(2L)).thenReturn(Optional.of(nonDefaultAddress));

        assertDoesNotThrow(() -> addressService.deleteDelivery(1L, 2L));

        verify(addressRepository).delete(nonDefaultAddress);
    }

    @Test
    @DisplayName("배송지 삭제 실패 - 배송지를 찾을 수 없음")
    void deleteDelivery_AddressNotFound() {
        when(addressRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(AddressException.class, () -> addressService.deleteDelivery(1L, 1L));
    }

    @Test
    @DisplayName("배송지 삭제 실패 - 기본 배송지 삭제 시도")
    void deleteDelivery_CannotDeleteDefaultAddress() {
        when(addressRepository.findById(1L)).thenReturn(Optional.of(testAddress));

        assertThrows(AddressException.class, () -> addressService.deleteDelivery(1L, 1L));
    }

}
