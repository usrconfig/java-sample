package com.seagame.ext.managers;

import com.creants.creants_2x.core.extension.QAntExtension;
import com.creants.creants_2x.socket.gate.entities.IQAntObject;
import com.creants.creants_2x.socket.gate.wood.QAntUser;
import com.seagame.ext.services.ServiceHelper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.List;

/**
 * @author LamHM
 */
public abstract class AbstractExtensionManager {
    @Autowired
    protected ServiceHelper serviceHelper;

    protected QAntExtension extension;

    public void setExtension(QAntExtension extension) {
        this.extension = extension;
    }


    public void send(String cmdName, IQAntObject params, List<QAntUser> recipients) {
        this.extension.send(cmdName, params, recipients);
    }


    public void send(String cmdName, IQAntObject params, QAntUser recipient) {
        this.extension.send(cmdName, params, recipient);
    }

    public void send(String cmdName, IQAntObject params, String recipientId) {
        QAntUser user = extension.getApi().getUserByName(recipientId);
        if (user != null)
            send(cmdName, params, user);
    }

    public Collection<QAntUser> getUserOnlineList() {
        return extension.getParentZone().getUserList();
    }

    public QAntUser getUserByName(String id) {
        return extension.getApi().getUserByName(id);
    }
}
