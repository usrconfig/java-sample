package com.seagame.ext.entities.item;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author LamHM
 */
@Getter
@Setter
@ToString
@JsonInclude(Include.NON_NULL)
public class ItemDropBase {
    @JacksonXmlProperty(localName = "daicanhgioi", isAttribute = true)
    private int id;
    @JacksonXmlProperty(localName = "capnguyenlieu", isAttribute = true)
    private int level;
    @JacksonXmlProperty(localName = "txtcapnguyenlieu", isAttribute = true)
    private String levelTxt;
    @JacksonXmlProperty(localName = "captinhhuyet", isAttribute = true)
    private int levelTinhHuyet;
    @JacksonXmlProperty(localName = "txtcaptinhhuyet", isAttribute = true)
    private String levelTinhHuyetTxt;
}
