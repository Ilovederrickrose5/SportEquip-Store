package com.sportsequipment.service;


import com.sportsequipment.entity.Address;
import java.util.List;
import java.util.Optional;

public interface AddressService {
    Address createAddress(Address address);
    Address updateAddress(Long id, Address address);
    void deleteAddress(Long id);
    Address getAddressById(Long id);
    List<Address> getAddressesByUserId(Long userId);
    Optional<Address> getDefaultAddressByUserId(Long userId);
    void setDefaultAddress(Long id, Long userId);
}