package com.pgmacdesign.androidtest.dto;

import java.util.Date;

import com.google.appengine.api.datastore.Blob;

public class Employee extends MasterObject {
	
	private Long id;
	private String sessionId;
	private Blob blobImage;
	private byte[] picture;
	private String firstName;
	private String lastName;
	private Date hireDate;
	private boolean attendedHrTraining;
	private String message;
	
	
	
	public String getSessionId() {
		return sessionId;
	}
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public Blob getBlob() {
		return blobImage;
	}
	public void setBlob(Blob blob) {
		this.blobImage = blob;
	}
	public byte[] getPicture() {
		return picture;
	}
	public void setPicture(byte[] picture) {
		this.picture = picture;
	}
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
	public Date getHireDate() {
		return hireDate;
	}
	public void setHireDate(Date hireDate) {
		this.hireDate = hireDate;
	}
	public boolean isAttendedHrTraining() {
		return attendedHrTraining;
	}
	public void setAttendedHrTraining(boolean attendedHrTraining) {
		this.attendedHrTraining = attendedHrTraining;
	}
	
	
}
