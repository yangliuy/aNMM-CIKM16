package main.multithread.threadpool;

import java.util.concurrent.*;
public class ThreadPoolTest {

    public static void main(String[] args) {
        int numWorkers = 100; //number of workers/ task
        int threadPoolSize = 4; // max number of threads
        CountDownLatch latch = new CountDownLatch(numWorkers);
    
        ExecutorService tpes =
            Executors.newFixedThreadPool(threadPoolSize);
    
        WorkerThread[] workers = new WorkerThread[numWorkers];
        for (int i = 0; i < numWorkers; i++) {
            workers[i] = new WorkerThread(i, latch);
            tpes.execute(workers[i]);
        }
        tpes.shutdown();
        try {
			latch.await();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
