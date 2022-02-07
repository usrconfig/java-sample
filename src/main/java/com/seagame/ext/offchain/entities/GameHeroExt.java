package com.seagame.ext.offchain.entities;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;

/**
 * @author LamHM
 */
@Getter
@Setter
public class GameHeroExt {
    @Id
    private String id;
    private long userId;
}
