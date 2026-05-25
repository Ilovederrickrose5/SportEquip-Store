package com.sportsequipment.mapper;

import com.sportsequipment.entity.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserMapper {

    User findById(Long id);

    List<User> findAll();

    void insert(User user);

    void update(User user);

    void deleteById(Long id);

    User findByUsername(String username);

    Integer existsByUsername(String username);

    Integer existsByEmail(String email);
}