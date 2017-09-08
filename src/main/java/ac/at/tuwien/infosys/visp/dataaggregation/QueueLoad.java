package ac.at.tuwien.infosys.visp.dataaggregation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

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

@Service
public class QueueLoad {

    @Autowired
    private QueueMonitorRepository qmr;

    private static final Logger LOG = LoggerFactory.getLogger(QueueLoad.class);


    @Value("${infrastructureHost}")
    private String infrastructureHost;

    @Value("${observationDuration}")
    private Integer observationDuration;

    //@PreDestroy
    public void generateCSV() {

        String distributedata = infrastructureHost + "/sourcemachinedata>distributedata";
        String availability = infrastructureHost + "/sourceavailability>availability";
        String temperature = infrastructureHost + "/sourcetemperature>temperature";

        String availabilityWarning = infrastructureHost + "/availability>warning";
        String temperatureWarning = infrastructureHost + "/temperature>warning";
        String generateReportWarning = infrastructureHost + "/generatereport>warning";
        String user = infrastructureHost + "/warning>user";
        String availabilityOEE = infrastructureHost + "/calculateavailability>calculateoee";
        String performanceOEE = infrastructureHost + "/calculateperformance>calculateoee";
        String qualityOEE = infrastructureHost + "/calculatequality>calculateoee";
        String generateReport = infrastructureHost + "/calculateoee>generatereport";
        String availabilityDistribute = infrastructureHost + "/distributedata>calculateavailability";
        String performanceDistribute = infrastructureHost + "/distributedata>calculateperformance";
        String qualityDistribute = infrastructureHost + "/distributedata>calculatequality";


        List<IncomingLoadData> data_rates = new ArrayList<>();
        List<IncomingLoadData> data_amount = new ArrayList<>();


        QueueMonitor firstSa = qmr.findFirstByQueueOrderByTimeAsc(distributedata);

        Integer minutes = 1;

        DateTime firstTime = firstSa.getTime();
        DateTime nextTime = firstTime.plusMinutes(minutes);

        List<QueueMonitor> activities1 = qmr.findByQueueAndTimeBetween(distributedata, firstTime, nextTime);
        List<QueueMonitor> activities2 = qmr.findByQueueAndTimeBetween(availability, firstTime, nextTime);
        List<QueueMonitor> activities3 = qmr.findByQueueAndTimeBetween(temperature, firstTime, nextTime);
        List<QueueMonitor> activities4 = qmr.findByQueueAndTimeBetween(availabilityWarning, firstTime, nextTime);
        List<QueueMonitor> activities5 = qmr.findByQueueAndTimeBetween(temperatureWarning, firstTime, nextTime);
        List<QueueMonitor> activities6 = qmr.findByQueueAndTimeBetween(generateReportWarning, firstTime, nextTime);
        List<QueueMonitor> activities7 = qmr.findByQueueAndTimeBetween(user, firstTime, nextTime);
        List<QueueMonitor> activities8 = qmr.findByQueueAndTimeBetween(availabilityOEE, firstTime, nextTime);
        List<QueueMonitor> activities9 = qmr.findByQueueAndTimeBetween(performanceOEE, firstTime, nextTime);
        List<QueueMonitor> activities10 = qmr.findByQueueAndTimeBetween(qualityOEE, firstTime, nextTime);
        List<QueueMonitor> activities11 = qmr.findByQueueAndTimeBetween(generateReport, firstTime, nextTime);
        List<QueueMonitor> activities12 = qmr.findByQueueAndTimeBetween(availabilityDistribute, firstTime, nextTime);
        List<QueueMonitor> activities13 = qmr.findByQueueAndTimeBetween(performanceDistribute, firstTime, nextTime);
        List<QueueMonitor> activities14 = qmr.findByQueueAndTimeBetween(qualityDistribute, firstTime, nextTime);

        Integer recordingCounter = 0;

        while (recordingCounter < observationDuration) {
            IncomingLoadData gd_load = new IncomingLoadData(recordingCounter);
            IncomingLoadData gd_rate = new IncomingLoadData(recordingCounter);

            if (!activities1.isEmpty()) {
                gd_load.setDistributedata(calculateLoad(activities1));
                gd_rate.setDistributedata(calculateRate(activities1));
            }
            if (!activities2.isEmpty()) {
                gd_load.setAvailability(calculateLoad(activities2));
                gd_rate.setAvailability(calculateRate(activities2));
            }
            if (!activities3.isEmpty()) {
                gd_load.setTemperature(calculateLoad(activities3));
                gd_rate.setTemperature(calculateRate(activities3));
            }
            if (!activities4.isEmpty()) {
                gd_load.setAvailabilityWarning(calculateLoad(activities4));
                gd_rate.setAvailabilityWarning(calculateRate(activities4));
            }
            if (!activities5.isEmpty()) {
                gd_load.setTemperatureWarning(calculateLoad(activities5));
                gd_rate.setTemperatureWarning(calculateRate(activities5));
            }
            if (!activities6.isEmpty()) {
                gd_load.setGenerateReportWarning(calculateLoad(activities6));
                gd_rate.setGenerateReportWarning(calculateRate(activities6));
            }
            if (!activities7.isEmpty()) {
                gd_load.setUser(calculateLoad(activities7));
                gd_rate.setUser(calculateRate(activities7));
            }
            if (!activities8.isEmpty()) {
                gd_load.setAvailabilityOEE(calculateLoad(activities8));
                gd_rate.setAvailabilityOEE(calculateRate(activities8));
            }
            if (!activities9.isEmpty()) {
                gd_load.setPerformanceOEE(calculateLoad(activities9));
                gd_rate.setPerformanceOEE(calculateRate(activities9));
            }
            if (!activities10.isEmpty()) {
                gd_load.setQualityOEE(calculateLoad(activities10));
                gd_rate.setQualityOEE(calculateRate(activities10));
            }
            if (!activities11.isEmpty()) {
                gd_load.setGenerateReport(calculateLoad(activities11));
                gd_rate.setGenerateReport(calculateRate(activities11));
            }
            if (!activities12.isEmpty()) {
                gd_load.setAvailabilityDistribute(calculateLoad(activities12));
                gd_rate.setAvailabilityDistribute(calculateRate(activities12));
            }
            if (!activities13.isEmpty()) {
                gd_load.setPerformanceDistribute(calculateLoad(activities13));
                gd_rate.setPerformanceDistribute(calculateRate(activities13));
            }
            if (!activities14.isEmpty()) {
                gd_load.setQualityDistribute(calculateLoad(activities14));
                gd_rate.setQualityDistribute(calculateRate(activities14));
            }


            gd_load.setWarningOperator(gd_load.getAvailabilityWarning() + gd_load.getTemperatureWarning() + gd_load.getGenerateReportWarning());
            gd_rate.setWarningOperator(gd_rate.getAvailabilityWarning() + gd_rate.getTemperatureWarning() + gd_rate.getGenerateReportWarning());

            gd_load.setOeeOperator(gd_load.getAvailabilityOEE() + gd_load.getPerformanceOEE() + gd_load.getQualityOEE());
            gd_load.setOeeOperator(gd_rate.getAvailabilityOEE() + gd_rate.getPerformanceOEE() + gd_rate.getQualityOEE());

            for (int i = 0; i<minutes; i++) {
                data_rates.add(gd_rate);
                data_amount.add(gd_load);
            }

            recordingCounter++;

            firstTime = nextTime;
            nextTime = firstTime.plusMinutes(1);
            activities1 = qmr.findByQueueAndTimeBetween(distributedata, firstTime, nextTime);
            activities2 = qmr.findByQueueAndTimeBetween(availability, firstTime, nextTime);
            activities3 = qmr.findByQueueAndTimeBetween(temperature, firstTime, nextTime);
            activities4 = qmr.findByQueueAndTimeBetween(availabilityWarning, firstTime, nextTime);
            activities5 = qmr.findByQueueAndTimeBetween(temperatureWarning, firstTime, nextTime);
            activities6 = qmr.findByQueueAndTimeBetween(generateReportWarning, firstTime, nextTime);
            activities7 = qmr.findByQueueAndTimeBetween(user, firstTime, nextTime);
            activities8 = qmr.findByQueueAndTimeBetween(availabilityOEE, firstTime, nextTime);
            activities9 = qmr.findByQueueAndTimeBetween(performanceOEE, firstTime, nextTime);
            activities10 = qmr.findByQueueAndTimeBetween(qualityOEE, firstTime, nextTime);
            activities11 = qmr.findByQueueAndTimeBetween(generateReport, firstTime, nextTime);
            activities12 = qmr.findByQueueAndTimeBetween(availabilityDistribute, firstTime, nextTime);
            activities13 = qmr.findByQueueAndTimeBetween(performanceDistribute, firstTime, nextTime);
            activities14 = qmr.findByQueueAndTimeBetween(qualityDistribute, firstTime, nextTime);

        }

        writeToFile(data_rates, "reporting/queueRate.csv");
        writeToFile(data_amount, "reporting/queueLoad.csv");
    }

    private void writeToFile(List<IncomingLoadData> data_amount, String filename) {
        CsvMapper mapper = new CsvMapper();
        CsvSchema schema = mapper.schemaFor(IncomingLoadData.class).withHeader();
        try {
            Path path = Paths.get(filename);
            if (Files.exists(path)) {
                Files.delete(path);
            }
            Files.createFile(path);
            Files.write(path, mapper.writer(schema).writeValueAsString(data_amount).getBytes());
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

    private Double calculateLoad(List<QueueMonitor> activities1) {
        Double load = 0.0;
        Integer counter = 0;
        for (QueueMonitor qm : activities1) {
            load+=qm.getAmount();
            counter++;
        }

        return ((load/counter));
    }

}
