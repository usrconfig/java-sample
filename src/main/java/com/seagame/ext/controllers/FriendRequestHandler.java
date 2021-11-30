package com.seagame.ext.controllers;

import com.creants.creants_2x.core.annotations.Instantiation;
import com.creants.creants_2x.socket.gate.entities.IQAntObject;
import com.creants.creants_2x.socket.gate.entities.QAntArray;
import com.creants.creants_2x.socket.gate.wood.QAntUser;
import com.seagame.ext.ExtApplication;
import com.seagame.ext.entities.GroupCount;
import com.seagame.ext.entities.Player;
import com.seagame.ext.entities.friend.FriendRequest;
import com.seagame.ext.exception.GameErrorCode;
import com.seagame.ext.exception.GameException;
import com.seagame.ext.managers.FriendManager;
import com.seagame.ext.managers.PlayerManager;
import com.seagame.ext.quest.QuestSystem;
import com.seagame.ext.services.NotifySystem;
import com.seagame.ext.util.NotificationHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.seagame.ext.exception.GameErrorCode.LACK_OF_INFOMATION;

/**
 * @author LamHM
 */
@Instantiation(Instantiation.InstantiationMode.SINGLE_INSTANCE)
public class FriendRequestHandler extends ZClientRequestHandler {
    private static final int GET_SUGGEST_LIST = 1;
    private static final int INVITE = 2;
    private static final int GET_BUDDY_LIST = 3;
    private static final int GET_INVITE_LIST = 4;
    private static final int ACCEPT_INVITE = 5;
    private static final int DENY_INVITE = 6;
    private static final int SEARCH_FRIEND = 7;
    private static final int UN_FRIEND = 8;
    private static final int GET_PLAYER_INVITE_LIST = 9;
    private static final int UNINVITE = 10;

    private QuestSystem questSystem;
    private NotifySystem notifySystem;
    private PlayerManager playerManager;
    private FriendManager friendManager;


    public FriendRequestHandler() {
        notifySystem = ExtApplication.getBean(NotifySystem.class);
        questSystem = ExtApplication.getBean(QuestSystem.class);
        playerManager = ExtApplication.getBean(PlayerManager.class);
        friendManager = ExtApplication.getBean(FriendManager.class);
    }


    @Override
    public void handleClientRequest(QAntUser user, IQAntObject params) {
        Integer action = this.getAction(params);
        if (action == null) {
            responseError(user, LACK_OF_INFOMATION);
            return;
        }
        switch (action) {
            case GET_SUGGEST_LIST:
                getSuggestList(user, params);
                break;
            case INVITE:
                doInvite(user, params);
                break;
            case GET_BUDDY_LIST:
                getBuddyList(user, params);
                break;
            case GET_INVITE_LIST:
                getInviteList(user, params);
                break;
            case GET_PLAYER_INVITE_LIST:
                setGetPlayerInviteSendList(user, params);
                break;
            case ACCEPT_INVITE:
                acceptInvite(user, params);
                break;
            case DENY_INVITE:
                denyInvite(user, params);
                break;
            case SEARCH_FRIEND:
                searchFriend(user, params);
                break;
            case UN_FRIEND:
                unfriend(user, params);
                break;
            case UNINVITE:
                uninvite(user, params);
                break;

            default:
                break;
        }
    }


    private void unfriend(QAntUser user, IQAntObject params) {
        String friendId = params.getUtfString("id");
        boolean isSuccess = friendManager.unfriend(user.getName(), friendId);
        send(params, user);
        if (isSuccess) {
            friendManager.unfriend(friendId, user.getName());
            QAntUser friend = getApi().getUserByName(friendId);
            if (friend != null) {
                params.putUtfString("id", user.getName());
                send(params, friend);
            }
        }
    }

    private void uninvite(QAntUser user, IQAntObject params) {
        String friendId = params.getUtfString("id");
        friendManager.unInvite(user.getName(), friendId);
        send(params, user);
    }


