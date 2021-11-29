package com.seagame.ext.services;

import com.seagame.ext.om.Etoken;
import com.seagame.ext.util.EtokenGenerator;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @author LamHM
 */
@Service
public class EtokenService implements InitializingBean {
    private static final long WAITING_SECONDS = 120;
    private static final long DELAY_SECONDS = 1;
    private Map<String, Etoken> tokenMap;


    @Override
    public void afterPropertiesSet() throws Exception {
        tokenMap = new HashMap<>();
    }


    public Etoken getToken(String gameHeroId) {
        Etoken etoken = tokenMap.get(gameHeroId);
        if (etoken == null) {
            etoken = createNew(gameHeroId);
        } else {
            long deltaSeconds = (System.currentTimeMillis() - etoken.getCreateTime()) / 1000;
            if (deltaSeconds > (WAITING_SECONDS - DELAY_SECONDS))
                etoken = createNew(gameHeroId);
            else
                etoken.setRemainSeconds(deltaSeconds);
        }

        return etoken;
    }


    private Etoken createNew(String gameHeroId) {
        Etoken etoken = new Etoken();
        etoken.setCreateTime(System.currentTimeMillis());
        etoken.setRemainSeconds(WAITING_SECONDS);
        etoken.setCode(EtokenGenerator.genEtoken());
        return etoken;
    }

}
