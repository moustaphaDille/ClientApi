package com.moustapha.tp.clients_api.config;
import java.util.concurrent.TimeUnit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.StaticHeadersWriter;

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // API REST stateless, pas de formulaire HTML
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll() // <-- ligne clé : autorise tout sans login
            )
            // .requiresChannel(channel -> channel
            //     .anyRequest().requiresSecure()
            // )
            .headers(headers -> headers
                .httpStrictTransportSecurity(hsts -> hsts
                    .includeSubDomains(true)
                    .maxAgeInSeconds(TimeUnit.DAYS.toSeconds(365))
                    .requestMatcher(request -> request.isSecure())
                )
                .addHeaderWriter(new StaticHeadersWriter(
                    "Content-Security-Policy",
                    "default-src 'self'"
                ))
            );

        return http.build();
    }

}
