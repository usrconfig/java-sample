package com.seagame.ext.entities.campaign;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/**
 * @author LamHM
 * http://www.baeldung.com/cascading-with-dbref-and-lifecycle-events-in-spring-data-mongodb
 * https://stackoverflow.com/questions/44259583/spring-mongo-db-dbref
 *
 * <br>
 * Lưu lại các chương đã chơi và mode cao nhất<br>
 * ví dụ:
 * <li>Chương 0 mode là hard</li>
 * <li>Chương 1 mode là easy</li>
 */
@Getter
@Setter
@Document(collection = "hero-chapter")
public class HeroChapter {
    private @Id
    String id;
    private @Indexed
    String playerId;
    private String chapterIndex;
    private String mode;
    private @Transient
    List<HeroStage> stages;


    public HeroChapter(String playerId, String chapterIndex, String mode) {
        this.id = genId(playerId, chapterIndex);
        this.playerId = playerId;
        this.chapterIndex = chapterIndex;
        this.mode = mode;
    }

    public static String genId(String gameHeroId, String chapterIndex) {
        return gameHeroId + "_" + chapterIndex;
    }

}
