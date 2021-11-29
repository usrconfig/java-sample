package com.seagame.ext.entities.hero.skill;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.mongodb.BasicDBObject;
import com.seagame.ext.Utils;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bson.json.JsonParseException;

/**
 * @author LamHM
 */
@Getter
@Setter
@ToString
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties({"tempFormat"})
public class SkillLevelBase {
    @JacksonXmlProperty(localName = "ID", isAttribute = true)
    private String ID;
    @JacksonXmlProperty(localName = "Description", isAttribute = true)
    private String Description;
    @JacksonXmlProperty(localName = "Level", isAttribute = true)
    private int Level;
    @JacksonXmlProperty(localName = "BattlePower", isAttribute = true)
    private int BattlePower;
    @JacksonXmlProperty(localName = "Cooldown", isAttribute = true)
    private int Cooldown;
    @JacksonXmlProperty(localName = "Specs", isAttribute = true)
    private String tempFormat;

    private BasicDBObject specs;

    public BasicDBObject getSpecs() {
        try {
            if (this.specs == null && !Utils.isNullOrEmpty(tempFormat)) {
                try {
                    this.specs = Utils.isNullOrEmpty(tempFormat) ? null : BasicDBObject.parse(tempFormat);
                } catch (JsonParseException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return specs;
    }
}
