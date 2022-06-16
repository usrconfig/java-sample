package com.seagame.ext.entities.hero;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.seagame.ext.util.NetworkConstant;
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
    @JacksonXmlProperty(localName = "Wol", isAttribute = true)
    private int Wol;
    @JacksonXmlProperty(localName = "Ken", isAttribute = true)
    private int Ken;
    @JacksonXmlProperty(localName = "Chaos", isAttribute = true)
    private int Chaos;
    @JacksonXmlProperty(localName = "Items", isAttribute = true)
    private String Items;

    @JsonIgnore
    public String getUpgradeCost() {
        List<String> strings = new ArrayList<>();
        if (getWol() > 0) {
            strings.add(NetworkConstant.WOL + "/" + getWol());
        }
        if (getKen() > 0) {
            strings.add(NetworkConstant.KEN + "/" + getKen());
        }
        if (getChaos() > 0) {
            strings.add(NetworkConstant.CHAO + "/" + getChaos());
        }
        strings.add(Items);
        return String.join("#", strings);
    }
}
