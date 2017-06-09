package ac.at.tuwien.infosys.visp.repository;


import ac.at.tuwien.infosys.visp.repository.entities.DockerContainerMonitor;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface DockerContainerMonitorRepository extends CrudRepository<DockerContainerMonitor, Long> {

    DockerContainerMonitor findFirstByContaineridOrderByTimestampDesc(String containerid);
    List<DockerContainerMonitor> findByOperator(String operator);
    List<DockerContainerMonitor> findByOperatorid(String operatorid);


    DockerContainerMonitor findFirstByOperatorOrderByTimestampDesc(String operator);
    DockerContainerMonitor findFirstByOperatoridOrderByTimestampDesc(String operator);



}
