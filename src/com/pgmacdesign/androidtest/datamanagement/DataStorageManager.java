package com.pgmacdesign.androidtest.datamanagement;

import java.util.concurrent.Future;
import java.util.logging.Logger;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.jdo.Transaction;

import com.google.appengine.api.NamespaceManager;
import com.google.appengine.api.datastore.AsyncDatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceConfig;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.ImplicitTransactionManagementPolicy;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.pgmacdesign.androidtest.dto.TestDTO;

/**
 * This class will manage data storage interactions with the database
 * @author pmacdowell
 */
public class DataStorageManager {
	//The name of your namespace (Just make it unique to you)
	private static final String NAMESPACE = "PGMacDesign-Namespace";
	
	//The persistence manager will help store the data
	private static final PersistenceManagerFactory PM = JDOHelper.getPersistenceManagerFactory();
	
	//Async Data transaction
	private static AsyncDatastoreService asyncDataService = null;
	
	//This is a grouping of threads that HOLD persistence manager objects
	private static final ThreadLocal<PersistenceManager> PMTHREAD = new ThreadLocal<PersistenceManager>(); 
	/*
	 * From the Javadoc:
	 This class provides thread-local variables. These variables differ from their normal counterparts 
	 in that each thread that accesses one (via its get or set method) has its own, independently initialized 
	 copy of the variable. ThreadLocal instances are typically private static fields in classes that wish to 
	 associate state with a thread (e.g., a user ID or Transaction ID). 
	 */
	
	/*
	I got this data from github, checkout this repo for more information:
	https://github.com/GoogleCloudPlatform/appengine-tck/blob/master/tests/appengine-tck-datastore/src/test/java/com/google/appengine/tck/datastore/ConfigTest.java
	 */
	public static synchronized void initialize(){
		//Set the datastore service config info and the async datastore service object
		DatastoreServiceConfig config = DatastoreServiceConfig.Builder.
				withImplicitTransactionManagementPolicy(ImplicitTransactionManagementPolicy.NONE);
		asyncDataService = DatastoreServiceFactory.getAsyncDatastoreService(config);
	}
	
	/**
	 * Sets the Persistence Manager namespace
	 */
	public static void setPM(){
		//Set the namespace
		NamespaceManager.set(NAMESPACE);
		//Set a persistence manager into the thread collection
		PMTHREAD.set(PM.getPersistenceManager());
	}
	
	/**
	 * Returns a persistanceManager object
	 * @return
	 */
	public static PersistenceManager getPMInstance(){
		PersistenceManager returnMe = PMTHREAD.get();
		return returnMe;
	}
	
	/**
	 * Deletes a persistence manager obejct
	 */
	public static void deletePM(){
		PersistenceManager pm = PMTHREAD.get();
		try {
			if(pm != null){
				Transaction transaction = pm.currentTransaction();
				//Check if it is both active and not null
				if(transaction != null  && transaction.isActive()){
					//In the event another transaction is taking place, rollback the transaction
					transaction.rollback();
				}
				pm.close(); //Close it
				PMTHREAD.remove(); //Remove it from the collection
			}
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * This will get a datastore item by the Id. 
	 * @param dto Class object that extends to our testDTO
	 * @param id an ID, this is really just the long ID that is generated by Google
	 * @return Returns an object that you will want to cast
	 */
	public static <E extends TestDTO> Object getDatastoreItemById(Class<E> dto, Object id){
		Object object = null;
		PersistenceManager pm = getPMInstance();
		try {
			//This is taken from: 
			//http://www.datanucleus.org/products/datanucleus/jdo/pm.html     AND
			//https://cloud.google.com/appengine/docs/java/datastore/jdo/creatinggettinganddeletingdata?hl=en
			object = pm.getObjectById(dto, id);
		} catch (Exception e){
			e.printStackTrace();
		}
		return object;
	}
	
	/**
	 * Saves data to the datastore
	 * @param objectToSave
	 */
	public static void saveData(Object objectToSave){
		//Get the instance first
		PersistenceManager pm = getPMInstance();
		//Taken from the JDO docs
		pm.makePersistent(objectToSave);
		//Lastly, commit it
		commitTransaction();
	}
	
	
	public static void deleteData(Object objectToDelete){
		//Get the instance first
		PersistenceManager pm = getPMInstance();
		//Taken from the JDO docs
		pm.deletePersistent(objectToDelete);
		//Lastly, commit it
		commitTransaction();
	}
	
	/**
	 * Commits a transaction to the server
	 */
	public static void commitTransaction(){
		//Get the instance first
		PersistenceManager pm = getPMInstance();
		//Create a transaction using the persistence manager
		Transaction transaction = pm.currentTransaction();
		if(transaction.isActive()){
			transaction.commit();
		} //If for some reason it is not active, activate it, then commit
		else {
			transaction.begin();
			transaction.commit();
		}
	}
	
	/**
	 * Creates a new query. If null is passed, no search params will be added
	 * @param dto The class object to be searching
	 * @param searchInfo The searchInfo to be used, if null is passed, omit it
	 * @return
	 */
	public static <E extends TestDTO> Query getQueryToDB(Class<E> dto, String searchInfo){
		PersistenceManager pm = getPMInstance();
		Query query;
		if(searchInfo == null){
			query = pm.newQuery(dto);
		} else {
			query = pm.newQuery(dto, searchInfo);
		}
		return query;
	}
	
	//These are all non-JDO API Calls
	/**
	 * Used for async datasore saves
	 * @param dtoToPut Entity (GAE Type) to put
	 */
	public static void putEventAsync(Entity dtoToPut){
		try {
			asyncDataService.put(dtoToPut);
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * Used for non-async datasore saves
	 * @param dtoToPut Entity (GAE Type) to put
	 */
	public static void putEventNonAsync(Entity dtoToPut){
		try {
			asyncDataService.put(dtoToPut).get();
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * Get an Entity using the Key as a param
	 * @param key
	 * @return
	 */
	public static Entity getEntity(Key key){
		try {
			Future<Entity> entity = asyncDataService.get(key);
			return entity.get();
		} catch (Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Deletes an Entity using the Key as a param
	 * @param key
	 */
	public static void deleteEvent(Key key){
		try {
			asyncDataService.delete(key);
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * Changes a regular query into a prepared query
	 * @param q The query to be converted
	 * @return Prepared Query (Of type GAE)
	 */
	public static PreparedQuery getPreparedQuery(com.google.appengine.api.datastore.Query q){
		try {
			PreparedQuery pq = asyncDataService.prepare(q);
			return pq;
		} catch (Exception e){
			e.printStackTrace();
			return null;
		}

	}
}
