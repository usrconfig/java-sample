package com.seagame.ext.dao;

import com.seagame.ext.entities.Player;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * @author LamHM
 */
@Repository
public interface PlayerRepository extends MongoRepository<Player, String>, PlayerRepositoryCustom {

    @Query(value = "{ _id: ?0}", delete = true)
    void remove(String id);

    int countPlayersByLoginTimeAfter(Date today);


    // https://www.mkyong.com/mongodb/spring-data-mongodb-select-fields-to-return/
    @Query(value = "{}")
    Page<Player> getPlayers(Pageable page);

    Player findPlayerByDeviceIdAndZoneName(String deviceId, String zoneName);

    Player findPlayerByLoginIdAndZoneName(long loginId, String zoneName);

    Player findPlayerByLogin3rdIdAndZoneName(long loginId, String zoneName);

    Player findPlayerById(String heroId);

    @Query("{}")
    List<Player> getTopByMaxHeroLevel(Pageable page);

    @Query("{}")
    List<Player> getTopByKill(Pageable page);

    @Query("{}")
    List<Player> getTopByWinRate(Pageable page);

    @Query("{}")
    List<Player> getTopByTrophy(Pageable page);

    @Query(value = "{ name: ?0 }", count = true)
    int isExistName(String name);

}
