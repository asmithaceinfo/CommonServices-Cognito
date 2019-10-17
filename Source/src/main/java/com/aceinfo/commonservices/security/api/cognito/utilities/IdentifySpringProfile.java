package com.aceinfo.commonservices.security.api.cognito.utilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@ConfigurationProperties("spring.profiles")

public class IdentifySpringProfile {

	private String activeProfile;
	
	private static final Logger logger = LoggerFactory.getLogger(IdentifySpringProfile.class);
	
	public String getActiveProfile() {
		return activeProfile;
	}

	public void setActiveProfile(String activeProfile) {
		this.activeProfile = activeProfile;
	}

	@Profile("dev")
    @Bean
    public String devDatabaseConnection() {
		logger.info(AppConstants.LOG_FORMATTER_TWOPARAMS, AppConstants.SPRINGPROFILE_MESSAGEPREFIX, getActiveProfile());
        return "dev";
    }

	@Profile("test")
    @Bean
    public String testDatabaseConnection() {
		logger.info(AppConstants.LOG_FORMATTER_TWOPARAMS, AppConstants.SPRINGPROFILE_MESSAGEPREFIX, getActiveProfile());
        return "test";
    }

	@Profile("stage")
    @Bean
    public String stageDatabaseConnection() {
		logger.info(AppConstants.LOG_FORMATTER_TWOPARAMS, AppConstants.SPRINGPROFILE_MESSAGEPREFIX, getActiveProfile());
		return "stage";
    }

	@Profile("prod")
    @Bean
    public String prodDatabaseConnection() {
		logger.info(AppConstants.LOG_FORMATTER_TWOPARAMS, AppConstants.SPRINGPROFILE_MESSAGEPREFIX, getActiveProfile());
        return "prod";
    }

}