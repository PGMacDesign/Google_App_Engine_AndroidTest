package com.pgmacdesign.androidtest.datamanagement;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.api.server.spi.config.ApiMethod;
import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.PreparedQuery;
import com.pgmacdesign.androidtest.dto.Employee;
import com.pgmacdesign.androidtest.dto.User;
import com.pgmacdesign.androidtest.misc.BCrypt;

import java.util.logging.Logger;
/**
 * Manages Datastore stuff
 * @author pmacdowell
 * Data taken from: https://cloud.google.com/appengine/docs/java/datastore/entities
 */
public class TESTDatastoreManager {
	private static final Logger log = Logger.getLogger(TESTDatastoreManager.class.getName());
	/**
	 * This creates a new entity and then writes it. Keep in mind that this
	 * will generate a random 'ID' for the employee. You can also create your
	 * own custom one by adding a second argument to the constructor 
	 * (IE  Entity employee = new Entity("Employee", "CustomKey1");
	 */
	public static void doStuff(){
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

		//As we omitted the argument, this will just create a new numeric ID
		Entity employee = new Entity("Employee");

		employee.setProperty("firstName", "Patrick");
		employee.setProperty("lastName", "MacDowell");

		Date hireDate = new Date();
		employee.setProperty("hireDate", hireDate);

		employee.setProperty("attendedHrTraining", true);

		datastore.put(employee);
	}
	
	public static void doStuff2(){
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

		for(int i = 0; i < 100; i++){
			Entity employee = new Entity("Employee");
	
			employee.setProperty("firstName", "Employee");
			String str = "Number " + (i+1);
			employee.setProperty("lastName", str);
	
			Date hireDate = new Date();
			employee.setProperty("hireDate", hireDate);
	
			if(i % 2 == 0){
				employee.setProperty("attendedHrTraining", true);
			} else {
				employee.setProperty("attendedHrTraining", false);
			}
			
	
			datastore.put(employee);
		} 
	}
	
