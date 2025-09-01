package example.concurrency.ch10;

import java.sql.Connection;
import javax.sql.DataSource;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OuterService {

  private final InnerService innerService;

  public OuterService(InnerService innerService) {
    this.innerService = innerService;
  }

  @Transactional
  public void outerMethod() {
    System.out.printf("OuterService.outerMethod is called by %s%n", Thread.currentThread().getName());
    try {
      Thread.sleep(1000);
      innerService.innerMethod();
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage());
    }
  }
}
