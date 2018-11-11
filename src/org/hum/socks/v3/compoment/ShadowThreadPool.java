package org.hum.socks.v3.compoment;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ShadowThreadPool {

	private static final ThreadPoolExecutor ExecutorService = new ThreadPoolExecutor(200, 300, 30, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(100000));
	
	public static void execute(Runnable task) {
		ExecutorService.execute(task);
	}
	
	public static int getActivityCount() {
		return ExecutorService.getPoolSize();
	}
}
