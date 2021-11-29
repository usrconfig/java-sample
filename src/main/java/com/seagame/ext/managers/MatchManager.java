package com.seagame.ext.managers;

import com.creants.creants_2x.socket.gate.entities.IQAntObject;
import com.creants.creants_2x.socket.gate.entities.QAntObject;
import com.seagame.ext.config.game.MonsterConfig;
import com.seagame.ext.entities.campaign.MatchInfo;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author LamHM
 */
@Service
public class MatchManager implements InitializingBean {
    private static final MonsterConfig monsterConfig = MonsterConfig.getInstance();
    private Map<String, MatchInfo> matchMap;


    @Override
    public void afterPropertiesSet() throws Exception {
        matchMap = new ConcurrentHashMap<>();
    }

    public MatchInfo getMatch(String gameHeroId) {
        return matchMap.get(gameHeroId);
    }


    public void newMatch(String gameHeroId, MatchInfo matchInfo) {
        matchMap.put(gameHeroId, matchInfo);
    }


    public IQAntObject finish(String gameHeroId, IQAntObject params) {
        IQAntObject result = QAntObject.newInstance();
        Boolean isWin = params.getBool("win");
        result.putByte("result", (byte) ((isWin != null && isWin) ? 1 : 0));
        if (!isWin) {
            removeMatch(gameHeroId);
            return result;
        }

        return result;
    }


    public void removeMatch(String gameHeroId) {
        if (matchMap.containsKey(gameHeroId))
            matchMap.remove(gameHeroId);
    }

}
