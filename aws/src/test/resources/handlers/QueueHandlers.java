package handlers;

import com.nimbusframework.nimbuscore.annotations.function.QueueServerlessFunction;
import com.nimbusframework.nimbuscore.eventabstractions.QueueEvent;
import java.util.List;
import models.Person;

public class QueueHandlers {

  @QueueServerlessFunction(id = "messageQueue", batchSize = 1)
  public void consumeQueue(Person item, QueueEvent event) {
    System.out.println(item.getName());
  }

  @QueueServerlessFunction(id = "messageQueue", batchSize = 10)
  public void consumeQueueList(List<Person> items, List<QueueEvent> event) {
    for (int i = 0; i < items.size(); i++) {
      System.out.println(items.get(i) + " " + event.get(i).getRequestId());
    }
  }

}
