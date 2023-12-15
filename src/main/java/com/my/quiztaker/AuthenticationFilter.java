package com.my.quiztaker;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.my.quiztaker.model.User;
import com.my.quiztaker.model.UserRepository;
import com.my.quiztaker.service.AuthenticationService;

@Component
public class AuthenticationFilter extends OncePerRequestFilter {
	@Autowired
	private AuthenticationService jwtService;
	
	@Autowired
	private UserRepository urepository;
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {
		
		String jws = request.getHeader(HttpHeaders.AUTHORIZATION);
		
		if (jws != null) {
			String username = jwtService.getAuthUser(request);
			
			Optional<User> optUser = urepository.findByUsername(username);
			
			Authentication authentication;
			
			if (optUser.isPresent()) {
				User user = optUser.get();
				boolean enabled = user.isAccountVerified();
				MyUser myUser = new MyUser(user.getId(), username, user.getPassword(),
						enabled, true, true, true, AuthorityUtils.createAuthorityList(user.getRole()));
				authentication = new UsernamePasswordAuthenticationToken(myUser, null, AuthorityUtils.createAuthorityList(user.getRole()));
			} else {
				authentication = new UsernamePasswordAuthenticationToken(null, null, Collections.emptyList());
			}
			
			SecurityContextHolder.getContext().setAuthentication(authentication);
		}
		filterChain.doFilter(request, response);
	}
}
