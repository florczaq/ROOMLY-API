package org.roomly.security.config;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.roomly.security.authentication.jwt.JwtAuthenticationFilter;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.context.annotation.Bean;

@EnableWebSecurity
@EnableMethodSecurity
@Configuration
@RequiredArgsConstructor
public class WebSecurityConfig implements WebMvcConfigurer {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Override
    public void addCorsMappings (CorsRegistry registry) {
        registry.addMapping("/**")
          .allowedOrigins("*")
          .allowedMethods("*")
          .allowedHeaders("*");
    }
    
    @Bean
    @Order(1)
    public SecurityFilterChain adminFilterChain (HttpSecurity http) {
        http
          .securityMatcher(
            "/", "/applications/**", "/instances/**", "/actuator/**", "/assets/**",
            "/favicon.ico", "/manifest.json", "/sw.js"
          )
          .csrf(AbstractHttpConfigurer::disable)
          .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
          .requestCache(AbstractHttpConfigurer::disable)
          .securityContext(AbstractHttpConfigurer::disable)
          .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
          .formLogin(AbstractHttpConfigurer::disable)
          .httpBasic(AbstractHttpConfigurer::disable);
        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain filterChain (HttpSecurity http) {
        http
          .securityMatcher("/auth/**", "/graphql", "/open/**", "/api/**")
          .csrf(AbstractHttpConfigurer::disable)
          .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
          .exceptionHandling(ex -> ex.authenticationEntryPoint(
            (request, response, authException) -> response.sendError(HttpServletResponse.SC_UNAUTHORIZED)
          ))
          .authorizeHttpRequests(auth -> auth
            .requestMatchers("/auth/**", "/graphql", "/open/**").permitAll()
            .anyRequest().authenticated()
          )
          .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
