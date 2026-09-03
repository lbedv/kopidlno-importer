package com.lbedv.kopidlnoimporter;

import com.lbedv.kopidlnoimporter.parser.RuianXmlParser;
import com.lbedv.kopidlnoimporter.service.AddressImportService;
import com.lbedv.kopidlnoimporter.service.DataDownloadService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password="
})
@ActiveProfiles("test")
@Transactional
class KopidlnoImporterApplicationTests {

    @MockitoBean
    private DataDownloadService dataDownloadService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private RuianXmlParser ruianXmlParser;

    @Autowired
    private AddressImportService addressImportService;

    @Test
    void testEndToEndImport() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            ZipEntry entry = new ZipEntry("test.xml");
            zos.putNextEntry(entry);
            String XML_CONTENT = """
                    <?xml version="1.0" encoding="UTF-8"?>
                    <vf:VymennyFormat xmlns:vf="urn:cz:isvs:ruian:schemas:VymennyFormatTypy:v1"
                                      xmlns:obi="urn:cz:isvs:ruian:schemas:ObecIntTypy:v1"
                                      xmlns:coi="urn:cz:isvs:ruian:schemas:CastObceIntTypy:v1">
                        <vf:Data>
                            <vf:Obce>
                                <vf:Obec>
                                    <obi:Kod>573060</obi:Kod>
                                    <obi:Nazev>Kopidlno</obi:Nazev>
                                    <obi:StatusKod>2</obi:StatusKod>
                                </vf:Obec>
                            </vf:Obce>
                            <vf:CastiObci>
                                <vf:CastObce>
                                    <coi:Kod>69299</coi:Kod>
                                    <coi:Nazev>Kopidlno Cast 1</coi:Nazev>
                                    <coi:Obec>
                                        <obi:Kod>573060</obi:Kod>
                                    </coi:Obec>
                                </vf:CastObce>
                            </vf:CastiObci>
                        </vf:Data>
                    </vf:VymennyFormat>
                    """;
            zos.write(XML_CONTENT.getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
        }

        InputStream mockStream = new ByteArrayInputStream(baos.toByteArray());
        when(dataDownloadService.downloadData()).thenReturn(mockStream);
        
        AppStartupRunner runner = new AppStartupRunner(dataDownloadService, ruianXmlParser, addressImportService);
        runner.run();

        // Verify DB
        Integer countObec = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM obec", Integer.class);
        assertEquals(1, countObec);
        
        Integer countCastObce = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM cast_obce", Integer.class);
        assertEquals(1, countCastObce);

        String nazevObec = jdbcTemplate.queryForObject("SELECT nazev FROM obec WHERE kod = 573060", String.class);
        assertEquals("Kopidlno", nazevObec);

        String nazevCastObce = jdbcTemplate.queryForObject("SELECT nazev FROM cast_obce WHERE kod = 69299", String.class);
        assertEquals("Kopidlno Cast 1", nazevCastObce);
    }
}
