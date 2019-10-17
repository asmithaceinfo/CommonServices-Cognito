package com.aceinfo.commonservices.security.api.cognito.controller;


import java.util.concurrent.atomic.AtomicLong;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.aceinfo.commonservices.security.api.cognito.models.HealthCheckVO;
import com.aceinfo.commonservices.security.api.cognito.utilities.AppConstants;
import com.aceinfo.commonservices.security.api.cognito.utilities.ApplicationUtility;



@RestController
public class HealthCheckController {
	
	@Value("${application.message}")
	private String applicationMessage;


    private final AtomicLong requestCounter = new AtomicLong();
       
    @GetMapping(path = AppConstants.ENDPOINT_HEALTHCHECK)
    public @ResponseBody HealthCheckVO getInfo() {
        return new HealthCheckVO(ApplicationUtility.getAplicationName(), ApplicationUtility.getApplicationHostName(), ApplicationUtility.getAplicationVersion(), applicationMessage, ApplicationUtility.getAplicationVendor(), requestCounter.incrementAndGet()); 
    }
}
