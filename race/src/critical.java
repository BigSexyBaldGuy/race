import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 *  {@code critical} is class that demonstrates critical sections.
 *  @version 20190040700
 *  @author Richard Barton
 */

public class critical {
    private static volatile AtomicLong currentCount =
            new AtomicLong(0);
    private static boolean      stopThreads;

    public static void main(String arg[])
    {
        int                     whichThread;
        int                     threadCount;

        javax.swing.Timer       valueTimer;
        Runnable                workToDo;
        Thread                  runningThread[];

        /*
         *  Check the command line arguments for a number of
         *  threads to start.  Assume a minimum of one.
         */
        threadCount = 1;
        if (arg.length > 0) {
            try {
                threadCount = Integer.parseInt(arg[0]);
            } catch (Exception exception) {
                threadCount = 1;
            }

            if (threadCount <= 0) {
                threadCount = 1;
            }
        }

        System.out.println("Using " + threadCount + " thread" +
                ((threadCount == 1) ? "" : "s"));

        workToDo = () -> {
            synchronized(critical.class) {
            Random threadRandom;
            int busyCount;

            /*
             *  Instantiate our pseudo-random number generator.
             */
            threadRandom = new Random();
            /*
             *  Until someone tells us to stop...
             */
            while (stopThreads == false) {
                //int busyCount;

                /*
                 *  Waste a random amount of time.
                 */
                for (busyCount = threadRandom.nextInt(1024 * 1024);
                     busyCount > 0; --busyCount);
                /*
                 *  Increment our shared variable.
                 *  Sycronize separately
                 */
                //++currentCount;
                currentCount.getAndIncrement();
                /*
                 *  Waste a random amount of time.
                 */
                for (busyCount= threadRandom.nextInt(1024 * 1024);
                     (busyCount > 0); --busyCount);
                /*
                 *  Decrement our shared variable.
                 *  Sycronize separately
                 */
                //--currentCount;
                currentCount.getAndDecrement();
            }
        }
        };

        /*
         *  Display the current value of our shared variable every
         *  quarter second.
         */
        valueTimer = new javax.swing.Timer(250, event -> {
            System.out.printf("\rCount = %10d", currentCount.get());
        });
        valueTimer.start();

        /*
         *  Let the threads run.
         */
        stopThreads = false;
        runningThread = new Thread[threadCount];
        for (whichThread = threadCount - 1;
             (whichThread >= 0); --whichThread) {
            /*
             *  Instantiate and start a thread.
             */
            runningThread[whichThread] = new Thread(workToDo);
            runningThread[whichThread].start();
        }

        /*
         *  Sleep for ten seconds.
         */
        try {
            Thread.sleep(10000);
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        /*
         *  Stop reporting the current value of our shared variable.
         */
        valueTimer.stop();
        while (valueTimer.isRunning() == true)  ;

        /*
         *  Tell all the threads to stop.
         */
        stopThreads = true;
        for (whichThread = threadCount - 1;
             (whichThread >= 0); --whichThread) {
            /*
             *  Wait for each thread to stop.
             */
            try {
                runningThread[whichThread].join();
            } catch (Exception exception) {
                continue;
            }
        }

        /*
         *  Report the final value of our shared variable.
         */
        System.out.printf("\rCount = %10d\nStopped\n",
                currentCount.get());
    }
}