    private void getInviteList(QAntUser user, IQAntObject params) {
        List<FriendRequest> requestList = friendManager.getRequestList(user.getName());
        QAntArray array = new QAntArray();
        requestList.forEach(friendRequest -> {
            Player player = playerManager.getPlayer(friendRequest.getUserRequestId());
            array.addQAntObject(player.buildFriendInfo());
        });
        params.putQAntArray("requestList", array);
        send(params, user);
    }

    private void setGetPlayerInviteSendList(QAntUser user, IQAntObject params) {
        List<FriendRequest> requestList = friendManager.getSendRequestList(user.getName());
        QAntArray array = new QAntArray();
        requestList.forEach(friendRequest -> {
            Player player = playerManager.getPlayer(friendRequest.getPlayerId());
            array.addQAntObject(player.buildFriendInfo());
        });
        params.putQAntArray("requestList", array);
        send(params, user);
    }


    private void denyInvite(QAntUser user, IQAntObject params) {
        friendManager.denyFriendRequest(user.getName(), params.getUtfString("id"));
        params.putInt(KEYI_CODE, 1);
        send(params, user);

        List<GroupCount> notificationCount = friendManager.getNotiCount(user.getName());
        GroupCount requestGroup = notificationCount.stream().filter(GroupCount::isRequestGroup).findAny().get();
        requestGroup.incrValue(-1);
        if (requestGroup.getCount() <= 0)
            send(CMD_NTF, NotificationHelper.buildFriendNtfObject(notificationCount), user);
    }


    private void acceptInvite(QAntUser user, IQAntObject params) {
        Player newFriend;
        String id = params.getUtfString("id");
        try {
            newFriend = friendManager.acceptFriendRequest(user.getName(), id);
        } catch (GameException e) {
            responseError(user, GameErrorCode.MAX_FRIEND);
            return;
        }

        if (newFriend == null) {
            responseError(user, GameErrorCode.PLAYER_NOT_FOUND);
            return;
        }

        // TODO error newFriend null, trong danh sach request bam ađ friend loi
        params.putQAntObject("friendInfo", newFriend.buildFriendInfo());
        send(params, user);
        notifySystem.notifyFriendChange(newFriend.getId(), user.getName(), "accept");
    }


    private void getBuddyList(QAntUser user, IQAntObject params) {
        params.putQAntArray("buddyList", friendManager.getBuddyList(user.getName()));
        send(params, user);
        send(CMD_NTF, NotificationHelper.buildFriendNtfObject(friendManager.getNotiCount(user.getName())), user);
    }


    private void doInvite(QAntUser user, IQAntObject params) {
        String userReceiveId = params.getUtfString("id");
        if (userReceiveId != null && !userReceiveId.equals(user.getName())) {
            boolean requestFriend = friendManager.requestFriend(user.getName(), userReceiveId);
            QAntUser friend = getApi().getUserByName(userReceiveId);
            List<GroupCount> notificationCount = friendManager.getNotiCount(userReceiveId);
            if (requestFriend) {
                notificationCount.stream().filter(GroupCount::isRequestGroup).findFirst().get().incrValue(1);
            }
            if (friend != null)
                send(CMD_NTF, NotificationHelper.buildFriendNtfObject(notificationCount), friend);
        }
        send(params, user);
    }


    private void getSuggestList(QAntUser user, IQAntObject params) {
        QAntArray array = new QAntArray();
        List<Player> values = new ArrayList<>(playerManager.getGameHeroMap().values());
        Collections.shuffle(values);
        values.stream().filter(player1 -> !player1.getId().equals(user.getName())).limit(10).forEach(player1 -> array.addQAntObject(player1.buildFriendInfo()));
        params.putQAntArray("buddyList", array);
        send(params, user);
    }


    private void searchFriend(QAntUser user, IQAntObject params) {
        String name = params.getUtfString("name");
        if (name == null || name.length() < 4) {
            responseError(user, GameErrorCode.NAME_MIN_MAX);
            return;
        }
        params.putQAntArray("buddyList", friendManager.searchFriendByName(user.getZone().getName(), name.trim(), user.getName()));
        params.putInt("page", 1);
        params.putInt("maxPage", 1);
        send(params, user);
    }

    @Override
    protected String getHandlerCmd() {
        return CMD_FRIEND;
    }
}
