package com.aceinfo.commonservices.security.api.cognito.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.NotImplementedException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.aceinfo.commonservices.security.api.cognito.models.AuthenticationRequest;
import com.aceinfo.commonservices.security.api.cognito.services.CognitoService;
import com.aceinfo.commonservices.security.api.cognito.utilities.AppConstants;
import com.aceinfo.commonservices.security.api.cognito.utilities.ApplicationUtility;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClientBuilder;
import com.amazonaws.services.cognitoidp.model.AdminAddUserToGroupRequest;
import com.amazonaws.services.cognitoidp.model.AdminCreateUserRequest;
import com.amazonaws.services.cognitoidp.model.AdminDeleteUserRequest;
import com.amazonaws.services.cognitoidp.model.AdminDisableUserRequest;
import com.amazonaws.services.cognitoidp.model.AdminEnableUserRequest;
import com.amazonaws.services.cognitoidp.model.AdminListGroupsForUserRequest;
import com.amazonaws.services.cognitoidp.model.AdminListGroupsForUserResult;
import com.amazonaws.services.cognitoidp.model.AdminRemoveUserFromGroupRequest;
import com.amazonaws.services.cognitoidp.model.AdminUpdateUserAttributesRequest;
import com.amazonaws.services.cognitoidp.model.AliasExistsException;
import com.amazonaws.services.cognitoidp.model.AttributeType;
import com.amazonaws.services.cognitoidp.model.ConfirmForgotPasswordRequest;
import com.amazonaws.services.cognitoidp.model.ConfirmForgotPasswordResult;
import com.amazonaws.services.cognitoidp.model.DeliveryMediumType;
import com.amazonaws.services.cognitoidp.model.ForgotPasswordRequest;
import com.amazonaws.services.cognitoidp.model.ForgotPasswordResult;
import com.amazonaws.services.cognitoidp.model.GroupType;
import com.amazonaws.services.cognitoidp.model.LimitExceededException;
import com.amazonaws.services.cognitoidp.model.ListGroupsRequest;
import com.amazonaws.services.cognitoidp.model.ListGroupsResult;
import com.amazonaws.services.cognitoidp.model.UserNotFoundException;
import com.amazonaws.services.cognitoidp.model.UsernameExistsException;

@Controller
public class CognitoController {
	@Value("${cognito_pool_id}")
	private String								cognitoPoolId;
	@Value("${cognito_client_id}")
	private String								cognitoClientId;
	private final CognitoService				cognitoService;
	private static AWSCognitoIdentityProvider	cognitoClient	= AWSCognitoIdentityProviderClientBuilder.defaultClient();

	@Autowired
	public CognitoController(CognitoService cognitoService) {
		this.cognitoService = cognitoService;
	}

	@PostMapping(path = AppConstants.ENDPOINT_VALIDATETOKEN)
	public ResponseEntity<Object> validateUserSession(@RequestBody AuthenticationRequest authRequest) {
		return cognitoService.validateUserSession(authRequest, cognitoPoolId, cognitoClientId);
	}

	@PostMapping(path = AppConstants.ENDPOINT_AUTHENTICATE)
	public ResponseEntity<Object> authenticateUser(@RequestBody AuthenticationRequest authRequest) {
		boolean validRequest = ApplicationUtility.validateAuthenticationRequest(authRequest);
		if (validRequest)
			return cognitoService.authenticateUser(authRequest, cognitoPoolId, cognitoClientId);
		else {
			return new ResponseEntity<>(AppConstants.APPLICATION_CONSTANTS_ERROR_INVALIDLOGINREQUEST + " UserLogin and Password are needed.", HttpStatus.BAD_REQUEST);
		}
	}

	@PostMapping(path = AppConstants.ENDPOINT_CONFIRMN_NEWUSER)
	public ResponseEntity<Object> confirmNewUser(@RequestBody AuthenticationRequest authRequest) {
		return cognitoService.confirmNewUser(authRequest, cognitoPoolId, cognitoClientId);
	}

