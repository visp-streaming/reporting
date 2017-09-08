package ac.at.tuwien.infosys.visp.dataaggregation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PreDestroy;

import ac.at.tuwien.infosys.visp.entities.ProcessingDurationData;
import ac.at.tuwien.infosys.visp.entities.SLAComplianceContainer;
import ac.at.tuwien.infosys.visp.entities.TimeToAdapt;
import ac.at.tuwien.infosys.visp.repository.ProcessingDurationRepository;
import ac.at.tuwien.infosys.visp.repository.entities.ProcessingDuration;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.rank.Median;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SLACompliance {

    @Autowired
    private ProcessingDurationRepository pcr;

    private Map<String, Integer> expectedDurations;

    @Value("${observationDuration}")
    private Integer observationDuration;

    private static final Logger LOG = LoggerFactory.getLogger(SLACompliance.class);

    @PreDestroy
    public void generateCSV() {

        expectedDurations = new HashMap<>();

        expectedDurations.put("distributedata", 1500);
        expectedDurations.put("availability", 600);
        expectedDurations.put("calculateperformance", 750);
        expectedDurations.put("calculateavailability", 750);
        expectedDurations.put("calculatequality", 750);
        expectedDurations.put("temperature", 600);
        expectedDurations.put("calculateoee", 700);
        expectedDurations.put("warning", 500);
        expectedDurations.put("generatereport", 1300);

        List<SLAComplianceContainer> data  = new ArrayList<>();

        List<TimeToAdapt> timeToAdapts = new ArrayList<>();

        DateTime time = null;


        Double globalCounter = 0.0;
        Double globalRealTimeCompliance = 0.0;
        Double globalnearRealTimeCompliance = 0.0;
        Double globalrelaxedCompliance = 0.0;

        Double globalCounterwithoutTempAndAvailability = 0.0;
        Double globalRealTimeCompliancewithoutTempAndAvailability = 0.0;
        Double globalnearRealTimeCompliancewithoutTempAndAvailability = 0.0;
        Double globalrelaxedCompliancewithoutTempAndAvailability = 0.0;


        for (Map.Entry<String, Integer> entry : expectedDurations.entrySet()) {
            List<ProcessingDurationData> timeToAdoptSingle = new ArrayList<>();

            List<ProcessingDuration> durations = pcr.findByOperatortypeOrderByIdAsc(entry.getKey());

            Integer total = 0;
            Integer violations = 0;
            Integer noViolations = 0;
            Integer maxDoubleTimeViolation = 0;
            Integer maxFiveTimeViolations = 0;
            Integer moreThanFiveTimeViolation = 0;
            Double average = 0.0;

            DateTime incident = null;
            DateTime resolveDouble = null;
            DateTime resolveNormal = null;


            for (ProcessingDuration duration : durations) {


                if (time==null) {
                    time = duration.getTime().plusMinutes(observationDuration);
                }

                if (duration.getTime().isAfter(time)) {
                    continue;
                }

                globalCounter++;

                if (!duration.getOperatortype().equals("temperature") && (!duration.getOperatortype().equals("availability"))) {
                    globalCounterwithoutTempAndAvailability++;
                }

                average+=duration.getDuration();
                total++;
                if (duration.getDuration() > entry.getValue()) {
                    violations++;

                    if (duration.getDuration() < (entry.getValue() * 2)) {
                        maxDoubleTimeViolation++;
                        globalnearRealTimeCompliance++;
                        globalrelaxedCompliance++;

                        if (!duration.getOperatortype().equals("temperature") && (!duration.getOperatortype().equals("availability"))) {
                            globalnearRealTimeCompliancewithoutTempAndAvailability++;
                            globalrelaxedCompliancewithoutTempAndAvailability++;
                        }


                        if (incident != null) {
                            resolveDouble = new DateTime(duration.getTime());
                        }

                    } else {
                        if (duration.getDuration() < (entry.getValue() * 5)) {
                            maxFiveTimeViolations++;
                            globalrelaxedCompliance++;

                            if (!duration.getOperatortype().equals("temperature") && (!duration.getOperatortype().equals("availability"))) {
                                globalrelaxedCompliancewithoutTempAndAvailability++;
                            }

                        } else {
                            moreThanFiveTimeViolation++;
                            incident = new DateTime(duration.getTime());
                        }
                    }
                } else {
                    noViolations++;
                    globalRealTimeCompliance++;
                    globalnearRealTimeCompliance++;
                    globalrelaxedCompliance++;

                    if (!duration.getOperatortype().equals("temperature") && (!duration.getOperatortype().equals("availability"))) {
                        globalRealTimeCompliancewithoutTempAndAvailability++;
                        globalnearRealTimeCompliancewithoutTempAndAvailability++;
                        globalrelaxedCompliancewithoutTempAndAvailability++;
                    }


                    if (incident != null) {
                        resolveNormal = new DateTime(duration.getTime());

                        if (resolveDouble == null) {
                            resolveDouble = resolveNormal;
                        }

                        Integer adoptnormal = incident.getMillisOfDay() - resolveNormal.getMillisOfDay();
                        Integer adopttwice = incident.getMillisOfDay() - resolveDouble.getMillisOfDay();

                        if ((Math.abs(adoptnormal) > 0) && (Math.abs(adopttwice) > 0)) {
                            ProcessingDurationData pdd = new ProcessingDurationData();
                                pdd.setDuration(Math.abs(adoptnormal) + 0.0);
                            timeToAdoptSingle.add(pdd);
                            timeToAdapts.add(new TimeToAdapt(entry.getKey(), Math.abs(adopttwice) , Math.abs(adoptnormal)));
                        }

                        incident = null;
                        resolveDouble = null;
                        resolveNormal = null;
                    }
                }
            }

            average/=total;

            SLAComplianceContainer container = new SLAComplianceContainer();
            container.setOperator(entry.getKey());
            container.setTotalItems(total);
            container.setTotalViolations(violations);
            container.setNoViolations(noViolations);
            container.setMaxDoubleTimeViolation(maxDoubleTimeViolation);
            container.setMaxFiveTimeViolation(maxFiveTimeViolations);
            container.setMoreThanFiveTimeViolation(moreThanFiveTimeViolation);
            container.setAverage(average);

            data.add(container);

            CsvMapper mapper = new CsvMapper();
            CsvSchema schema = mapper.schemaFor(ProcessingDurationData.class);
            try {
                Path dirPath = Paths.get("reporting/operators/");
                Path path = Paths.get("reporting/operators/timeToAdapt_" + entry.getKey() + ".csv");

                if (!Files.exists(dirPath)) {
                    Files.createDirectories(dirPath);
                }

                if (Files.exists(path)) {
                    Files.delete(path);
                }
                Files.createFile(path);
                Files.write(path, mapper.writer(schema).writeValueAsString(timeToAdoptSingle).getBytes());
            } catch (IOException e) {
                LOG.error(e.getMessage());
            }
        }


        Path slaComplianceMetricsPath = Paths.get("reporting/complianceMetrics.csv");
        try {
            if (Files.exists(slaComplianceMetricsPath)) {
                Files.delete(slaComplianceMetricsPath);
            }
            Files.createFile(slaComplianceMetricsPath);


            Integer totalItems = 0;
            Integer maxNearRealtimeViolations = 0;
            Integer maxRelaxedTimeViolations = 0;
            Integer moreThanRelaxedTimeViolations = 0;
            Integer noViolations = 0;


            for (SLAComplianceContainer container : data) {
                totalItems += container.getTotalItems();
                maxNearRealtimeViolations += container.getMaxDoubleTimeViolation();
                maxRelaxedTimeViolations += container.getMaxFiveTimeViolation();
                moreThanRelaxedTimeViolations += container.getMoreThanFiveTimeViolation();
                noViolations += container.getNoViolations();
            }

            Files.write(slaComplianceMetricsPath, ("Total Items: " + totalItems + "\n").getBytes());
            Files.write(slaComplianceMetricsPath, ("No violations: " + noViolations + "\n").getBytes(), StandardOpenOption.APPEND);
            Files.write(slaComplianceMetricsPath, ("Max Near-Realtime Violations: " + maxNearRealtimeViolations + "\n").getBytes(), StandardOpenOption.APPEND);
            Files.write(slaComplianceMetricsPath, ("Max Relaxed-Time Violations: " + maxRelaxedTimeViolations + "\n").getBytes(), StandardOpenOption.APPEND);
            Files.write(slaComplianceMetricsPath, ("More than Relaxed-Time violations: " + moreThanRelaxedTimeViolations + "\n").getBytes(), StandardOpenOption.APPEND);

            Files.write(slaComplianceMetricsPath, ("Real Time Compliance without temperature and availability: " + globalRealTimeCompliancewithoutTempAndAvailability/globalCounterwithoutTempAndAvailability + "\n").getBytes(), StandardOpenOption.APPEND);
            Files.write(slaComplianceMetricsPath, ("Near Real Time Compliance without temperature and availability: " + globalnearRealTimeCompliancewithoutTempAndAvailability/globalCounterwithoutTempAndAvailability + "\n").getBytes(), StandardOpenOption.APPEND);
            Files.write(slaComplianceMetricsPath, ("Relaxed Time Compliance without temperature and availability: " + globalrelaxedCompliancewithoutTempAndAvailability/globalCounterwithoutTempAndAvailability + "\n").getBytes(), StandardOpenOption.APPEND);

        } catch (IOException e) {
            LOG.error(e.getMessage());
        }

        CsvMapper mapper = new CsvMapper();
        CsvSchema schema = mapper.schemaFor(SLAComplianceContainer.class).withHeader();
        try {
            Path path = Paths.get("reporting/containerQosCompliance.csv");
            if (Files.exists(path)) {
                Files.delete(path);
            }
            Files.createFile(path);
            Files.write(path, mapper.writer(schema).writeValueAsString(data).getBytes());
        } catch (IOException e) {
            LOG.error(e.getMessage());
        }

        try {
            Path path = Paths.get("reporting/timeToAdapt.csv");
            if (Files.exists(path)) {
                Files.delete(path);
            }
            Files.createFile(path);

            DescriptiveStatistics realTimeAdopt = new DescriptiveStatistics();
            DescriptiveStatistics nearrealTimeAdopt = new DescriptiveStatistics();

            for (TimeToAdapt t : timeToAdapts) {
                realTimeAdopt.addValue(t.getTimeToAdoptInTime());
                nearrealTimeAdopt.addValue(t.getTimeToAdoptTwiceTheTime());
            }

            realTimeAdopt.setMeanImpl(new Mean());
            Files.write(path, ("Realtime - mean: " + realTimeAdopt.getMean() + "\n").getBytes());
            Files.write(path, ("Realtime - standarddeviation mean: " + realTimeAdopt.getStandardDeviation() + "\n").getBytes(), StandardOpenOption.APPEND);
            realTimeAdopt.setMeanImpl(new Median());
            Files.write(path, ("Realtime - median: " + realTimeAdopt.getMean() + "\n").getBytes(), StandardOpenOption.APPEND);
            Files.write(path, ("Realtime - standarddeviation meadian: " + realTimeAdopt.getStandardDeviation() + "\n").getBytes(), StandardOpenOption.APPEND);
            Files.write(path, ("Realtime - max: " + realTimeAdopt.getMax() + "\n").getBytes(), StandardOpenOption.APPEND);
            Files.write(path, ("Realtime - min: " + realTimeAdopt.getMin() + "\n").getBytes(), StandardOpenOption.APPEND);

            nearrealTimeAdopt.setMeanImpl(new Mean());
            Files.write(path, ("Near-Realtime - mean: " + nearrealTimeAdopt.getMean() + "\n").getBytes(), StandardOpenOption.APPEND);
            Files.write(path, ("Near-Realtime - standarddeviation mean: " + nearrealTimeAdopt.getStandardDeviation() + "\n").getBytes(), StandardOpenOption.APPEND);
            nearrealTimeAdopt.setMeanImpl(new Median());
            Files.write(path, ("Near-Realtime - median: " + nearrealTimeAdopt.getMean() + "\n").getBytes(), StandardOpenOption.APPEND);
            Files.write(path, ("Near-Realtime - standarddeviation median: " + nearrealTimeAdopt.getStandardDeviation() + "\n").getBytes(), StandardOpenOption.APPEND);
            Files.write(path, ("Near-Realtime - max: " + nearrealTimeAdopt.getMax() + "\n").getBytes(), StandardOpenOption.APPEND);
            Files.write(path, ("Near-Realtime - min: " + nearrealTimeAdopt.getMin() + "\n").getBytes(), StandardOpenOption.APPEND);

        } catch (IOException e) {
            LOG.error(e.getMessage());
        }

    }
}
