package com.seagame.ext.managers;

import com.creants.creants_2x.socket.gate.entities.IQAntArray;
import com.creants.creants_2x.socket.gate.entities.IQAntObject;
import com.creants.creants_2x.socket.gate.entities.QAntArray;
import com.seagame.ext.dao.BuddyListRepository;
import com.seagame.ext.dao.FriendRequestRepository;
import com.seagame.ext.entities.GroupCount;
import com.seagame.ext.entities.Player;
import com.seagame.ext.entities.friend.BuddyList;
import com.seagame.ext.entities.friend.FriendRequest;
import com.seagame.ext.exception.GameException;
import com.seagame.ext.util.NetworkConstant;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author LamHM
 */
@Service
public class FriendManager implements InitializingBean {

    @Autowired
    private FriendRequestRepository friendRequestRepo;

    @Autowired
    private BuddyListRepository buddyListRepo;

    @Autowired
    private PlayerManager gameHeroManager;

    private Map<String, BuddyList> friendMap;

    private Map<String, NotifyObject> notificationMap;

    private Map<String, Set<String>> friendUsedMap;

    private int countUser;


    @Override
    public void afterPropertiesSet() throws Exception {
        friendMap = new ConcurrentHashMap<>();
        notificationMap = new ConcurrentHashMap<>();
        friendUsedMap = new HashMap<>();
        countUser = countUser();
    }


    private Integer countUser() {
        return 0;
    }

    public List<GroupCount> getNotiCount(String gameHeroId) {
        NotifyObject notifyObject = notificationMap.get(gameHeroId);
        if (notifyObject == null || notifyObject.isExpire())
            notifyObject = countUserRequest(gameHeroId);

        return notifyObject.getNotiList();
    }


    public NotifyObject countUserRequest(String gameHeroId) {
        List<GroupCount> notiList;
        BuddyList buddyList = buddyListRepo.getBuddyListContainFields(gameHeroId);
        notiList = new ArrayList<>(Arrays.asList(
                new GroupCount(NetworkConstant.FRIEND_REQUEST_GROUP, friendRequestRepo.countRequest(gameHeroId)),
                new GroupCount(NetworkConstant.NEW_FRIEND_GROUP, buddyList != null ? 0 : 0)));
        NotifyObject notifyObject = new NotifyObject(notiList, System.currentTimeMillis());
        notificationMap.put(gameHeroId, notifyObject);
        return notifyObject;
    }

    public IQAntArray searchFriendByName(String server, String name, String gameHeroId) {
        IQAntArray result = QAntArray.newInstance();
        // TODO refactor performance
        gameHeroManager.findPlayer(server, name).forEach(gameHero -> {
            if (!gameHero.getId().equals(gameHeroId)) {
                IQAntObject friendInfo = gameHero.buildFriendInfo();
                result.addQAntObject(friendInfo);
            }
        });
        return result;
    }


    public BuddyList getBuddyList(String gameHeroId, boolean includeGameHeroInfo) {
        BuddyList buddyList = friendMap.get(gameHeroId);
        if (buddyList == null) {
            buddyList = buddyListRepo.findById(gameHeroId).orElseGet(() -> buddyListRepo.save(new BuddyList(gameHeroId)));
            friendMap.put(gameHeroId, buddyList);
            friendUsedMap.put(gameHeroId, new HashSet<>());
        }

        return buddyList;
    }

    public IQAntArray getBuddyList(String gameHeroId) {
        IQAntArray result = QAntArray.newInstance();

        BuddyList buddyList = getBuddyList(gameHeroId, true);
        Set<String> friendIds = buddyList.getFriendIds();
        friendIds.forEach(id -> {
            Player gameHero = gameHeroManager.getPlayer(id);
            if (gameHero != null) {
                // TODO refactor performance
                IQAntObject friendInfo = gameHero.buildFriendInfo();
                result.addQAntObject(friendInfo);
            }

        });
        getNotiCount(gameHeroId).stream().filter(GroupCount::isNewFriendGroup).findFirst().get().setCount(0);
        return result;
    }


