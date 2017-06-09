package ac.at.tuwien.infosys.visp.dataaggregation;

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

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        expectedDurations.put("distributedata", 900);
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

            List<ProcessingDuration> durations = pcr.findByOperatorOrderByIdDesc(entry.getKey());

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

                if (!duration.getOperator().equals("temperature") && (!duration.getOperator().equals("availability"))) {
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

                        if (!duration.getOperator().equals("temperature") && (!duration.getOperator().equals("availability"))) {
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

                            if (!duration.getOperator().equals("temperature") && (!duration.getOperator().equals("availability"))) {
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

                    if (!duration.getOperator().equals("temperature") && (!duration.getOperator().equals("availability"))) {
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

                        if ((adoptnormal > 0) && (adopttwice > 0)) {
                            ProcessingDurationData pdd = new ProcessingDurationData();
                            if (adoptnormal<0) {
                                pdd.setDuration(adoptnormal * -1.0);
                            } else {
                                pdd.setDuration(adoptnormal + 0.0);
                            }
                            timeToAdoptSingle.add(pdd);
                            timeToAdapts.add(new TimeToAdapt(entry.getKey(), adopttwice , adoptnormal));
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
            container.setTotal(total);
            container.setViolations(violations);
            container.setNoViolations(noViolations);
            container.setMaxDoubleTimeViolation(maxDoubleTimeViolation);
            container.setMaxFiveTimeViolation(maxFiveTimeViolations);
            container.setMoreThanFiveTimeViolation(moreThanFiveTimeViolation);
            container.setAverage(average);

            data.add(container);

            CsvMapper mapper = new CsvMapper();
            CsvSchema schema = mapper.schemaFor(ProcessingDurationData.class);
            try {
                Path path = Paths.get("reporting/" + entry.getKey() + "_timetoadopt.csv");
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
            Files.write(slaComplianceMetricsPath, ("RealTimeCompliance: " + globalRealTimeCompliance/globalCounter + "\n").getBytes());
            Files.write(slaComplianceMetricsPath, ("NearRealTimeCompliance: " + globalnearRealTimeCompliance/globalCounter + "\n").getBytes(), StandardOpenOption.APPEND);
            Files.write(slaComplianceMetricsPath, ("RelaxedTimeCompliance: " + globalrelaxedCompliance/globalCounter + "\n").getBytes(), StandardOpenOption.APPEND);
            Files.write(slaComplianceMetricsPath, ("RealTimeCompliance wo temp and avail: " + globalRealTimeCompliancewithoutTempAndAvailability/globalCounterwithoutTempAndAvailability + "\n").getBytes(), StandardOpenOption.APPEND);
            Files.write(slaComplianceMetricsPath, ("NearRealTimeCompliance wo temp and avail: " + globalnearRealTimeCompliancewithoutTempAndAvailability/globalCounterwithoutTempAndAvailability + "\n").getBytes(), StandardOpenOption.APPEND);
            Files.write(slaComplianceMetricsPath, ("RelaxedTimeCompliance wo temp and avail: " + globalrelaxedCompliancewithoutTempAndAvailability/globalCounterwithoutTempAndAvailability + "\n").getBytes(), StandardOpenOption.APPEND);

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
            Files.write(path, ("TimeToAdoptRealtime - mean: " + realTimeAdopt.getMean() + "\n").getBytes());
            Files.write(path, ("TimeToAdoptRealtime - standarddeviation: " + realTimeAdopt.getStandardDeviation() + "\n").getBytes(), StandardOpenOption.APPEND);
            realTimeAdopt.setMeanImpl(new Median());
            Files.write(path, ("TimeToAdoptRealtime - median: " + realTimeAdopt.getMean() + "\n").getBytes(), StandardOpenOption.APPEND);
            Files.write(path, ("TimeToAdoptRealtime - standarddeviation: " + realTimeAdopt.getStandardDeviation() + "\n").getBytes(), StandardOpenOption.APPEND);
            Files.write(path, ("TimeToAdoptRealtime - max: " + realTimeAdopt.getMax() + "\n").getBytes(), StandardOpenOption.APPEND);
            Files.write(path, ("TimeToAdoptRealtime - min: " + realTimeAdopt.getMin() + "\n").getBytes(), StandardOpenOption.APPEND);

            nearrealTimeAdopt.setMeanImpl(new Mean());
            Files.write(path, ("TimeToAdoptNearRealtime - mean: " + nearrealTimeAdopt.getMean() + "\n").getBytes(), StandardOpenOption.APPEND);
            Files.write(path, ("TimeToAdoptNearRealtime - standarddeviation mean: " + nearrealTimeAdopt.getStandardDeviation() + "\n").getBytes(), StandardOpenOption.APPEND);
            nearrealTimeAdopt.setMeanImpl(new Median());
            Files.write(path, ("TimeToAdoptNearRealtime - median: " + nearrealTimeAdopt.getMean() + "\n").getBytes(), StandardOpenOption.APPEND);
            Files.write(path, ("TimeToAdoptNearRealtime - standarddeviation median: " + nearrealTimeAdopt.getStandardDeviation() + "\n").getBytes(), StandardOpenOption.APPEND);
            Files.write(path, ("TimeToAdoptNearRealtime - max: " + nearrealTimeAdopt.getMax() + "\n").getBytes(), StandardOpenOption.APPEND);
            Files.write(path, ("TimeToAdoptNearRealtime - min: " + nearrealTimeAdopt.getMin() + "\n").getBytes(), StandardOpenOption.APPEND);

        } catch (IOException e) {
            LOG.error(e.getMessage());
        }

    }
}
