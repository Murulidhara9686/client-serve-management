package com.csm.config;

import com.csm.model.User;
import com.csm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserRepository userRepository;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/register", "/login", "/css/**", "/js/**", "/error").permitAll()
                .requestMatchers("/customer/**").hasRole("CUSTOMER")
                .requestMatchers("/manager/**").hasRole("MANAGER")
                .requestMatchers("/assistant/**").hasRole("ASSISTANT_MANAGER")
                .requestMatchers("/employee/**").hasRole("EMPLOYEE")
                .requestMatchers("/stats").hasAnyRole("MANAGER", "ASSISTANT_MANAGER")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/")
                .loginProcessingUrl("/login")
                .successHandler((req, res, auth) -> {
                    String role = auth.getAuthorities().iterator().next().getAuthority();
                    switch (role) {
                        case "ROLE_CUSTOMER"          -> res.sendRedirect("/customer/dashboard");
                        case "ROLE_MANAGER"           -> res.sendRedirect("/manager/dashboard");
                        case "ROLE_ASSISTANT_MANAGER" -> res.sendRedirect("/assistant/dashboard");
                        case "ROLE_EMPLOYEE"          -> res.sendRedirect("/employee/dashboard");
                        default                        -> res.sendRedirect("/");
                    }
                })
                .failureUrl("/?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/?loggedout=true")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .exceptionHandling(ex -> ex.accessDeniedPage("/access-denied"));
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return email -> {
            User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
            return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
            );
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
