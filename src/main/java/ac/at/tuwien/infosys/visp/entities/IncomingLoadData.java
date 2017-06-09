package ac.at.tuwien.infosys.visp.entities;


import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

@Data
@JsonPropertyOrder
public class IncomingLoadData {

    private Integer counter;
    private String time;
    private Double distributedata;
    private Double availability;
    private Double temperature;
    private Double availabilityWarning;
    private Double temperatureWarning;
    private Double generateReportWarning;
    private Double user;
    private Double availabilityOEE;
    private Double performanceOEE;
    private Double qualityOEE;
    private Double generateReport;
    private Double availabilityDistribute;
    private Double performanceDistribute;
    private Double qualityDistribute;

    private Double warningOperator;
    private Double oeeOperator;


    public IncomingLoadData(String time, Integer counter) {
        this.counter = counter;
        this.time = time;
        this.distributedata = 0.0;
        this.availability = 0.0;
        this.temperature = 0.0;
        this.availabilityWarning = 0.0;
        this.temperatureWarning = 0.0;
        this.generateReportWarning = 0.0;
        this.user = 0.0;
        this.availabilityOEE = 0.0;
        this.performanceOEE = 0.0;
        this.qualityOEE = 0.0;
        this.generateReport = 0.0;
        this.availabilityDistribute = 0.0;
        this.performanceDistribute = 0.0;
        this.qualityDistribute = 0.0;

    }

}
