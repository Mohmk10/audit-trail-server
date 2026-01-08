package com.mohmk10.audittrail.admin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class AdminConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    public AdminConfig(JwtAuthenticationFilter jwtAuthenticationFilter, OAuth2SuccessHandler oAuth2SuccessHandler) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.oAuth2SuccessHandler = oAuth2SuccessHandler;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        // Health check and actuator endpoints - public
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/health").permitAll()
                        .requestMatchers("/").permitAll()
                        // Auth endpoints - public
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        // OAuth2 endpoints - public
                        .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                        // Events endpoints - allow both API Key and JWT auth
                        .requestMatchers("/api/v1/events/**").permitAll()
                        .requestMatchers("/api/v1/events").permitAll()
                        // Search endpoints - public (tenant filtering done in service)
                        .requestMatchers("/api/v1/search/**").permitAll()
                        // Admin health endpoint - public
                        .requestMatchers("/api/v1/health").permitAll()
                        // Reports templates - public
                        .requestMatchers("/api/v1/reports/templates").permitAll()
                        // Swagger/OpenAPI - public
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        // Other endpoints require authentication
                        .anyRequest().permitAll()
                )
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(oAuth2SuccessHandler)
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
                "https://audit-trail-dashboard.vercel.app",
                "http://localhost:4200",
                "http://localhost:3000"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-API-Key", "X-Tenant-ID"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
