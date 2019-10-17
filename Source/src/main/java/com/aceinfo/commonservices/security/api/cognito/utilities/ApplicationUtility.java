package com.aceinfo.commonservices.security.api.cognito.utilities;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aceinfo.commonservices.security.api.cognito.models.AuthenticationRequest;
import com.amazonaws.services.cognitoidp.model.AttributeType;

public class ApplicationUtility {
	private ApplicationUtility() {
		throw new IllegalStateException("ApplicationUtility class");
	}

	private static final Logger logger = LoggerFactory.getLogger(ApplicationUtility.class);

	public static String getApplicationHostName() {
		String	hostName		= "nohostname.com";
		String	tempHostName	= null;
		try {
			tempHostName = InetAddress.getLocalHost().getHostName();
		} catch (Exception e) {
			logger.error(e.getMessage().trim());
		}
		if (tempHostName != null)
			hostName = tempHostName;
		return hostName;
	}

	public static String getAplicationVersion() {
		String	applicationVersion		= "Version Not Defined";
		String	tempApplicationVersion	= null;
		try {
			tempApplicationVersion = ApplicationUtility.class.getPackage().getImplementationVersion();
		} catch (Exception e) {
			logger.error(e.getMessage().trim());
		}
		if (tempApplicationVersion != null)
			applicationVersion = tempApplicationVersion;
		return applicationVersion;
	}

	public static String getAplicationName() {
		String	applicationName		= "Application Name Not Defined";
		String	tempApplicationName	= null;
		try {
			tempApplicationName = ApplicationUtility.class.getPackage().getImplementationTitle();
		} catch (Exception e) {
			logger.error(e.getMessage().trim());
		}
		if (tempApplicationName != null)
			applicationName = tempApplicationName;
		return applicationName;
	}

	public static String getAplicationVendor() {
		String	applicationVendor		= "Vendor Not Defined";
		String	tempApplicationVendor	= null;
		try {
			tempApplicationVendor = ApplicationUtility.class.getPackage().getImplementationVendor();
		} catch (Exception e) {
			logger.error(e.getMessage().trim());
		}
		if (tempApplicationVendor != null)
			applicationVendor = tempApplicationVendor;
		return applicationVendor;
	}

	public static boolean validateAuthenticationRequest(AuthenticationRequest authRequest) {
		return (!isValid(authRequest.getUserName()) && !isValid(authRequest.getUserPassword()));
	}

	public static boolean isValid(String str) {
		return (str == null || str.isEmpty());
	}
	
	public static Map<String, String> convertCognitoAttributesToMap(List<AttributeType> userAtts) {
		Map<String, String> usermap = new HashMap<>();
		userAtts.stream().forEach(p -> usermap.put(p.getName(), p.getValue()));
		return usermap;
	}
}
