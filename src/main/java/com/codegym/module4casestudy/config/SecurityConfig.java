package com.codegym.module4casestudy.config;

import com.codegym.module4casestudy.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@ComponentScan(basePackages = "com.codegym.module4casestudy")
public class SecurityConfig {

    @Autowired
    private CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        System.out.println("Creating BCryptPasswordEncoder");
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        System.out.println("=== Configuring HTTP security ===");
        http
            .authorizeRequests()
                // Static resources - public access
                .antMatchers("/static/**", "/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
                .antMatchers("/favicon.ico").permitAll()
                
                // Public pages - no authentication required
                .antMatchers("/", "/home", "/login", "/register").permitAll()
                .antMatchers("/test/**").permitAll() // For testing only
                
                // Admin exclusive access
                .antMatchers("/admin/**").hasRole("ADMIN")
                .antMatchers("/api/admin/**").hasRole("ADMIN")
                
                // Teacher exclusive access
                .antMatchers("/teacher/**").hasRole("TEACHER")
                .antMatchers("/api/teacher/**").hasRole("TEACHER")
                
                // Student exclusive access
                .antMatchers("/student/**").hasRole("STUDENT")
                .antMatchers("/api/student/**").hasRole("STUDENT")
                
                // Grade management - only teachers and admins
                .antMatchers("/grades/create", "/grades/update", "/grades/delete").hasAnyRole("TEACHER", "ADMIN")
                .antMatchers("/grades/**").hasAnyRole("STUDENT", "TEACHER", "ADMIN")
                
                // Class management - admins and teachers
                .antMatchers("/classes/create", "/classes/update", "/classes/delete").hasRole("ADMIN")
                .antMatchers("/classes/**").hasAnyRole("TEACHER", "ADMIN")
                
                // User management - admin only
                .antMatchers("/users/**").hasRole("ADMIN")
                
                // Subject management - admin only
                .antMatchers("/subjects/**").hasRole("ADMIN")
                
                // Schedule management - teachers and admins
                .antMatchers("/schedules/create", "/schedules/update", "/schedules/delete").hasAnyRole("TEACHER", "ADMIN")
                .antMatchers("/schedules/**").hasAnyRole("STUDENT", "TEACHER", "ADMIN")
                
                // Registration periods - admin only
                .antMatchers("/registration-periods/**").hasRole("ADMIN")
                
                // All other requests must be authenticated
                .anyRequest().authenticated()
            .and()
            .formLogin()
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .successHandler(customAuthenticationSuccessHandler)
                .failureUrl("/login?error=true")
                .permitAll()
            .and()
            .logout()
                .logoutSuccessUrl("/?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            .and()
            // Enable CSRF protection
            .csrf()
                .csrfTokenRepository(org.springframework.security.web.csrf.CookieCsrfTokenRepository.withHttpOnlyFalse())
                .ignoringAntMatchers("/api/**") // Ignore CSRF for API endpoints if needed
            .and()
            // Additional security headers
            .headers()
                .frameOptions().deny()
                .contentTypeOptions().and()
                .httpStrictTransportSecurity(hstsConfig -> hstsConfig
                    .maxAgeInSeconds(31536000)
                    .includeSubDomains(true));

        System.out.println("HTTP security configured with CSRF enabled and detailed URL authorization");
        return http.build();
    }
    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
