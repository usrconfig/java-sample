package com.seagame.ext.entities.hero.skill;

import lombok.Getter;

@Getter
public class SkillLevel {
    private int level;
    private int DMGRate;

    public SkillLevel(int level, int DMGRate) {
        this.level = level;
        this.DMGRate = DMGRate;
    }
}