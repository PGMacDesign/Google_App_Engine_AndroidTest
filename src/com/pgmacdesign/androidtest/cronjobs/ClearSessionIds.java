package com.pgmacdesign.androidtest.cronjobs;

import java.util.logging.Logger;

import com.pgmacdesign.androidtest.datamanagement.TESTDatastoreManager;

/**
 * This class will clear sessionIds from the datastore every X hours (set in settings)
 * @author pmacdowell
 */
public class ClearSessionIds extends MasterCronJob {

	private static final Logger log = Logger.getLogger(ClearSessionIds.class.getName());
	
	@Override
	public void execute() {
		try {
			int numDeleted = TESTDatastoreManager.deleteSessionId();
			log.warning(log + ": Number Deleted = " + numDeleted);
		} catch (Exception e){
			log.warning(log + ": Error, no sessionIds deleted: " + e.toString());
		}
	}
}
