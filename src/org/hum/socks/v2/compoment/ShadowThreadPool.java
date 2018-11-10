package org.hum.socks.v2.compoment;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ShadowThreadPool {

	private static final ExecutorService ExecutorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 20);
	
	public static void execute(Runnable task) {
		ExecutorService.execute(task);
	}
}
