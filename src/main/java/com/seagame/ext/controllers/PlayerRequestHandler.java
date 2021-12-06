package com.seagame.ext.controllers;

import com.creants.creants_2x.core.annotations.Instantiation;
import com.creants.creants_2x.core.service.WebService;
import com.creants.creants_2x.core.util.QAntTracer;
import com.creants.creants_2x.socket.gate.entities.IQAntObject;
import com.creants.creants_2x.socket.gate.entities.QAntObject;
import com.creants.creants_2x.socket.gate.wood.QAntUser;
import com.seagame.ext.ExtApplication;
import com.seagame.ext.config.game.HeroConfig;
import com.seagame.ext.entities.Player;
import com.seagame.ext.entities.hero.HeroBase;
import com.seagame.ext.entities.hero.HeroClass;
import com.seagame.ext.exception.GameErrorCode;
import com.seagame.ext.exception.NameException;
import com.seagame.ext.managers.HeroClassManager;
import com.seagame.ext.managers.HeroItemManager;
import com.seagame.ext.managers.PlayerManager;
import com.seagame.ext.services.AutoIncrementService;
import com.seagame.ext.util.GameUtils;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;

import java.util.Arrays;

import static com.seagame.ext.exception.GameErrorCode.LACK_OF_INFOMATION;


/**
 * @author LamHM
 */
@Instantiation(Instantiation.InstantiationMode.SINGLE_INSTANCE)
public class PlayerRequestHandler extends ZClientRequestHandler {
    private static final int UPDATE_PROFILE = 1;
    private static final int LINK_ACCOUNT_FB = 2;
    private static final int LINK_ACCOUNT_GG = 3;
    private static final int CHECK_NAME = 4;
    private static final int LINK_ACCOUNT_3RD = 5;
    private static final int UN_LINK_ACCOUNT_FB_GG = 6;

    private static final int ADD_ITEM_HERO_TEST = 7;


    private PlayerManager playerManager;
    private HeroClassManager heroClassManager;
    private HeroItemManager heroItemManager;
    private AutoIncrementService autoIncrementService;


    public PlayerRequestHandler() {
        playerManager = ExtApplication.getBean(PlayerManager.class);
        heroClassManager = ExtApplication.getBean(HeroClassManager.class);
        heroItemManager = ExtApplication.getBean(HeroItemManager.class);
        autoIncrementService = ExtApplication.getBean(AutoIncrementService.class);
    }


    @Override
    public void handleClientRequest(QAntUser user, IQAntObject params) {
        Integer action = this.getAction(params);
        if (action == null) {
            responseError(user, LACK_OF_INFOMATION);
            return;
        }

        switch (action) {
            case UPDATE_PROFILE:
                updateProfile(user, params);
                break;
            case LINK_ACCOUNT_FB:
                linkAccountFb(user, params);
                break;
            case UN_LINK_ACCOUNT_FB_GG:
                unlinkAccount(user, params);
                break;
            case LINK_ACCOUNT_3RD:
                linkAccount3rd(user, params);
                break;
            case LINK_ACCOUNT_GG:
                linkAccountGG(user, params);
                break;
            case CHECK_NAME:
                checkName(user, params);
                break;
            case ADD_ITEM_HERO_TEST:
                addItemHero(user, params);
                break;
            default:
                break;
        }
    }

    private void addItemHero(QAntUser user, IQAntObject params) {
        String giftCode = params.getUtfString("code");
        if (giftCode.startsWith("add:")) {
            customeGift(user, giftCode);
            send(params, user);
        }
    }

    private void customeGift(QAntUser user, String giftCode) {
        String gameHeroId = user.getName();
        String[] addItemArr = StringUtils.substringsBetween(giftCode, "ai:", "|");
        if (addItemArr != null && addItemArr.length > 0) {
            heroItemManager.addItems(gameHeroId, addItemArr[0].trim());
        }
        String[] addHeroArr = StringUtils.substringsBetween(giftCode, "ah:", "|");
        if (addHeroArr != null && addHeroArr.length > 0) {
            Arrays.stream(addHeroArr[0].trim().split("#")).forEach(s -> {
                HeroBase heroBase = HeroConfig.getInstance().getHeroBase(s);
                HeroClass heroClass = new HeroClass(heroBase.getID(), 1);
                heroClass.setId(autoIncrementService.genHeroId());
                heroClass.setPlayerId(user.getName());
                heroClassManager.save(heroClass);
            });
        }
    }

