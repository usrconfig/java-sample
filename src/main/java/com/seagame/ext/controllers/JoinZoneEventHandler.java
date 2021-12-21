package com.seagame.ext.controllers;

import com.creants.creants_2x.core.IQAntEvent;
import com.creants.creants_2x.core.QAntEventParam;
import com.creants.creants_2x.core.annotations.Instantiation;
import com.creants.creants_2x.core.extension.BaseServerEventHandler;
import com.creants.creants_2x.socket.gate.entities.IQAntObject;
import com.creants.creants_2x.socket.gate.entities.QAntObject;
import com.creants.creants_2x.socket.gate.wood.QAntUser;
import com.seagame.ext.ExtApplication;
import com.seagame.ext.entities.Player;
import com.seagame.ext.managers.PlayerManager;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author LamHM
 */
@Instantiation(Instantiation.InstantiationMode.SINGLE_INSTANCE)
public class JoinZoneEventHandler extends BaseServerEventHandler {

    private PlayerManager playerManager;


    public JoinZoneEventHandler() {
        playerManager = ExtApplication.getBean(PlayerManager.class);
    }


    @Override
    public void handleServerEvent(IQAntEvent event) {
        QAntUser user = (QAntUser) event.getParameter(QAntEventParam.USER);
        IQAntObject response = new QAntObject();
        Collection<String> itemsUpdate = new ArrayList<>();
        Player player = playerManager.login(user, response, itemsUpdate);
        user.setFullName(player.getName());
        IQAntObject params = player.buildLoginInfo();
        params.putQAntObject("stats", response);
        send("cmd_join_game", params, user);
    }
}
