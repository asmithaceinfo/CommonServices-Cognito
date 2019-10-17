package com.aceinfo.commonservices.security.api.cognito.config;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig extends WebMvcConfigurationSupport {
	
	private static final Set<String> DEFAULT_PRODUCES_AND_CONSUMES =  new HashSet<>(Arrays.asList("application/json","application/xml"));
		
    @Bean
    public Docket productApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.aceinfo.commonservices.security.api.cognito"))
                .paths(PathSelectors.any())
                .build()
                .apiInfo(cognitoAPIInfo())
                .useDefaultResponseMessages(false)                                             
                .consumes(DEFAULT_PRODUCES_AND_CONSUMES)
                .produces(DEFAULT_PRODUCES_AND_CONSUMES);
    }
    private ApiInfo cognitoAPIInfo() {
        return new ApiInfoBuilder()
                .title("Cognito Authorization API")
                .description("\"Common Services - Cognito API\"")
                .version("1.0.0")
                .contact(new Contact("CoE@AceInfo", "https://www.aceinfosolutions.com/", "coe@aceinfosolutions.com"))
                .build();
    }
    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("swagger-ui.html")
                .addResourceLocations("classpath:/META-INF/resources/");

        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }
}
