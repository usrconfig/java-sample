package com.seagame.ext.entities.mail;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * @author LamHM
 */
@Getter
@Setter
@JsonInclude(Include.NON_NULL)
public class MailBase {
    @JacksonXmlProperty(localName = "Id", isAttribute = true)
    private String id;
    @JacksonXmlProperty(localName = "Group", isAttribute = true)
    private String group;
    @JacksonXmlProperty(localName = "Title", isAttribute = true)
    private String title;
    @JacksonXmlProperty(localName = "Description", isAttribute = true)
    private String content;
    @JacksonXmlProperty(localName = "ShortDescription", isAttribute = true)
    private String shortContent;
    @JacksonXmlProperty(localName = "GiftString", isAttribute = true)
    private String giftString;
    @JacksonXmlProperty(localName = "HeroString", isAttribute = true)
    private String heroString;

}
