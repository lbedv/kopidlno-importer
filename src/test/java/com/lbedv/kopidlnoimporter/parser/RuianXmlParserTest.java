package com.lbedv.kopidlnoimporter.parser;

import com.lbedv.kopidlnoimporter.dto.ParseResult;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RuianXmlParserTest {

    @Test
    void testParse() throws Exception {
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
                                <vf:CastObce>
                                    <coi:Kod>69300</coi:Kod>
                                    <coi:Nazev>Kopidlno Cast 2</coi:Nazev>
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

        InputStream in = new ByteArrayInputStream(baos.toByteArray());
        RuianXmlParser parser = new RuianXmlParser();
        ParseResult result = parser.parse(in);

        assertEquals(1, result.obce().size());
        assertEquals(573060L, result.obce().get(0).kod());
        assertEquals("Kopidlno", result.obce().get(0).nazev());

        assertEquals(2, result.castiObce().size());
        assertEquals(69299L, result.castiObce().get(0).kod());
        assertEquals("Kopidlno Cast 1", result.castiObce().get(0).nazev());
        assertEquals(573060L, result.castiObce().get(0).obecKod());
    }
}
