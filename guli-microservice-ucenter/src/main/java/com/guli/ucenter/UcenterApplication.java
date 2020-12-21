package com.guli.ucenter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author helen
 * @since 2019/7/1
 */
@SpringBootApplication
@ComponentScan(basePackages={"com.guli.ucenter","com.guli.common"})
@EnableEurekaClient
public class UcenterApplication {
	public static void main(String[] args) {
		SpringApplication.run(UcenterApplication.class, args);
	}
}