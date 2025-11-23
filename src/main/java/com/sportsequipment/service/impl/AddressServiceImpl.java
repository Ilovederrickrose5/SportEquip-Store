package com.sportsequipment.service.impl;

import jakarta.annotation.Nonnull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sportsequipment.entity.Address;
import com.sportsequipment.exception.ResourceNotFoundException;
import com.sportsequipment.repository.AddressRepository;
import com.sportsequipment.service.AddressService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AddressServiceImpl implements AddressService {

    @Autowired
    private AddressRepository addressRepository;

    @Override
    public Address createAddress(Address address) {
        // 如果这是用户的第一个地址，自动设为默认地址
        if (address.getUser() != null) {
            List<Address> userAddresses = addressRepository.findByUserId(address.getUser().getId());
            if (userAddresses.isEmpty()) {
                address.setDefault(true);
            }
        }
        // 如果用户设置了新地址为默认，先取消其他地址的默认状态
        else if (address.isDefault() && address.getUser() != null) {
            cancelDefaultAddresses(address.getUser().getId());
        }
        return addressRepository.save(address);
    }

    @Override
    public Address updateAddress(Long id, Address address) {
        Address existingAddress = getAddressById(id);

        // 验证用户是否有权限修改此地址
        if (existingAddress.getUser() == null || address.getUser() == null || 
            !existingAddress.getUser().getId().equals(address.getUser().getId())) {
            throw new SecurityException("You are not authorized to update this address");
        }

        existingAddress.setName(address.getName());
        existingAddress.setPhone(address.getPhone());
        existingAddress.setAddress(address.getAddress());
        existingAddress.setUpdatedAt(LocalDateTime.now());

        // 如果设置为默认地址，先取消其他地址的默认状态
        if (address.isDefault() && !existingAddress.isDefault() && address.getUser() != null) {
            cancelDefaultAddresses(address.getUser().getId());
            existingAddress.setDefault(true);
        }
        // 如果取消默认地址，不做任何处理

        return addressRepository.save(existingAddress);
    }

    @Override
    public void deleteAddress(@Nonnull Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Address ID cannot be null");
        }
        Address address = getAddressById(id);
        addressRepository.delete(address);
    }

    @Override
    public Address getAddressById(@Nonnull Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Address ID cannot be null");
        }
        return addressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + id));
    }

    @Override
    public List<Address> getAddressesByUserId(Long userId) {
        return addressRepository.findByUserId(userId);
    }

    @Override
    public Optional<Address> getDefaultAddressByUserId(Long userId) {
        return addressRepository.findByUserIdAndIsDefaultTrue(userId);
    }

    @Override
    @Transactional
    public void setDefaultAddress(Long id, Long userId) {
        // 先取消所有该用户的默认地址
        cancelDefaultAddresses(userId);
        // 然后将指定地址设为默认
        Address address = getAddressById(id);
        if (address.getUser() == null || !address.getUser().getId().equals(userId)) {
            throw new SecurityException("You are not authorized to set this address as default");
        }
        address.setDefault(true);
        address.setUpdatedAt(LocalDateTime.now());
        addressRepository.save(address);
    }

    private void cancelDefaultAddresses(Long userId) {
        List<Address> addresses = addressRepository.findByUserId(userId);
        for (Address address : addresses) {
            if (address.isDefault()) {
                address.setDefault(false);
                address.setUpdatedAt(LocalDateTime.now());
                addressRepository.save(address);
            }
        }
    }
}