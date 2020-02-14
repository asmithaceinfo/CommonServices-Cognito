package com.aceinfo.commonservices.security.api.cognito.config;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CommonsRequestLoggingFilter;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;



@Configuration
@EnableWebSecurity
//@EnableWebMvc
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);


	
    @Value("${cors.filter}")
    private String customCORSFilter;
    
	@Bean
	public JwtAuthFilter authTokenFilterBean() throws Exception {
		return new JwtAuthFilter();
	}
	


	@Override
	protected void configure(HttpSecurity http) throws Exception {
		logger.error("in our security config");
		http.cors().and().csrf().disable() // We don't need CSRF for JWT based authentication
				.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and().authorizeRequests().antMatchers("/actuator/**").permitAll()
				.antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
				.antMatchers("/healthcheck").permitAll()
				.antMatchers(HttpMethod.GET, "/login/**").permitAll()
				.antMatchers("/authenticate").permitAll()
				.antMatchers("/validatesessiontoken").permitAll()
				.antMatchers("/confirmnewuser").permitAll()
				.antMatchers("/forgotPassword/**").permitAll()
				.antMatchers("/confirmPassword/**").permitAll()
				.antMatchers("/test/**").permitAll()
				.antMatchers("/swagger-resources/configuration/ui").permitAll()
				.antMatchers("/swagger*").permitAll()
				.antMatchers("/signUp*").permitAll()
				
				
				.antMatchers(HttpMethod.GET,"/v2/api-docs", "/webjars/**", "/swagger-resources/**", "/configuration/**", "/*.html", "/favicon.ico", "/**/*.html", "/**/*.css", "/**/*.js").permitAll()
			
				.anyRequest().authenticated() // Protected API End-points
				.and().addFilterBefore(authTokenFilterBean(), UsernamePasswordAuthenticationFilter.class);
				//.oauth2Login();
		
		
	}

	
	@Bean
    CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(customCORSFilter, "http://localhost:9000", "http://localhost:8080", "http://localhost:4200", "http://localhost:9001", "https://*.apps.aceinfosolutions.com"));
        configuration.setAllowedMethods(Arrays.asList("POST", "GET",  "PUT", "OPTIONS", "DELETE"));
        //configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowedHeaders(Arrays.asList("Content-Type"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}
