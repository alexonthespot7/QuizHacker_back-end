package com.my.quiztaker.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.my.quiztaker.MyUser;
import com.my.quiztaker.model.User;
import com.my.quiztaker.model.UserRepository;

@Service
public class UserDetailServiceImpl implements UserDetailsService {
	@Autowired
	private UserRepository urepository;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Optional<User> user = urepository.findByUsername(username);
		
		MyUser myUser = null;
		
		if (user.isPresent()) {
			User currUser = user.get();
			
			boolean enabled = currUser.isAccountVerified();
			
			myUser = new MyUser(currUser.getId(), username,
					currUser.getPassword(), enabled, true, true, true,
					AuthorityUtils.createAuthorityList(currUser.getRole()));
		} else {
			throw new UsernameNotFoundException("User (" + username + ") not found.");
		}
		
		return myUser;
	}
}
