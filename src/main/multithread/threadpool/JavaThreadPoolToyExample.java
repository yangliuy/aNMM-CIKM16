package main.multithread.threadpool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class JavaThreadPoolToyExample {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ExecutorService fixedThreadPool = Executors.newFixedThreadPool(3);
		System.out.println("the number of avaiable cores: " + Runtime.getRuntime().availableProcessors());
		for (int i = 0; i < 10; i++) {
			final int index = i;
			fixedThreadPool.execute(new Runnable() {
		 
				@Override
				public void run() {
					try {
						System.out.println(index);
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
		}
	}

}
