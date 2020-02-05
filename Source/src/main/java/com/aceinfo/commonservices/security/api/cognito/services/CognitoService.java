package com.aceinfo.commonservices.security.api.cognito.services;

import org.json.JSONArray;
import org.springframework.http.ResponseEntity;

import com.aceinfo.commonservices.security.api.cognito.models.AuthenticationRequest;

public interface CognitoService {
	public ResponseEntity<Object> validateUserSession(AuthenticationRequest userAuthRequest, String cognitoPoolId, String cognitoClientId);	
	public ResponseEntity<Object> authenticateUser(AuthenticationRequest userAuthRequest, String cognitoPoolId, String cognitoClientId, String s, String a, String r);
	public ResponseEntity<Object> getUsers(String cognitoPoolId, String a, String b, String r);
	public Boolean validEmail(String cognitoPoolId, String a, String b, String r, String e);
	public ResponseEntity<Object> confirmNewUser(AuthenticationRequest userAuthRequest, String cognitoPoolId, String cognitoClientId);
	public JSONArray getGroupsForAUser(String userName, String cognitoPoolId);
}
