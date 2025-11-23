package com.sportsequipment.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.sportsequipment.entity.Address;
import com.sportsequipment.entity.User;
import com.sportsequipment.service.AddressService;
import com.sportsequipment.service.UserService;

import java.util.List;

/**
 * 地址控制器，处理用户地址相关的HTTP请求
 * 
 * @author System
 */
@RestController
@RequestMapping("/api/addresses")
public class AddressController {

    private final AddressService addressService;
    private final UserService userService;
    
    public AddressController(AddressService addressService, UserService userService) {
        this.addressService = addressService;
        this.userService = userService;
    }

    // 获取当前用户的所有地址
    @GetMapping
    public ResponseEntity<List<Address>> getCurrentUserAddresses() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userService.findByUsername(username);

        List<Address> addresses = addressService.getAddressesByUserId(user.getId());
        return ResponseEntity.ok(addresses);
    }

    // 获取当前用户的默认地址
    @GetMapping("/default")
    public ResponseEntity<?> getDefaultAddress() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userService.findByUsername(username);

        return addressService.getDefaultAddressByUserId(user.getId())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    // 创建新地址
    @PostMapping
    public ResponseEntity<Address> createAddress(@RequestBody Address address) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userService.findByUsername(username);

        address.setUser(user);
        Address createdAddress = addressService.createAddress(address);
        return ResponseEntity.ok(createdAddress);
    }

    // 更新地址
    @PutMapping("/{id}")
    public ResponseEntity<Address> updateAddress(@PathVariable Long id, @RequestBody Address address) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userService.findByUsername(username);

        address.setUser(user);
        Address updatedAddress = addressService.updateAddress(id, address);
        return ResponseEntity.ok(updatedAddress);
    }

    // 删除地址
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAddress(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userService.findByUsername(username);

        Address address = addressService.getAddressById(id);
        if (address.getUser() == null || !address.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body("You are not authorized to delete this address");
        }

        addressService.deleteAddress(id);
        return ResponseEntity.noContent().build();
    }

    // 设置默认地址
    @PutMapping("/{id}/default")
    public ResponseEntity<?> setDefaultAddress(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userService.findByUsername(username);

        addressService.setDefaultAddress(id, user.getId());
        return ResponseEntity.ok().build();
    }
}