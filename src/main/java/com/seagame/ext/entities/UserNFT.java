package com.seagame.ext.entities;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

/**
 * @author LamHa
 */
@Getter
@Setter
public class UserNFT {
    @Id
    private long id;
    @Indexed
    private String username;
    private String password;
    private String fullName;
    private String avatar;
    private int gender;
    private String location;
    private String birthday;
    private String email;
    private String provider;
    private Date createTime;
    private String deviceId;
    @Indexed
    private String clientId;


    public UserNFT() {
        location = "vn";
    }


    public UserNFT(String deviceId) {
        super();
        id = -1;
        this.deviceId = deviceId;
    }
}
