package ac.at.tuwien.infosys.visp.dataaggregation;

import ac.at.tuwien.infosys.visp.entities.IncomingLoadData;
import ac.at.tuwien.infosys.visp.repository.QueueMonitorRepository;
import ac.at.tuwien.infosys.visp.repository.entities.QueueMonitor;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class IncomingLoad {

    @Autowired
    private QueueMonitorRepository qmr;

    private static final Logger LOG = LoggerFactory.getLogger(IncomingLoad.class);

    @Value("${infrastructureHost}")
    private String infrastructureHost;

    @Value("${observationDuration}")
    private Integer observationDuration;

    @PreDestroy
    public void generateCSV() {

        String distributedata = infrastructureHost + "/sourcemachinedata>distributedata";
        String availability = infrastructureHost + "/sourceavailability>availability";
        String temperature = infrastructureHost + "/sourcetemperature>temperature";

        List<IncomingLoadData> data = new ArrayList<>();

        QueueMonitor firstSa = qmr.findFirstByQueueOrderByTimeAsc(distributedata);

        DateTime firstTime = firstSa.getTime();
        Integer minutes = 1;
        DateTime nextTime = firstTime.plusMinutes(1);

        List<QueueMonitor> activities1 = qmr.findByQueueAndTimeBetween(distributedata, firstTime, nextTime);
        List<QueueMonitor> activities2 = qmr.findByQueueAndTimeBetween(availability, firstTime, nextTime);
        List<QueueMonitor> activities3 = qmr.findByQueueAndTimeBetween(temperature, firstTime, nextTime);

        Integer recordingCounter = 0;

        while (recordingCounter < observationDuration) {
            IncomingLoadData gd = new IncomingLoadData(firstTime.toString(), recordingCounter);

            if (!activities1.isEmpty()) {
                gd.setDistributedata(calculateRate(activities1));
            }
            if (!activities2.isEmpty()) {
                gd.setAvailability(calculateRate(activities2));
            }
            if (!activities3.isEmpty()) {
                gd.setTemperature(calculateRate(activities3));
            }

            data.add(gd);
            recordingCounter++;

            firstTime = nextTime;
            nextTime = firstTime.plusMinutes(1);
            activities1 = qmr.findByQueueAndTimeBetween(distributedata, firstTime, nextTime);
            activities2 = qmr.findByQueueAndTimeBetween(availability, firstTime, nextTime);
            activities3 = qmr.findByQueueAndTimeBetween(temperature, firstTime, nextTime);

        }

        CsvMapper mapper = new CsvMapper();
        CsvSchema schema = mapper.schemaFor(IncomingLoadData.class).withHeader();
        try {
            Path path = Paths.get("reporting/incomingQueues.csv");
            if (Files.exists(path)) {
                Files.delete(path);
            }
            Files.createFile(path);
            Files.write(path, mapper.writer(schema).writeValueAsString(data).getBytes());
        } catch (IOException e) {
            LOG.error(e.getMessage());
        }
    }

    private Double calculateRate(List<QueueMonitor> activities1) {
        Double rate = 0.0;
        Integer counter = 0;
        for (QueueMonitor qm : activities1) {
            rate+=qm.getIncomingRate();
            counter++;
        }

        return ((rate/counter) * 60);
    }

}
