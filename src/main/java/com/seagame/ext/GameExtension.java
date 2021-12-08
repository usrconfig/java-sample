package com.seagame.ext;

import com.creants.creants_2x.core.QAntEventType;
import com.creants.creants_2x.core.extension.QAntExtension;
import com.creants.creants_2x.core.util.QAntTracer;
import com.seagame.ext.bot.UnityBotManager;
import com.seagame.ext.controllers.*;
import com.seagame.ext.managers.HeroItemManager;
import com.seagame.ext.managers.PlayerManager;
import com.seagame.ext.quest.QuestSystem;
import com.seagame.ext.services.NotifySystem;
import com.seagame.ext.services.ServiceHelper;

/**
 * @author LamHM
 */
public class GameExtension extends QAntExtension implements ExtensionEvent {


    @Override
    public void init() {
        QAntTracer.info(this.getClass(), "========================= START MU =========================");
        long startTime = System.currentTimeMillis();
        addEventRequestHandler();
        ExtApplication.getBean(PlayerManager.class).setExtension(this);
        ExtApplication.getBean(HeroItemManager.class).setExtension(this);
        ExtApplication.getBean(ServiceHelper.class).setExtension(this);
        ExtApplication.getBean(NotifySystem.class).setExtension(this);
        ExtApplication.getBean(UnityBotManager.class).setExtension(this);
        ExtApplication.getBean(QuestSystem.class).setExtension(this);

        QAntTracer.info(this.getClass(),
                "------- start time: " + (System.currentTimeMillis() - startTime) / 1000 + " seconds");
        QAntTracer.info(this.getClass(), "========================= MU STARTED =========================");
//        QAntTracer.info(this.getClass(), "========================= Start Default Unity BOT =========================");
//        ExtApplication.getBean(UnityBotManager.class).initBaseRoom();
//        QAntTracer.info(this.getClass(), "========================= Start Bot Complete =========================");
    }


    private void addEventRequestHandler() {
        addEventHandler(QAntEventType.USER_LOGIN, LoginEventHandler.class);
        addEventHandler(QAntEventType.USER_LOGOUT, LogoutEventHandler.class);
        addEventHandler(QAntEventType.USER_JOIN_ZONE, JoinZoneEventHandler.class);
        addEventHandler(QAntEventType.USER_DISCONNECT, DisconnectEventHandler.class);
        addRequestHandler(CMD_HERO, HeroRequestHandler.class);
        addRequestHandler(CMD_ITEM, ItemRequestHandler.class);
        addRequestHandler(CMD_DAILY_EVENT, DailyEventRequestHandler.class);
        addRequestHandler(CMD_CAMPAIGN, CampaignRequestHandler.class);
        addRequestHandler(CMD_TEAM, TeamRequestHandler.class);
        addRequestHandler(CMD_QUEST, QuestRequestHandler.class);
        addRequestHandler(CMD_MAIL, MailRequestHandler.class);
        addRequestHandler(CMD_USER, PlayerRequestHandler.class);
        addRequestHandler(CMD_ARENA, ArenaRequestHandler.class);

//        addRequestHandler(CMD_SETTING, SettingRequestHandler.class);
//        addRequestHandler(CMD_QUEST, QuestRequestHandler.class);
//        addRequestHandler(CMD_SHOP, ShopRequestHandler.class);
//        addRequestHandler(CMD_CHAT, ChatRequestHandler.class);
//        addRequestHandler(CMD_COMMON, CommonRequestHandler.class);
//        addRequestHandler(CMD_GIFT_EVENTS, GiftRequestHandler.class);
//        addRequestHandler(CMD_FRIEND, FriendRequestHandler.class);
//        addRequestHandler(CMD_PAYMENT, PaymentRequestHandler.class);
//        addRequestHandler(CMD_GUILD, GuildRequestHandler.class);
//        addRequestHandler(CMD_GM, GameMasterRequestHandler.class);
//        addRequestHandler(CMD_BOT, BotRequestHandler.class);
//        addRequestHandler(CMD_ROOM, RoomRequestHandler.class);

//        addRequestHandler(CMD_ADMIN_SEND_MAIL, SendMailRequestHandler.class);
//        addRequestHandler(CMD_ADMIN_RESET_ACCOUNT, ResetAccountRequestHandler.class);
//        addRequestHandler(CMD_ADMIN_CHAT_ACTION, AdminChatRequestHandler.class);
    }

    @Override
    public void destroy() {
        super.destroy();
    }
}

