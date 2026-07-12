package com.sportsequipment.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sportsequipment.entity.Address;
import com.sportsequipment.exception.ResourceNotFoundException;
import com.sportsequipment.mapper.AddressMapper;
import com.sportsequipment.service.AddressService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AddressServiceImpl implements AddressService {

    private final AddressMapper addressMapper;

    public AddressServiceImpl(AddressMapper addressMapper) {
        this.addressMapper = addressMapper;
    }

    @Override
    public Address createAddress(Address address) {
        // 如果这是用户的第一个地址，自动设为默认地址
        if (address.getUserId() != null) {
            List<Address> userAddresses = addressMapper.findByUserId(address.getUserId());
            if (userAddresses.isEmpty()) {
                address.setDefault(true);
            }
        }
        // 如果用户设置了新地址为默认，先取消其他地址的默认状态
        if (address.isDefault() && address.getUserId() != null) {
            cancelDefaultAddresses(address.getUserId());
        }
        address.setCreatedAt(LocalDateTime.now());
        address.setUpdatedAt(LocalDateTime.now());
        addressMapper.insert(address);
        return address;
    }

    @Override
    public Address updateAddress(Long id, Address address) {
        Address existingAddress = getAddressById(id);

        // 验证用户是否有权限修改此地址
        if (existingAddress.getUserId() == null || address.getUserId() == null ||
                !existingAddress.getUserId().equals(address.getUserId())) {
            throw new SecurityException("You are not authorized to update this address");
        }

        existingAddress.setName(address.getName());
        existingAddress.setPhone(address.getPhone());
        existingAddress.setAddress(address.getAddress());
        existingAddress.setUpdatedAt(LocalDateTime.now());

        // 如果设置为默认地址，先取消其他地址的默认状态
        if (address.isDefault() && !existingAddress.isDefault() && address.getUserId() != null) {
            cancelDefaultAddresses(address.getUserId());
            existingAddress.setDefault(true);
        }
        // 如果取消默认地址，不做任何处理

        addressMapper.update(existingAddress);
        return existingAddress;
    }

    @Override
    public void deleteAddress(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Address ID cannot be null");
        }
        getAddressById(id);
        addressMapper.deleteById(id);
    }

    @Override
    public Address getAddressById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Address ID cannot be null");
        }
        Address address = addressMapper.findById(id);
        if (address == null) {
            throw new ResourceNotFoundException("Address not found with id: " + id);
        }
        return address;
    }

    @Override
    public List<Address> getAddressesByUserId(Long userId) {
        return addressMapper.findByUserId(userId);
    }

    @Override
    public Optional<Address> getDefaultAddressByUserId(Long userId) {
        Address address = addressMapper.findByUserIdAndIsDefaultTrue(userId);
        return Optional.ofNullable(address);
    }

    @Override
    @Transactional
    public void setDefaultAddress(Long id, Long userId) {
        // 先取消所有该用户的默认地址
        cancelDefaultAddresses(userId);
        // 然后将指定地址设为默认
        Address address = getAddressById(id);
        if (address.getUserId() == null || !address.getUserId().equals(userId)) {
            throw new SecurityException("You are not authorized to set this address as default");
        }
        address.setDefault(true);
        address.setUpdatedAt(LocalDateTime.now());
        addressMapper.update(address);
    }

    private void cancelDefaultAddresses(Long userId) {
        List<Address> addresses = addressMapper.findByUserId(userId);
        for (Address address : addresses) {
            if (address.isDefault()) {
                address.setDefault(false);
                address.setUpdatedAt(LocalDateTime.now());
                addressMapper.update(address);
            }
        }
    }
}