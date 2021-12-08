package com.seagame.ext.controllers;

import com.creants.creants_2x.core.annotations.Instantiation;
import com.creants.creants_2x.core.util.QAntTracer;
import com.creants.creants_2x.socket.gate.entities.IQAntArray;
import com.creants.creants_2x.socket.gate.entities.IQAntObject;
import com.creants.creants_2x.socket.gate.entities.QAntArray;
import com.creants.creants_2x.socket.gate.entities.QAntObject;
import com.creants.creants_2x.socket.gate.wood.QAntUser;
import com.seagame.ext.ExtApplication;
import com.seagame.ext.entities.Player;
import com.seagame.ext.entities.arena.ArenaHistory;
import com.seagame.ext.entities.arena.ArenaPower;
import com.seagame.ext.entities.arena.RevengeInfo;
import com.seagame.ext.entities.item.HeroItem;
import com.seagame.ext.exception.GameErrorCode;
import com.seagame.ext.exception.GameException;
import com.seagame.ext.exception.UseItemException;
import com.seagame.ext.managers.ArenaManager;
import com.seagame.ext.managers.HeroItemManager;
import com.seagame.ext.managers.PlayerManager;
import com.seagame.ext.quest.QuestSystem;
import com.seagame.ext.util.CalculateUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author LamHM
 */
@Instantiation(Instantiation.InstantiationMode.SINGLE_INSTANCE)
public class ArenaRequestHandler extends ZClientRequestHandler {
    private static final int FIND_FEE = 1;
    private static final int JOIN = 1;
    private static final int FIND = 2;
    private static final int FIGHT = 3;
    private static final int FINISH = 4;
    private static final int GET_HISTORY_LIST = 5;
    private static final int REPLAY = 6;
    private static final int REVENGE = 7;
    private static final int GET_OPPONENT_INFO = 8;
    private static final int GET_ARENA_RANKING = 9;

    private static final String BLESS_INDEX = "9999";
    private static final int ARENA_ENERGY_COST = 1;

    private ArenaManager arenaManager;
    private QuestSystem questSystem;
    private HeroItemManager heroItemManger;
    private PlayerManager playerManager;


    public ArenaRequestHandler() {
        arenaManager = ExtApplication.getBean(ArenaManager.class);
        questSystem = ExtApplication.getBean(QuestSystem.class);
        heroItemManger = ExtApplication.getBean(HeroItemManager.class);
        playerManager = ExtApplication.getBean(PlayerManager.class);
    }


    @Override
    public void handleClientRequest(QAntUser user, IQAntObject params) {
        Integer act = params.getInt("act");
        if (act == null)
            act = JOIN;

        switch (act) {
            case JOIN:
                join(user, params);
                break;
            case FIND:
                find(user, params);
                break;
            case FIGHT:
                attack(user, params);
                break;
            case FINISH:
                finish(user, params);
                break;
            case GET_HISTORY_LIST:
                getHistoryList(user, params);
                break;
            case REPLAY:
                replay(user, params);
                break;
            case REVENGE:
                revenge(user, params);
                break;
            case GET_OPPONENT_INFO:
                getOpponentInfo(user, params);
                break;
            case GET_ARENA_RANKING:
                getArenaRanking(user, params);
                break;

            default:
                break;
        }
    }


    private void getArenaRanking(QAntUser user, IQAntObject params) {
        List<ArenaPower> top = arenaManager.getTopArenaPoint(user.getZone().getName());
        IQAntArray topArr = QAntArray.newInstance();
        top.stream().map(ArenaPower::buildInfo)
                .forEach(topArr::addQAntObject);
        params.putQAntArray("arr", topArr);
        send(params, user);

    }

