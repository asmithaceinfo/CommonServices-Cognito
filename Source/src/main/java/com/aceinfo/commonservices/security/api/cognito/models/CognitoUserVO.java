package com.aceinfo.commonservices.security.api.cognito.models;

import java.util.List;

import com.amazonaws.services.cognitoidp.model.GroupType;

public class CognitoUserVO {
	
	private String userName;
	private String password;
	private List<GroupType> groups;
	private String token;
	private String tempPassInd ="N";
	private String lastName;
	private String firstName;
	private String email;
	private String secondaryEmail;
	private String phoneNumber;
	private String address;

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public List<GroupType> getGroups() {
		return groups;
	}

	public void setGroups(List<GroupType> groups) {
		this.groups = groups;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getTempPassInd() {
		return tempPassInd;
	}

	public void setTempPassInd(String tempPassInd) {
		this.tempPassInd = tempPassInd;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	
	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
	
	public String getSecondaryEmail() {
		return secondaryEmail;
	}

	public void setSecondaryEmail(String secondaryEmail) {
		this.secondaryEmail = secondaryEmail;
	}
	
	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
	
	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}
}
