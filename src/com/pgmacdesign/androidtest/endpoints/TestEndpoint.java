package com.pgmacdesign.androidtest.endpoints;

import java.util.Random;
import java.util.logging.Logger;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.appengine.api.datastore.KeyFactory;
import com.pgmacdesign.androidtest.datamanagement.TESTDatastoreManager;
import com.pgmacdesign.androidtest.dto.Employee;
import com.pgmacdesign.androidtest.dto.TestDTO;
import com.pgmacdesign.androidtest.dto.User;
import com.pgmacdesign.androidtest.misc.BCrypt;

/**
 * This class will serve as a tester for different endpoint methods
 * @author pmacdowell
 */
@Api(name = "testendpoint", 
namespace = @ApiNamespace(ownerDomain = "pgmacdesign", ownerName = "pgmacdesign"))
public class TestEndpoint {
	//Initialize the logger for logging purposes
	private static final Logger log = Logger.getLogger(TestEndpoint.class.getName());
	
	@ApiMethod(name = "testReturn", httpMethod = "POST")
	public TestDTO testReturn(TestDTO dto) throws Exception {
		//First make sure that the DTO is not null
		if(dto == null){
			throw new Exception("Error! Null object passed in");
		}
		
		//Next make sure that the String retrieved from the password is not null
		String dtoPassword = dto.getPassword();
		if(dtoPassword == null || dtoPassword.equalsIgnoreCase("")){
			throw new Exception("Error! Password is empty");
		}
		
		//Lastly, compare to some hard-coded password to test
		if(dtoPassword.equalsIgnoreCase("password123")){
			//If the password matches what you want it do, build an object and return it
			dto.setAge(30);
			dto.setFirstName("Patrick");
			dto.setLastName("MacDowell");
			dto.setMessage("Congrats! You did it right!");
			return dto;
		} else {
			//If the password does not match, return something else
			dto.setMessage("Password is incorrect!");
			return dto;
		}
	}
	
	@ApiMethod(name = "testInputData", httpMethod = "POST")
	public void testInputData() throws Exception {
		try {
			TESTDatastoreManager.doStuff();
		} catch (Exception e){
			e.printStackTrace();
			throw new Exception("Oh no! An error occurred");
		}
	}
	
	@ApiMethod(name = "testRetrieveData", httpMethod = "POST")
	public Employee testRetrieveData(Employee emp) throws Exception {
		log.warning("log statement here. WARNING level");
		log.severe("log statement here. SEVERE level");
		
		//Check params first
		if(emp == null){
			throw new Exception("No object passed");
		}
		String firstName = emp.getFirstName();
		if(firstName == null || firstName.isEmpty()){
			throw new Exception("No first name passed");
		}
		//Once checked, query with first name
		try {
			emp = TESTDatastoreManager.queryStuff(firstName);
			if (emp != null){
				return emp;
			} else {
				throw new Exception("Nobody here by that name! Sorry!");
			}
		} catch (Exception e){
			e.printStackTrace();
			throw new Exception(e.toString());
		}
	}
	
	@ApiMethod(name = "testDeleteData", httpMethod = "POST")
	public void testDeleteData(Employee emp) throws Exception {
		//Check params first
		if(emp == null){
			throw new Exception("No object passed");
		}
		log.warning("TESTEndpoint Line " + 95);
		String firstName = emp.getFirstName();
		if(firstName == null || firstName.isEmpty()){
			throw new Exception("No first name passed");
		}
		//Once checked, query with first name
		try {
			emp = TESTDatastoreManager.queryStuff(firstName);
			if (emp != null){
				boolean bool = TESTDatastoreManager.deleteStuff(emp);
				if(bool){
					//Deleted! Huzzah!
				} else {
					throw new Exception("Could not delete Employee");
				}
			} else {
				throw new Exception("Nobody here by that name! Sorry!");
			}
		} catch (Exception e){
			e.printStackTrace();
			throw new Exception("Oh no! An error occurred");
		}
	}
	
