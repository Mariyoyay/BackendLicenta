package org.example.backend.config;

import org.example.backend.filters.CustomAuthenticationFilter;
import org.example.backend.filters.CustomAuthorizationFilter;
import org.example.backend.service.JwtService;
import org.example.backend.service.RefreshTokenService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import static org.example.backend.utils.RoleNames.*;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final AuthenticationProvider authenticationProvider;
    private final CustomAuthorizationFilter customAuthorizationFilter;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final AuthenticationConfiguration authenticationConfiguration;

    public SecurityConfig(AuthenticationProvider authenticationProvider,
                          CustomAuthorizationFilter customAuthorizationFilter,
                          JwtService jwtService, RefreshTokenService refreshTokenService,
                          AuthenticationConfiguration authenticationConfiguration) {
        this.authenticationProvider = authenticationProvider;
        this.customAuthorizationFilter = customAuthorizationFilter;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.authenticationConfiguration = authenticationConfiguration;

    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        CustomAuthenticationFilter customAuthenticationFilter =
                new CustomAuthenticationFilter(authenticationManager(authenticationConfiguration), jwtService, refreshTokenService);
        customAuthenticationFilter.setFilterProcessesUrl("/api/auth/login");

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable) ///??
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/api/auth/**").permitAll()

                        .requestMatchers("/api/users/manage/add").hasAnyAuthority(ROLE_EMPLOYEE, ROLE_DOCTOR)
                        .requestMatchers("/api/users/roles/manage/**").hasAuthority(ROLE_ADMIN)
                        .requestMatchers("/api/users/doctor/set_color").hasAuthority(ROLE_DOCTOR)

                        .requestMatchers("/api/time_slots/appointment/schedule").hasAuthority(ROLE_PATIENT)
                        .requestMatchers("/api/time_slots/appointment/manage/**").hasAnyAuthority(ROLE_EMPLOYEE, ROLE_DOCTOR)
                        .requestMatchers("/api/time_slots/occupied/**").hasAuthority(ROLE_DOCTOR)
                        .requestMatchers("/api/time_slots/daySchedule/**").hasAnyAuthority(ROLE_EMPLOYEE, ROLE_DOCTOR)

                        .requestMatchers("/api/offices/**").hasAnyAuthority(ROLE_EMPLOYEE, ROLE_DOCTOR)

                        .requestMatchers("/api/users/myself").authenticated()
                        .requestMatchers("/api/users/**").hasAnyAuthority(ROLE_EMPLOYEE, ROLE_DOCTOR, ROLE_ADMIN)

                        .requestMatchers(HttpMethod.GET, "/api/users/public_resource").permitAll()
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider)
                .addFilter(customAuthenticationFilter)
                .addFilterBefore(customAuthorizationFilter, UsernamePasswordAuthenticationFilter.class)

                ;

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

//        configuration.setAllowedOrigins(List.of("http://localhost:8080", "http://localhost:5173"));//, "http://192.168.100.231:5173"));
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With", "Accept"));

        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
