package com.seagame.ext.offchain.services;

import com.creants.eventhandling.dto.GameAssetDTO;
import com.seagame.ext.config.game.HeroConfig;
import com.seagame.ext.config.game.ItemConfig;
import com.seagame.ext.entities.hero.HeroBase;
import com.seagame.ext.entities.item.ItemBase;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AssetMappingManager implements InitializingBean {
    private Map<String, GameAssetDTO> gameAssetMapByAssetId;

    @Override
    public void afterPropertiesSet() throws Exception {
        gameAssetMapByAssetId = new HashMap<>();
        Collection<HeroBase> heroes = HeroConfig.getInstance().getHeroes();
        Map<String, GameAssetDTO> heroMapByAssetId = heroes.stream().map(HeroBase::toGameAsset)
                .collect(Collectors.toMap(GameAssetDTO::getAssetId, o -> o));
        gameAssetMapByAssetId.putAll(heroMapByAssetId);

        Collection<ItemBase> items = ItemConfig.getInstance().getItems();
        Map<String, GameAssetDTO> itemMapById = items.stream()
                .map(ItemBase::toGameAsset).collect(Collectors.toMap(GameAssetDTO::getAssetId, o -> o));
        gameAssetMapByAssetId.putAll(itemMapById);

        Collection<ItemBase> equipBases = ItemConfig.getInstance().getEquips();
        Map<String, GameAssetDTO> equipMapById = equipBases.stream()
                .map(ItemBase::toGameAsset).collect(Collectors.toMap(GameAssetDTO::getAssetId, o -> o));
        gameAssetMapByAssetId.putAll(equipMapById);
    }

    public List<GameAssetDTO> findAllByAssetIdIn(List<String> assetIds) {
        return assetIds.stream().map(gameAssetMapByAssetId::get).filter(asset -> !Objects.isNull(asset))
                .collect(Collectors.toList());
    }

    public GameAssetDTO findByAssetId(String assetId) {
        return gameAssetMapByAssetId.get(assetId);
    }
}
