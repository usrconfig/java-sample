package com.seagame.ext.services;

import com.creants.creants_2x.socket.gate.entities.IQAntObject;
import com.creants.creants_2x.socket.gate.entities.QAntObject;
import com.creants.creants_2x.socket.gate.wood.QAntUser;
import com.seagame.ext.ExtApplication;
import com.seagame.ext.controllers.ExtensionEvent;
import com.seagame.ext.entities.Player;
import com.seagame.ext.exception.GameErrorCode;
import com.seagame.ext.managers.AbstractExtensionManager;
import com.seagame.ext.managers.PlayerManager;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * @author LamHM
 */
@Service
public class NotifySystem extends AbstractExtensionManager implements ExtensionEvent {

    public void notifyMiniGame(String heroId, String func) {
        try {
            QAntUser receiverUser = getUserByName(heroId);
            IQAntObject antObject = new QAntObject();
            antObject.putInt("act", ExtensionEvent.NOTIFY_MINI_GAME);
            antObject.putUtfString("func", func);
            send(ExtensionEvent.CMD_NTF, antObject, receiverUser);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void notifyExpChange(String owner, IQAntObject buildInfoLevel, QAntUser user) {
        try {
            QAntUser receiverUser = getUserByName(owner);
            IQAntObject antObject = new QAntObject();
            antObject.putInt("act", ExtensionEvent.NOTIFY_EXP_CHANGE);
            antObject.putQAntObject("hero", buildInfoLevel);
            send(ExtensionEvent.CMD_NTF, antObject, receiverUser);
            if (user != null && !user.getName().equals(owner))
                send(ExtensionEvent.CMD_NTF, antObject, user);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void responseError(QAntUser user, GameErrorCode error) {
        IQAntObject createErrorMsg = MessageFactory.createErrorMsg(CMD_NTF, error);
        send("cmd_exception", createErrorMsg, user);
    }

    public void notifyPlayerInfoChange(String playerId, IQAntObject buildInfoLevel) {
        try {
            QAntUser receiverUser = getUserByName(playerId);
            IQAntObject antObject = new QAntObject();
            antObject.putInt("act", ExtensionEvent.NOTIFY_PLAYER_INFO_CHANGE);
            antObject.putQAntObject("player", buildInfoLevel);
            send(ExtensionEvent.CMD_NTF, antObject, receiverUser);
        } catch (Exception ignored) {
        }
    }

    public void notifyRoomTimeout(List<QAntUser> users) {
        IQAntObject createErrorMsg = MessageFactory.createErrorMsg(CMD_NTF, GameErrorCode.ROOM_TIME_OUT);
        send("cmd_exception", createErrorMsg, users);
    }

    public void notifyFriendChange(String userId, String playerId, String code) {
        try {
            QAntUser receiverUser = getUserByName(userId);
            IQAntObject antObject = new QAntObject();
            antObject.putInt("act", ExtensionEvent.NOTIFY_FRIEND_CHANGE);
            PlayerManager playerManager = ExtApplication.getBean(PlayerManager.class);
            Player player = playerManager.getPlayer(playerId);
            antObject.putQAntObject("player", player.buildFriendInfo());
            antObject.putUtfString("code", code);
            send(ExtensionEvent.CMD_NTF, antObject, receiverUser);
        } catch (Exception ignored) {
        }
    }
}
