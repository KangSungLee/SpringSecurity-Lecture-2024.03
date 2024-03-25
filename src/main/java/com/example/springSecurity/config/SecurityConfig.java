package com.example.springSecurity.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity	
public class SecurityConfig {

	@Bean	// @Bean 주입되기 위해 사용
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.csrf(auth -> auth.disable())		// 괄호 안에 람다함수를 사용해야 함
			.headers(x -> x.frameOptions(y -> y.disable()))		// CK Editor image upload 때 필요
		;
		
		return http.build();
	}
	
}
// 실전에서 응요할때 사용하는 식