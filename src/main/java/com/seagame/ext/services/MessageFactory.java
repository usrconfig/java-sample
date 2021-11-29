package com.seagame.ext.services;

import com.creants.creants_2x.socket.gate.entities.IQAntObject;
import com.creants.creants_2x.socket.gate.entities.QAntArray;
import com.creants.creants_2x.socket.gate.entities.QAntObject;
import com.seagame.ext.controllers.ExtensionEvent;
import com.seagame.ext.exception.GameErrorCode;
import com.seagame.ext.util.NetworkConstant;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;


/**
 * @author LamHM
 */
public class MessageFactory implements NetworkConstant, ExtensionEvent {


    public static IQAntObject createErrorMsg(String errorCmd, GameErrorCode errorCode, String... attrbutes) {
        IQAntObject params = QAntObject.newInstance();
        params.putShort("ec", errorCode.getId());

        if (attrbutes == null)
            params.putUtfString("msg", errorCode.getMsg());
        else
            params.putUtfString("msg", new MessageFormat(errorCode.getMsg()).format(attrbutes));

        params.putUtfString("cmd", errorCmd);
        return params;
    }


    public static IQAntObject createErrorMsg(String errorCmd, Integer action, GameErrorCode errorCode,
                                             String... attrbutes) {
        IQAntObject errorMsg = createErrorMsg(errorCmd, errorCode, attrbutes);
        if (action != null)
            errorMsg.putInt(KEYI_ACTION, action);
        return errorMsg;
    }


    private static IQAntObject buildNotification(String group, String type) {
        IQAntObject response = QAntObject.newInstance();
        response.putUtfString("group", group);
        response.putUtfString("type", type);
        return response;
    }

}
