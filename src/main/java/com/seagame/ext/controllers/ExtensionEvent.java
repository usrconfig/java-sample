package com.seagame.ext.controllers;

/**
 * @author LamHM
 */
public interface ExtensionEvent {
    String CMD_ITEM = "cmd_item";
    String CMD_HERO = "cmd_hero";
    String CMD_DAILY_EVENT = "cmd_daily_event";
    String CMD_CAMPAIGN = "cmd_campaign";
    String CMD_TEAM = "cmd_team";
    String CMD_ARENA = "cmd_arena";



    String CMD_SETTING = "cmd_setting";
    String CMD_USER = "cmd_user";
    String CMD_USER_LOGIN = "cmd_user_login";
    String CMD_QUEST = "cmd_quest";
    String CMD_NTF = "cmd_ntf";
    String CMD_EXCEPTION = "cmd_exception";
    String CMD_COMMON = "cmd_common";
    String CMD_GIFT_VER2 = "cmd_gift_v2";
    String CMD_GM = "cmd_GM";
    String CMD_BOT = "cmd_bot";
    String CMD_ROOM = "cmd_room";

    String CMD_CHAT = "cmd_chat";
    String CMD_BOSS_EVENT_JOIN = "cmd_boss_event_join";
    String CMD_GIFT_EVENTS = "cmd_gift_events";
    String CMD_MAIL = "cmd_mail";
    String CMD_SHOP = "cmd_shop";
    String CMD_FRIEND = "cmd_friend";
    String CMD_PAYMENT = "cmd_payment";
    String CMD_GUILD = "cmd_guild";

    String CMD_ADMIN_SEND_MAIL = "game_master.sendMail";
    String CMD_ADMIN_RESET_ACCOUNT = "game_master.resetAccount";
    String CMD_ADMIN_CHAT_ACTION = "game_master.chatAction";

    String CMD_NTF_ASSETS_CHANGE = "cmd_assets_change";


    String NTF_GROUP_QUEST = "quest";
    String NTF_GROUP_ARENA = "arena";
    String NTF_GROUP_MAIL = "mail";
    String NTF_GROUP_FRIEND = "friend";
    String NTF_TYPE_COUNT = "count";
    String NTF_TYPE_SOLO = "solo";
    String NTF_TYPE_FINISH = "finish";

    int NOTIFY_NEW_MAIL = 1;
    int NOTIFY_PLAYER_INFO_CHANGE = 2;
    int NOTIFY_FRIEND_CHANGE = 3;
    int NOTIFY_EXP_CHANGE = 4;
    int NOTIFY_MINI_GAME = 5;


}
