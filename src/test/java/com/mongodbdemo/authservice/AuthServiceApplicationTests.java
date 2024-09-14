package com.mongodbdemo.authservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;

@SpringBootTest
class AuthServiceApplicationTests {

	@Test
	void contextLoads() {
		try (var mockedSpringApplication = mockStatic(SpringApplication.class)) {
			mockedSpringApplication.when(() -> SpringApplication.run(any(Class.class), any(String[].class)))
					.thenReturn(null);

			AuthServiceApplication.main(new String[]{});

			mockedSpringApplication.verify(() -> SpringApplication.run(AuthServiceApplication.class, new String[]{}));
		}	}

}
