package com.sportsequipment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sportsequipment.entity.Address;
import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByUserId(Long userId);
    Optional<Address> findByUserIdAndIsDefaultTrue(Long userId);
    void deleteByUserId(Long userId);
}