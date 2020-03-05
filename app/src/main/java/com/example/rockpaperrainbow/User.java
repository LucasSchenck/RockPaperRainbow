package com.example.rockpaperrainbow;

import java.io.Serializable;

public class User implements Serializable {
    private String id;
    private int elo;
    private String nickname;

    public User(){}

    public User(String id, String nickname, int elo){
        this.id = id;
        this.nickname = nickname;
        this.elo = elo;
    }

    public String getId() {
        return id;
    }

    public int getElo() {
        return elo;
    }

    public String getNickname() {
        return nickname;
    }
}
