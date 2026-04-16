package com.utc.flight_booking_service.identity.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    // Danh sách các Endpoint công khai cho Auth và User
    private final String[] PUBLIC_ENDPOINTS = {
            "/users", "/auth/login", "/auth/introspect", "/auth/logout", "/auth/refresh",
             "/users/forgot-password"
    };

    // Danh sách các Endpoint công khai cho Thanh toán và Tra cứu
    private final String[] PAYMENT_PUBLIC_ENDPOINTS = {
            "/payments/**", "/flights/**", "/ancillary-catalogs/**",
            "/v1/airports/**", "/v1/airlines/**"
    };

    // Đọc danh sách Domain được phép từ biến môi trường app.cors.allowed-origins
    // Nếu không tìm thấy trên Render, mặc định sẽ dùng localhost
    @Value("${app.cors.allowed-origins:http://localhost:5173,http://localhost:3000}")
    private List<String> allowedOrigins;

    @Autowired
    private CustomJwtDecoder customJwtDecoder;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                // Kích hoạt cấu hình CORS từ Bean corsConfigurationSource bên dưới
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                .authorizeHttpRequests(request -> request
                        // Cho phép các yêu cầu POST công khai
                        .requestMatchers(HttpMethod.GET, "/ping/**").permitAll()
                        .requestMatchers(HttpMethod.POST, PUBLIC_ENDPOINTS).permitAll()
                        // Cho phép GET/POST cho các thông tin thanh toán và sân bay
                        .requestMatchers(HttpMethod.GET, PAYMENT_PUBLIC_ENDPOINTS).permitAll()
                        .requestMatchers(HttpMethod.POST, PAYMENT_PUBLIC_ENDPOINTS).permitAll()
                        .requestMatchers(HttpMethod.GET, "/v1/admin/flights/**").permitAll()
                        // Cấu hình cho Swagger UI
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/v3/api-docs",
                                "/webjars/**"
                        ).permitAll()
                        // Tất cả các yêu cầu còn lại phải đăng nhập
                        .anyRequest().authenticated()
                )

                // Cấu hình JWT Resource Server (Xác thực Token)
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwtConfigurer -> jwtConfigurer
                                        .decoder(customJwtDecoder)
                                        .jwtAuthenticationConverter(jwtAuthenticationConverter()))
                                .authenticationEntryPoint(new JwtAuthenticationEntryPoint())
                )
                // Tắt CSRF vì chúng ta dùng JWT (stateless)
                .csrf(AbstractHttpConfigurer::disable);

        return httpSecurity.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();

        // Gán danh sách domain được phép truy cập
        corsConfiguration.setAllowedOrigins(allowedOrigins);
        // Các phương thức HTTP được phép
        corsConfiguration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        // Cho phép tất cả các Header
        corsConfiguration.setAllowedHeaders(List.of("*"));
        // Cho phép gửi kèm Credentials (Token, Cookie)
        corsConfiguration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);

        return source;
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix("");

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);

        return jwtAuthenticationConverter;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }
}