package com.seagame.ext.entities.hero;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author LamHM
 */

@Getter
public class LevelBase {
    @JacksonXmlProperty(localName = "ID", isAttribute = true)
    private int ID;
    @JacksonXmlProperty(localName = "Zen", isAttribute = true)
    private int Zen;
    @JacksonXmlProperty(localName = "Bless", isAttribute = true)
    private int Bless;
    @JacksonXmlProperty(localName = "Chaos", isAttribute = true)
    private int Chaos;

    @JsonIgnore
    public String getUpgradeCost() {
        List<String> strings = new ArrayList<>();
        if (getZen() > 0) {
            strings.add("9900/" + getZen());
        }
        if (getBless() > 0) {
            strings.add("9999/" + getBless());
        }
        if (getChaos() > 0) {
            strings.add("9901/" + getChaos());
        }
        return String.join("#", strings);
    }
}
