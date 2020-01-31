package com.aceinfo.commonservices.security.api.cognito.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.NotImplementedException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.aceinfo.commonservices.security.api.cognito.helpers.CognitoHelper;
import com.aceinfo.commonservices.security.api.cognito.models.AuthenticationRequest;
import com.aceinfo.commonservices.security.api.cognito.models.CognitoUserVO;
import com.aceinfo.commonservices.security.api.cognito.utilities.AppConstants;
import com.aceinfo.commonservices.security.api.cognito.utilities.ApplicationUtility;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClientBuilder;
import com.amazonaws.services.cognitoidp.model.AdminAddUserToGroupRequest;
import com.amazonaws.services.cognitoidp.model.AdminCreateUserRequest;
import com.amazonaws.services.cognitoidp.model.AdminInitiateAuthRequest;
import com.amazonaws.services.cognitoidp.model.AdminInitiateAuthResult;
import com.amazonaws.services.cognitoidp.model.AdminListGroupsForUserRequest;
import com.amazonaws.services.cognitoidp.model.AdminListGroupsForUserResult;
import com.amazonaws.services.cognitoidp.model.AdminRespondToAuthChallengeRequest;
import com.amazonaws.services.cognitoidp.model.AdminRespondToAuthChallengeResult;
import com.amazonaws.services.cognitoidp.model.AttributeType;
import com.amazonaws.services.cognitoidp.model.AuthFlowType;
import com.amazonaws.services.cognitoidp.model.ChallengeNameType;
import com.amazonaws.services.cognitoidp.model.DeliveryMediumType;
import com.amazonaws.services.cognitoidp.model.GetUserRequest;
import com.amazonaws.services.cognitoidp.model.GetUserResult;
import com.amazonaws.services.cognitoidp.model.GroupType;
import com.amazonaws.services.cognitoidp.model.ListUsersRequest;
import com.amazonaws.services.cognitoidp.model.ListUsersResult;
import com.amazonaws.services.cognitoidp.model.NotAuthorizedException;
import com.amazonaws.services.cognitoidp.model.PasswordResetRequiredException;
import com.amazonaws.services.cognitoidp.model.UserNotConfirmedException;
import com.amazonaws.services.cognitoidp.model.UserNotFoundException;
import com.amazonaws.services.cognitoidp.model.UsernameExistsException;
import com.amazonaws.util.StringUtils;

@Service
public class CognitoServiceImpl implements CognitoService {
	private final Logger						logger			= LoggerFactory.getLogger(this.getClass());
	private CognitoHelper						cognitoHelper	= new CognitoHelper();
	
	@Autowired
	private static AWSCognitoIdentityProvider	cognitoClient;//	= AWSCognitoIdentityProviderClientBuilder.defaultClient();

