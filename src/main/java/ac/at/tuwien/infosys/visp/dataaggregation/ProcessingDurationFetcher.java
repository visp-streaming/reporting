package ac.at.tuwien.infosys.visp.dataaggregation;

import ac.at.tuwien.infosys.visp.entities.ProcessingDurationData;
import ac.at.tuwien.infosys.visp.repository.ProcessingDurationRepository;
import ac.at.tuwien.infosys.visp.repository.entities.ProcessingDuration;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProcessingDurationFetcher {

    @Autowired
    private ProcessingDurationRepository pcr;

    private static final Logger LOG = LoggerFactory.getLogger(ProcessingDurationFetcher.class);

    @PreDestroy
    public void generateCSV() {
        obtainProcessingDurationData("distributedata");
        obtainProcessingDurationData("availability");
        obtainProcessingDurationData("calculateperformance");
        obtainProcessingDurationData("calculateavailability");
        obtainProcessingDurationData("calculatequality");
        obtainProcessingDurationData("temperature");
        obtainProcessingDurationData("calculateoee");
        obtainProcessingDurationData("warning");
        obtainProcessingDurationData("generatereport");
    }

    private ArrayList<ProcessingDurationData> obtainProcessingDurationData(String parameter) {
        List<ProcessingDuration> items = pcr.findByOperator(parameter);

        ArrayList<ProcessingDurationData> results = new ArrayList<>();

        for (ProcessingDuration pd : items) {
            ProcessingDurationData p = new ProcessingDurationData();
            if (pd.getDuration()<0) {
                p.setDuration(pd.getDuration() * -1);
            } else {
                p.setDuration(pd.getDuration());
            }
            results.add(p);
        }

        CsvMapper mapper = new CsvMapper();
        CsvSchema schema = mapper.schemaFor(ProcessingDurationData.class);
        try {
            Path dirPath = Paths.get("reporting/operators/");
            Path path = Paths.get("reporting/operators/duration_" + parameter + ".csv");

            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }

            if (Files.exists(path)) {
                Files.delete(path);
            }
            Files.createFile(path);
            Files.write(path, mapper.writer(schema).writeValueAsString(results).getBytes());
        } catch (IOException e) {
            LOG.error(e.getMessage());
        }

        return results;
    }

}