	/**
	 * Stores a user into the DB. Returns a boolean
	 * @param dto User DTO to be used
	 * @return Returns a success boolean
	 */
	public static boolean storeUser(User dto){
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Entity user = new Entity("User");
		user.setProperty("firstName", dto.getFirstName());
		user.setProperty("lastName", dto.getLastName());
		//NOTE! this is the HASHED password, not the raw unhashed pw
		user.setProperty("password", dto.getPassword());
		try{
			datastore.put(user);
			return true;
		} catch (Exception e){
			e.printStackTrace();
			return false;
		}
	}
	/**
	 * Insert a sessionId into the datastore
	 * @param dto User DTO which references the sessionId
	 * @return Return a boolean for confirmation
	 */
	public static boolean storeSessionId(User dto){
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Entity sessionId = new Entity("sessionId");
		/*
		Set the sessionId and the first / last name tied to it.
		While we may not use the first and last name right now,
		we might in the future, so for now, store em.
		 */
		
		sessionId.setProperty("sessionId", dto.getSessionId());
		sessionId.setProperty("firstName", dto.getFirstName());
		sessionId.setProperty("lastName", dto.getLastName());

		//Date for a time reference
		Date date = new Date();
		long currentDateMilliseconds = date.getTime();
		//Putting the date time reference in as a long in Epoch Time
		sessionId.setProperty("dateCreated", currentDateMilliseconds);
		
		//Insert it into the datastore
		try{
			datastore.put(sessionId);
			return true;
		} catch (Exception e){
			e.printStackTrace();
			return false;
		}
	}
	/**
	 * Checks if a user exists and matches their username/ pw. Returns a boolean
	 * @param dto User DTO to be used
	 * @return Returns a success boolean if they provided the same information
	 */
	public static boolean checkUser(User dto){
		//Filters by first name
		Filter firstNameFilter = new FilterPredicate(
				"firstName", FilterOperator.EQUAL, dto.getFirstName()
				);
		//Filters by last name
		Filter lastNameFilter = new FilterPredicate(
				"lastName", FilterOperator.EQUAL, dto.getLastName()
				);
		//Combines the filters into one so that it checks against ALL 3
		Filter combinedFilter = CompositeFilterOperator.and(firstNameFilter, lastNameFilter);
		
		//Prepare the Query
		Query q = new Query("User").setFilter(combinedFilter);
		log.warning("Query = " + q.toString());
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
		//Create the username/ pw structure like we did when we created it
		String username = dto.getFirstName() + " " + dto.getLastName();
		username = username.toLowerCase();
		
		//Passwords, we will maintain their caps
		String superUsernamePassword = username + dto.getPassword();
		
		// Use PreparedQuery interface to retrieve results
		PreparedQuery pq = datastore.prepare(q);
		//Loops through all results. In our case, we are going to just use the first one
		for (Entity result : pq.asIterable()) {
			log.warning("Entity returned, found user");
			/*
			Now that we have the user, loop through the result(s) and check if the pw
			matches via a special method call. Note, generally we would want to write
			some code when a user is entered to check for duplicates, but for now, 
			let's stick with the simple stuff. Keep in mind that this will only check
			for the FIRST result, so if you enter your name twice, you won't be able 
			to pass validation without deleting one first. 
			 */
			String storedPassword = (String) result.getProperty("password");
			
			//Now, compare the passed and stored passwords
			if(BCrypt.checkpw(superUsernamePassword, storedPassword)){
				return true;
			} else {
				return false;
			}	
		}
		//Nothing was returned, return false
		log.warning("Entity not returned, found nothing");
		return false;
	}
	/**
	 * Checks if a sessionId exists in the database
	 * @param ssId The session Id being checked
	 * @return Returns a success boolean if the sessionId is valid
	 */
	public static boolean validateSession(String ssId){
		//Filters by sessionId
		Filter sessionFilter = new FilterPredicate(
				"sessionId", FilterOperator.EQUAL, ssId
				);
		
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
		//Prepare the Query
		Query q = new Query("sessionId").setFilter(sessionFilter);
		
		// Use PreparedQuery interface to retrieve results
		PreparedQuery pq = datastore.prepare(q);
		//Loops through all results. In our case, we are going to just use the first one
		for (Entity result : pq.asIterable()) {
			log.warning("Entity returned, found sessionId");
			/*
			If this gets returned at all, we know the sessionId is valid, therefore, 
			we can just return true. If we wanted to add additional security measures
			(IE checking sessionId against the name or something) it would go here.
			I am purposefully not checking against the date here and I'll show why
			at a later date. 
			 */
			return true;	
		}
		//Nothing was returned, return false
		log.warning("Entity not returned, found nothing");
		return false;
	}
	/**
	 * Deletes the sessionIds from the datastore by checking against 12 hour windows
	 * @return an int of how many sessionIds were deleted
	 */
	public static int deleteSessionId(){
		int totalDeleted = 0;
		//This should total 12 hours in milliseconds
		long twelveHours = 1000 * 60 * 60 * 12; 
		//Current time (long format)
		Date date = new Date();
		long currentTimeMilliseconds = date.getTime();
		//Current time - 12 hours
		long timeToDeleteBy = currentTimeMilliseconds - twelveHours;
		
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
		//Prepare the Query. No filters as we want ALL sessionIds
		Query q = new Query("sessionId");
		
		// Use PreparedQuery interface to retrieve results
		PreparedQuery pq = datastore.prepare(q);
		//Loops through all results. In our case, we are going to just use the first one
		for (Entity result : pq.asIterable()) {
			log.warning("Entity returned, found sessionId");
			
			long expiryTime = 0L;
			try { //Try catch here in case the casting goes incorrectly
				expiryTime = (long) result.getProperty("dateCreated");
			} catch (Exception e){
				e.printStackTrace();
			}
			if(expiryTime == 0){
				continue; //Breaks the current loop
			}
			//Check against the date and see if it is more than 12 hours old
			if(expiryTime < timeToDeleteBy){
				//More than 12 hours old, delete 
				Key key = result.getKey();
				datastore.delete(key);
				//Increment the counter so we know one was deleted
				totalDeleted++;
			}		
		}
		return totalDeleted;
	}
	/**
	 * Deletes the entity passed in
	 * @param entity Entity to be deleted
	 */
	public static boolean deleteStuff(Employee employee){
		// Get the Datastore Service
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Query q = buildAQuery(employee.getFirstName());
		// Use PreparedQuery interface to retrieve results
		PreparedQuery pq = datastore.prepare(q);
		Entity entity = null;
		try {
			for (Entity result : pq.asIterable()) {
				//This is inefficient, but we are just writing simple code at this point
				entity = result; 
			}
		} catch(Exception e){
			e.printStackTrace();
		}
		if(entity != null){
			//First get their key
			Key key = entity.getKey();
			datastore.delete(key);
			return true;
		} else {
			return false; 
		}

	}
	
