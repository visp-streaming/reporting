package ac.at.tuwien.infosys.visp.entities;


import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

@Data
@JsonPropertyOrder({"minute"})
public class GraphData {

    private Integer minute;
    private Integer vmUp;
    private Integer vmDown;
    private Integer vmprolongLease;
    private Integer operatorUp;
    private Integer operatorDown;
    private Integer operatorMigrate;
    private Integer totalVMs;
    private Integer totalContainer;

    public GraphData(Integer minute) {
        this.minute = minute;
        this.vmUp = 0;
        this.vmprolongLease = 0;
        this.vmDown = 0;
        this.operatorUp = 0;
        this.operatorDown = 0;
        this.operatorMigrate = 0;
        this.totalVMs = 0;
        this.totalContainer = 0;
    }

    public void vmUpInc() {
        this.vmUp++;
    }

    public void vmDownInc() {
        this.vmDown++;
    }

    public void vmProlongLease() {
        this.vmprolongLease++;
    }

    public void operatorUpInc() {
        this.operatorUp++;
    }

    public void operatorDownInc() {
        this.operatorDown++;
    }

    public void operatorUpDec() {
        this.operatorUp--;
    }

    public void operatorDownDec() {
        this.operatorDown--;
    }

    public void operatorMigrateInc() {
        this.operatorMigrate++;
    }



}