    private void revenge(QAntUser user, IQAntObject params) {
        try {
            heroItemManger.useArenaTicket(user, 1);
        } catch (GameException e) {
            responseError(user, GameErrorCode.NOT_ENOUGH_TICKET);
            return;
        }

        String gameHeroId = user.getName();
        long battleId = params.getLong("id");
        List<ArenaHistory> battle = arenaManager.getBattle(battleId);
        ArenaHistory arenaHistory = battle.stream().filter(history -> history.getPlayerId().equals(user.getName()))
                .findFirst().get();

        battle.remove(arenaHistory);
        ArenaPower attacker = arenaManager.getArenaPower(gameHeroId);
        ArenaPower defender = arenaManager.fight(user, attacker, battle.get(0).getPlayerId());

        RevengeInfo revengeInfo = new RevengeInfo();
        revengeInfo.setOpponent(defender);
        revengeInfo.setHistoryId(arenaHistory.getId());
        arenaManager.cacheBattle(gameHeroId, revengeInfo);

        if (defender == null) {
//            IQAntObject createErrorMsg = MessageFactory.createErrorMsg(CMD_ARENA, GameErrorCode.OPPONENT_IN_BATTLE);
//            createErrorMsg.putInt(KEYI_ACTION, REVENGE);
//            createErrorMsg.putUtfString("msg", "You have been returned " + FIND_FEE + " Zen");
//            sendError(createErrorMsg, user);
//            heroItemManger.addConsumeableItem(user, ZEN_INDEX, FIND_FEE);
            return;
        }

//        send(CMD_NTF, MessageFactory.buildNotiUpdateArenaInfo(attacker), user);
        params.putQAntObject("your_team", defender.getOpponent().buildInfo());
        params.putQAntObject("opponent", defender.buildInfo());
        send(params, user);
        trackParams(params);
    }


    private void getOpponentInfo(QAntUser user, IQAntObject params) {
        String opponentId = params.getUtfString("id");
        ArenaPower attackerPower = arenaManager.getArenaPower(user.getName());
        ArenaPower opponentPower = arenaManager.getDefenderTeam(opponentId);

        IQAntObject buildArenaInfo = opponentPower.buildInfo();
        buildArenaInfo.putInt("winPoint",
                CalculateUtil.calcTrophyAttackerWin(attackerPower.getArenaPoint(), opponentPower.getArenaPoint(),
                        attackerPower.getAtkTeam().getTeamPower(), opponentPower.getAtkTeam().getTeamPower()));
        buildArenaInfo.putInt("losePoint",
                CalculateUtil.calcTrophyAttackerLose(attackerPower.getArenaPoint(), opponentPower.getArenaPoint(),
                        attackerPower.getAtkTeam().getTeamPower(), opponentPower.getAtkTeam().getTeamPower()));
        params.putQAntObject("opponent", buildArenaInfo);
        send(params, user);
        trackParams(params);
    }


    private void replay(QAntUser user, IQAntObject params) {
        long id = params.getLong("id");
        List<ArenaHistory> battle = arenaManager.getBattle(id);
        ArenaHistory arenaHistory = battle.stream().filter(history -> history.getPlayerId().equals(user.getName()))
                .findFirst().get();
        IQAntObject battleObj = QAntObject.newFromObject(arenaHistory);
        battleObj.removeElement("opponentInfo");

        battle.remove(arenaHistory);

        params.putUtfString("opponent", arenaHistory.getOpponent());
        params.putUtfString("your_team", battle.get(0).getOpponent());
        params.putQAntObject("battle", battleObj);
        send(params, user);
        trackParams(params);
    }


    private void getHistoryList(QAntUser user, IQAntObject params) {
        Integer page = params.getInt("page");
        if (page == null)
            page = 0;

        params.putQAntArray("historyList",
                QAntArray.newFromListObject(arenaManager.getHistoryList(user.getName(), page)));

        send(params, user);
        trackParams(params);
    }


    private void finish(QAntUser user, IQAntObject params) {
        QAntTracer.debug(this.getClass(), "------------ Arena fight finish -----------------");
        Boolean isWin = params.getBool("win");
        ArenaPower arenaPower = arenaManager.finish(user, isWin, params);
        params.putQAntObject("arena", arenaPower.buildInfo());
        send(params, user);
        trackParams(params);
    }


