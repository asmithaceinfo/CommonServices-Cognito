package com.aceinfo.commonservices.security.api.cognito.utilities;

public class AppConstants {
	
	private AppConstants() {
	    throw new IllegalStateException("Application Utility Class for Constants");
	  }

	public static final String ENDPOINT_VALIDATETOKEN = "/validatesessiontoken";	
	public static final String ENDPOINT_AUTHENTICATE = "/authenticate";
	
	
	public static final String ENDPOINT_CONFIRMN_NEWUSER = "/confirmnewuser";

	public static final String ENDPOINT_GETGROUPS_PERUSER = "/getUserGroups/{username}";
	
	public static final String ENDPOINT_DELETEUSER = "/deleteUser";
	public static final String ENDPOINT_DISABLEUSER = "/disableUser";
	public static final String ENDPOINT_ADDUSER = "/addUser";
	public static final String ENDPOINT_ENABLEUSER = "/enableUser";
	public static final String ENDPOINT_UPDATEUSER = "/updateUser";
	public static final String ENDPOINT_HEALTHCHECK = "/healthcheck";
	
	public static final String ENDPOINT_GETUSERS_ALL = "/getUsers";
	
	public static final String ENDPOINT_UPDATEUSERGROUPS = "/updateUserGroups";
	
	
	
	public static final String APPLICATION_CONSTANTS_EXCEPTION = "Exception : ";
	public static final String APPLICATION_CONSTANTS_ERROR_INVALIDLOGINREQUEST = "Incomplete Request: ";
	
	public static final String ERROR_VALIDATECLIENTTOKEN_TITLE = "Invalid Client Token : ";
	public static final String ERROR_VALIDATECLIENTTOKEN_MESSAGE = "This Client is not Authorized to use Cognito API";
	
	public static final String ERROR_VALIDATEUSERSESSION_TITLE = "Invalid User Token : ";
	public static final String ERROR_VALIDATEUSERSESSION_MESSAGE = "User Session token is invalid";
	
	public static final String ERROR_AUTHENTICATEUSER_USEREXPIRED = "Account Issue : ";
	public static final String ERROR_AUTHENTICATEUSER_USEREXPIRED_MESSAGE = "User account has expired, it must be reset by an administrator.";

	public static final String ERROR_AUTHENTICATEUSER_USERNOTAUTHORIZED = "Account Issue : ";
	public static final String ERROR_AUTHENTICATEUSER_USERNOTAUTHORIZED_MESSAGE = "User is not authorized";

	public static final String ERROR_AUTHENTICATEUSER_USERNOTFOUND = "Account Issue : ";
	public static final String ERROR_AUTHENTICATEUSER_USERNOTFOUND_MESSAGE = "User account not found in Cognito";

	public static final String ERROR_AUTHENTICATEUSER_USERPASSRESETREQUIRED = "Account Issue : ";
	public static final String ERROR_AUTHENTICATEUSER_USERPASSRESETREQUIRED_MESSAGE = "User password needs to be reset.";

	public static final String ERROR_AUTHENTICATEUSER_USERNOTCONFIRMED = "Account Issue : ";
	public static final String ERROR_AUTHENTICATEUSER_USERNOTCONFIRMED_MESSAGE = "User needs to confirm his account.";

	public static final String ERROR_AUTHENTICATEUSER_UNKNOWNEXCEPTION = "Application Error : ";
	public static final String ERROR_AUTHENTICATEUSER_UNKNOWNEXCEPTION_MESSAGE = "Unexpected error in login attempt.";

	public static final String APPLICATION_CONSTANTS_ERRORMESSAGETITLE_XXX = "UNKNOWN";
	public static final String APPLICATION_CONSTANTS_ERRORMESSAGEMESSAGE_XXX = "UNKNOWN";
	
	
	
	public static final String ATTRIBUTES_COGNITO_EMAIL = "email";
	public static final String ATTRIBUTES_COGNITO_FAMILYNAME = "custom:lastname";
	public static final String ATTRIBUTES_COGNITO_GIVENNAME = "custom:firstname";
	public static final String ATTRIBUTES_COGNITO_USERNAME = "username";
	public static final String ATTRIBUTES_COGNITO_USERPASS = "password";
	public static final String ATTRIBUTES_COGNITO_USERNEWPASS = "new_password";
	public static final String ATTRIBUTES_COGNITO_EMAILVARIFICATIONGFLAG = "email_verified";
	
	public static final String APPLICATION_CONSTANTS_TRUE_FLAG = "true";
	public static final String APPLICATION_CONSTANTS_FALSE_FLAG = "false";
	
	public static final String SPRINGPROFILE_MESSAGEPREFIX = "Active Profile  URL = ";
	
	public static final String LOG_FORMATTER_TWOPARAMS = "{} {}";

}
