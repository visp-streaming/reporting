package ac.at.tuwien.infosys.visp.entities;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

@Data
@JsonPropertyOrder({ "operator" })
public class SLAComplianceContainer {

    private String operator;
    private Integer totalItems;
    private Integer totalViolations;
    private Integer noViolations;
    private Integer maxDoubleTimeViolation;
    private Integer maxFiveTimeViolation;
    private Integer moreThanFiveTimeViolation;
    private Double average;

}
