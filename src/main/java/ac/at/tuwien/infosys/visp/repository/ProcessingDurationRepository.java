package ac.at.tuwien.infosys.visp.repository;


import java.util.List;

import ac.at.tuwien.infosys.visp.repository.entities.ProcessingDuration;
import org.springframework.data.repository.CrudRepository;

public interface ProcessingDurationRepository extends CrudRepository<ProcessingDuration, Long> {

    List<ProcessingDuration> findFirst5ByOperatortypeOrderByIdDesc(String operator);
    List<ProcessingDuration> findByOperatortypeOrderByIdDesc(String operator);
    List<ProcessingDuration> findByOperatortypeOrderByIdAsc(String operator);

    List<ProcessingDuration> findByOperatortype(String operator);

}
