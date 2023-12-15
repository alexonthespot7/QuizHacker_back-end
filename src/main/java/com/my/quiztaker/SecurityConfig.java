package com.my.quiztaker;

import static org.springframework.security.config.Customizer.withDefaults;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.my.quiztaker.service.UserDetailServiceImpl;


@Configuration
@EnableWebSecurity
public class SecurityConfig {
	@Autowired
	private UserDetailServiceImpl userDetailsService;
	
	@Autowired
	private AuthenticationFilter authenticationFilter;
	
	@Autowired
	private AuthEntryPoint authExceptionHandler;
	
	private static final String[] SWAGGER_PATHS = {"/swagger-ui/**", "/v3/api-docs/**", "/api-docs/**", "/v3/api-docs", "/swagger-ui.html"};

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.csrf((csrf) -> csrf.disable()).cors(withDefaults())
				.sessionManagement(
						(sessionManagement) -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests((authorizeHttpRequests) -> authorizeHttpRequests
						.requestMatchers(SWAGGER_PATHS).permitAll()
						.requestMatchers("/error/**").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/quizzes", "/api/quizzes/*", "/api/questions", "/api/questions/*", "/api/answers/*", "/api/difficulties", "/api/categories", "/api/users").permitAll()
						.requestMatchers(HttpMethod.POST, "/api/login", "/api/signup").permitAll()
						.requestMatchers(HttpMethod.PUT, "/api/verify/*", "/api/resetpassword").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/quizzesbyuser/*", "/api/usersauth/*", "/api/users/*", "/api/personalquizzes/*", "/api/getavatar/*").authenticated()
						.requestMatchers(HttpMethod.POST, "/api/createquiz", "/api/sendattempt/*").authenticated()
						.requestMatchers(HttpMethod.PUT, "/api/updatequiz/*",  "/api/savequestions/*", "/api/publishquiz/*", "/api/updateavatar/*").authenticated()
						.requestMatchers(HttpMethod.DELETE, "/api/deletequestion/*", "/api/deletequiz/*").authenticated()
						.anyRequest().hasAuthority("ADMIN"))
				.addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter.class)
				.exceptionHandling((exceptionHandling) -> exceptionHandling.authenticationEntryPoint(authExceptionHandler));

		return http.build();
	}
	
	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(userDetailsService).passwordEncoder(new BCryptPasswordEncoder());
	}
	
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
		return authConfig.getAuthenticationManager();
	}
	
	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowedOrigins(Arrays.asList("*"));
		config.setAllowedMethods(Arrays.asList("*"));
		config.setAllowedHeaders(Arrays.asList("*"));
		config.setAllowCredentials(false);
		config.applyPermitDefaultValues();

		source.registerCorsConfiguration("/**", config);
		return source;
	}		
	
}
