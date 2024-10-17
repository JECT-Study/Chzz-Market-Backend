package org.chzz.market.domain.address.controller;

import lombok.RequiredArgsConstructor;
import org.chzz.market.common.config.LoginUser;
import org.chzz.market.domain.address.dto.request.AddressDto;
import org.chzz.market.domain.address.dto.request.DeliveryDto;
import org.chzz.market.domain.address.service.AddressService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/addresses")
@RequiredArgsConstructor
public class AddressController {
    private final AddressService addressService;

    @GetMapping
    public ResponseEntity<?> getAddresses(
            @LoginUser Long userId,
            Pageable pageable
    ) {
        return ResponseEntity.ok(addressService.getAddresses(userId, pageable));
    }

    @PostMapping
    public ResponseEntity<?> addAddress(
            @LoginUser Long userId,
            @RequestBody AddressDto addressDto
    ) {
        addressService.addAddress(userId, addressDto);
        return ResponseEntity.ok().build();//TODO 2024 09 11 16:42:57 : redirect
    }

    @PostMapping("/delivery")
    public ResponseEntity<Long> addDelivery(
            @LoginUser Long userId,
            @RequestBody DeliveryDto deliveryDto
    ) {
        addressService.addDelivery(userId, deliveryDto);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/delivery/{addressId}")
    public ResponseEntity<Void> updateDelivery(
            @LoginUser Long userId,
            @PathVariable Long addressId,
            @RequestBody DeliveryDto deliveryDto
    ) {
        addressService.updateDelivery(userId, addressId, deliveryDto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/delivery/{addressId}")
    public ResponseEntity<Void> deleteDelivery(
            @LoginUser Long userId,
            @PathVariable Long addressId
    ) {
        addressService.deleteDelivery(userId, addressId);
        return ResponseEntity.noContent().build();
    }
}
