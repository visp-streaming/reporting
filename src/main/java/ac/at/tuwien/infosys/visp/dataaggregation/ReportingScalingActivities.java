package ac.at.tuwien.infosys.visp.dataaggregation;

import ac.at.tuwien.infosys.visp.entities.GraphData;
import ac.at.tuwien.infosys.visp.entities.OperatorContainer;
import ac.at.tuwien.infosys.visp.repository.ScalingActivityRepository;
import ac.at.tuwien.infosys.visp.repository.entities.ScalingActivity;
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
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReportingScalingActivities {

    @Autowired
    private ScalingActivityRepository sar;

    private static final Logger LOG = LoggerFactory.getLogger(ReportingScalingActivities.class);

    @Value("${observationDuration}")
    private Integer observationDuration;

    @PreDestroy
    public void generateCSV() {

        List<GraphData> data = new ArrayList<>();

        ScalingActivity firstSa = sar.findFirstByOrderByTimeAsc();

        DateTime firstTime = firstSa.getTime();
        DateTime nextTime = firstTime.plusMinutes(1);

        List<ScalingActivity> activities = sar.findByTimeBetween(firstTime, nextTime);

        Integer vmCounter = 0;
        Integer containerCounter = 0;

        Integer totalVMStarts = 0;
        Integer totalVMProlongings = 0;
        Integer totalVMDownscalings = 0;
        Integer totalContainerUpscalings = 0;
        Integer totalContainerDownscalings = 0;
        Integer totalContainerMigrations = 0;

        Integer recordingCounter = 0;

        List<OperatorContainer> operators = new ArrayList<>();

        while (recordingCounter < observationDuration) {
            GraphData gd = new GraphData(firstTime.toString(), recordingCounter);

            OperatorContainer opContainer = new OperatorContainer();
            opContainer.setTime(firstTime.toString());

            for (ScalingActivity sa : activities) {
                switch (sa.getScalingActivity()) {
                    case "startVM":
                        gd.vmUpInc();
                        vmCounter++;
                        totalVMStarts++;
                        break;
                    case "stopVM":
                        gd.vmDownInc();
                        vmCounter--;
                        totalVMDownscalings++;
                        break;
                    case "prolongLease":
                        gd.vmProlongLease();
                        totalVMProlongings++;
                        break;
                    case "scaleup":
                        totalContainerUpscalings++;
                        gd.operatorUpInc();
                        containerCounter++;
                        switch (sa.getOperator()) {
                            case "distributedata" : opContainer.distributedataInc(); break;
                            case "availability" : opContainer.availabilityInc(); break;
                            case "calculateperformance" : opContainer.calculateperformanceInc(); break;
                            case "calculateavailability" : opContainer.calculateavailabilityInc(); break;
                            case "calculatequality" : opContainer.calculatequalityInc(); break;
                            case "temperature" : opContainer.temperatureInc(); break;
                            case "warning" : opContainer.warningInc(); break;
                            case "generatereport" : opContainer.generatereportInc(); break;
                        }
                        break;
                    case "scaledown":
                        totalContainerDownscalings++;
                        gd.operatorDownInc();
                        containerCounter--;
                        switch (sa.getOperator()) {
                            case "distributedata" : opContainer.distributedataDec(); break;
                            case "availability" : opContainer.availabilityDec(); break;
                            case "calculateperformance" : opContainer.calculateperformanceDec(); break;
                            case "calculateavailability" : opContainer.calculateavailabilityDec(); break;
                            case "calculatequality" : opContainer.calculatequalityDec(); break;
                            case "temperature" : opContainer.temperatureDec(); break;
                            case "warning" : opContainer.warningDec(); break;
                            case "generatereport" : opContainer.generatereportDec(); break;
                        }
                        break;
                    case "migration":
                        totalContainerUpscalings--;
                        totalContainerDownscalings--;
                        totalContainerMigrations++;
                        gd.operatorMigrateInc();
                        gd.operatorDownDec();
                        gd.operatorUpDec();
                        break;
                    default: //do nothing
                }
            }

            operators.add(opContainer);

            gd.setTotalVMs(vmCounter);
            gd.setTotalContainer(containerCounter);
            recordingCounter++;

            data.add(gd);

            firstTime = nextTime;
            nextTime = firstTime.plusMinutes(1);
            activities = sar.findByTimeBetween(firstTime, nextTime);
        }

        CsvMapper mapper1 = new CsvMapper();
        CsvSchema schema1 = mapper1.schemaFor(OperatorContainer.class).withHeader();
        try {
            Path path = Paths.get("reporting/operatorQuantity.csv");
            if (Files.exists(path)) {
                Files.delete(path);
            }
            Files.createFile(path);
            Files.write(path, mapper1.writer(schema1).writeValueAsString(operators).getBytes());
        } catch (IOException e) {
            LOG.error(e.getMessage());
        }

        Path overalmetricsPath = Paths.get("reporting/costMetrics.csv");
        try {
            if (Files.exists(overalmetricsPath)) {
                Files.delete(overalmetricsPath);
            }
            Files.createFile(overalmetricsPath);
            Files.write(overalmetricsPath, ("TotalVMStarts: " + totalVMStarts + "\n").getBytes());
            Files.write(overalmetricsPath, ("TotalVMProlongings: " + totalVMProlongings + "\n").getBytes(), StandardOpenOption.APPEND);
            Files.write(overalmetricsPath, ("TotalVMDownscalings: " + totalVMDownscalings + "\n").getBytes(), StandardOpenOption.APPEND);
            Files.write(overalmetricsPath, ("TotalContainerUpscalings: " + totalContainerUpscalings + "\n").getBytes(), StandardOpenOption.APPEND);
            Files.write(overalmetricsPath, ("TotalContainerDownscalings: " + totalContainerDownscalings + "\n").getBytes(), StandardOpenOption.APPEND);
            Files.write(overalmetricsPath, ("TotalContainerMigrations: " + totalContainerMigrations + "\n").getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            LOG.error(e.getMessage());
        }


        CsvMapper mapper = new CsvMapper();
        CsvSchema schema = mapper.schemaFor(GraphData.class).withHeader();
        try {
            Path path = Paths.get("reporting/scalingactivities.csv");
            if (Files.exists(path)) {
                Files.delete(path);
            }
            Files.createFile(path);
            Files.write(path, mapper.writer(schema).writeValueAsString(data).getBytes());
        } catch (IOException e) {
            LOG.error(e.getMessage());
        }
    }

}
