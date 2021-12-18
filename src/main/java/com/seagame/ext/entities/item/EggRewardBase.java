package com.seagame.ext.entities.item;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author LamHM
 */
@Getter
@Setter
@ToString
@JsonInclude(Include.NON_NULL)
public class EggRewardBase implements Serializable {
    @JacksonXmlProperty(localName = "Index", isAttribute = true)
    private String Index;
    @JacksonXmlProperty(localName = "Reward", isAttribute = true)
    private String Reward;
    @JacksonXmlProperty(localName = "Count", isAttribute = true)
    private int Count;
    @JacksonXmlProperty(localName = "Rate", isAttribute = true)
    private int Rate;
}
