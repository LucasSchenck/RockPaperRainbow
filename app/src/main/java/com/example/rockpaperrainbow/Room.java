package com.example.rockpaperrainbow;

public class Room {
    private String id;
    private String player1ID;
    private String player1Nick;
    private String player2ID;
    private String player2Nick;
    private int score1;
    private int score2;
    private boolean open;
    private String currentMove1;
    private String currentMove2;

    public Room(){}

    public Room(String id, String player1, String player1Nick, String player2, String player2Nick){
        this.id = id;
        this.player1ID = player1;
        this.player2ID = player2;
        this.player1Nick = player1Nick;
        this.player2Nick = player2Nick;
        this.open = true;
        score1 = 0;
        score2 = 0;
    }

    public int getScore1(){
        return score1;
    }

    public int getScore2(){
        return score2;
    }

    public String getPlayer1ID(){
        return player1ID;
    }

    public String getPlayer2ID() {
        return player2ID;
    }

    public String getPlayer1Nick() {
        return player1Nick;
    }

    public String getPlayer2Nick() {
        return player2Nick;
    }

    public String getCurrentMove1() {
        return currentMove1;
    }

    public String getCurrentMove2() {
        return currentMove2;
    }

    public Boolean getOpen(){ return open; }

    public String getId(){ return id; }
}