	@PostMapping(path = AppConstants.ENDPOINT_ADDUSER)
	public ResponseEntity<Object> addUser(ModelMap model, @RequestBody Object o) {
		JSONObject							j				= new JSONObject((Map<?, ?>) o);
		String								userName		= j.getString(AppConstants.ATTRIBUTES_COGNITO_USERNAME).toLowerCase();
		String								emailAddress	= j.getString(AppConstants.ATTRIBUTES_COGNITO_EMAIL).toLowerCase();
		String								lastname		= j.getString("lastname");
		String								firstname		= j.getString("firstname");
		JSONArray							groups			= j.getJSONArray("roles");
		AdminCreateUserRequest				cognitoRequest	= new AdminCreateUserRequest().withUserPoolId(cognitoPoolId).withUsername(userName)
				.withUserAttributes(new AttributeType().withName(AppConstants.ATTRIBUTES_COGNITO_EMAIL).withValue(emailAddress),
						new AttributeType().withName("email_verified").withValue("true"),
						new AttributeType().withName(AppConstants.ATTRIBUTES_COGNITO_FAMILYNAME).withValue(lastname),
						new AttributeType().withName(AppConstants.ATTRIBUTES_COGNITO_GIVENNAME).withValue(firstname))
				.withDesiredDeliveryMediums(DeliveryMediumType.EMAIL).withForceAliasCreation(Boolean.FALSE);
		List<AdminAddUserToGroupRequest>	roleRequests	= new ArrayList<>();
		for (int i = 0; i < groups.length(); i++) {
			String						role	= groups.getString(i);
			AdminAddUserToGroupRequest	request	= new AdminAddUserToGroupRequest().withUserPoolId(cognitoPoolId).withUsername(userName).withGroupName(role);
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
			throw new NotImplementedException("Other Error on CreateUser \nAWS Cognito Error: " + e.getMessage().trim());
		}
		try {
			for (AdminAddUserToGroupRequest request : roleRequests) {
				cognitoClient.adminAddUserToGroup(request);
			}
		} catch (UserNotFoundException e) {
			throw new NotImplementedException("User was not found \nAWS Cognito Error: " + e.getMessage().trim());
		} catch (Exception e) {
			throw new NotImplementedException("Other Error on AddUserToGroup \nAWS Cognito Error: " + e.getMessage().trim());
		}
		ResponseEntity<Object> lstResult = getUsers();
		return new ResponseEntity<>(lstResult.getBody(), HttpStatus.OK);
	}

