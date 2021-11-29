package com.seagame.ext.entities.arena;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Getter;

import java.util.List;
import java.util.Optional;

/**
 * @author LamHM
 */
@Getter
public class ArenaInfoBase {
    @JacksonXmlProperty(localName = "TopWeek", isAttribute = true)
    private List<ArenaTopReward> topWeek;
    @JacksonXmlProperty(localName = "TopMonth", isAttribute = true)
    private List<ArenaTopReward> topMonth;

//    public Optional<ArenaTopReward> getWeekReward(int rank) {
//        return getTopWeek().stream()
//                .filter(arenaTopReward -> arenaTopReward.getRank() == rank).findFirst();
//    }
//
//    public Optional<ArenaTopReward> getMonthReward(int rank) {
//        return getTopMonth().stream()
//                .filter(arenaTopReward -> arenaTopReward.getRank() == rank).findFirst();
//    }

}
