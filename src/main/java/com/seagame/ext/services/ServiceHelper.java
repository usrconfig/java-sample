package com.seagame.ext.services;

import com.creants.creants_2x.socket.gate.entities.IQAntObject;
import com.creants.creants_2x.socket.gate.wood.QAntUser;
import com.seagame.ext.controllers.ExtensionEvent;
import com.seagame.ext.entities.Player;
import com.seagame.ext.managers.AbstractExtensionManager;
import com.seagame.ext.util.NetworkConstant;
import org.springframework.stereotype.Service;

/**
 * @author LamHM
 */
@Service
public class ServiceHelper extends AbstractExtensionManager implements NetworkConstant, ExtensionEvent, IMailService, INotifyService {


    private ServiceHelper() {
    }

    @Override
    public void sendTestMail(Player player) {
    }

    @Override
    public void sendTestInfoMail(Player player) {
    }

    @Override
    public void createWelcomeNewPlayerMail(String gameId) {

    }


    @Override
    public void sendNotify(IQAntObject iqAntObject, QAntUser user) {
        send(CMD_NTF, iqAntObject, user);
    }

    @Override
    public void sendNotify(IQAntObject iqAntObject, String user) {
        send(CMD_NTF, iqAntObject, user);
    }

    public void sendVipRewards(Player player) {

    }
}
