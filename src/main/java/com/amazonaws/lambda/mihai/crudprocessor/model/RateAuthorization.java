package com.amazonaws.lambda.mihai.crudprocessor.model;

public class RateAuthorization {

	private Boolean authorization;
	private Integer unauthorizedHours;
	
	public RateAuthorization() {}
	
	public RateAuthorization (Boolean authorization, Integer unauthorizedHours) {
		this.authorization = authorization;
		this.unauthorizedHours = unauthorizedHours;
	}
	
	public String toString() {
		return 	"authorization: " + authorization +
				" unauthorizedHours: " + unauthorizedHours;
	}
	
	public Boolean getAuthorization() {
		return authorization;
	}
	public void setAuthorization(Boolean authorization) {
		this.authorization = authorization;
	}
	public Integer getUnauthorizedHours() {
		return unauthorizedHours;
	}
	public void setUnauthorizedHours(Integer unauthorizedHours) {
		this.unauthorizedHours = unauthorizedHours;
	}
	
	
}
