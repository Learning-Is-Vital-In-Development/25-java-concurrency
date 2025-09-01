package example.concurrency.ch10;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class DeadLockTest {

  @Autowired
  private OuterService outerService;

  @Test
  public void testDeadLoc() throws InterruptedException {
    ExecutorService executorService = Executors.newFixedThreadPool(2);
    CountDownLatch startLatch = new CountDownLatch(1);
    CountDownLatch doneLatch = new CountDownLatch(2);

    System.out.println("=== 데드락 테스트 시작 ===");

    // Thread 1
    executorService.submit(() -> {
      try {
        startLatch.await(); // 동시 시작을 위한 대기
        System.out.println("Thread-1 시작");
        outerService.outerMethod();
        System.out.println("Thread-1 완료");
      } catch (Exception e) {
        System.err.println("Thread-1 에러: " + e.getMessage());
        e.printStackTrace();
      } finally {
        doneLatch.countDown();
      }
    });

    // Thread 2
    executorService.submit(() -> {
      try {
        startLatch.await(); // 동시 시작을 위한 대기
        System.out.println("Thread-2 시작");
        outerService.outerMethod();
        System.out.println("Thread-2 완료");
      } catch (Exception e) {
        System.err.println("Thread-2 에러: " + e.getMessage());
        e.printStackTrace();
      } finally {
        doneLatch.countDown();
      }
    });

    // 잠시 대기 후 동시 실행
    Thread.sleep(100);
    System.out.println("두 스레드 동시 시작!");
    startLatch.countDown();

    // 최대 30초 대기 (데드락이면 타임아웃)
    boolean completed = doneLatch.await(30, TimeUnit.SECONDS);

    if (!completed) {
      System.err.println("⚠️ 타임아웃 발생 - 데드락 가능성!");
    } else {
      System.out.println("✅ 모든 스레드 정상 완료");
    }

    executorService.shutdownNow();
  }
}
