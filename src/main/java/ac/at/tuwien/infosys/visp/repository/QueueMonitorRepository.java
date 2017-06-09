package ac.at.tuwien.infosys.visp.repository;


import ac.at.tuwien.infosys.visp.repository.entities.QueueMonitor;
import org.joda.time.DateTime;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface QueueMonitorRepository extends CrudRepository<QueueMonitor, Long> {

    List<QueueMonitor> findFirst5ByOperatorOrderByIdDesc(String operator);

    QueueMonitor findFirstByOperatorOrderByIdDesc(String operator);

    QueueMonitor findFirstByQueueOrderByTimeAsc(String queue);

    List<QueueMonitor> findByQueueAndTimeBetween(String queue, DateTime start, DateTime end);


}
