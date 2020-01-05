package handlers;

import com.nimbusframework.nimbuscore.annotations.function.QueueServerlessFunction;
import com.nimbusframework.nimbuscore.eventabstractions.QueueEvent;
import models.Person;
import models.Queue;

import java.util.List;

public class QueueHandlers {

  @QueueServerlessFunction(queue = Queue.class, batchSize = 1)
  public void consumeQueue(Person item, QueueEvent event) {
    System.out.println(item.getName());
  }

  @QueueServerlessFunction(queue = Queue.class, batchSize = 10)
  public void consumeQueueList(List<Person> items, List<QueueEvent> event) {
    for (int i = 0; i < items.size(); i++) {
      System.out.println(items.get(i) + " " + event.get(i).getRequestId());
    }
  }

}
