package com.sportsequipment.security;

import com.sportsequipment.entity.User;
import com.sportsequipment.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserDetailsServiceImpl implements UserDetailsManager {
    private static final Logger logger = LoggerFactory.getLogger(UserDetailsServiceImpl.class);
    
    @Autowired
    UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.info("尝试加载用户: {}", username);
        
        try {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> {
                        logger.warn("未找到用户: {}", username);
                        return new UsernameNotFoundException("User Not Found with username: " + username);
                    });

            logger.info("成功加载用户: {}, 角色: {}", username, user.getRole());
            return UserDetailsImpl.build(user);
        } catch (UsernameNotFoundException e) {
            logger.error("用户名未找到: {}", username, e);
            throw e;
        } catch (Exception e) {
            logger.error("加载用户 {} 时发生错误: {}", username, e.getMessage(), e);
            throw new UsernameNotFoundException("Error loading user: " + username, e);
        }
    }

    @Override
    public void createUser(UserDetails user) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void updateUser(UserDetails user) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void deleteUser(String username) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void changePassword(String oldPassword, String newPassword) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean userExists(String username) {
        return userRepository.findByUsername(username).isPresent();
    }
}
    