    private void attack(QAntUser user, IQAntObject params) {

        try {
            heroItemManger.useArenaTicket(user, 1);
        } catch (GameException e) {
            responseError(user, GameErrorCode.NOT_ENOUGH_TICKET);
            return;
        }

        try {
            playerManager.useEnergy(user.getName(), ARENA_ENERGY_COST);
        } catch (UseItemException e) {
            responseError(user, GameErrorCode.NOT_ENOUGH_ENERGY);
            return;
        }


        ArenaPower attacker = arenaManager.getArenaPower(user.getName());
        ArenaPower defender = arenaManager.fight(user, attacker, params.getUtfString("id"));

        if (defender == null) {
//            IQAntObject createErrorMsg = MessageFactory.createErrorMsg(CMD_ARENA, GameErrorCode.OPPONENT_IN_BATTLE);
//            createErrorMsg.putInt(KEYI_ACTION, FIGHT);
//            createErrorMsg.putUtfString("msg", "You have been returned " + FIND_FEE + " Zen");
//            sendError(createErrorMsg, user);
//            heroItemManger.addConsumeableItem(user, ZEN_INDEX, FIND_FEE);
            return;
        }

        arenaManager.cacheBattle(user.getName(), defender);

//        send(CMD_NTF, MessageFactory.buildNotiUpdateArenaInfo(attacker), user);
        params.putQAntObject("your_team", defender.getOpponent().buildInfo());
        params.putQAntObject("opponent", defender.buildInfo());
        send(params, user);
        trackParams(params);
    }


    private void find(QAntUser user, IQAntObject params) {

        try {
            ArenaPower attacker = arenaManager.getArenaPower(user.getName());
            Player playerRequest = playerManager.getPlayer(user.getName());
            List<ArenaPower> opponents = arenaManager.findOpponent(attacker, playerRequest.getZoneName());

            IQAntArray opponentArr = QAntArray.newInstance();

            opponents.forEach(arenaPower -> {

                IQAntObject buildArenaInfo = arenaPower.buildInfo();
                buildArenaInfo.putInt("winPoint",
                        CalculateUtil.calcTrophyAttackerWin(attacker.getArenaPoint(), arenaPower.getArenaPoint(),
                                attacker.getAtkTeam().getTeamPower(), arenaPower.getAtkTeam().getTeamPower()));
                buildArenaInfo.putInt("losePoint",
                        CalculateUtil.calcTrophyAttackerLose(attacker.getArenaPoint(), arenaPower.getArenaPoint(),
                                attacker.getAtkTeam().getTeamPower(), arenaPower.getAtkTeam().getTeamPower()));
                opponentArr.addQAntObject(buildArenaInfo);
            });
            HeroItem heroItem = heroItemManger.useItem(user, BLESS_INDEX, FIND_FEE);
            heroItemManger.notifyAssetChange(user, Collections.singletonList(heroItem));
            params.putQAntArray("opponents", opponentArr);
            send(params, user);
            trackParams(params);
        } catch (UseItemException e) {
            responseError(user, GameErrorCode.NOT_ENOUGH_CURRENCY_ITEM);
        }

    }


    private void join(QAntUser user, IQAntObject params) {
        ArenaPower arenaPower = arenaManager.join(user.getName());
        if (arenaPower == null) {
            params.putQAntObject("arena", QAntObject.newFromObject(new ArenaPower()));
            send(params, user);
            return;
        }
        params.putQAntObject("arena", arenaPower.buildInfo());
        params.putLong("rankingSeconds", arenaManager.getNextRankingSeconds());
        send(params, user);
        trackParams(params);
    }


    public static void main(String[] args) {
        Stream.iterate(1, n -> n + 1).limit(10).forEach(System.out::println);
    }

    @Override
    protected String getHandlerCmd() {
        return CMD_ARENA;
    }
}
