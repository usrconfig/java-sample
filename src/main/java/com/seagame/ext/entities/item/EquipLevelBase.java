package com.seagame.ext.entities.item;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * @author LamHM
 */
@Getter
@Setter
@ToString
@JsonInclude(Include.NON_DEFAULT)
public class EquipLevelBase implements Serializable {
    @JacksonXmlProperty(localName = "Level", isAttribute = true)
    private int Level;
    @JacksonXmlProperty(localName = "CombineMaterial", isAttribute = true)
    private String CombineMaterial;
    @JacksonXmlProperty(localName = "KENCost", isAttribute = true)
    private String KENCost;
    @JacksonXmlProperty(localName = "SuccessRatePercent", isAttribute = true)
    private int SuccessRatePercent;
    @JacksonXmlProperty(localName = "LevelDown", isAttribute = true)
    private int LevelDown;

    public String getCost() {
        return getKENCost()+"#"+getCombineMaterial();
    }
}
