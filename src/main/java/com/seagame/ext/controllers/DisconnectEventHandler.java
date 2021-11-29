package com.seagame.ext.controllers;

import com.creants.creants_2x.core.IQAntEvent;
import com.creants.creants_2x.core.QAntEventParam;
import com.creants.creants_2x.core.annotations.Instantiation;
import com.creants.creants_2x.core.exception.QAntException;
import com.creants.creants_2x.core.extension.BaseServerEventHandler;
import com.creants.creants_2x.core.util.QAntTracer;
import com.creants.creants_2x.socket.gate.wood.QAntUser;
import com.seagame.ext.ExtApplication;
import com.seagame.ext.bot.UnityBotManager;
import com.seagame.ext.managers.PlayerManager;

/**
 * @author LamHM
 */
@Instantiation(Instantiation.InstantiationMode.SINGLE_INSTANCE)
public class DisconnectEventHandler extends BaseServerEventHandler {

    private PlayerManager playerManager;
    private UnityBotManager unityBotManager;


    public DisconnectEventHandler() {
        playerManager = ExtApplication.getBean(PlayerManager.class);
        unityBotManager = ExtApplication.getBean(UnityBotManager.class);
    }

    @Override
    public void handleServerEvent(IQAntEvent event) throws QAntException {
        Object parameter = event.getParameter(QAntEventParam.USER);
        if (parameter != null) {
            QAntUser user = (QAntUser) parameter;
            playerManager.logout(user);
            unityBotManager.trackBotOut(user.getName());
            QAntTracer.debug(this.getClass(),
                    "User disconnected. gameId: " + user.getName() + "-" + user.getFullName());
        } else {
            QAntTracer.debug(this.getClass(), "User disconnected.");
        }
    }

}
