package ac.at.tuwien.infosys.visp.repository;


import ac.at.tuwien.infosys.visp.repository.entities.ProcessingDuration;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ProcessingDurationRepository extends CrudRepository<ProcessingDuration, Long> {

    List<ProcessingDuration> findFirst5ByOperatorOrderByIdDesc(String operator);
    List<ProcessingDuration> findByOperatorOrderByIdDesc(String operator);
    List<ProcessingDuration> findByOperator(String operator);

}
