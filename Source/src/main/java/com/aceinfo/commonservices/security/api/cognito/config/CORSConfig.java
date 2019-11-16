package com.aceinfo.commonservices.security.api.cognito.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
@Configuration
@EnableWebMvc
@ComponentScan("com.aceinfo.commonservices.security.api.cognito")
public class CORSConfig implements WebMvcConfigurer {
	
    @Value("${cors.filter}")
    private String customCORSFilter;

	@Override
	public void addCorsMappings(CorsRegistry registry) {
	  registry.addMapping("/**")
	   	  .allowedOrigins(customCORSFilter, "http://localhost:9000", "http://localhost:8080", "http://localhost:4200", "http://localhost:9001", "https://*.apps.aceinfosolutions.com")
		  //.allowedMethods("POST", "GET",  "PUT", "OPTIONS", "DELETE")
	   	.allowedMethods("GET", "POST", "PUT", "DELETE")
		  .allowedHeaders("*")
		  .allowCredentials(true)
		  .maxAge(4800);
	}
} 