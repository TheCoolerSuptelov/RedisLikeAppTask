package com.github.theCoolerSuptelov.redisLikeInMemory.redisLikeSpringApp;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.github.theCoolerSuptelov.redisLikeInMemory.*")
public class RedisLikeSpringAppApplication {


	public static void main(String[] args) {

		SpringApplication.run(RedisLikeSpringAppApplication.class, args);

	}

}
