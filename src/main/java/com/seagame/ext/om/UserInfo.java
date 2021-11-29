package com.seagame.ext.om;

import org.springframework.data.annotation.Id;

/**
 * @author LamHM
 */
public class UserInfo {
    @Id
    private int id;
    private String name;


    public int getId() {
        return id;
    }


    public void setId(int id) {
        this.id = id;
    }


    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }

}
