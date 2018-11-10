package org.hum.socks.v2.common;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.hum.socks.v2.compoment.ShadowThreadPool;

public class Logger {

	static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
	
	public static void log(String str) {
		System.out.println("[socks-client][" + Thread.currentThread().getName() + "][" + ShadowThreadPool.getActivityCount() + "]\t\t" + sdf.format(new Date()) + "\t\t" + str);
	}
}
