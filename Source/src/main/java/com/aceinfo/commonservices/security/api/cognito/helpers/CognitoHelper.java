package com.aceinfo.commonservices.security.api.cognito.helpers;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aceinfo.commonservices.security.api.cognito.models.AuthenticationRequest;
import com.aceinfo.commonservices.security.api.cognito.utilities.ApplicationUtility;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClientBuilder;
import com.amazonaws.services.cognitoidp.model.AdminInitiateAuthResult;
import com.amazonaws.services.cognitoidp.model.AdminListGroupsForUserRequest;
import com.amazonaws.services.cognitoidp.model.AdminListGroupsForUserResult;
import com.amazonaws.services.cognitoidp.model.AttributeType;
import com.amazonaws.services.cognitoidp.model.GetUserRequest;
import com.amazonaws.services.cognitoidp.model.GetUserResult;
import com.amazonaws.services.cognitoidp.model.NotAuthorizedException;
import com.amazonaws.services.cognitoidp.model.UserNotFoundException;
import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;

public class CognitoHelper {
	private static AWSCognitoIdentityProvider	cognitoClient	= AWSCognitoIdentityProviderClientBuilder.defaultClient();
	private final Logger						logger			= LoggerFactory.getLogger(this.getClass());

	/**
	 * Validate the OAuth Client ID Claim matches that of the token
	 * 
	 * @param token OAuth Token (Required)
	 * @return true if matches, false otherwise
	 */
	public boolean validateCognitoClient(String token, String cognitoClientId) {
		boolean result = false;
		try {
			DecodedJWT	userSessionToken	= JWT.decode(token);
			Claim		registeredClientID	= userSessionToken.getClaim("client_id");
			if (registeredClientID != null) {
				result = cognitoClientId.equals(registeredClientID.asString());
			}
		} catch (Exception exception) {
			logger.error(exception.getMessage().trim());
		}
		return result;
	}

	public AuthenticationRequest getUserDetailsAfterAuthentication(AuthenticationRequest userAuthRequest, AdminInitiateAuthResult authenticationResult, String cognitoPoolId) {
		Map<String, String>				userMap;
		AdminListGroupsForUserRequest	userGroupsRequestObject		= new AdminListGroupsForUserRequest();
		AdminListGroupsForUserResult	userGroupsResponseObject	= null;
		GetUserRequest					getUserDetailsRequest		= new GetUserRequest();
		GetUserResult					getUserDetailsResult		= null;
		userAuthRequest.setCognitoSession(authenticationResult.getAuthenticationResult().getAccessToken());
		userGroupsRequestObject.withUserPoolId(cognitoPoolId);
		userGroupsRequestObject.withUsername(userAuthRequest.getUserName());
		getUserDetailsRequest.withAccessToken(userAuthRequest.getCognitoSession());
		try {
			userGroupsResponseObject	= cognitoClient.adminListGroupsForUser(userGroupsRequestObject);
			getUserDetailsResult		= cognitoClient.getUser(getUserDetailsRequest);
			userAuthRequest.setUserGroups(userGroupsResponseObject.getGroups());
			List<AttributeType> cognitoUserAttributes = getUserDetailsResult.getUserAttributes();
			userMap = ApplicationUtility.convertCognitoAttributesToMap(cognitoUserAttributes);
			userAuthRequest.setUserEmail(userMap.get("email"));
			userAuthRequest.setFirstName(userMap.get("firstname"));
			userAuthRequest.setLastName(userMap.get("lastname"));
		} catch (UserNotFoundException userNotFoundException) {
			throw userNotFoundException;
		} catch (NotAuthorizedException notAuthorizedException) {
			throw notAuthorizedException;
		} catch (Exception exception) {
			throw exception;
		}
		return userAuthRequest;
	}
}