    private void unlinkAccount(QAntUser user, IQAntObject params) {
        Player player = playerManager.getPlayer(user.getName());
        if (!player.isLinkFbGG()) {
            responseError(user, GameErrorCode.LACK_OF_INFOMATION);
            return;
        }
        if (!player.isLink3rd()) {
            responseError(user, GameErrorCode.LACK_OF_INFOMATION);
            return;
        }
        player.unlinkFbGG();
        playerManager.updateGameHero(player);
        params.putInt(KEYI_CODE, 1);
        send(params, user);
    }


    private void checkName(QAntUser user, IQAntObject params) {
        String Name = params.getUtfString("name");
        IQAntObject antObject = QAntObject.newInstance();
        try {
            antObject.putBool("isValid", GameUtils.correctPlayerName(Name) != null);
        } catch (NameException e) {
//            switch (e.getCode()) {
//                case NameException.MAX_SIZE:
//                case NameException.MIN_SIZE:
//                    responseError(user, GameErrorCode.NAME_MIN_MAX);
//                    break;
//                case NameException.ALREADY:
//                    responseError(user, GameErrorCode.EXIST_HERO_NAME);
//                    break;
//                default:
//                    responseError(user, GameErrorCode.LACK_OF_INFOMATION);
//                    break;
//            }
            antObject.putBool("isValid", false);
        }
        send(antObject, user);
    }

    private void linkAccount3rd(QAntUser user, IQAntObject params) {
        String platform = params.getUtfString("platform");
        String token = params.getUtfString("token");
        String zone = user.getZone().getName();
        String accountInfo = null;
        try {
            accountInfo = WebService.getInstance().link3RD(platform, token);
            QAntTracer.info(this.getClass(), accountInfo);

            JSONObject accountObj = JSONObject.fromObject(accountInfo);
            int code = accountObj.getInt("code");
            if (code != 1 && code != 1006) {
                responseError(user, GameErrorCode.UNKNOW_EXCEPTION);
                return;
            }

            Player player = playerManager.getPlayer(user.getName());

            params.removeElement("platform");
            params.removeElement("token");
            String creantsToken = accountObj.getString("token");
            params.putUtfString("token", creantsToken);

            JSONObject userInfo = accountObj.getJSONObject("data").getJSONObject("user");
            long loginId = userInfo.getLong("id");
            String provider = userInfo.getString("provider");

            // tài khoản này đã được link  rồi
            if (player.isLink3rd(provider)) {
                responseError(user, GameErrorCode.ACCOUNT_HAS_BEEN_LINKED);
                return;
            }

            // tài khoản fb này đã tồn tại trên hệ thống account
            if (returnExistAccount(user, params, zone, code, loginId)) return;

            params.putInt(KEYI_CODE, 1);
            if (!player.setLoginId(loginId, provider)) {
                responseError(user, GameErrorCode.ACCOUNT_HAS_BEEN_LINKED);
                return;
            }
            playerManager.updateGameHero(player);
            params.putQAntObject("player", player.buildLoginInfo());
            send(params, user);
        } catch (Exception e) {
            QAntTracer.error(this.getClass(), "Link account 3rd fail! " + platform + "-" + token);
            responseError(user, GameErrorCode.UNKNOW_EXCEPTION);
        }
    }

