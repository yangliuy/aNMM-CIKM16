package main.multithread.threadpool;

import java.util.concurrent.CountDownLatch;

public class WorkerThread implements Runnable {
    private int workerNumber;
    private CountDownLatch latch;

    WorkerThread(int number, CountDownLatch latch) {
        workerNumber = number;
        this.latch = latch;
    }

    public void run() {
        for (int i=0;i<=100;i+=20) {
        // Perform some work ...
            System.out.println("Worker number: " + workerNumber + ", percent complete: " + i );
            try {
                Thread.sleep((int)(2000));
            } catch (InterruptedException e) {
            }
        }
        this.latch.countDown();
        System.out.println("test latch count : " + latch.getCount());
    }
}
