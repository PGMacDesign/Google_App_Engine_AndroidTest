package com.pgmacdesign.androidtest.misc;
import java.util.logging.Logger;

public class L {
	
	//This class logs data to the logcat	
	public static void Log(String message){
		Logger log = Logger.getLogger(L.class.getName());
		log.severe(message);
	}
	
	//Print to the locat via println
	public static void m(String message){
		System.out.println("String: " + message);
	}

	//Print to the locat via println
	public static void m(int message){
		System.out.println("int: " + Integer.toString(message));
	}
	
	//Print to the locat via println
	public static void m(Integer message){
		System.out.println("Integer: " + Integer.toString(message));
	}
	
	//Print to the locat via println
	public static void m(double message){
		System.out.println("Double: " + Double.toString(message));
	}
	
	//Print to the locat via println
	public static void m(float message){
		System.out.println("Float: " + Float.toString(message));
	}
	
	//Print to the locat via println
	public static void m(long message){
		System.out.println("long: " + Float.toString(message));
	}
	
}
