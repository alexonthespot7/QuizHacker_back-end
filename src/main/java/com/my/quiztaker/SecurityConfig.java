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
						.requestMatchers(HttpMethod.GET, "/quizzes", "/quizzes/*", "/questions", "/questions/*", "/answers/*", "/difficulties", "/categories", "/users").permitAll()
						.requestMatchers(HttpMethod.POST, "/login", "/signup").permitAll()
						.requestMatchers(HttpMethod.PUT, "/verify/*", "/resetpassword").permitAll()
						.requestMatchers(HttpMethod.GET, "/addattempt/*", "/quizzesbyuser/*", "/usersauth/*", "/users/*", "/personalquizzes/*", "/getavatar/*").authenticated()
						.requestMatchers(HttpMethod.POST, "/createquiz", "/changepassword", "/sendattempt/*", "/updateavatar/*").authenticated()
						.requestMatchers(HttpMethod.PUT, "/updatequiz/*",  "/savequestions/*", "/publishquiz/*").authenticated()
						.requestMatchers(HttpMethod.DELETE, "/deletequestion/*", "/deletequiz/*").authenticated()
						.anyRequest().permitAll())
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
