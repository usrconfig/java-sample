package com.seagame.ext.util;

import com.creants.creants_2x.socket.gate.entities.IQAntArray;
import com.creants.creants_2x.socket.gate.entities.IQAntObject;
import com.creants.creants_2x.socket.gate.entities.QAntArray;
import com.creants.creants_2x.socket.gate.entities.QAntObject;
import com.seagame.ext.controllers.ExtensionEvent;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author LamHM
 */
public class NotificationHelper implements ExtensionEvent {

    public static IQAntObject buildQuestNtfObject(IQAntArray questArr) {
        IQAntObject notification = QAntObject.newInstance();
        notification.putUtfString("group", NTF_GROUP_QUEST);
        notification.putUtfString("type", NTF_TYPE_COUNT);
        notification.putQAntArray("quests", questArr);
        return notification;
    }


    public static IQAntObject buildFriendNtfObject(Map<String, Integer> countMap) {
        IQAntObject notification = QAntObject.newInstance();
        notification.putUtfString("group", NTF_GROUP_FRIEND);
        notification.putUtfString("type", NTF_TYPE_COUNT);
        notification.putQAntArray("mapNo", buildCountArray(countMap));
        return notification;
    }


    public static IQAntArray buildCountArray(Map<String, Integer> countMap) {
        IQAntArray questArr = QAntArray.newInstance();
        for (String groupId : countMap.keySet()) {
            IQAntObject quest = QAntObject.newInstance();
            quest.putUtfString("group", groupId);
            quest.putInt("no", countMap.get(groupId));
            questArr.addQAntObject(quest);
        }

        return questArr;
    }


    public static IQAntObject incrNtfValue(IQAntObject notification, String groupId, int value) {
        IQAntArray questArr = notification.getCASArray("quests");
        for (int i = 0; i < questArr.size(); i++) {
            IQAntObject questObj = questArr.getQAntObject(i);
            if (questObj.getUtfString("group").equals(groupId)) {
                Integer no = questObj.getInt("no") + value;
                questObj.putInt("no", no < 0 ? 0 : no);
                break;
            }
        }

        notification.putQAntArray("quests", questArr);
        return notification;
    }


    public static IQAntObject incrNtfValue(IQAntObject notification, Map<String, Integer> countMap) {
        countMap.keySet().stream().forEach(groupId -> incrNtfValue(notification, groupId, countMap.get(groupId)));
        return notification;
    }

}
