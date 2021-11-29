package com.seagame.ext.services;

import com.creants.creants_2x.socket.gate.entities.IQAntObject;
import com.creants.creants_2x.socket.gate.wood.QAntUser;

public interface INotifyService {

    void sendNotify(IQAntObject iqAntObject, QAntUser user);

    void sendNotify(IQAntObject iqAntObject, String user);
}
