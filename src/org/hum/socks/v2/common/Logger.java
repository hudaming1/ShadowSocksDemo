package org.hum.socks.v2.common;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {

	static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
	
	public static void log(String str) {
		System.out.println("[socks-client][" + Thread.currentThread().getName() + "]\t\t" + sdf.format(new Date()) + "\t\t" + str);
	}
}
