package org.chzz.market.domain.address.controller;

import lombok.RequiredArgsConstructor;
import org.chzz.market.common.config.LoginUser;
import org.chzz.market.domain.address.dto.request.AddressDto;
import org.chzz.market.domain.address.service.AddressService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/addresses")
@RequiredArgsConstructor
public class AddressController implements AddressApi {
    private final AddressService addressService;

    @Override
    @GetMapping
    public ResponseEntity<Page<AddressDto>> getAddresses(
            @LoginUser Long userId,
            Pageable pageable) {
        return ResponseEntity.ok(addressService.getAddresses(userId, pageable));
    }

    @Override
    @PostMapping
    public ResponseEntity<Void> addAddress(
            @LoginUser Long userId,
            @RequestBody AddressDto addressDto
    ) {
        addressService.save(userId, addressDto);
        return ResponseEntity.ok().build();//TODO 2024 09 11 16:42:57 : redirect
    }
}
