package ac.at.tuwien.infosys.visp.repository.entities;

import lombok.Data;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Data
@Entity
public class QueueMonitor {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime time;

    private String operator;
    private String queue;
    private Integer amount;
    private Double incomingRate;

    public QueueMonitor() {
    }

    public QueueMonitor(DateTime time, String operator, String queue, Integer amount, Double incomingRate) {
        this.time = time;
        this.operator = operator;
        this.queue = queue;
        this.amount = amount;
        this.incomingRate = incomingRate;
    }

}
