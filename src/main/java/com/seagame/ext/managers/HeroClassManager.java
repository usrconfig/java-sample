package com.seagame.ext.managers;

import com.creants.creants_2x.socket.gate.entities.IQAntObject;
import com.seagame.ext.dao.HeroRepository;
import com.seagame.ext.entities.hero.HeroClass;
import com.seagame.ext.services.AutoIncrementService;
import com.seagame.ext.util.NetworkConstant;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author LamHM
 */
@Service
public class HeroClassManager implements InitializingBean, NetworkConstant {
    private static final int MAX_HERO_PER_PAGE = 20;

    @Autowired
    private AutoIncrementService autoIncrService;
    @Autowired
    private HeroRepository heroRepository;
    @Autowired
    private HeroItemManager itemManager;


    @Override
    public void afterPropertiesSet() {
    }


    public long genHeroId() {
        return autoIncrService.genHeroId();
    }


    public void removeGameHeroData(String gameHeroId) {
        heroRepository.remove(gameHeroId);
    }

    public Page<HeroClass> getHeroPage(String gameHeroId, int page) {
        return heroRepository.findHeroesByPlayerId(gameHeroId, PageRequest.of(page - 1, MAX_HERO_PER_PAGE));
    }

    public HeroClass getHeroWithId(String userId, long heroId) {
        return heroRepository.getHeroByPlayerId(userId, heroId);
    }


    public void remove(Collection<HeroClass> heroes) {
        heroRepository.deleteAll(heroes);
    }


    public void remove(HeroClass hero) {
        heroRepository.delete(hero);
    }


    public List<HeroClass> save(Collection<HeroClass> heroes) {
        return heroRepository.saveAll(heroes);
    }


    public HeroClass save(HeroClass heroClass) {
        return heroRepository.save(heroClass);
    }


    public boolean isExistName(String name) {
        return heroRepository.countName(name) > 0;
    }


    public List<HeroClass> getHeroes() {
        return heroRepository.getHeroes();
    }

    public List<HeroClass> getHeroes(List<String> list) {
        return heroRepository.getHeroes(list);
    }

    public int countHero(String userId) {
        return heroRepository.countHero(userId);
    }

    public void applyStats(HeroClass receiverUser, IQAntObject params) {
//        if (params.containsKey("hp")) {
//            float hp = params.getFloat("hp");
//            receiverUser.setHp(hp);
//        }
//        if (params.containsKey("mp")) {
//            float mp = params.getFloat("mp");
//            receiverUser.setMp(mp);
//        }
//        save(receiverUser);

    }

    public List<HeroClass> findHeroes(Collection<Long> heroIds, boolean includeEquipment) {
        return heroRepository.findAllCustom(heroIds).stream()
                .map(heroClass -> {
                    if (includeEquipment) {
                        setHeroBaseAndEquipment(heroClass);
                    }
                    return heroClass;
                })
                .collect(Collectors.toList());
    }

    private void setHeroBaseAndEquipment(HeroClass heroClass) {
        heroClass.setEquipments(itemManager.getTakeOnEquipments(heroClass.getPlayerId(), heroClass.getId()));
    }

}