	/**
	 * Updates an existing entity with already modified passed in variable
	 * @param entity ALREADY MODIFIED entity to be updated. Modify values beforehand
	 */
	public static boolean updateStuff(Entity entity){
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Key key =  datastore.put(entity);
		if (key != null){
			return true;
		} else {
			return false;
		}
	}
	/**
	 * Gets an entity from the DB and returns it
	 * @param emp
	 * @return
	 */
	public static Entity getSpecificEmployee(Employee emp){
		if(emp == null){
			return null;
		} 
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Long id = emp.getId();
		Key key = KeyFactory.createKey("Employee", id);
		try {
			Entity ent = datastore.get(key);
			return ent;
		} catch (EntityNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static Employee queryStuff(String firstName){
		// Get the Datastore Service
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Query q = buildAQuery(firstName);
		// Use PreparedQuery interface to retrieve results
		PreparedQuery pq = datastore.prepare(q);
		//Loops through all results. In our case, we are going to just use the first one
		for (Entity result : pq.asIterable()) {
			
			//Get results via the properties
			String actualFirstName = (String) result.getProperty("firstName");
			String actualLastName = (String) result.getProperty("lastName");
			boolean attendedHrTraining = (boolean) result.getProperty("attendedHrTraining");
			Date hireDate = (Date) result.getProperty("hireDate");
			Long id = (Long) result.getKey().getId();
			
			//Build an employee
			Employee emp = new Employee();
		  	emp.setFirstName(actualFirstName);
		  	emp.setLastName(actualLastName);
		  	emp.setAttendedHrTraining(attendedHrTraining);
		  	emp.setHireDate(hireDate);
		  	emp.setId(id);
		  	
		  	return emp;
		}
		return null;
	}

	/**
	 * Return a list of all of the employees
	 * @return
	 */
	public static List<Employee> getAllEmployees(){
		// Get the Datastore Service
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Query q = buildAQuery(null);
		// Use PreparedQuery interface to retrieve results
		PreparedQuery pq = datastore.prepare(q);
		//List for returning
		List<Employee> returnedList = new ArrayList<>();
		//Loops through all results and add them to the returning list 
		for (Entity result : pq.asIterable()) {
			
			//Get results via the properties
			String actualFirstName = (String) result.getProperty("firstName");
			String actualLastName = (String) result.getProperty("lastName");
			boolean attendedHrTraining = (boolean) result.getProperty("attendedHrTraining");
			Date hireDate = (Date) result.getProperty("hireDate");
			Blob blob = (Blob) result.getProperty("picture");
			byte[] photo = blob.getBytes();
			
			//Build an employee
			Employee emp = new Employee();
		  	emp.setFirstName(actualFirstName);
		  	emp.setLastName(actualLastName);
		  	emp.setAttendedHrTraining(attendedHrTraining);
		  	emp.setHireDate(hireDate);
		  	emp.setPicture(photo);
		  	
		  	returnedList.add(emp);
		}
		return returnedList;
	}
	/**
	 * Builds a Query
	 * @param firstName First name, if null passed, no search params
	 * @return
	 */
	public static Query buildAQuery(String firstName){
		Query q;
		if(firstName != null){
			Filter nameFilter = new FilterPredicate("firstName", 
					FilterOperator.EQUAL, firstName);
	
			q = new Query("Employee").setFilter(nameFilter);
		} else {
			q = new Query("Employee");
		}
		
		return q;
	}
}
