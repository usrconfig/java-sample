package com.seagame.ext.entities.mail;

import com.creants.creants_2x.socket.gate.entities.IQAntObject;
import com.creants.creants_2x.socket.gate.entities.QAntObject;
import com.creants.creants_2x.socket.gate.protocol.serialization.SerializableQAntType;
import com.seagame.ext.Utils;
import com.seagame.ext.config.game.MailConfig;
import com.seagame.ext.util.NetworkConstant;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.time.DateFormatUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;
import java.util.Map;

/**
 * @author LamHM
 */
@Getter
@Setter
@Document(collection = "mail")
public class Mail implements SerializableQAntType, NetworkConstant {
    public static final int EXPIRE_SECS = 604800;
    public @Id
    long id;
    private @Indexed
    long heroId;
    private String title;
    private boolean isSys;
    private boolean seen;
    private boolean claimed;
    private long sender;
    private String content;
    private String shortContent;
    private String giftString;
    private String baseId;
    @Indexed(expireAfterSeconds = EXPIRE_SECS)//7 ngày
    private long createTime;
    private String[] properties;
    private Map<String, Object> attributes;

    @Transient
    private MailBase mailBase;

    public Mail() {
        this.id = System.currentTimeMillis();
        this.createTime = System.currentTimeMillis();
    }


    public Mail(long receiver, long sender) {
        this.id = System.currentTimeMillis();
        this.heroId = receiver;
        this.sender = sender;
        this.createTime = System.currentTimeMillis();
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void addAttribute(String key, Object value) {
        if (attributes == null)
            attributes = new HashMap<>();

        attributes.put(key, value);
    }


    public Mail(long receiver, MailBase mailBase) {
        this.id = System.currentTimeMillis();
        this.heroId = receiver;
        this.createTime = System.currentTimeMillis();
        this.baseId = mailBase.getId();
    }

    public void initMailBase() {
        if (this.baseId != null) {
            this.mailBase = MailConfig.getInstance().getMailById(this.baseId);
        }
    }


    public IQAntObject buildShortInfo() {
        IQAntObject mailObj = QAntObject.newInstance();
        mailObj.putLong("id", id);
        String shortContent = getShortContent();
        if (shortContent != null)
            mailObj.putUtfString("shortContent", shortContent);
        mailObj.putBool("seen", this.seen);
        mailObj.putBool("isSys", this.isSys);
        mailObj.putBool("claimed", this.claimed);
        String title = getTitle();
        if (title != null)
            mailObj.putUtfString("title", title);
        String giftString = getGiftString();
        if (giftString != null)
            mailObj.putBool("gift", !Utils.isNullOrEmpty(giftString));

        mailObj.putUtfString("expiredDate", getExpiredDate());
        return mailObj;
    }

    public void hadSeen() {
        seen = true;
    }

    public IQAntObject buildFullInfo() {
        IQAntObject mailObj = QAntObject.newInstance();
        mailObj.putLong("id", id);
        String shortContent = getShortContent();
        if (shortContent != null)
            mailObj.putUtfString("shortContent", shortContent);

        mailObj.putBool("seen", this.seen);
        mailObj.putBool("isSys", this.isSys);
        mailObj.putBool("claimed", this.claimed);
        String content = getContent();
        if (content != null)
            mailObj.putUtfString("content", content);
        String title = getTitle();
        if (title != null)
            mailObj.putUtfString("title", title);
        String giftString = getGiftString();
        if (giftString != null)
            mailObj.putUtfString("giftString", giftString);
        if (this.baseId != null)
            mailObj.putUtfString("baseId", baseId);

        mailObj.putUtfString("expiredDate", getExpiredDate());
        return mailObj;
    }

    public Mail setHeroId(long receiverId) {
        this.heroId = receiverId;
        return this;
    }

    public Mail setShortContent(String shortContent) {
        this.shortContent = shortContent;
        return this;
    }

    public String getTitle() {
        if (this.mailBase != null) {
            return this.mailBase.getTitle();
        }
        return title;
    }

    public String getContent() {
        if (this.mailBase != null) {
            return this.mailBase.getContent();
        }
        return content;
    }

    public String getGiftString() {
        if (this.baseId != null) {
            return MailConfig.getInstance().getMailById(baseId).getGiftString();
        } else {
            return giftString;
        }
    }

    public String getExpiredDate() {
        return DateFormatUtils.format(createTime + EXPIRE_SECS * 1000, "dd/MM/yyyy HH:mm");
    }


    public String getShortContent() {
        if (this.mailBase != null) {
            return this.mailBase.getShortContent();
        }
        return shortContent;
    }


    public boolean canDoActionClaim() {
        return !this.claimed && getGiftString() != null;
    }

    public boolean canDoRemove() {
        return getGiftString() == null || this.claimed;
    }

    public void reNew() {
        this.id = System.currentTimeMillis();
    }

    public void setClaimed() {
        this.claimed = true;
    }
}
