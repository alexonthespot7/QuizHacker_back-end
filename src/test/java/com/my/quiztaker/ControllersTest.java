package com.my.quiztaker;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.my.quiztaker.web.RestAuthenticatedController;
import com.my.quiztaker.web.RestPublicController;

@SpringBootTest
class ControllersTest {
	
	@Autowired
	private RestAuthenticatedController restAuthenticatedController;
	
	@Autowired
	private RestPublicController restPublicController;
	
	@Test
	public void contextLoads() throws Exception {
		assertThat(restAuthenticatedController).isNotNull();
	}
	
	@Test
	public void contextLoadsThree() throws Exception {
		assertThat(restPublicController).isNotNull();
	}

}
