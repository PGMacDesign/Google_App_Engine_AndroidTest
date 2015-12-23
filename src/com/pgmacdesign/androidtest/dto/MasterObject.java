package com.pgmacdesign.androidtest.dto;

import java.util.List;

public class MasterObject {
    private List<Employee> employees;
    private List<User> users;
    private String message;
    
    

    public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public List<Employee> getEmployees() {
        return employees;
    }

    public void setEmployees(List<Employee> items) {
        this.employees = items;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }
}
