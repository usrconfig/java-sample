package com.seagame.ext.controllers;

import com.creants.creants_2x.core.annotations.Instantiation;
import com.creants.creants_2x.core.util.QAntTracer;
import com.creants.creants_2x.socket.gate.entities.IQAntArray;
import com.creants.creants_2x.socket.gate.entities.IQAntObject;
import com.creants.creants_2x.socket.gate.entities.QAntArray;
import com.creants.creants_2x.socket.gate.wood.QAntUser;
import com.seagame.ext.ExtApplication;
import com.seagame.ext.config.game.ItemConfig;
import com.seagame.ext.dao.MailRepository;
import com.seagame.ext.entities.Player;
import com.seagame.ext.entities.item.HeroItem;
import com.seagame.ext.entities.mail.Mail;
import com.seagame.ext.entities.mail.MailManager;
import com.seagame.ext.exception.GameErrorCode;
import com.seagame.ext.managers.HeroItemManager;
import com.seagame.ext.managers.PlayerManager;
import com.seagame.ext.services.NotifySystem;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author LamHM
 */
@Instantiation(Instantiation.InstantiationMode.SINGLE_INSTANCE)
public class MailRequestHandler extends ZClientRequestHandler {
    private static final int OPEN_MAIL = 1;
    private static final int MAIL_ACTION = 2;
    private static final int CLAIM_ALL_REWARDS = 3;
    private static final int REMOVE_ALL_INFO_MAIL = 4;
    private static final int GET_SHORT_LIST = 5;

    private static final int ACTION_ACCEPT = 1;
    private static final int ACTION_DENIE = 2;

    private MailRepository mailRepository;
    private MailManager mailManager;
    private PlayerManager playerManager;
    private HeroItemManager heroItemManager;
    private NotifySystem notifySystem;


    public MailRequestHandler() {
        mailRepository = ExtApplication.getBean(MailRepository.class);
        playerManager = ExtApplication.getBean(PlayerManager.class);
        mailManager = ExtApplication.getBean(MailManager.class);
        heroItemManager = ExtApplication.getBean(HeroItemManager.class);
        notifySystem = ExtApplication.getBean(NotifySystem.class);
    }


    @Override
    public void handleClientRequest(QAntUser user, IQAntObject params) {
        Integer action = this.getAction(params);
        if (action == null)
            action = GET_SHORT_LIST;

        switch (action) {
            case GET_SHORT_LIST:
                getShortListInfo(user, params);
                break;
            case OPEN_MAIL:
                processOpenMail(user, params);
                break;
            case MAIL_ACTION:
                mailAction(user, params);
                break;
            case REMOVE_ALL_INFO_MAIL:
                removeAllInfoMail(user, params);
                break;
            case CLAIM_ALL_REWARDS:
                claimAllRewards(user, params);
                break;
            default:
                break;
        }

    }


    private void processOpenMail(QAntUser user, IQAntObject params) {
        Long id = params.getLong("id");
        if (id == null) {
            responseError(user, GameErrorCode.LACK_OF_INFOMATION);
            return;
        }
        Mail mail = mailRepository.getMail(id);
        if (mail == null) {
            responseError(user, GameErrorCode.UNKNOW_EXCEPTION);
            return;
        }
        mail.hadSeen();
        mail.initMailBase();
        mailRepository.save(mail);
        send(mail.buildFullInfo(), user);
    }

    private void claimAllRewards(QAntUser user, IQAntObject params) {
        try {
            String gameHeroId = user.getName();
            Player player = playerManager.getPlayer(gameHeroId);
            List<Mail> mails = mailRepository.getAllByHeroId(player.getActiveHeroId());
            if (mails.size() > 0) {
                List<Mail> mailStream = mails.stream().filter(Mail::canDoActionClaim).collect(Collectors.toList());
                params.putLongArray("ids", mailStream.stream().map(Mail::getId).collect(Collectors.toList()));
                String giftString = mailStream.stream().map(Mail::getGiftString).filter(Objects::nonNull)
                        .collect(Collectors.joining("#"));

                List<HeroItem> heroItems = heroItemManager.addItems(user, giftString);
                QAntTracer.debug(PlayerManager.class, "applyRewards : " + heroItems.toString());
                heroItemManager.notifyAssetChange(user, heroItems);
                ItemConfig.getInstance().buildUpdateRewardsReceipt(params, heroItems);
                ItemConfig.getInstance().buildRewardsReceipt(params, giftString);
                mailRepository.deleteAll(mailStream);
            }
            send(params, user);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private void removeAllInfoMail(QAntUser user, IQAntObject params) {
        try {
            String gameHeroId = user.getName();
            Player player = playerManager.getPlayer(gameHeroId);
            List<Mail> mails = mailRepository.getAllByHeroId(player.getActiveHeroId());
            if (mails.size() > 0) {
                List<Mail> mailStream = mails.stream().filter(Mail::canDoRemove).collect(Collectors.toList());
                params.putLongArray("ids", mailStream.stream().map(Mail::getId).collect(Collectors.toList()));
                mailRepository.deleteAll(mailStream);
            }
            send(params, user);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void mailAction(QAntUser user, IQAntObject params) throws NumberFormatException {
        long id = params.getLong("id");
        Mail mail = mailRepository.getMail(id);
        if (mail == null || mail.isClaimed()) {
            responseError(user, GameErrorCode.MAIL_RECEIVED);
            return;
        }
        if (params.containsKey("action")) {
            int action = params.getInt("action");
            switch (action) {
                case ACTION_ACCEPT:
                    acceptMail(mail, user, params);
                    break;
                case ACTION_DENIE:
                    denieMail(mail, user, params);
                    break;
                default:
                    break;
            }
        } else {
            claimReward(mail, user, params);
        }
        mailRepository.delete(mail);
        send(params, user);
    }

    private void denieMail(Mail mail, QAntUser user, IQAntObject params) {
    }

    private void acceptMail(Mail mail, QAntUser user, IQAntObject params) {

    }


    private void claimReward(Mail mail, QAntUser user, IQAntObject params) {
        if (mail != null && !mail.isClaimed()) {
            String giftString = mail.getGiftString();
            mail.setClaimed();
            if (giftString != null) {
                List<HeroItem> heroItems = heroItemManager.addItems(user, giftString);
                QAntTracer.debug(PlayerManager.class, "applyRewards : " + heroItems.toString());
                heroItemManager.notifyAssetChange(user, heroItems);
                ItemConfig.getInstance().buildUpdateRewardsReceipt(params, heroItems);
                ItemConfig.getInstance().buildRewardsReceipt(params, giftString);
            }
        }
    }


    private void getShortListInfo(QAntUser user, IQAntObject params) {
        int page = params.getInt("page");
        Player player = playerManager.getPlayer(user.getName());
        List<Mail> mails = mailRepository.getMailList(player.getActiveHeroId(),
                PageRequest.of(page, 20, Sort.by(Sort.Direction.DESC, "_id")));

        int countMail = mailRepository.countMailByHeroId(player.getActiveHeroId());
        int maxPage = countMail <= 0 ? 1 : countMail / 20;

        IQAntArray mailArr = QAntArray.newInstance();
        mails.forEach(mail -> {
                    mail.initMailBase();
                    mailArr.addQAntObject(mail.buildShortInfo());
                }
        );
        params.putQAntArray("mails", mailArr);
        params.putInt("maxPage", maxPage);
        send(params, user);
    }


    public static void main(String[] args) {
    }

    @Override
    protected String getHandlerCmd() {
        return CMD_MAIL;
    }
}
