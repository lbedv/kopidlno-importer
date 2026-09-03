package com.lbedv.kopidlnoimporter;

import com.lbedv.kopidlnoimporter.dto.ParseResult;
import com.lbedv.kopidlnoimporter.parser.RuianXmlParser;
import com.lbedv.kopidlnoimporter.service.AddressImportService;
import com.lbedv.kopidlnoimporter.service.DataDownloadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Component
@Profile("!test")
public class AppStartupRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AppStartupRunner.class);

    private final DataDownloadService dataDownloadService;
    private final RuianXmlParser ruianXmlParser;
    private final AddressImportService addressImportService;

    public AppStartupRunner(DataDownloadService dataDownloadService, RuianXmlParser ruianXmlParser, AddressImportService addressImportService) {
        this.dataDownloadService = dataDownloadService;
        this.ruianXmlParser = ruianXmlParser;
        this.addressImportService = addressImportService;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("Starting RUIAN data import...");

        try (InputStream inputStream = dataDownloadService.downloadData()) {
            log.info("Data downloaded successfully. Starting XML parsing...");
            ParseResult result = ruianXmlParser.parse(inputStream);
            
            log.info("Parsing finished. Found {} obce and {} casti obce.", result.obce().size(), result.castiObce().size());
            
            log.info("Starting database import...");
            addressImportService.importData(result);
            
            log.info("Import completed successfully.");
        } catch (Exception e) {
            log.error("Error during RUIAN data import", e);
            throw e; // Rethrow to ensure Spring Boot exits with a non-zero exit code
        }
    }
}
