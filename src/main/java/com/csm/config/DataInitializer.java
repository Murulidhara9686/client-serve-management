package com.csm.config;

import com.csm.model.Role;
import com.csm.model.User;
import com.csm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            seed("Goutham Manager",   "manager@csm.com",   Role.MANAGER);
            seed("Abhilash Assistant","assistant@csm.com",  Role.ASSISTANT_MANAGER);
            seed("Vishal Employee",   "employee@csm.com",  Role.EMPLOYEE);
            seed("Ravi Customer",     "customer@csm.com",  Role.CUSTOMER);

            System.out.println("\n=================================");
            System.out.println("  ClientServe — Demo accounts  ");
            System.out.println("=================================");
            System.out.println("  Manager   : manager@csm.com   / password");
            System.out.println("  Assistant : assistant@csm.com / password");
            System.out.println("  Employee  : employee@csm.com  / password");
            System.out.println("  Customer  : customer@csm.com  / password");
            System.out.println("=================================\n");
        }
    }

    private void seed(String name, String email, Role role) {
        User u = new User();
        u.setName(name);
        u.setEmail(email);
        u.setPassword(passwordEncoder.encode("password"));
        u.setRole(role);
        u.setPhone("9876543210");
        userRepository.save(u);
    }
}
