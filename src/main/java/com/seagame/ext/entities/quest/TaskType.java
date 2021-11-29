package com.seagame.ext.entities.quest;

/**
 * @author LamHM
 */
public enum TaskType {
    TALK(0),
    VISIT_FIND(1),
    KILL(2),
    COLLECT(3);

    int id;


    TaskType(int id) {
        this.id = id;
    }


    public int getId() {
        return id;
    }

}
