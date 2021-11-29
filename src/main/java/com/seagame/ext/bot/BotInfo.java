package com.seagame.ext.bot;

import com.creants.creants_2x.core.entities.Room;
import com.creants.creants_2x.core.extension.QAntExtension;
import com.creants.creants_2x.socket.gate.entities.QAntObject;
import com.seagame.ext.entities.Player;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@ToString
public class BotInfo {
    String botID;
    String botToken;
    String zone;
    int room;
    String userId;
    boolean active;
    int maxPlayers;
    int playerCount;
    List<String> playerIds;
    String group;
    int priority;

    public BotInfo() {
        playerIds = new ArrayList<>();
    }

    public BotInfo(String botID, String botToken, String zone) {
        this.botID = botID;
        this.botToken = botToken;
        this.zone = zone;
        playerIds = new ArrayList<>();
    }

    public String getGroup() {
        if (this.group == null) {
            return "#";
        }
        return this.group;
    }

    public BotInfo(Room room, QAntExtension extension) {
        this.botID = room.getName();
        this.botToken = room.getName();
        this.zone = extension.getParentZone().getName();
        playerIds = new ArrayList<>();
    }

    public void active(Player player) {
        this.userId = player.getId();
        this.active = true;
    }

    public void inactive() {
        this.active = false;
    }

    public String buildBotString() {
        return UnityBotManager.BOT_PREFIX + botID + "#" + botToken + "#" + zone + "#" + room;
    }

    public void playerJoin(String playerId) {
        playerCount++;
        playerIds.add(playerId);
    }

    public void playerLeave(String playerId) {
        if (playerIds.remove(playerId))
            playerCount--;
    }

    public boolean canJoin() {
        return playerCount < maxPlayers;
    }

    public QAntObject build() {
        QAntObject object = new QAntObject();
        object.putUtfString("id", this.getBotID());
        object.putInt("max", maxPlayers);
        object.putInt("count", playerCount);
        return object;
    }
}