    private void linkAccountFb(QAntUser user, IQAntObject params) {
        String fbToken = params.getUtfString("fb_token");
        String zone = user.getZone().getName();
        String accountInfo;
        try {
            accountInfo = WebService.getInstance().linkFb(fbToken);
            QAntTracer.info(this.getClass(), accountInfo);

            JSONObject accountObj = JSONObject.fromObject(accountInfo);
            int code = accountObj.getInt("code");
            if (code != 1 && code != 1006) {
                responseError(user, GameErrorCode.UNKNOW_EXCEPTION);
                return;
            }

            Player player = playerManager.getPlayer(user.getName());
            // tài khoản này đã được link  rồi
            if (player.isLinkFbGG()) {
                responseError(user, GameErrorCode.ACCOUNT_HAS_BEEN_LINKED);
                return;
            }

            params.removeElement("fb_token");
            String creantsToken = accountObj.getString("token");
            params.putUtfString("token", creantsToken);

            JSONObject userInfo = accountObj.getJSONObject("data").getJSONObject("user");
            long loginId = userInfo.getLong("id");
            String provider = userInfo.getString("provider");
            // tài khoản fb này đã tồn tại trên hệ thống account
            if (returnExistAccount(user, params, zone, code, loginId)) return;

            params.putInt(KEYI_CODE, 1);
            if (!player.setLoginId(loginId, provider)) {
                responseError(user, GameErrorCode.ACCOUNT_HAS_BEEN_LINKED);
                return;
            }
            playerManager.updateGameHero(player);
            params.putQAntObject("player", player.buildLoginInfo());
            send(params, user);
        } catch (Exception e) {
            QAntTracer.error(this.getClass(), "Link account fb fail! fb_token:" + fbToken);
            responseError(user, GameErrorCode.UNKNOW_EXCEPTION);
            e.printStackTrace();
        }
    }

    private void linkAccountGG(QAntUser user, IQAntObject params) {
        String ggToken = params.getUtfString("gg_token");
        String zone = user.getZone().getName();
        String accountInfo;
        try {
            accountInfo = WebService.getInstance().linkGG(ggToken);
            QAntTracer.info(this.getClass(), accountInfo);

            JSONObject accountObj = JSONObject.fromObject(accountInfo);
            int code = accountObj.getInt("code");
            if (code != 1 && code != 1006) {
                responseError(user, GameErrorCode.UNKNOW_EXCEPTION);
                return;
            }

            Player player = playerManager.getPlayer(user.getName());
            // tài khoản này đã được link  rồi
            if (player.isLinkFbGG()) {
                responseError(user, GameErrorCode.ACCOUNT_HAS_BEEN_LINKED);
                return;
            }

            params.removeElement("gg_token");
            String creantsToken = accountObj.getString("token");
            params.putUtfString("token", creantsToken);

            JSONObject userInfo = accountObj.getJSONObject("data").getJSONObject("user");
            long loginId = userInfo.getLong("id");
            String provider = userInfo.getString("provider");
            // tài khoản fb này đã tồn tại trên hệ thống account
            if (returnExistAccount(user, params, zone, code, loginId)) return;

            params.putInt(KEYI_CODE, 1);
            if (!player.setLoginId(loginId, provider)) {
                responseError(user, GameErrorCode.ACCOUNT_HAS_BEEN_LINKED);
                return;
            }
            playerManager.updateGameHero(player);
            params.putQAntObject("player", player.buildLoginInfo());
            send(params, user);
        } catch (Exception e) {
            QAntTracer.error(this.getClass(), "Link account gg fail! gg_token:" + ggToken);
            responseError(user, GameErrorCode.UNKNOW_EXCEPTION);
        }
    }

    private boolean returnExistAccount(QAntUser user, IQAntObject params, String zone, int code, long loginId) {
        if (code == 1006) {
            Player playerExist = playerManager.getPlayer(loginId, zone);
            if (playerExist != null) {
                params.putInt(KEYI_CODE, 1006);
                params.putQAntObject("player", playerExist.buildFriendInfo());
                send(params, user);
                return true;
            }
        }
        return false;
    }


    private void updateProfile(QAntUser user, IQAntObject params) {
        String newName = params.containsKey("name") ? params.getUtfString("name") : null;
        String avatar = params.containsKey("avatar") ? params.getUtfString("avatar") : null;
        Player player = playerManager.getPlayer(user.getName());
        if (newName != null) {
            newName = newName.trim();
            if (newName.length() < 3 || newName.length() > 15) {
                responseError(user, GameErrorCode.NAME_MIN_MAX);
                return;
            }

            boolean existName = playerManager.isExistName(newName);
            if (existName) {
                responseError(user, GameErrorCode.EXIST_HERO_NAME);
                return;
            }

            player.setName(newName);
            player.setNamed(true);
        }
        if (avatar != null) {
            player.setAvatar(avatar);
        }
        playerManager.updateGameHero(player);
        IQAntObject object = QAntObject.newInstance();
        object.putUtfString("name", player.getName());
        object.putUtfString("avatar", player.getAvatar());
        send(params, user);
    }

    @Override
    protected String getHandlerCmd() {
        return CMD_USER;
    }
}
