package ac.at.tuwien.infosys.visp.entities;


public class TimeToAdapt {

    private String operator;
    private Integer timeToAdoptTwiceTheTime;
    private Integer timeToAdoptInTime;


    public TimeToAdapt(String operator, Integer timeToAdoptTwiceTheTime, Integer timeToAdoptInTime) {
        this.operator = operator;
        this.timeToAdoptTwiceTheTime = timeToAdoptTwiceTheTime;
        this.timeToAdoptInTime = timeToAdoptInTime;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public Integer getTimeToAdoptTwiceTheTime() {
        return timeToAdoptTwiceTheTime;
    }

    public void setTimeToAdoptTwiceTheTime(Integer timeToAdoptTwiceTheTime) {
        this.timeToAdoptTwiceTheTime = timeToAdoptTwiceTheTime;
    }

    public Integer getTimeToAdoptInTime() {
        return timeToAdoptInTime;
    }

    public void setTimeToAdoptInTime(Integer timeToAdoptInTime) {
        this.timeToAdoptInTime = timeToAdoptInTime;
    }
}
