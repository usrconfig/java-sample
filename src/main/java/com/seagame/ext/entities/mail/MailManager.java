package com.seagame.ext.entities.mail;

import com.seagame.ext.Utils;
import com.seagame.ext.config.game.MailConfig;
import com.seagame.ext.dao.MailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MailManager {
    @Autowired
    private MailRepository mailRepository;

    public Mail sendSystemMail(String toGameHeroId, String mailId) {
        MailBase mailBase = MailConfig.getInstance().getMailById(mailId);
        Mail mail = new Mail(toGameHeroId, mailBase);
        mail.setSys(true);
        return mailRepository.save(mail);
    }

    public Mail sendPrivateMail(String fromGameHeroId, String toGameHeroId, String title, String content, String gift) {
        Mail mail = new Mail(toGameHeroId, fromGameHeroId);
        mail.setSys(false);
        mail.setTitle(title);
        mail.setContent(content);
        if (Utils.isNullOrEmpty(gift)) {
            mail.setGiftString(gift);
        }
        return mailRepository.save(mail);
    }


}
