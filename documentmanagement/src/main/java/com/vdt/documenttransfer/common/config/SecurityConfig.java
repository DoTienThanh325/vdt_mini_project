package com.vdt.documenttransfer.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.vdt.documenttransfer.common.security.CustomUserDetailsService;
import com.vdt.documenttransfer.common.security.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
        private final CustomUserDetailsService customUserDetailsService;
        private final JwtAuthenticationFilter jwtAuthenticationFilter;

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
                return config.getAuthenticationManager();
        }

        @Bean
        public AuthenticationProvider authenticationProvider() {
                DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
                provider.setUserDetailsService(customUserDetailsService);
                provider.setPasswordEncoder(passwordEncoder());
                return provider;
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                return http
                                .csrf(csrf -> csrf.disable())
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(
                                                                "/api/auth/login",
                                                                "/api/auth/register",
                                                                "/api/auth/refresh-token",
                                                                "/ws/**")
                                                .permitAll()

                                                .requestMatchers(
                                                                "/api/admin/**",
                                                                "/api/users/*/assignment",
                                                                "/api/users/*/status",
                                                                "/api/interconnected-systems/**",
                                                                "/api/organizations/*/status",
                                                                "/api/organizations/*/",
                                                                "/api/users/",
                                                                "/api/users",
                                                                "/api/users/status/*")
                                                .hasRole("ADMIN")

                                                .requestMatchers(
                                                                "/api/organizations/new")
                                                .hasRole("ORGADMIN")

                                                .requestMatchers(
                                                                "/api/documents/new",
                                                                "/api/documents/*/files",
                                                                "/api/documents/createdBy")
                                                .hasRole("STAFF")

                                                .requestMatchers(
                                                                "/api/documents/*/approve",
                                                                "/api/documents/*/reject")
                                                .hasRole("MANAGER")

                                                .requestMatchers(
                                                                "/api/documents/*/sign/check",
                                                                "/api/documents/*/transfer",
                                                                "/api/documents/*/receive",
                                                                "/api/interconnect/**")
                                                .hasRole("CLERK")

                                                .requestMatchers(
                                                                "/api/documents/status/*")
                                                .hasAnyRole("MANAGER", "LEADER", "CLERK")

                                                .anyRequest().authenticated())
                                .authenticationProvider(authenticationProvider())
                                .addFilterBefore(
                                                jwtAuthenticationFilter,
                                                UsernamePasswordAuthenticationFilter.class)
                                .build();
        }
}
