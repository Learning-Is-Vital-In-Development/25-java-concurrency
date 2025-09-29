package example.concurrency.ch13;


import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

public class DistributedLockTest {

  @Test
  void lockTest() throws Exception {
    Config config = new Config();
    config.useSingleServer()
        .setAddress("redis://localhost:6379")
        .setConnectionPoolSize(64)
        .setConnectionMinimumIdleSize(24)
        .setConnectTimeout(10000)
        .setTimeout(3000)
        .setRetryAttempts(4)
        .setRetryInterval(1500);

    RedissonClient client = Redisson.create(config);

    RLock lock = client.getLock("test-lock");
    lock.tryLock(3, 3, TimeUnit.SECONDS);
    CountDownLatch startLatch = new CountDownLatch(5);
    ExecutorService executorService = Executors.newFixedThreadPool(5);
    for (int i = 0; i < 5; i++) {
      final int threadIndex = i;
      executorService.submit(() -> {
        try {
          System.out.println("Thread-" + threadIndex + " trying to acquire lock...");
          lock.lock();
          System.out.println("Thread-" + threadIndex + " acquired lock.");
          // Critical section
          Thread.sleep(2000); // Simulate work
        } catch (InterruptedException e) {
          e.printStackTrace();
        } finally {
          startLatch.countDown();
          try {
            lock.unlock();
          } catch (Exception e) {
            System.out.println("Thread-" + threadIndex + " failed to release lock: " + e.getMessage());
          }
          System.out.println("Thread-" + threadIndex + " released lock.");
        }
      });
    }
    startLatch.await();

  }
}
