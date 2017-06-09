package ac.at.tuwien.infosys.visp.entities;


import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

@Data
@JsonPropertyOrder
public class ProcessingDurationData {

    private Double duration;

}
