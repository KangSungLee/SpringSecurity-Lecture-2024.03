package com.example.springSecurity.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

import jakarta.servlet.DispatcherType;


@Configuration
@EnableWebSecurity	
public class SecurityConfig {

	@Bean	// @Bean 주입되기 위해 사용
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.csrf(auth -> auth.disable())		// 괄호 안에 람다함수를 사용해야 함
			.headers(x -> x.frameOptions(y -> y.disable()))		// CK Editor image upload 때 필요
			.authorizeHttpRequests(auth -> auth
					.dispatcherTypeMatchers(DispatcherType.FORWARD).permitAll()
					.requestMatchers("user/register",	// 로그인 없이 사용할수있는 목록
							"/img/**", "/css/**", "/js/**", "/error/**").permitAll()	//** 모든것들
					.requestMatchers("/admin/**").hasAuthority("ADMIN")	// 어드민만 사용할수있는 목록
					.anyRequest().authenticated()
			)
			.formLogin(auth -> auth
					.loginPage("/user/login") 			// 로그인 폼
					.loginProcessingUrl("/user/login") 	// 스프링 시큐리티가 낚아 챔. UserDetailsService 구현객체에서 처리해주어야 함
					.usernameParameter("uid")
					.passwordParameter("pwd")
					.defaultSuccessUrl("/user/loginSuccess", true) 		// 내가 로그인후 해야할 일, 예) 세션 세팅, 오늘의 메세지 등
					.permitAll()
			)
		;
		
		return http.build();
	}
	
}
// 실전에서 응요할때 사용하는 식