	@ApiMethod(name = "validateSession", httpMethod = "POST")
	public User validateSession(User dto) throws Exception {
		if(dto == null){
			dto.setMessage("Please pass a valid User object");
			return dto;
		}
		String sessionId = null;
		sessionId = dto.getSessionId();
		if(sessionId == null){
			dto.setMessage("Please pass a valid sessionId");
			return dto;
		}
		if(TESTDatastoreManager.validateSession(sessionId)){
			dto.setMessage("Success!");
			return dto;
		} else {
			dto.setMessage("Failure!");
			return dto;
		}
	}
	
	@ApiMethod(name = "storeUserData", httpMethod = "POST")
	public User storeUserData(User dto) throws Exception {
		//Check params first
		if(dto == null){
			throw new Exception("No object passed");
		}
		
		String firstName = dto.getFirstName();
		String lastName = dto.getLastName();
		String password = dto.getPassword();
		
		if(firstName == null || lastName == null){
			throw new Exception ("Please enter a first and last name");
		}
		if(firstName.isEmpty() || lastName.isEmpty()){
			throw new Exception ("Please enter a first and last name");
		}
		if(password == null){
			throw new Exception ("Please enter a password");
		}
		if(password.isEmpty()){
			throw new Exception ("Please enter a password");
		}
		
		/*
		Since generally its good practice to only make the user have their
		password case sensitive, I'm going to make the username (which is
		their first and last name) lowercase.
		 */
		String username = firstName + " " + lastName;
		username = username.toLowerCase();
		
		//Passwords, we will maintain their caps
		String superUsernamePassword = username + password;
		
		//Now that params are good, hash the password
		String hashedPassword = BCrypt.hashpw(superUsernamePassword, BCrypt.gensalt());
		//SET the password here to make sure it matches
		dto.setPassword(hashedPassword);
		log.warning("PW Being Saved: " + hashedPassword);
		//If successful, it stored into the DB, if not, it failed somehow
		if(TESTDatastoreManager.storeUser(dto)){
			//Good practice when returning the same DTO to delete the password
			dto.setPassword("");
			dto.setMessage("User Stored into Database");
		} else {
			dto.setPassword("");
			dto.setMessage("Attempt to Write User Failed!");
		}
		return dto;	
	}
	
	@ApiMethod(name = "checkUserData", httpMethod = "POST")
	public User checkUserData(User dto) throws Exception {
		//Check params first
		if(dto == null){
			throw new Exception("No object passed");
		}
		
		String firstName = dto.getFirstName();
		String lastName = dto.getLastName();
		String password = dto.getPassword();
		
		if(firstName == null || lastName == null){
			throw new Exception ("Please enter a first and last name");
		}
		if(firstName.isEmpty() || lastName.isEmpty()){
			throw new Exception ("Please enter a first and last name");
		}
		if(password == null){
			throw new Exception ("Please enter a password");
		}
		if(password.isEmpty()){
			throw new Exception ("Please enter a password");
		}

		//Check if it matches. If it does, get a code, set it, return it
		if(TESTDatastoreManager.checkUser(dto)){
			String sessionId = generateRandomCode();
			//Good practice when returning the same DTO to delete the password
			dto.setPassword(""); 
			dto.setMessage("User Validated");
			dto.setSessionId(sessionId);
			boolean bool = TESTDatastoreManager.storeSessionId(dto);
		} //If it does not match, erase the PW entered, do NOT set a code, and return dto 
		else {
			dto.setPassword(""); //Good practice when returning the same DTO
			dto.setMessage("User NOT Validated");
		}
		return dto;
	}
	
	
	//if (BCrypt.checkpw(signInInfo.getPassword(), pw_hash)) { }
	//String token = KeyFactory.createKeyString("Token", String.valueOf(new Random().nextInt(999)));
		
	/**
	 * Generates a random token to be used
	 * @return String
	 */
	private static String generateRandomCode(){
		String token = null;
		
		try {
			token = KeyFactory.createKeyString("Token", String.valueOf(new Random().
					nextInt(999)));
		} catch (Exception e){
			e.printStackTrace();
		}
		log.warning("token = " + token);
		return token;
	}
}
