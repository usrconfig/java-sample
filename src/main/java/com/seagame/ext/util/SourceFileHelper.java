package com.seagame.ext.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.multipart.MultipartFile;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;

/**
 * @author LamHM
 */
public class SourceFileHelper {
    public static final String EXPORT_FOLDER = "export/";
    public static final String RESOURCE_FOLDER = "resources/";
    private static final XMLInputFactory f = XMLInputFactory.newFactory();


    public static XMLStreamReader getStreamReader(String fileName) throws FileNotFoundException, XMLStreamException {
        return f.createXMLStreamReader(new FileInputStream(RESOURCE_FOLDER + fileName), "UTF-8");
    }


    public static String exportJsonFile(Object data, String fileName) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(new File(EXPORT_FOLDER + fileName), data);
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(data);
    }


    public static String updateFile(Object data, String fileName) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(new File(RESOURCE_FOLDER + fileName), data);
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(data);
    }


    public static String saveFile(MultipartFile file, String fileName) throws IOException {
        byte[] bytes = file.getBytes();
        BufferedOutputStream stream = new BufferedOutputStream(
                new FileOutputStream(new File(RESOURCE_FOLDER + fileName)));
        stream.write(bytes);
        stream.close();
        return new String(bytes);
    }
}
