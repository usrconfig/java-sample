package com.seagame.ext.entities.team;

import com.creants.creants_2x.socket.gate.entities.QAntArray;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;
import java.util.Map;

/**
 * @author LamHM
 */
@Getter
@Setter
@Document(collection = "battle-team")
public class BattleTeam {
    @Id
    private String playerId;
    private Map<String, Team> teamMap;

    public BattleTeam(String playerId) {
        this.playerId = playerId;
        teamMap = new HashMap<>();
    }


    public void addTeam(Team team) {
        teamMap.put(team.getTeamType(), team);
    }


    public Team getArenaTeam() {
        return getTeam(TeamType.ARENA.getCode());
    }

    public Team getTeam(String type) {
        return teamMap.get(type);
    }


    public Team getCampaignTeam() {
        return getTeam(TeamType.CAMPAIGN.getCode());
    }


    public Team getDefenceTeam() {
        return getTeam(TeamType.DEFENCE.getCode());
    }


    public QAntArray buildInfo() {
        QAntArray qAntArray = new QAntArray();
        teamMap.values().forEach(team -> qAntArray.addQAntObject(team.buildObject()));
        return qAntArray;
    }
}
