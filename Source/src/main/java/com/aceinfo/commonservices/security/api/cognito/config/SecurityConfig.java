package com.aceinfo.commonservices.security.api.cognito.config;
  

import org.springframework.context.annotation.Bean; 
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity; 
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity; 
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
  
  @Configuration
  @EnableWebSecurity 
  @EnableGlobalMethodSecurity(prePostEnabled = true)
  public class SecurityConfig  extends WebSecurityConfigurerAdapter {

	  
  @Bean 
  public JwtAuthFilter authTokenFilterBean() throws Exception { 
	  return new JwtAuthFilter();
	  }

  
  @Override
  protected void configure(HttpSecurity http) throws Exception {


	  http.cors().and()
      .csrf().disable() // We don't need CSRF for JWT based authentication
      .sessionManagement()
          .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
       .and()
          .authorizeRequests()
              .antMatchers("/actuator/**").permitAll()
              .antMatchers("/healthcheck").permitAll()
        // .antMatchers("/login/**").permitAll()
              .antMatchers(HttpMethod.GET, "/login/**").permitAll()
              .antMatchers("/authenticate").permitAll()
              .antMatchers("/validatesessiontoken").permitAll()
              .antMatchers("/confirmnewuser").permitAll()
              .antMatchers("/forgotPassword/**").permitAll()
              .antMatchers("/confirmPassword/**").permitAll()
              .antMatchers("/test/**").permitAll()
               .antMatchers("/swagger-resources/configuration/ui").permitAll() 
               .antMatchers("/swagger*").permitAll()
               .antMatchers(
                       HttpMethod.GET,
                      // "/",
                       "/v2/api-docs",           // swagger
                       "/webjars/**",            // swagger-ui webjars
                       "/swagger-resources/**",  // swagger-ui resources
                       "/configuration/**",      // swagger configuration
                       "/*.html",
                       "/favicon.ico",
                       "/**/*.html",
                       "/**/*.css",
                       "/**/*.js"
               ).permitAll()
      .anyRequest()
              .authenticated() // Protected API End-points
      .and()
      .addFilterBefore(authTokenFilterBean(),
    	      UsernamePasswordAuthenticationFilter.class);
      //.oauth2Login();

  
  } 

  }
 