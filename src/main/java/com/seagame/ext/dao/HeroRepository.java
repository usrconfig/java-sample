package com.seagame.ext.dao;

import com.seagame.ext.entities.hero.HeroClass;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

/**
 * @author LamHM
 */
@Repository
public interface HeroRepository extends MongoRepository<HeroClass, Long> {

    @Query("{'playerId' : ?0}")
    List<HeroClass> getHeroByPlayerId(String userId);

    @Query("{'playerId' : ?0,'id' : ?1}")
    HeroClass getHeroByPlayerId(String playerId, long heroId);

    List<HeroClass> findHeroesByPlayerId(String playerId);

    Page<HeroClass> findHeroesByPlayerId(String playerId, Pageable page);

    List<HeroClass> findHeroesByPlayerId(String playerId, Pageable page, boolean reqNew);


    @Query("{'id' : {'$in' : ?0}}")
    List<HeroClass> findAllCustom(Collection<Long> heroIds);


    List<HeroClass> findAllByPlayerId(String playerId);


    @Query(value = "{ playerId: ?0}", delete = true)
    void remove(String playerId);

    @Query(value = "{ name: ?0 }", count = true)
    int countName(String name);

    @Query("{isMonster : true}")
    List<HeroClass> getHeroes();

    @Query("{'playerId' : {'$in' : ?0}}")
    List<HeroClass> getHeroes(List<String> list);

    @Query(value = "{ playerId: ?0 }", count = true)
    int countHero(String playerId);
}