	@DeleteMapping(path = AppConstants.ENDPOINT_DELETEUSER)
	public ResponseEntity<Object> deleteUser(ModelMap model, @RequestBody Object o) {
		JSONObject				j				= new JSONObject((Map<?, ?>) o);
		String					userName		= j.getString(AppConstants.ATTRIBUTES_COGNITO_USERNAME).toLowerCase();
		AdminDeleteUserRequest	cognitoRequest	= new AdminDeleteUserRequest().withUserPoolId(cognitoPoolId).withUsername(userName);
		try {
			cognitoClient.adminDeleteUser(cognitoRequest);
		} catch (Exception e) {
			return new ResponseEntity<>(e.getMessage().trim(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>("User Successfully Deleted", HttpStatus.OK);
	}

	@PostMapping(path = AppConstants.ENDPOINT_DISABLEUSER)
	public ResponseEntity<Object> disableUser(ModelMap model, @RequestBody Object o) {
		JSONObject				j				= new JSONObject((Map<?, ?>) o);
		String					userName		= j.getString(AppConstants.ATTRIBUTES_COGNITO_USERNAME).toLowerCase();
		AdminDisableUserRequest	cognitoRequest	= new AdminDisableUserRequest().withUserPoolId(cognitoPoolId).withUsername(userName);
		try {
			cognitoClient.adminDisableUser(cognitoRequest);
		} catch (UserNotFoundException e) {
			throw new NotImplementedException("TODO: User was not found \nAWS Cognito Error: " + e.getMessage().trim());
		} catch (Exception e) {
			throw new NotImplementedException("TODO: Other Error on DeleteUser \nAWS Cognito Error: " + e.getMessage().trim());
		}
		return new ResponseEntity<>("User Successfully Disabled", HttpStatus.OK);
	}

	@PostMapping(path = AppConstants.ENDPOINT_ENABLEUSER)
	public ResponseEntity<Object> enableUser(ModelMap model, @RequestBody Object o) {
		JSONObject				j				= new JSONObject((Map<?, ?>) o);
		String					userName		= j.getString(AppConstants.ATTRIBUTES_COGNITO_USERNAME).toLowerCase();
		AdminEnableUserRequest	cognitoRequest	= new AdminEnableUserRequest().withUserPoolId(cognitoPoolId).withUsername(userName);
		try {
			cognitoClient.adminEnableUser(cognitoRequest);
		} catch (UserNotFoundException e) {
			throw new NotImplementedException("TODO: User was not found \nAWS Cognito Error: " + e.getMessage().trim());
		} catch (Exception e) {
			throw new NotImplementedException("TODO: Other Error on DeleteUser \nAWS Cognito Error: " + e.getMessage().trim());
		}
		return new ResponseEntity<>("User Enabled", HttpStatus.OK);
	}

	@PostMapping(path = AppConstants.ENDPOINT_UPDATEUSER)
	public ResponseEntity<Object> updateUser(ModelMap model, @RequestBody Object o) {
		JSONObject							j				= new JSONObject((Map<?, ?>) o);
		String								userName		= j.getString(AppConstants.ATTRIBUTES_COGNITO_USERNAME).toLowerCase();
		String								emailAddress	= j.getString("email");
		String								lastname		= j.getString("lastname");
		String								firstname		= j.getString("firstname");
		AdminUpdateUserAttributesRequest	cognitoRequest	= new AdminUpdateUserAttributesRequest();
		cognitoRequest.withUserPoolId(cognitoPoolId).withUsername(userName).withUserAttributes(new AttributeType().withName("email").withValue(emailAddress),
				new AttributeType().withName("email_verified").withValue("true"), new AttributeType().withName(AppConstants.ATTRIBUTES_COGNITO_FAMILYNAME).withValue(lastname),
				new AttributeType().withName(AppConstants.ATTRIBUTES_COGNITO_GIVENNAME).withValue(firstname));
		try {
			cognitoClient.adminUpdateUserAttributes(cognitoRequest);
		} catch (AliasExistsException e) {
			throw new NotImplementedException("TODO: UpdateUserAttributes failed because Email or phone exists on other account \nAWS Cognito Error: " + e.getMessage().trim());
		} catch (UserNotFoundException e) {
			throw new NotImplementedException("TODO: UpdateUserAttributes failed because user was not found \nAWS Cognito Error: " + e.getMessage().trim());
		} catch (Exception e) {
			return new ResponseEntity<>(e.getMessage().trim(), HttpStatus.UNAUTHORIZED);
		}
		return new ResponseEntity<>("User Updated", HttpStatus.OK);
	}

	/**
	 * Get Groups from Cognito Pool
	 */
	@GetMapping("/getGroups")
	public ResponseEntity<Object> getGroups() {
		ListGroupsRequest lstGroup = new ListGroupsRequest();
		lstGroup.withUserPoolId(cognitoPoolId);
		ListGroupsResult lstRest = null;
		try {
			lstRest = cognitoClient.listGroups(lstGroup);
		} catch (Exception e) {
			return new ResponseEntity<>(e.getMessage().trim(), HttpStatus.UNAUTHORIZED);
		}
		return new ResponseEntity<>(lstRest, HttpStatus.OK);
	}

	/**
	 * getUsers from aws cognito pool
	 * 
	 * @return
	 */
	@GetMapping(path = AppConstants.ENDPOINT_GETUSERS_ALL)
	public ResponseEntity<Object> getUsers() {
		return cognitoService.getUsers(cognitoPoolId);
	}

	/**
	 * Get User Groups from Cognito Pool
	 */
	@GetMapping(path = AppConstants.ENDPOINT_GETGROUPS_PERUSER)
	public ResponseEntity<Object> getUserGroups(@PathVariable(AppConstants.ATTRIBUTES_COGNITO_USERNAME) String username) {
		JSONArray	groups			= cognitoService.getGroupsForAUser(username, cognitoPoolId);
		HttpHeaders	responseHeaders	= new HttpHeaders();
		responseHeaders.setContentType(MediaType.APPLICATION_JSON);
		JSONObject groupObject = new JSONObject();
		groupObject.put("groups", groups);
		return new ResponseEntity<>(groupObject.toString(), responseHeaders, HttpStatus.OK);
	}

	/**
	 * Confirm a users code and reset their password.
	 */
	@PostMapping(path = "/confirmPassword/{username}/{confirmationCode}/{newPassword}")
	public ResponseEntity<Object> resetPasswordConfirm(@PathVariable("username") String username, @PathVariable("confirmationCode") String confirmationCode, @PathVariable("newPassword") String newPassword) {
		ConfirmForgotPasswordRequest	confPWReq		= new ConfirmForgotPasswordRequest().withUsername(username).withClientId(cognitoClientId)
				.withConfirmationCode(confirmationCode).withPassword(newPassword);
		ConfirmForgotPasswordResult		confPWResult	= cognitoClient.confirmForgotPassword(confPWReq);
		return new ResponseEntity<>(confPWResult, HttpStatus.OK);
	}

	/**
	 * Send reset password code to email for User
	 */
	@PostMapping(path = "/forgotPassword/{username}")
	public ResponseEntity<Object> resetPassword(@PathVariable("username") String username) {
		ForgotPasswordRequest	fpRequest	= new ForgotPasswordRequest().withUsername(username).withClientId(cognitoClientId);
		ForgotPasswordResult	fpResult;
		try {
			fpResult = cognitoClient.forgotPassword(fpRequest);
		} catch (LimitExceededException e) {
			return new ResponseEntity<>("{}", HttpStatus.FORBIDDEN);
		}
		return new ResponseEntity<>(fpResult, HttpStatus.OK);
	}

	@PostMapping(path = AppConstants.ENDPOINT_UPDATEUSERGROUPS)
	public ResponseEntity<Object> updateUserGroups(ModelMap model, @RequestBody Object o) {
		JSONObject						j			= new JSONObject((Map<?, ?>) o);
		String							userName	= j.getString("username").toLowerCase();
		JSONArray						groups		= j.getJSONArray("groups");
		AdminListGroupsForUserRequest	groupsReq	= new AdminListGroupsForUserRequest();
		groupsReq.withUsername(userName);
		groupsReq.withUserPoolId(cognitoPoolId);
		AdminListGroupsForUserResult groupsRes = null;
		try {
			groupsRes = cognitoClient.adminListGroupsForUser(groupsReq);
		} catch (Exception e) {
			throw new NotImplementedException("TODO: getUserGroups failed because Unknown Error \nAWS Cognito Error: " + e.getMessage().trim());
		}
		List<GroupType>	oldGroups	= groupsRes.getGroups();
		List<String>	og			= new ArrayList<>();
		List<String>	ng			= new ArrayList<>();
		// find groups to delete
		for (int i = 0; i < oldGroups.size(); i++) {
			String name = oldGroups.get(i).getGroupName();
			og.add(name);
		}
		for (int i = 0; i < groups.length(); i++) {
			ng.add(groups.getString(i));
		}
		for (int i = 0; i < og.size(); i++) {
			if (!ng.contains(og.get(i))) {
				AdminRemoveUserFromGroupRequest removeGroupRequest = new AdminRemoveUserFromGroupRequest();
				removeGroupRequest.withGroupName(og.get(i)).withUsername(userName).withUserPoolId(cognitoPoolId);
				try {
					cognitoClient.adminRemoveUserFromGroup(removeGroupRequest);
				} catch (Exception e) {
					throw new NotImplementedException("TODO: removeFromGroup failed because Unknown Error \nAWS Cognito Error: " + e.getMessage().trim());
				}
			}
		}
		for (int i = 0; i < ng.size(); i++) {
			if (!og.contains(ng.get(i))) {
				AdminAddUserToGroupRequest addGroupRequest = new AdminAddUserToGroupRequest();
				addGroupRequest.withGroupName(ng.get(i)).withUsername(userName).withUserPoolId(cognitoPoolId);
				try {
					cognitoClient.adminAddUserToGroup(addGroupRequest);
				} catch (Exception e) {
					throw new NotImplementedException("TODO: AddtoGroups failed because Unknown Error \nAWS Cognito Error: " + e.getMessage().trim());
				}
			}
		}
		return new ResponseEntity<>("{}", HttpStatus.FORBIDDEN);
	}
}
