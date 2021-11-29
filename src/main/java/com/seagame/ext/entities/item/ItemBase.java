package com.seagame.ext.entities.item;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.mongodb.BasicDBObject;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author LamHM
 */
@Getter
@Setter
@ToString
@JsonInclude(Include.NON_NULL)
public class ItemBase implements Serializable {
    @JacksonXmlProperty(localName = "ID", isAttribute = true)
    private String id;
    @JacksonXmlProperty(localName = "Name", isAttribute = true)
    private String name;
    @JacksonXmlProperty(localName = "Description", isAttribute = true)
    private String desc;
    @JacksonXmlProperty(localName = "Type", isAttribute = true)
    private String type;
    @JacksonXmlProperty(localName = "Rank", isAttribute = true)
    private int rank;
    @JacksonXmlProperty(localName = "Icon", isAttribute = true)
    private String icon;

    private List<RewardBase> rewards;


}
