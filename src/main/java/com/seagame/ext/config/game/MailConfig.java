package com.seagame.ext.config.game;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.seagame.ext.entities.mail.MailBase;
import com.seagame.ext.util.SourceFileHelper;

import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author LamHM
 */
public class MailConfig {
    public static final String MAIL_CONFIG = "mails.xml";
    private static MailConfig instance;
    private Map<String, List<MailBase>> mailGroup;
    private Map<String, MailBase> mailBaseMap;


    public static MailConfig getInstance() {
        if (instance == null) {
            instance = new MailConfig();
        }

        return instance;
    }


    private MailConfig() {
        loadMailBase();
    }


    private void loadMailBase() {
        mailGroup = new HashMap<>();
        mailBaseMap = new HashMap<>();
        try {
            Map<String, MailBase> mailBaseMap = new HashMap<>();
            XMLStreamReader sr = SourceFileHelper.getStreamReader(MAIL_CONFIG);
            XmlMapper mapper = new XmlMapper();
            sr.next();
            sr.next();
            MailBase mail;
            while (sr.hasNext()) {
                try {
                    mail = mapper.readValue(sr, MailBase.class);
                    mailBaseMap.put(mail.getId(), mail);
                } catch (NoSuchElementException ignored) {
                }
            }

            this.mailBaseMap = mailBaseMap;
            mailGroup = mailBaseMap.values().stream().collect(Collectors.groupingBy(MailBase::getGroup));

            sr.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public List<MailBase> getMailByGroup(String groupId) {
        return new ArrayList<>(mailGroup.get(groupId));
    }


    public MailBase getMailById(String id) {
        return mailBaseMap.get(id);
    }


    public String reload() throws IOException {
        loadMailBase();
        return "";
    }


    public void writeToJsonFile() throws Exception {
        SourceFileHelper.exportJsonFile(mailBaseMap.values(),
                "mail.json");
    }


    public static void main(String[] args) throws Exception {
        MailConfig.getInstance().writeToJsonFile();
    }

}
