package com.seagame.ext.dao;

import com.seagame.ext.entities.Player;

import java.util.List;

/**
 * @author LamHM
 */
public interface PlayerRepositoryCustom {

    List<Player> listGameHero(String server, String nameRegex, int skip, int limit);

    int countGameHero(String server, String nameRegex);

}
