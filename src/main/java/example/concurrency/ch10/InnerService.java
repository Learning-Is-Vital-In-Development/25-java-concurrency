package example.concurrency.ch10;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InnerService {

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void innerMethod() {
    System.out.printf("InnerService.innerMethod is called by %s%n", Thread.currentThread().getName());
  }
}