    public List<FriendRequest> getRequestList(String gameHeroId) {
        return friendRequestRepo.getRequestList(gameHeroId, PageRequest.of(0, 10));
    }

    public List<FriendRequest> getSendRequestList(String gameHeroId) {
        return friendRequestRepo.getSendRequestList(gameHeroId, PageRequest.of(0, 10));
    }


    public void denyFriendRequest(String gameHeroId, String userRequestId) {
        friendRequestRepo.deleteById(FriendRequest.genRequestId(userRequestId, gameHeroId));
    }


    /**
     * @param gameHeroId    user yêu cầu request
     * @param userReceiveId user nhận request
     * @return
     */
    public boolean requestFriend(String gameHeroId, String userReceiveId) {
        if (isFriend(gameHeroId, userReceiveId))
            return false;

        Player gameHero = gameHeroManager.getPlayer(gameHeroId);
        if (gameHero.isNPC())
            return false;

        if (friendRequestRepo.existsById(FriendRequest.genRequestId(gameHeroId, userReceiveId)))
            return false;

        FriendRequest userRequest = new FriendRequest(gameHeroId, userReceiveId);
        friendRequestRepo.save(userRequest);
        return true;
    }


    public boolean isFriend(String gameHeroId, String friendId) {
        return getBuddyList(gameHeroId, false).isFriend(friendId);
    }


    public boolean isNewFriend(String gameHeroId, String userRequestId) {
        return !(getBuddyList(gameHeroId, false).isFriend(userRequestId)
                || friendRequestRepo.existsById(FriendRequest.genRequestId(userRequestId, gameHeroId)));
    }


    public void addUsedList(String gameHeroId, String friendId) {
        friendUsedMap.get(gameHeroId).add(friendId);
    }


    public void resetUsedList(String gameHeroId) {
        friendUsedMap.put(gameHeroId, new HashSet<>());
    }


    public boolean unfriend(String gameHeroId, String friendId) {
        BuddyList buddyList = getBuddyList(gameHeroId, false);
        if (buddyList.isFriend(friendId)) {
            buddyList.getFriendIds().removeIf(item -> item.equals(friendId));
            BuddyList buddyListCache = friendMap.get(gameHeroId);
            buddyListCache.getFriendIds().removeIf(item -> item.equals(friendId));
            buddyListRepo.save(buddyList);
            return true;
        }
        return false;
    }


    public Player acceptFriendRequest(String gameHeroId, String userRequestId) throws GameException {
        BuddyList buddyList = getBuddyList(gameHeroId, true);
        if (buddyList.isMaxFriend())
            throw new GameException();

        friendRequestRepo.deleteById(FriendRequest.genRequestId(userRequestId, gameHeroId));
        friendRequestRepo.deleteById(FriendRequest.genRequestId(gameHeroId, userRequestId));
        Player gameHeroRequester = gameHeroManager.getPlayer(userRequestId);
        buddyList.addFriend(gameHeroRequester);
        buddyListRepo.save(buddyList);

        BuddyList requestUserBuddyList = getBuddyList(userRequestId, true);
        Player gameHero = gameHeroManager.getPlayer(gameHeroId);
        requestUserBuddyList.addFriend(gameHero);
        buddyListRepo.save(requestUserBuddyList);

        return gameHeroRequester;
    }

    public void unInvite(String gameHeroId, String userRequestId) {
        friendRequestRepo.deleteById(FriendRequest.genRequestId(userRequestId, gameHeroId));
        friendRequestRepo.deleteById(FriendRequest.genRequestId(gameHeroId, userRequestId));
    }

    private class NotifyObject {
        private List<GroupCount> notiList;
        private long timeMillis;


        public NotifyObject(List<GroupCount> notiList, long timeMillis) {

            this.notiList = notiList;
            this.timeMillis = timeMillis;
        }


        public List<GroupCount> getNotiList() {
            return notiList;
        }


        public boolean isExpire() {
            return (System.currentTimeMillis() - timeMillis) > 10000;
        }
    }
}
