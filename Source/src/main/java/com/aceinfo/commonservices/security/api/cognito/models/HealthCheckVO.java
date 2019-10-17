package com.aceinfo.commonservices.security.api.cognito.models;

import java.io.Serializable;

@SuppressWarnings("serial")
public class HealthCheckVO implements Serializable{
	
    private String applicationName;
    private String applicationHostName;
    private String applicationVersion;    
    private String applicationMessage;
    private String applicationVendor;
    private long requestNumber;    
    private String testString = "TestString";


	public HealthCheckVO(String applicationName, String applicationHostName, String applicationVersion, String applicationMessage, String applicationVendor, long requestNumber) {
		this.applicationName = applicationName;
		this.applicationHostName = applicationHostName;
		this.applicationVersion = applicationVersion;
		this.applicationMessage = applicationMessage;
		this.applicationVendor = applicationVendor;
		this.requestNumber = requestNumber;
    }


	public HealthCheckVO() {
	}


	public String getApplicationName() {
		return applicationName;
	}


	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}


	public String getApplicationHostName() {
		return applicationHostName;
	}


	public void setApplicationHostName(String applicationHostName) {
		this.applicationHostName = applicationHostName;
	}


	public String getApplicationVersion() {
		return applicationVersion;
	}


	public void setApplicationVersion(String applicationVersion) {
		this.applicationVersion = applicationVersion;
	}


	public String getApplicationMessage() {
		return applicationMessage;
	}


	public void setApplicationMessage(String applicationMessage) {
		this.applicationMessage = applicationMessage;
	}


	public long getRequestNumber() {
		return requestNumber;
	}


	public void setRequestNumber(long requestNumber) {
		this.requestNumber = requestNumber;
	}


	public String getApplicationVendor() {
		return applicationVendor;
	}


	public void setApplicationVendor(String applicationVendor) {
		this.applicationVendor = applicationVendor;
	}


	public String getTestString() {
		return testString;
	}


	public void setTestString(String testString) {
		this.testString = testString;
	}


	@Override
	public String toString() {
		return String.format("HealthCheckVO [applicationName=%s, applicationHostName=%s, applicationVersion=%s, applicationMessage=%s, applicationVendor=%s, requestNumber=%s, testString=%s]",
				applicationName, applicationHostName, applicationVersion, applicationMessage, applicationVendor, requestNumber, testString);
	}
}
