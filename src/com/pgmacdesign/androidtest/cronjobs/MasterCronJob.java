package com.pgmacdesign.androidtest.cronjobs;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Serves as a master class that others can extend to
 * @author pmacdowell
 */
public abstract class MasterCronJob extends HttpServlet{

	private static final Logger log = Logger.getLogger(MasterCronJob.class.getName());
	
	/**
	 * This makes a GET call. Required as per GAE
	 */
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)	throws IOException {

		try {
			//Child classes execute this
			execute();			
		} catch (Exception e) {
			log(log + e.toString());
		}
	}

	/**
	 * This makes a POST call, but as we only need Get, it auto calls get for us.
	 * This is mostly in place in case someone calls a post method when they 
	 * mean to call the get only.
	 */
	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doGet(req, resp);
	}
	
	/**
	 * This is the method that children must implement and it will execute 
	 * automatically upon being called / pinged
	 */
	public abstract void execute();
}
