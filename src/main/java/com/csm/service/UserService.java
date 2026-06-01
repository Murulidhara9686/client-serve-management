package com.csm.service;

import com.csm.model.Role;
import com.csm.model.User;
import com.csm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User register(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Transactional
    public User save(User user) {
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) { return userRepository.findByEmail(email); }

    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) { return userRepository.findById(id); }

    @Transactional(readOnly = true)
    public List<User> findByRole(Role role) { return userRepository.findByRole(role); }

    @Transactional(readOnly = true)
    public List<User> findAll() { return userRepository.findAll(); }
}
