package com.seagame.ext.services;

import com.seagame.ext.dao.SequenceRepository;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AutoIncrementService implements InitializingBean {
    private static final String HERO_STAGE_ID = "hero_stage_id";
    private static final String CHAOS_STAGE_ID = "chaos_stage_id";
    public static final String HERO_ID = "hero_id";
    public static final String ACCOUNT_ID = "account_id";
    public static final String QUEST_ID = "quest_id";
    public static final String ITEM_ID = "item_id";
    public static final String MAIL_ID = "mail_id";
    public static final String NPC_ID = "npc_id";
    public static final String ARENA_BATTLE_ID = "arena_battle_id";
    public static final String HERO_SHOP_PACKAGE_ID = "hero_shop_package_id";
    public static final String PRODUCT_PURCHASE_ID = "product_purchase_id";
    public static final String GIFT_CODE_ID = "gift_code_id";
    public static final String ARENA_SEASON_ID = "arena_season_id";
    public static final String GUILD_ID = "guild_id";
    public static final String FRIEND_BATTLE_ID = "friend_battle_id";
    public static final String RELIC_LOG_ID = "relic_log_id";
    public static final String SERVER_DAYS_ID = "server_days";

    @Autowired
    private SequenceRepository sequenceRepository;


    @Override
    public void afterPropertiesSet() throws Exception {
        sequenceRepository.createSequenceDocument(HERO_ID, 1000);
        sequenceRepository.createSequenceDocument(ACCOUNT_ID, 1000);
        sequenceRepository.createSequenceDocument(HERO_STAGE_ID, 0);
        sequenceRepository.createSequenceDocument(QUEST_ID, 0);
        sequenceRepository.createSequenceDocument(ITEM_ID, 0);
        sequenceRepository.createSequenceDocument(MAIL_ID, 0);
        sequenceRepository.createSequenceDocument(NPC_ID, 0);
        sequenceRepository.createSequenceDocument(CHAOS_STAGE_ID, 0);
        sequenceRepository.createSequenceDocument(HERO_SHOP_PACKAGE_ID, 0);
        sequenceRepository.createSequenceDocument(ARENA_BATTLE_ID, 0);
        sequenceRepository.createSequenceDocument(PRODUCT_PURCHASE_ID, 0);
        sequenceRepository.createSequenceDocument(GIFT_CODE_ID, 0);
        sequenceRepository.createSequenceDocument(ARENA_SEASON_ID, 0);
        sequenceRepository.createSequenceDocument(GUILD_ID, 100);
        sequenceRepository.createSequenceDocument(FRIEND_BATTLE_ID, 0);
        sequenceRepository.createSequenceDocument(RELIC_LOG_ID, 0);
        sequenceRepository.createSequenceDocument(SERVER_DAYS_ID, 0);
    }


    public long genHeroStageId() {
        return sequenceRepository.getNextSequenceId(HERO_STAGE_ID);
    }


    public long genGuildId() {
        return sequenceRepository.getNextSequenceId(GUILD_ID);
    }


    public long genArenaSeasonId() {
        return sequenceRepository.getNextSequenceId(ARENA_SEASON_ID);
    }


    public long genGiftCodeId() {
        return sequenceRepository.getNextSequenceId(GIFT_CODE_ID);
    }


    public long genProductPurchaseId() {
        return sequenceRepository.getNextSequenceId(PRODUCT_PURCHASE_ID);
    }


    public long genArenaBattleId() {
        return sequenceRepository.getNextSequenceId(ARENA_BATTLE_ID);
    }


    public long genHeroShopPackageId() {
        return sequenceRepository.getNextSequenceId(HERO_SHOP_PACKAGE_ID);
    }


    public long genChaosStageId() {
        return sequenceRepository.getNextSequenceId(CHAOS_STAGE_ID);
    }


    public long genHeroId() {
        return sequenceRepository.getNextSequenceId(HERO_ID);
    }
    public long getHeroId() {
        return sequenceRepository.getSequenceId(HERO_ID);
    }


    public long genAccountId() {
        return sequenceRepository.getNextSequenceId(ACCOUNT_ID);
    }


    public long genQuestId() {
        return sequenceRepository.getNextSequenceId(QUEST_ID);
    }


    public long genItemId() {
        return sequenceRepository.getNextSequenceId(ITEM_ID);
    }


    public long genMailId() {
        return sequenceRepository.getNextSequenceId(MAIL_ID);
    }


    public long genNPCId() {
        return sequenceRepository.getNextSequenceId(NPC_ID);
    }

    public long genFriendBattleId() {
        return sequenceRepository.getNextSequenceId(FRIEND_BATTLE_ID);
    }

    public long genRelicLogId() {
        return sequenceRepository.getNextSequenceId(RELIC_LOG_ID);
    }

    public long genNextDays() {
        return sequenceRepository.getNextSequenceId(SERVER_DAYS_ID);
    }

    public long getCurrentDays() {
        return sequenceRepository.getSequenceId(SERVER_DAYS_ID);
    }
}