	public ResponseEntity<Object> validateUserSession(AuthenticationRequest userAuthRequest, String cognitoPoolId, String cognitoClientId) {
		JSONObject		responseJSON	= new JSONObject();
		HttpStatus		responseStatus	= HttpStatus.NOT_IMPLEMENTED;
		GetUserRequest	getUserRequest	= new GetUserRequest();
		getUserRequest.setAccessToken(userAuthRequest.getCognitoSession());
		GetUserResult getUserResult = null;
		
		try {
			getUserResult = cognitoClient.getUser(getUserRequest);
			if (cognitoHelper.validateCognitoClient(userAuthRequest.getCognitoSession(), cognitoClientId)) {
				responseJSON.put("userName", getUserResult.getUsername());
				JSONArray groups = getGroupsForAUser(getUserResult.getUsername(), cognitoPoolId);
				responseJSON.put("groups", groups);
				responseStatus = HttpStatus.OK;
			} else {
				responseJSON.put(AppConstants.ERROR_VALIDATECLIENTTOKEN_TITLE, AppConstants.ERROR_VALIDATECLIENTTOKEN_MESSAGE);
				responseStatus = HttpStatus.UNAUTHORIZED;
			}
		} catch (UserNotFoundException | NotAuthorizedException e) {
			logger.error(e.getMessage().trim());
			responseJSON.put(AppConstants.ERROR_VALIDATEUSERSESSION_TITLE, AppConstants.ERROR_VALIDATEUSERSESSION_MESSAGE);
			responseJSON.put(AppConstants.APPLICATION_CONSTANTS_EXCEPTION, e.getMessage().trim());
			responseStatus = HttpStatus.FORBIDDEN;
		} catch (Exception e) {
			logger.error(e.getMessage().trim());
			responseJSON.put(AppConstants.APPLICATION_CONSTANTS_EXCEPTION, e.getMessage().trim());
			responseStatus = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		return new ResponseEntity<>(responseJSON.toString(), responseStatus);
	}

	public ResponseEntity<Object> authenticateUser(AuthenticationRequest userAuthRequest, String cognitoPoolId, String cognitoClientId, String secret, String access, String reg) {
		
		BasicAWSCredentials credChain = new BasicAWSCredentials(access, secret);
		
		AWSCognitoIdentityProvider	loccognitoClient	= AWSCognitoIdentityProviderClientBuilder.standard()
				 .withCredentials(new AWSStaticCredentialsProvider(credChain))
				 .withRegion(reg)
				 .build();//.defaultClient();
		JSONObject	out				= new JSONObject();
		HttpStatus	status			= HttpStatus.NOT_IMPLEMENTED;
		HttpHeaders	responseHeaders	= new HttpHeaders();
		responseHeaders.setContentType(MediaType.APPLICATION_JSON);
		boolean				exceptionOccured	= false;
		Map<String, String>	authParams			= new HashMap<>();
		authParams.put((AppConstants.ATTRIBUTES_COGNITO_USERNAME).toUpperCase(), userAuthRequest.getUserName());
		authParams.put((AppConstants.ATTRIBUTES_COGNITO_USERPASS).toUpperCase(), userAuthRequest.getUserPassword());
		AdminInitiateAuthResult authenticationResult = new AdminInitiateAuthResult();
		try {
			AdminInitiateAuthRequest initiateAuthRequest = new AdminInitiateAuthRequest().withAuthFlow(AuthFlowType.ADMIN_NO_SRP_AUTH).withAuthParameters(authParams).withClientId(cognitoClientId).withUserPoolId(cognitoPoolId);
			authenticationResult = loccognitoClient.adminInitiateAuth(initiateAuthRequest);
			userAuthRequest.setCognitoSession(authenticationResult.getSession());
		} catch (NotAuthorizedException notAuthorizedException) {
			exceptionOccured = true;
			logger.error(notAuthorizedException.getMessage());
			out.put(AppConstants.ERROR_AUTHENTICATEUSER_USERNOTAUTHORIZED, AppConstants.ERROR_AUTHENTICATEUSER_USERNOTAUTHORIZED_MESSAGE);
			out.put(AppConstants.APPLICATION_CONSTANTS_EXCEPTION, notAuthorizedException.getMessage());
			status = HttpStatus.UNAUTHORIZED;
		} catch (UserNotFoundException userNotFoundException) {
			exceptionOccured = true;
			logger.error(userNotFoundException.getMessage());
			out.put(AppConstants.ERROR_AUTHENTICATEUSER_USERNOTFOUND, AppConstants.ERROR_AUTHENTICATEUSER_USERNOTFOUND_MESSAGE);
			out.put(AppConstants.APPLICATION_CONSTANTS_EXCEPTION, userNotFoundException.getMessage());
			status = HttpStatus.UNAUTHORIZED;
		} catch (PasswordResetRequiredException passwordResetRequiredException) {
			exceptionOccured = true;
			logger.error(passwordResetRequiredException.getMessage());
			out.put(AppConstants.ERROR_AUTHENTICATEUSER_USERPASSRESETREQUIRED, AppConstants.ERROR_AUTHENTICATEUSER_USERPASSRESETREQUIRED_MESSAGE);
			out.put(AppConstants.APPLICATION_CONSTANTS_EXCEPTION, passwordResetRequiredException.getMessage());
			status = HttpStatus.LOCKED;
		} catch (UserNotConfirmedException userNotConfirmedException) {
			exceptionOccured = true;
			logger.error(userNotConfirmedException.getMessage());
			out.put(AppConstants.ERROR_AUTHENTICATEUSER_USERNOTCONFIRMED, AppConstants.ERROR_AUTHENTICATEUSER_USERNOTCONFIRMED_MESSAGE);
			out.put(AppConstants.APPLICATION_CONSTANTS_EXCEPTION, userNotConfirmedException.getMessage());
			status = HttpStatus.LOCKED;
		} catch (Exception exception) {
			exceptionOccured = true;
			logger.error(exception.getMessage());
			out.put(AppConstants.ERROR_AUTHENTICATEUSER_UNKNOWNEXCEPTION, AppConstants.ERROR_AUTHENTICATEUSER_UNKNOWNEXCEPTION_MESSAGE);
			out.put(AppConstants.APPLICATION_CONSTANTS_EXCEPTION, exception.getMessage());
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		String challengeName = authenticationResult.getChallengeName();
		try {
			if (!exceptionOccured && StringUtils.isNullOrEmpty(challengeName)) {
				userAuthRequest = cognitoHelper.getUserDetailsAfterAuthentication(userAuthRequest, authenticationResult, cognitoPoolId);
				status = HttpStatus.OK;
			} else if (ChallengeNameType.NEW_PASSWORD_REQUIRED.name().equals(challengeName)) {
				userAuthRequest.setUserConfirmed(false);
				status = HttpStatus.ACCEPTED;
			}
		} catch (NullPointerException npe) {
			logger.error(npe.getMessage());
		}
		if (!exceptionOccured) {
			return new ResponseEntity<>(userAuthRequest, HttpStatus.OK);
		}else {
		return new ResponseEntity<>(out.toString(), responseHeaders, status);
		}
	}

	public ResponseEntity<Object> confirmNewUser(AuthenticationRequest userAuthRequest, String cognitoPoolId, String cognitoClientId) {
		HttpStatus	status			= HttpStatus.NOT_IMPLEMENTED;
		HttpHeaders	responseHeaders	= new HttpHeaders();
		boolean		exceptionOccured			= false;
		HttpHeaders	httpHeaders		= new HttpHeaders();
		JSONObject	out				= new JSONObject();
		if (!userAuthRequest.getNewPassword().equals(userAuthRequest.getConfirmNewPassword())) {
			return new ResponseEntity<>("!!! New Password and Confirmed New Password should match !!!", httpHeaders, HttpStatus.NOT_ACCEPTABLE);
		}
		Map<String, String> authParams = new HashMap<>();
		authParams.put(AppConstants.ATTRIBUTES_COGNITO_USERNAME.toUpperCase(), userAuthRequest.getUserName());
		authParams.put(AppConstants.ATTRIBUTES_COGNITO_USERPASS.toUpperCase(), userAuthRequest.getTempPassword());
		Map<String, String> challengeResponses = new HashMap<>();
		challengeResponses.put(AppConstants.ATTRIBUTES_COGNITO_USERNAME.toUpperCase(), userAuthRequest.getUserName());
		challengeResponses.put(AppConstants.ATTRIBUTES_COGNITO_USERPASS.toUpperCase(), userAuthRequest.getTempPassword());
		challengeResponses.put(AppConstants.ATTRIBUTES_COGNITO_USERNEWPASS.toUpperCase(), userAuthRequest.getNewPassword());
		AdminRespondToAuthChallengeRequest	finalRequest		= new AdminRespondToAuthChallengeRequest().withChallengeName(ChallengeNameType.NEW_PASSWORD_REQUIRED)
				.withChallengeResponses(challengeResponses).withClientId(cognitoClientId).withUserPoolId(cognitoPoolId).withSession(userAuthRequest.getCognitoSession());
		AdminRespondToAuthChallengeResult	challengeResponse	= null;
		try {
			challengeResponse = cognitoClient.adminRespondToAuthChallenge(finalRequest);
		} catch (Exception e) {
			exceptionOccured = true;
			if (e.getMessage().contains("Invalid session")) {
				out.put(AppConstants.APPLICATION_CONSTANTS_ERRORMESSAGETITLE_XXX, "Invalid session for the user");
				status = HttpStatus.UNAUTHORIZED;
			} else {
				out.put(AppConstants.APPLICATION_CONSTANTS_ERRORMESSAGETITLE_XXX, e.getMessage().trim());
				status = HttpStatus.BAD_REQUEST;
			}
		}
		if (!exceptionOccured) {
			CognitoUserVO		userVo	= new CognitoUserVO();
			Map<String, String>	userMap;
			userVo.setUserName(userAuthRequest.getUserName());
			userVo.setToken(challengeResponse.getAuthenticationResult().getAccessToken());
			AdminListGroupsForUserRequest requestId = new AdminListGroupsForUserRequest();
			requestId.withUserPoolId(cognitoPoolId);
			requestId.withUsername(userAuthRequest.getUserName());
			GetUserRequest userReq = new GetUserRequest();
			userReq.withAccessToken(userVo.getToken());
			GetUserResult					userRes	= null;
			AdminListGroupsForUserResult	adminre	= null;
			try {
				adminre	= cognitoClient.adminListGroupsForUser(requestId);
				userRes	= cognitoClient.getUser(userReq);
				userVo.setGroups(adminre.getGroups());
				List<AttributeType> userAtts = userRes.getUserAttributes();
				userMap = ApplicationUtility.convertCognitoAttributesToMap(userAtts);
				userVo.setEmail(userMap.get(AppConstants.ATTRIBUTES_COGNITO_EMAIL));
				userVo.setFirstName(userMap.get(AppConstants.ATTRIBUTES_COGNITO_GIVENNAME));
				userVo.setLastName(userMap.get(AppConstants.ATTRIBUTES_COGNITO_FAMILYNAME));
			} catch (UserNotFoundException e) {
				throw new NotImplementedException("TODO: User not found on getUser or getGroups \nAWS Cognito Error: " + e.getMessage().trim());
			} catch (NotAuthorizedException e) {
				exceptionOccured = true;
				out.put(AppConstants.APPLICATION_CONSTANTS_ERRORMESSAGETITLE_XXX, AppConstants.APPLICATION_CONSTANTS_ERRORMESSAGEMESSAGE_XXX);
				status = HttpStatus.UNAUTHORIZED;
			} catch (Exception e) {
				exceptionOccured = true;
				out.put(AppConstants.APPLICATION_CONSTANTS_ERRORMESSAGETITLE_XXX, e.getMessage().trim());
				status = HttpStatus.NOT_ACCEPTABLE;
			}
			if (!exceptionOccured) {
				return new ResponseEntity<>(userVo, HttpStatus.OK);
			}
		}
		return new ResponseEntity<>(out.toString(), responseHeaders, status);
	}

	public ResponseEntity<Object> addUser(AuthenticationRequest userAuthRequest, String cognitoPoolId) {
		List<GroupType>						userGroups		= userAuthRequest.getUserGroups();
		AdminCreateUserRequest				cognitoRequest	= new AdminCreateUserRequest().withUserPoolId(cognitoPoolId).withUsername(userAuthRequest.getUserName().toLowerCase())
				.withUserAttributes(new AttributeType().withName(AppConstants.ATTRIBUTES_COGNITO_EMAIL).withValue(userAuthRequest.getUserEmail().toLowerCase()),
						new AttributeType().withName(AppConstants.ATTRIBUTES_COGNITO_EMAILVARIFICATIONGFLAG).withValue(AppConstants.APPLICATION_CONSTANTS_TRUE_FLAG),
						new AttributeType().withName(AppConstants.ATTRIBUTES_COGNITO_FAMILYNAME).withValue(userAuthRequest.getLastName().toLowerCase()),
						new AttributeType().withName(AppConstants.ATTRIBUTES_COGNITO_GIVENNAME).withValue(userAuthRequest.getFirstName().toLowerCase()))
				.withDesiredDeliveryMediums(DeliveryMediumType.EMAIL).withForceAliasCreation(Boolean.FALSE);
		List<AdminAddUserToGroupRequest>	roleRequests	= new ArrayList<>();
		for (int i = 0; i < userGroups.size(); i++) {
			String						role	= userGroups.get(i).getGroupName();
			AdminAddUserToGroupRequest	request	= new AdminAddUserToGroupRequest().withUserPoolId(cognitoPoolId).withUsername(userAuthRequest.getUserName().toLowerCase())
					.withGroupName(role);
			roleRequests.add(request);
		}
		try {
			cognitoClient.adminCreateUser(cognitoRequest);
		} catch (UsernameExistsException e) {
			if (e.getMessage().contains(AppConstants.ATTRIBUTES_COGNITO_EMAIL)) {
				throw new NotImplementedException("TODO: User with this email already exists\nAWS Cognito Error: " + e.getMessage().trim());
			} else {
				throw new NotImplementedException("TODO: Username exists " + e.getMessage().trim());
			}
		} catch (Exception e) {
			throw new NotImplementedException("TODO: Other Error on CreateUser \nAWS Cognito Error: " + e.getMessage().trim());
		}
		try {
			for (AdminAddUserToGroupRequest request : roleRequests) {
				cognitoClient.adminAddUserToGroup(request);
			}
		} catch (UserNotFoundException e) {
			throw new NotImplementedException("TODO: User was not found \nAWS Cognito Error: " + e.getMessage().trim());
		} catch (Exception e) {
			throw new NotImplementedException("TODO: Other Error on AddUserToGroup \nAWS Cognito Error: " + e.getMessage().trim());
		}
		ResponseEntity<Object> lstResult = getUsers(cognitoPoolId);
		return new ResponseEntity<>(lstResult.getBody(), HttpStatus.OK);
	}
	

	public ResponseEntity<Object> getUsers(String cognitoPoolId) {
		ListUsersRequest lstUserReq = new ListUsersRequest();
		lstUserReq.withUserPoolId(cognitoPoolId);
		ListUsersResult lstResult;
		try {
			lstResult = cognitoClient.listUsers(lstUserReq);
		} catch (Exception e) {
			JSONObject out = new JSONObject();
			logger.debug("Failed to Get List of Users from Cognito");
			try {
				out.put(AppConstants.APPLICATION_CONSTANTS_ERRORMESSAGETITLE_XXX, AppConstants.ENDPOINT_GETUSERS_ALL + " Failed: " + e.getMessage().trim());
			} catch (JSONException e1) {
				logger.error(AppConstants.ENDPOINT_GETUSERS_ALL, e);
			}
			return new ResponseEntity<>(e.getMessage().trim(), HttpStatus.UNAUTHORIZED);
		}
		return new ResponseEntity<>(lstResult, HttpStatus.OK);
	}
	
	/**
	 * Retrieve groups/roles for a specific user
	 * 
	 * @param userName User Name (required)
	 * @return JSONArray with group information
	 */
	public JSONArray getGroupsForAUser(String userName, String cognitoPoolId) {
		JSONArray						groupsForAUser	= new JSONArray();
		AdminListGroupsForUserRequest	requestUserGrop	= new AdminListGroupsForUserRequest();
		requestUserGrop.withUserPoolId(cognitoPoolId);
		requestUserGrop.withUsername(userName);
		AdminListGroupsForUserResult userGroups = cognitoClient.adminListGroupsForUser(requestUserGrop);
		if (userGroups != null) {
			for (GroupType group : userGroups.getGroups()) {
				groupsForAUser.put(group.getGroupName());
			}
		}
		return groupsForAUser;
	}
}
