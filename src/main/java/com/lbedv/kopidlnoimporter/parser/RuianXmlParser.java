package com.lbedv.kopidlnoimporter.parser;

import com.lbedv.kopidlnoimporter.dto.CastObceDto;
import com.lbedv.kopidlnoimporter.dto.ObecDto;
import com.lbedv.kopidlnoimporter.dto.ParseResult;
import org.springframework.stereotype.Component;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Component
public class RuianXmlParser {

    private static final String NS_VF = "urn:cz:isvs:ruian:schemas:VymennyFormatTypy:v1";
    private static final String NS_OBI = "urn:cz:isvs:ruian:schemas:ObecIntTypy:v1";
    private static final String NS_COI = "urn:cz:isvs:ruian:schemas:CastObceIntTypy:v1";

    private final XMLInputFactory factory;

    public RuianXmlParser() {
        this.factory = XMLInputFactory.newInstance();
        this.factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
        this.factory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
    }

    public ParseResult parse(InputStream inputStream) throws Exception {
        List<ObecDto> obce = new ArrayList<>();
        List<CastObceDto> castiObce = new ArrayList<>();
        boolean xmlFound = false;

        try (ZipInputStream zis = new ZipInputStream(inputStream)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().endsWith(".xml")) {
                    parseXmlFile(zis, obce, castiObce);
                    xmlFound = true;
                    break;
                }
            }
        }

        if (!xmlFound) {
            throw new IllegalArgumentException("No .xml file found in the provided ZIP archive");
        }

        return new ParseResult(obce, castiObce);
    }

    private void parseXmlFile(InputStream xmlStream, List<ObecDto> obce, List<CastObceDto> castiObce) throws XMLStreamException {
        XMLStreamReader reader = factory.createXMLStreamReader(xmlStream);
        try {
            while (reader.hasNext()) {
                int event = reader.next();
                if (event == XMLStreamConstants.START_ELEMENT) {
                    String ns = reader.getNamespaceURI();
                    String localName = reader.getLocalName();

                    if (NS_VF.equals(ns) && "Obec".equals(localName)) {
                        obce.add(parseObec(reader));
                    } else if (NS_VF.equals(ns) && "CastObce".equals(localName)) {
                        castiObce.add(parseCastObce(reader));
                    }
                }
            }
        } finally {
            reader.close();
        }
    }

    private ObecDto parseObec(XMLStreamReader reader) throws XMLStreamException {
        Long kod = null;
        String nazev = null;

        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                String ns = reader.getNamespaceURI();
                String localName = reader.getLocalName();

                if (NS_OBI.equals(ns) && "Kod".equals(localName)) {
                    kod = parseLongSafe(reader);
                } else if (NS_OBI.equals(ns) && "Nazev".equals(localName)) {
                    nazev = reader.getElementText();
                } else {
                    skipElement(reader);
                }
            } else if (event == XMLStreamConstants.END_ELEMENT) {
                if (NS_VF.equals(reader.getNamespaceURI()) && "Obec".equals(reader.getLocalName())) {
                    break;
                }
            }
        } // End of while loop

        if (kod == null) {
            throw new XMLStreamException("Missing mandatory element 'Kod' for Obec", reader.getLocation());
        }
        if (nazev == null) {
            throw new XMLStreamException("Missing mandatory element 'Nazev' for Obec", reader.getLocation());
        }
        return new ObecDto(kod, nazev);
    }

    private CastObceDto parseCastObce(XMLStreamReader reader) throws XMLStreamException {
        Long kod = null;
        String nazev = null;
        Long obecKod = null;

        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                String ns = reader.getNamespaceURI();
                String localName = reader.getLocalName();

                if (NS_COI.equals(ns) && "Kod".equals(localName)) {
                    kod = parseLongSafe(reader);
                } else if (NS_COI.equals(ns) && "Nazev".equals(localName)) {
                    nazev = reader.getElementText();
                } else if (NS_COI.equals(ns) && "Obec".equals(localName)) {
                    obecKod = parseObecKod(reader);
                } else {
                    skipElement(reader);
                }
            } else if (event == XMLStreamConstants.END_ELEMENT) {
                if (NS_VF.equals(reader.getNamespaceURI()) && "CastObce".equals(reader.getLocalName())) {
                    break;
                }
            }
        } // End of while loop

        if (kod == null) {
            throw new XMLStreamException("Missing mandatory element 'Kod' for CastObce", reader.getLocation());
        }
        if (nazev == null) {
            throw new XMLStreamException("Missing mandatory element 'Nazev' for CastObce", reader.getLocation());
        }
        if (obecKod == null) {
            throw new XMLStreamException("Missing mandatory element 'Obec' (obecKod) for CastObce", reader.getLocation());
        }
        return new CastObceDto(kod, nazev, obecKod);
    }

    private Long parseObecKod(XMLStreamReader reader) throws XMLStreamException {
        Long obecKod = null;
        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                String ns = reader.getNamespaceURI();
                String localName = reader.getLocalName();

                if (NS_OBI.equals(ns) && "Kod".equals(localName)) {
                    obecKod = parseLongSafe(reader);
                } else {
                    skipElement(reader);
                }
            } else if (event == XMLStreamConstants.END_ELEMENT) {
                if (NS_COI.equals(reader.getNamespaceURI()) && "Obec".equals(reader.getLocalName())) {
                    break;
                }
            }
        }
        return obecKod;
    }

    private Long parseLongSafe(XMLStreamReader reader) throws XMLStreamException {
        String text = reader.getElementText();
        if (text == null || text.trim().isEmpty()) {
            throw new XMLStreamException("Empty value for numeric element " + reader.getLocalName(), reader.getLocation());
        }
        try {
            return Long.parseLong(text.trim());
        } catch (NumberFormatException e) {
            throw new XMLStreamException("Invalid numeric format for element " + reader.getLocalName() + ": '" + text + "'", reader.getLocation(), e);
        }
    }

    private void skipElement(XMLStreamReader reader) throws XMLStreamException {
        int depth = 1;
        while (depth > 0 && reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                depth++;
            } else if (event == XMLStreamConstants.END_ELEMENT) {
                depth--;
            }
        }
    }
}