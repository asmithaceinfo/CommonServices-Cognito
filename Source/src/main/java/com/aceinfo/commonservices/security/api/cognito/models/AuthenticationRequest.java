package com.aceinfo.commonservices.security.api.cognito.models;

import java.io.Serializable;
import java.util.List;

import com.amazonaws.services.cognitoidp.model.GroupType;

@SuppressWarnings("serial")
public class AuthenticationRequest implements Serializable{
	
	private String firstName;
	private String lastName;
	private String userName;
	private String userPassword;
	private String tempPassword;
	private String newPassword;
	private String confirmNewPassword;
	private String cognitoSession;
	private String userEmail;
	
	private boolean userEmailVarifiedFlag = true;
	
	private boolean userConfirmed = true;
	
	private List<GroupType> userGroups;
	
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getUserPassword() {
		return userPassword;
	}
	public void setUserPassword(String userPassword) {
		this.userPassword = userPassword;
	}
	public String getTempPassword() {
		return tempPassword;
	}
	public void setTempPassword(String tempPassword) {
		this.tempPassword = tempPassword;
	}
	public String getNewPassword() {
		return newPassword;
	}
	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}
	public String getConfirmNewPassword() {
		return confirmNewPassword;
	}
	public void setConfirmNewPassword(String confirmNewPassword) {
		this.confirmNewPassword = confirmNewPassword;
	}
	public String getCognitoSession() {
		return cognitoSession;
	}
	public void setCognitoSession(String cognitoSession) {
		this.cognitoSession = cognitoSession;
	}
	public String getUserEmail() {
		return userEmail;
	}
	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}
	public boolean isUserEmailVarifiedFlag() {
		return userEmailVarifiedFlag;
	}
	public void setUserEmailVarifiedFlag(boolean userEmailVarifiedFlag) {
		this.userEmailVarifiedFlag = userEmailVarifiedFlag;
	}
	public boolean isUserConfirmed() {
		return userConfirmed;
	}
	public void setUserConfirmed(boolean userConfirmed) {
		this.userConfirmed = userConfirmed;
	}
	
	public List<GroupType> getUserGroups() {
		return userGroups;
	}
	public void setUserGroups(List<GroupType> userGroups) {
		this.userGroups = userGroups;
	}
	@Override
	public String toString() {
		return String.format(
				"AuthenticationRequest [firstName=%s, lastName=%s, userName=%s, cognitoSession=%s, userEmail=%s, userEmailVarifiedFlag=%s, userConfirmed=%s, userGroups=%s]",
				firstName, lastName, userName, cognitoSession, userEmail, userEmailVarifiedFlag, userConfirmed, userGroups);
	}


	
}
