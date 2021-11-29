package com.seagame.ext.dao;

import com.seagame.ext.entities.item.HeroEquipment;
import com.seagame.ext.entities.item.HeroItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author LamHM
 */
@Repository
public interface HeroItemRepository extends MongoRepository<HeroItem, Long> {

    @Query("{playerId: ?0,  $and :[{type:{ $ne: 'currency'}}]}")
    Page<HeroItem> getItemList(String playerId, long heroId, Pageable page);


    @Query("{playerId: ?0, cofferState: 1, $and :[{type:{ $ne: 'currency'}}]}")
    Page<HeroItem> getItemInCoffer(String playerId, Pageable page);


    @Query("{'playerId' : ?0, 'index' : {'$in' : ?1}}")
    List<HeroItem> getItemList(String playerId, Set<String> indexes);

    @Query("{'playerId' : ?0, 'index' : {'$in' : ?1},'heroId' : ?2}")
    List<HeroItem> getItemListHeroId(String playerId, Set<String> indexes, long heroId);

    @Query("{'playerId' : ?0, 'id' : {'$in' : ?1}}")
    List<HeroItem> getItemByItemId(String playerId, Collection<Long> itemIds);

    @Query("{'playerId' : ?0,'heroId' : ?1, 'equipSlot' :{ $ne: 0}}")
    List<HeroItem> getEquipmentSlots(String playerId, long heroId);

    List<HeroItem> getAllByPlayerIdAndIndexIsIn(String playerId, Collection<String> itemIds);


    @Query("{'_id' : ?0, 'playerId' : ?1}")
    HeroEquipment getEquipment(long itemId, String playerId);


    @Query("{'playerId' : ?0, 'type' : ?2}")
    List<HeroItem> getCurrencyItemList(String playerId, long activeHero, String code);


    @Query("{'playerId' : ?0, 'index' : {'$in' : ?2}}")
    List<HeroItem> getConsumeAbleItemList(String playerId, long activeHero, Collection<String> indexes);


    Collection<HeroItem> getAllByPlayerIdAndIndex(String playerId, String index);

    Collection<HeroItem> getAllByPlayerIdAndHeroIdAndIndex(String playerId, long heroId, String index);

    Collection<HeroItem> getHeroItemsByPlayerIdAndIndex(String playerId, String index);


    @Query(value = "{ playerId: ?0}", delete = true)
    void remove(String playerId);


}
