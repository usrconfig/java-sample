package com.seagame.ext.offchain.services;

import com.creants.creants_2x.core.util.QAntTracer;
import com.creants.creants_2x.socket.gate.wood.QAntUser;
import com.creants.eventhandling.dto.*;
import com.creants.eventhandling.service.IOffchainEventHandling;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seagame.ext.ExtApplication;
import com.seagame.ext.Utils;
import com.seagame.ext.config.game.HeroConfig;
import com.seagame.ext.config.game.ItemConfig;
import com.seagame.ext.entities.Player;
import com.seagame.ext.entities.hero.HeroBase;
import com.seagame.ext.entities.hero.HeroClass;
import com.seagame.ext.entities.item.EquipBase;
import com.seagame.ext.entities.item.HeroItem;
import com.seagame.ext.entities.item.ItemBase;
import com.seagame.ext.managers.AbstractExtensionManager;
import com.seagame.ext.managers.HeroClassManager;
import com.seagame.ext.managers.HeroItemManager;
import com.seagame.ext.managers.PlayerManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LamHM
 */
@RestController
public class OffChainApiController extends AbstractExtensionManager implements IOffchainEventHandling {
    private static final String ZONE = "nf1";
    @Autowired
    private AssetMappingManager assetMappingManager;
    @Autowired
    private PlayerManager playerManager;
    @Autowired
    private HeroItemManager heroItemManager;
    @Autowired
    private HeroClassManager heroClassManager;

    @Override
    public ResponseEntity<OffchainMessageResponse> updateBalance(UpdateBalanceRequest updateBalanceReq) {
        String contractAddress = updateBalanceReq.getContractAddress();
        try {
            QAntTracer.warn(OffChainApiController.class, "updateBalance" + new ObjectMapper().writeValueAsString(updateBalanceReq));
            Player player = playerManager.getOrCreatePlayerByDevice(contractAddress, ZONE);
            playerManager.updateOffchainBalance(player);
            QAntUser receiverUser = getUserByName(player.getId());
            if (receiverUser != null)
                heroItemManager.notifyAssetChange(receiverUser);
        } catch (Exception e) {
            QAntTracer.warn(OffChainApiController.class, "updateBalance : fail");
        }
        return ResponseEntity.status(HttpStatus.OK).body(new OffchainMessageResponse());
    }

    @Override
    public ResponseEntity<OffchainMessageResponse> updateAsset(List<UpdateAssetRequest> updateAssetRequests) {
        updateAssetRequests.forEach(updateAssetRequest -> {
            String contractAddress = updateAssetRequest.getOwner();
            try {
                QAntTracer.warn(OffChainApiController.class, "updateAsset" + new ObjectMapper().writeValueAsString(updateAssetRequest));
                Player player = playerManager.getOrCreatePlayerByDevice(contractAddress, ZONE);
                playerManager.updateOffchainAssets(player.getId(), updateAssetRequest);
                playerManager.updateOffchainBalance(player);
                QAntUser receiverUser = getUserByName(player.getId());
                if (receiverUser != null)
                    heroItemManager.notifyAssetChange(receiverUser);
            } catch (Exception e) {
                QAntTracer.warn(OffChainApiController.class, "updateAsset : fail");
            }
        });
        return ResponseEntity.status(HttpStatus.OK).body(new OffchainMessageResponse());
    }

    @Override
    public ResponseEntity<OffchainMessageResponse> getAsset(List<String> assetIds) {
        OffchainMessageResponse response = new OffchainMessageResponse();
        List<GameAssetDTO> assets = assetMappingManager.findAllByAssetIdIn(assetIds);
        response.setData(assets);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Override
    public ResponseEntity<OffchainMessageResponse> getAssets(GetAssetsRequest address) {
        String contractAddress = address.getContractAddress();
        try {
            OffchainMessageResponse response = new OffchainMessageResponse();
            Player player = playerManager.getOrCreatePlayerByDevice(contractAddress, ZONE);
            List<HeroClass> heroes = heroClassManager.getHeroes(player.getId());
            List<GameAssetDTO> assets = new ArrayList<>();
            heroes.forEach(heroClass -> {
                HeroBase heroBase = HeroConfig.getInstance().getHeroBase(heroClass.getCharIndex());
                GameAssetDTO gameAssetDTO = heroBase.toGameAsset();
                gameAssetDTO.setOfcId(heroClass.getOfcId());
                gameAssetDTO.getAttribute().put("level", heroClass.getLevel());
                gameAssetDTO.getAttribute().put("rank", heroClass.getRank());
                assets.add(gameAssetDTO);
            });

            List<HeroItem> allItems = heroItemManager.getAllItems(player.getId());
            allItems.forEach(heroItem -> {
                ItemBase itemBase = ItemConfig.getInstance().getItem(heroItem.getIndex());
                GameAssetDTO gameAssetDTO = itemBase.toGameAsset();
                gameAssetDTO.setOfcId(heroItem.getOfcId());
                gameAssetDTO.getAttribute().put("level", heroItem.getLevel());
                gameAssetDTO.getAttribute().put("rank", heroItem.getRank());
                if (itemBase instanceof EquipBase) {
                    gameAssetDTO.getAttribute().put("image_url", ((EquipBase) itemBase).getRanks().get(heroItem.getRank()).getIcon());
                }
                gameAssetDTO.setAclass(Utils.getOClassEquip(heroItem.getRank()));
                assets.add(gameAssetDTO);
            });
            response.setData(assets);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            QAntTracer.warn(OffChainApiController.class, "updateBalance : fail");
        }
        return ResponseEntity.status(HttpStatus.OK).body(new OffchainMessageResponse());

    }
}
