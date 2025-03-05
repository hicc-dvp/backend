package hicc.club_fair_2025.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

	// 보안 필터 체인 설정 (CORS, CSRF, 전체 허용)
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				.cors(cors -> cors.configurationSource(corsConfigurationSource())) // CORS 설정 적용
				.csrf(csrf -> csrf.disable()) // CSRF 비활성화
				.authorizeHttpRequests(auth -> auth.anyRequest().permitAll()); // 모든 요청 허용

		return http.build();
	}

	// CORS 설정: 특정 오리진 및 메서드, 헤더 허용
	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
	    CorsConfiguration configuration = new CorsConfiguration();
	    configuration.setAllowedOrigins(List.of("https://hicc.space", "https://api.hicc.space", "http://localhost:5173"));
	    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
	    configuration.setAllowedHeaders(List.of("*"));
	    configuration.setAllowCredentials(true);
	
	    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
	    source.registerCorsConfiguration("/**", configuration);
	    return source;
	}

	/**
	* CORS 필터가 Spring Security보다 먼저 실행되도록 설정
	*/
	@Bean
	public FilterRegistrationBean<CorsFilter> corsFilter() {
		FilterRegistrationBean<CorsFilter> filterBean = new FilterRegistrationBean<>(new CorsFilter(corsConfigurationSource()));
		filterBean.setOrder(0); // 가장 먼저 실행되도록 설정
	        return filterBean;
	}
}
