package com.seagame.ext.controllers;

import com.creants.creants_2x.core.IQAntEvent;
import com.creants.creants_2x.core.QAntEventParam;
import com.creants.creants_2x.core.QAntEventSysParam;
import com.creants.creants_2x.core.QAntSystemEvent;
import com.creants.creants_2x.core.annotations.Instantiation;
import com.creants.creants_2x.core.api.QAntAPI;
import com.creants.creants_2x.core.controllers.SystemRequest;
import com.creants.creants_2x.core.controllers.system.Login;
import com.creants.creants_2x.core.exception.QAntErrorCode;
import com.creants.creants_2x.core.exception.QAntException;
import com.creants.creants_2x.core.extension.BaseServerEventHandler;
import com.creants.creants_2x.core.service.WebService;
import com.creants.creants_2x.core.util.QAntTracer;
import com.creants.creants_2x.socket.gate.entities.IQAntObject;
import com.creants.creants_2x.socket.gate.entities.QAntObject;
import com.creants.creants_2x.socket.gate.wood.QAntUser;
import com.creants.creants_2x.socket.io.IRequest;
import com.creants.creants_2x.socket.io.IResponse;
import com.creants.creants_2x.socket.io.Response;
import com.seagame.ext.ExtApplication;
import com.seagame.ext.bot.UnityBotManager;
import com.seagame.ext.entities.Player;
import com.seagame.ext.exception.GameErrorCode;
import com.seagame.ext.managers.PlayerManager;
import com.seagame.ext.services.AutoIncrementService;
import com.seagame.ext.services.MessageFactory;
import com.seagame.ext.util.NetworkConstant;
import io.netty.channel.Channel;
import net.sf.json.JSONObject;

/**
 * @author LamHM
 */
@Instantiation(Instantiation.InstantiationMode.SINGLE_INSTANCE)
public class LoginEventHandler extends BaseServerEventHandler implements NetworkConstant {

    private AutoIncrementService autoIncrService;
    private PlayerManager playerManager;
    private UnityBotManager unityBotManager;


    public LoginEventHandler() {
        autoIncrService = ExtApplication.getBean(AutoIncrementService.class);
        playerManager = ExtApplication.getBean(PlayerManager.class);
        unityBotManager = ExtApplication.getBean(UnityBotManager.class);
    }


    @Override
    public void handleServerEvent(IQAntEvent event) throws QAntException {
        QAntSystemEvent systemEvent = (QAntSystemEvent) event;
        IRequest sysParameter = (IRequest) systemEvent.getSysParameter(QAntEventSysParam.REQUEST_OBJ);
        IQAntObject reqObj = sysParameter.getContent();
        String token = reqObj.getUtfString(KEYS_USER_NAME);
        String zoneName = reqObj.getUtfString(KEYS_ZONE_NAME);

        IResponse response = new Response();
        response.setId(SystemRequest.Login.getId());
        response.setTargetController(QAntAPI.SYSTEM_CONTROLLER);
        Channel sender = sysParameter.getSender();
        response.setRecipients(sender);

        String verify;
        try {
            QAntTracer.info(this.getClass(), "verifying token: " + token);
            verify = WebService.getInstance().verify(token);
        } catch (Exception e) {
            sendError(zoneName, response, QAntErrorCode.GRAPH_API_FAIL);
            getApi().disconnect(sender);
            throw new QAntException("Verify graph api fail. Token:" + token);
        }

        IQAntObject outData = (IQAntObject) event.getParameter(QAntEventParam.LOGIN_OUT_DATA);
        JSONObject userInfo = JSONObject.fromObject(verify);
        int code = userInfo.getInt("code");
        outData.putInt("code", code);
        if (code != 1) {
            sendError(zoneName, response, QAntErrorCode.TOKEN_EXPIRED);
            return;
        }

        JSONObject userObj = userInfo.getJSONObject("data");
        long loginId = userObj.getLong("id");
        String provider = userObj.getString("provider");

        String deviceId = null;
        Player player;
        if (loginId > 0) {
            player = playerManager.getPlayer(loginId, zoneName);
        } else {
            deviceId = userObj.getString("deviceId");
            player = playerManager.getPlayerByDevice(deviceId, zoneName);
        }

        if (player == null) {
            long gameId = autoIncrService.genAccountId();
            String playerId = zoneName + "#" + gameId;
            String fullname = "Guest#" + gameId;

            player = new Player(playerId, gameId, fullname);
            player.setDeviceId(deviceId);
            player.setLoginId(loginId, provider);
            player.setZoneName(zoneName);
            playerManager.updateGameHero(player);
        }


        String gameHeroName = player.getName();
        QAntUser user = getApi().getUserByName(player.getId());
        if (user != null) {
            send(ExtensionEvent.CMD_EXCEPTION,
                    MessageFactory.createErrorMsg(ExtensionEvent.CMD_USER_LOGIN, GameErrorCode.LOGIN_BY_OTHER_DEIVCE),
                    user);
            getApi().logout(user);
            getApi().disconnect(user.getChannel());
        }

        outData.putUtfString(Login.NEW_LOGIN_NAME, player.getId());
        outData.putLong("accountId", player.getGameId());
        outData.putLong("server_time", System.currentTimeMillis());
        outData.putUtfString("fn", gameHeroName);
        outData.putUtfString("tk", token);
        String avatar = "";
        if (userObj.containsKey("avatar"))
            avatar = userObj.getString("avatar");
        outData.putUtfString("avt", avatar);

        QAntTracer.info(this.getClass(), "-login: " + player.getId() + "-" + gameHeroName);

        unityBotManager.trackBotIn(player);
    }


    private void sendError(String zoneName, IResponse response, QAntErrorCode errCode) {
        IQAntObject resObj = QAntObject.newInstance();
        resObj.putShort("ec", errCode.getId());
        resObj.putUtfString("ep", zoneName);
        resObj.putUtfString("rs", errCode.getName());
        response.setContent(resObj);
        response.write();
    }

}
