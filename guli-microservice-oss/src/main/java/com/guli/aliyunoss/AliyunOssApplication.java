package com.guli.aliyunoss;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author helen
 * @since 2019/6/28
 */
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
@ComponentScan(basePackages={"com.guli.aliyunoss","com.guli.common"})
public class AliyunOssApplication {

	public static void main(String[] args){
		SpringApplication.run(AliyunOssApplication.class, args);
	}
}
