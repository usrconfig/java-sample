package com.seagame.ext.controllers;

import com.creants.creants_2x.core.extension.BaseClientRequestHandler;
import com.creants.creants_2x.core.util.QAntTracer;
import com.creants.creants_2x.socket.gate.entities.IQAntObject;
import com.creants.creants_2x.socket.gate.wood.QAntUser;
import com.seagame.ext.exception.GameErrorCode;
import com.seagame.ext.services.MessageFactory;
import com.seagame.ext.util.NetworkConstant;

import java.util.List;

public abstract class ZClientRequestHandler extends BaseClientRequestHandler implements NetworkConstant, ExtensionEvent {
    protected Integer action;
    private Integer correlationId;


    protected void responseError(QAntUser user, GameErrorCode error, String... attrs) {
        IQAntObject createErrorMsg = MessageFactory.createErrorMsg(this.getHandlerCmd(), this.action, error, attrs);
        QAntTracer.debug(ZClientRequestHandler.class, String.join(",", attrs));
        sendError(createErrorMsg, user);
    }


    protected abstract String getHandlerCmd();

    protected Integer getAction(IQAntObject params) {
        this.action = params.getInt(KEYI_ACTION);
        this.correlationId = params.getInt(KEYI_CORR_ID);
        return this.action;
    }

    @Override
    protected void send(String cmdName, IQAntObject params, List<QAntUser> recipients) {
        if (this.action != null)
            params.putInt("act", this.action);
        if (this.correlationId != null)
            params.putInt("coId", this.correlationId);
        super.send(cmdName, params, recipients);
    }

    @Override
    protected void send(String cmdName, IQAntObject params, QAntUser recipient) {
        if (this.action != null)
            params.putInt("act", this.action);
        if (this.correlationId != null)
            params.putInt("coId", this.correlationId);
        super.send(cmdName, params, recipient);
    }

    protected void send(IQAntObject params, List<QAntUser> recipients) {
        send(getHandlerCmd(), params, recipients);
    }

    protected void send(IQAntObject params, QAntUser recipient) {
        send(getHandlerCmd(), params, recipient);
    }

    @Override
    protected void sendError(IQAntObject params, QAntUser recipient) {
        if (this.action != null)
            params.putInt("act", this.action);
        if (this.correlationId != null)
            params.putInt("coId", this.correlationId);
        super.sendError(params, recipient);
    }

    public void sendNotify(IQAntObject iqAntObject, QAntUser user) {
        send(CMD_NTF, iqAntObject, user);
    }

    public void sendNotify(IQAntObject iqAntObject, String gameHeroId) {
        QAntUser user = getApi().getUserByName(gameHeroId);
        if (user != null)
            sendNotify(iqAntObject, user);
    }

    protected void customAct(int act) {
        this.action = act;
    }
}
