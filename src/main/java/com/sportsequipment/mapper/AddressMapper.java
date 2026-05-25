package com.sportsequipment.mapper;

import com.sportsequipment.entity.Address;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AddressMapper {

    Address findById(Long id);

    List<Address> findAll();

    void insert(Address address);

    void update(Address address);

    void deleteById(Long id);

    List<Address> findByUserId(Long userId);

    Address findByUserIdAndIsDefaultTrue(Long userId);

    void deleteByUserId(Long userId);
}