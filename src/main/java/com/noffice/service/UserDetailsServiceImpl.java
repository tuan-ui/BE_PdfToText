package com.noffice.service;


import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.noffice.entity.User;
import com.noffice.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

	private final UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = userRepository.findByUsername(username)
				.orElseThrow(() -> new UsernameNotFoundException("User not found"));
		if (user.getIsActive() == null || !user.getIsActive()) {
			throw new UsernameNotFoundException("User is locked");
		}
		return user;
//		if (username.toLowerCase().equals("admin")) {
////			throw new UsernameNotFoundException("User not found");
//		}
//
//		User userTest = new User();
//		userTest.setUsername("admin");
//		userTest.setPassword("123");
//		return userTest;
		
		
	}

}
