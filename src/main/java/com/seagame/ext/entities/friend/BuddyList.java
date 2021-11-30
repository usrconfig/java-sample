package com.seagame.ext.entities.friend;

import com.seagame.ext.entities.Player;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author LamHM
 */
@Getter
@Document(collection = "buddy-list")
public class BuddyList {
    private @Id
    String gameHeroId;
    @Transient
    private static final int maxFriend = 50;
    private Set<String> friendIds;
    @Field("fpReceivedList")
    private Set<String> fbList;
    private int newFriends;


    public BuddyList() {
    }


    public BuddyList(String gameHeroId) {
        this.gameHeroId = gameHeroId;
        friendIds = new HashSet<>();
        fbList = new HashSet<>();
    }


    public int getNewFriends() {
        return newFriends;
    }


    public void setNewFriends(int newFriends) {
        this.newFriends = newFriends;
    }


    public String getGameHeroId() {
        return gameHeroId;
    }


    public void addFriend(Player player) {
        friendIds.add(player.getId());
    }


    public boolean isMaxFriend() {
        return friendIds.size() >= maxFriend;
    }


    public void setGameHeroId(String gameHeroId) {
        this.gameHeroId = gameHeroId;
    }


    public Set<String> getFriendIds() {
        return friendIds;
    }


    public void setFriendIds(Set<String> friendIds) {
        this.friendIds = friendIds;
    }


    public boolean isFriend(String gameHeroId) {
        return friendIds.contains(gameHeroId);
    }


    public int getMaxFriend() {
        return maxFriend;
    }


    public boolean checkFPReceived(String receiver) {
        return fbList != null && fbList.contains(receiver);
    }


    public Set<String> getFriendNotReceivedFBYet() {
        Set<String> result = friendIds.stream().filter(gameHeroId -> !fbList.contains(gameHeroId))
                .collect(Collectors.toSet());
        fbList.addAll(result);
        return result;
    }


    public void resetReceivedFPList() {
        fbList = new HashSet<>();
    }


    public boolean checkAndAddFriendPointReceived(String receiver) {
        if (fbList == null)
            fbList = new HashSet<>();

        if (fbList.contains(receiver))
            return false;

        fbList.add(receiver);
        return true;
    }

}
