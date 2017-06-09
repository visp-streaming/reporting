package ac.at.tuwien.infosys.visp.entities;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

@Data
@JsonPropertyOrder
public class SLAComplianceContainer {

    private String operator;
    private Integer total;
    private Integer violations;
    private Integer noViolations;
    private Integer maxDoubleTimeViolation;
    private Integer maxFiveTimeViolation;
    private Integer moreThanFiveTimeViolation;
    private Double average;


}
