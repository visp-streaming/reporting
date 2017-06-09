package ac.at.tuwien.infosys.visp.entities;


import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

@Data
@JsonPropertyOrder
public class OperatorContainer {

    private String time;
    private Integer distributedata = 0;
    private Integer availability = 0;
    private Integer calculateperformance = 0;
    private Integer calculateavailability = 0;
    private Integer calculatequality = 0;
    private Integer temperature = 0;
    private Integer calculateoee = 0;
    private Integer warning = 0;
    private Integer generatereport = 0;

    public void distributedataInc() {
        this.distributedata++;
    }

    public void distributedataDec() {
        this.distributedata--;
    }

    public void availabilityInc() {
        this.availability++;
    }

    public void availabilityDec() {
        this.availability--;
    }

    public void calculateperformanceInc() {
        this.calculateperformance++;
    }

    public void calculateperformanceDec() {
        this.calculateperformance--;
    }

    public void calculateavailabilityInc() {
        this.calculateavailability++;
    }

    public void calculateavailabilityDec() {
        this.calculateavailability--;
    }

    public void calculatequalityInc() {
        this.calculatequality++;
    }

    public void calculatequalityDec() {
        this.calculatequality--;
    }

    public void temperatureInc() {
        this.temperature++;
    }

    public void temperatureDec() {
        this.temperature--;
    }

    public void calculateoeeInc() {
        this.calculateoee++;
    }

    public void calculateoeeDec() {
        this.calculateoee--;
    }

    public void warningInc() {
        this.warning++;
    }

    public void warningDec() {
        this.warning--;
    }

    public void generatereportInc() {
        this.generatereport++;
    }

    public void generatereportDec() {
        this.generatereport--;
    }
}
