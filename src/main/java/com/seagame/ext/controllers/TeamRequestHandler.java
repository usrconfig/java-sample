package com.seagame.ext.controllers;

import com.creants.creants_2x.core.annotations.Instantiation;
import com.creants.creants_2x.socket.gate.entities.IQAntObject;
import com.creants.creants_2x.socket.gate.wood.QAntUser;
import com.seagame.ext.ExtApplication;
import com.seagame.ext.dao.BattleTeamRepository;
import com.seagame.ext.entities.hero.HeroClass;
import com.seagame.ext.entities.team.BattleTeam;
import com.seagame.ext.entities.team.Team;
import com.seagame.ext.entities.team.TeamType;
import com.seagame.ext.managers.HeroClassManager;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static com.seagame.ext.exception.GameErrorCode.LACK_OF_INFOMATION;

/**
 * @author LamHM
 */
@Instantiation(Instantiation.InstantiationMode.SINGLE_INSTANCE)
public class TeamRequestHandler extends ZClientRequestHandler {
    public static final int GET_TEAMS = 1;
    public static final int SUBMIT_TEAM = 2;


    private BattleTeamRepository battleTeamRep;
    private HeroClassManager heroClassManager;


    public TeamRequestHandler() {
        battleTeamRep = ExtApplication.getBean(BattleTeamRepository.class);
        heroClassManager = ExtApplication.getBean(HeroClassManager.class);
    }


    @Override
    public void handleClientRequest(QAntUser user, IQAntObject params) {

        Integer action = this.getAction(params);
        if (action == null) {
            responseError(user, LACK_OF_INFOMATION);
            return;
        }
        switch (action) {
            case GET_TEAMS:
                getTeams(user, params);
                break;
            case SUBMIT_TEAM:
                submitTeam(user, params);
                break;
        }

    }

    private void submitTeam(QAntUser user, IQAntObject params) {
        String playerId = user.getName();
        String idx = params.getUtfString("idx");//cp/ar/df
        BattleTeam battleTeam = battleTeamRep.findById(playerId).orElse(new BattleTeam(playerId));
        Collection<Long> heroIds = params.getLongArray("heroes");

        Team oldTeam = battleTeam.getTeam(TeamType.valueOf(idx));
        List<HeroClass> heroes = heroClassManager.findHeroes(heroIds, false);
        Team team = Team.createTeam(idx, heroes);
        team.setFormation(heroIds.toArray(new Long[]{}));
        Integer leaderIndex = params.getInt("leaderIndex");
        team.setLeaderIndex(leaderIndex);
        battleTeam.addTeam(team);
        battleTeamRep.save(battleTeam);
        params.putQAntObject("team", team.buildObject());
        send(params, user);
        heroes.forEach(heroClass -> heroClass.updateTeam(idx));
        if (oldTeam != null) {
            List<HeroClass> oldHeroes = heroClassManager.findHeroes(oldTeam.getHeroIds().stream().filter(aLong -> !heroIds.contains(aLong)).collect(Collectors.toList()), false);
            oldHeroes.forEach(heroClass -> heroClass.removeTeam(idx));
            heroes.addAll(oldHeroes);
        }
        heroClassManager.save(heroes);
    }

    private void getTeams(QAntUser user, IQAntObject params) {
        String playerId = user.getName();
        BattleTeam battleTeam = battleTeamRep.findById(playerId).orElseGet(() -> {
            BattleTeam battleTeam1 = new BattleTeam(playerId);
            battleTeamRep.save(battleTeam1);
            return battleTeam1;
        });
        params.putQAntArray("teams", battleTeam.buildInfo());
        send(params, user);
    }

    @Override
    protected String getHandlerCmd() {
        return ExtensionEvent.CMD_TEAM;
    }
}
