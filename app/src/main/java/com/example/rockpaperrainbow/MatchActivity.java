package com.example.rockpaperrainbow;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import static com.example.rockpaperrainbow.MainActivity.GAME_ID;
import static com.example.rockpaperrainbow.MainActivity.IS_PLAYER_ONE;
import static com.example.rockpaperrainbow.MainActivity.OPPONENT_ID;
import static com.example.rockpaperrainbow.MainActivity.OPPONENT_NICK;
import static com.example.rockpaperrainbow.MainActivity.PLAYER_ID;
import static com.example.rockpaperrainbow.MainActivity.PLAYER_NICK;

public class MatchActivity extends AppCompatActivity {
    private static final String TAG = "MatchActivity";
    public static final String GAMES_PATH = "/games";
    private static final String ROCK_IMAGE_PATH = "https://cdn.pixabay.com/photo/2014/12/22/00/03/rock-576669_960_720.png";
    private static final String PAPER_IMAGE_PATH = "https://encrypted-tbn0.gstatic.com/images?q=tbn%3AANd9GcRJx66zDEew-5rLFjuxxseVWv_5T14Gj8t-bgXkMRm3LIXrSA12";
    private static final String SCISSORS_IMAGE_PATH = "https://encrypted-tbn0.gstatic.com/images?q=tbn%3AANd9GcSxwxYCriqrI-BO83sQlGNRtNbIKvjFr_l0XouYQyx3UY6U2Zsn";
    private static final String VICTORY_IMAGE_PATH = "https://cdn3.f-cdn.com/contestentries/579824/19431054/578055a922fae_thumb900.jpg";
    private static final String DEFEAT_IMAGE_PATH = "https://i1.sndcdn.com/artworks-000228243240-0s3khk-t500x500.jpg";
    private String gameID;
    private String player1ID;
    private String player2ID;
    private String player1Nick;
    private String player2Nick;
    private String currentMove1;
    private String currentMove2;
    private String result;
    private Long score1;
    private Long score2;
    private Boolean isPlayer1;
    private DatabaseReference gameRef;
    private ImageView myMoveImage;
    private ImageView opponentMoveImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match2);

        Intent prev = getIntent();
        isPlayer1 = prev.getBooleanExtra(IS_PLAYER_ONE, false);

        if (isPlayer1) {
            player1ID = prev.getStringExtra(PLAYER_ID);
            player2ID = prev.getStringExtra(OPPONENT_ID);
            player1Nick = prev.getStringExtra(PLAYER_NICK);
            player2Nick = prev.getStringExtra(OPPONENT_NICK);
        } else {
            player1ID = prev.getStringExtra(OPPONENT_ID);
            player2ID = prev.getStringExtra(PLAYER_ID);
            player1Nick = prev.getStringExtra(OPPONENT_NICK);
            player2Nick = prev.getStringExtra(PLAYER_NICK);
        }
        gameID = prev.getStringExtra(GAME_ID);
        myMoveImage = findViewById(R.id.match_myMoveImage);
        opponentMoveImage = findViewById(R.id.match_opponentMoveImage);

        gameRef = FirebaseDatabase.getInstance().getReference().child("/game_rooms").child(gameID);

        TextView myScore = findViewById(R.id.match_myScore);
        TextView opponentScore = findViewById(R.id.match_opponentScore);
        myScore.setText("0");
        opponentScore.setText("0");

        setListeners();

        startMatch();
    }

    public void startMatch(){
        startRound();
    }

    public void startRound(){
        TextView countdown = findViewById(R.id.match_countdown);
        countdown.setText("Ready!");
        myMoveImage.setVisibility(View.VISIBLE);

        new CountDownTimer(6000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if(millisUntilFinished <= 3000) countdown.setText(String.valueOf(millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                calcResult();
                startRound();
            }
        }.start();
    }

    public void calcResult(){
        if(currentMove2 == null) {}
        else if(currentMove2.equals("rock")) Picasso.get()
                .load(ROCK_IMAGE_PATH).into(opponentMoveImage);
        else if(currentMove2.equals("paper")) Picasso.get()
                .load(PAPER_IMAGE_PATH).into(opponentMoveImage);
        else if(currentMove2.equals("scissors")) Picasso.get().load(SCISSORS_IMAGE_PATH).into(opponentMoveImage);

        if (currentMove1 != null && currentMove2 != null) {
            if(currentMove1.equals(currentMove2)){
                result = "draw";
            }
            else{
                if(currentMove1.equals("rock")){
                    if (currentMove2.equals("paper")) result = "win2";
                    else result = "win1";
                }
                else if(currentMove1.equals("paper")){
                    if(currentMove2.equals("scissors")) result = "win2";
                    else result = "win1";
                }
                else if(currentMove1.equals("scissors")){
                    if(currentMove2.equals("rock")) result = "win2";
                    else result = "win1";
                }
            }
        } else {
            if(currentMove1 != null && currentMove2 == null) result = "win1";
            else if(currentMove1 == null && currentMove2 != null) result = "win2";
            else result = "draw";
        }

        if(!result.equals("draw")) updateScore();

        myMoveImage.setVisibility(View.GONE);
    }

    private void drawVictory(){
        Picasso.get().load(VICTORY_IMAGE_PATH).into(new ImageView(MatchActivity.this));
    }

    private void drawDefeat(){
        Picasso.get().load(DEFEAT_IMAGE_PATH).into(new ImageView(MatchActivity.this));
    }

    private void endMatch(){
        if(isPlayer1){
            if(score1 >= 3) drawVictory();
            else drawDefeat();
        }
        else{
            if(score2 >= 3) drawVictory();
            else drawDefeat();
        }

        if(isPlayer1) gameRef.setValue(null);

        Context t = this;
        new CountDownTimer(5000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) { }

            @Override
            public void onFinish() {
                startActivity(new Intent(t, MainActivity.class));
            }
        }.start();
    }

    public void setCurrentMoveRock(View view){
        if(isPlayer1) {
            currentMove1 = "rock";
            gameRef.child("currentMove1").setValue(currentMove1);
        }
        else {
            currentMove2 = "rock";
            gameRef.child("currentMove2").setValue(currentMove2);
        }
        Picasso.get().load(ROCK_IMAGE_PATH).into(myMoveImage);
    }

    public void setCurrentMovePaper(View view){
        if(isPlayer1) {
            currentMove1 = "paper";
            gameRef.child("currentMove1").setValue(currentMove1);
        }
        else {
            currentMove2 = "paper";
            gameRef.child("currentMove2").setValue(currentMove2);
        }
        Picasso.get().load(PAPER_IMAGE_PATH).into(myMoveImage);
    }

    public void setCurrentMoveScissors(View view){
        if(isPlayer1) {
            currentMove1 = "scissors";
            gameRef.child("currentMove1").setValue(currentMove1);
        }
        else {
            currentMove2 = "scissors";
            gameRef.child("currentMove2").setValue(currentMove2);
        }
        Picasso.get().load(SCISSORS_IMAGE_PATH).into(myMoveImage);
    }

    private void updateScore(){
        if(result.equals("win1")){
            score1++;
            if(isPlayer1) gameRef.child("score1").setValue(score1);
        }
        else{
            score2++;
            if(!isPlayer1) gameRef.child("score2").setValue(score2);
        }
        TextView myScore = findViewById(R.id.match_myScore);
        TextView opponentScore = findViewById(R.id.match_opponentScore);
        if(isPlayer1) {
            myScore.setText(String.valueOf(score1));
            opponentScore.setText(String.valueOf(score2));
        }
        else{
            myScore.setText(String.valueOf(score2));
            opponentScore.setText(String.valueOf(score1));
        }

        if(score1 >= 3 || score2 >= 3) endMatch();
    }

    private void setListeners(){
        gameRef.child("currentMove1").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                currentMove1 = (String) dataSnapshot.getValue();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        gameRef.child("currentMove2").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                currentMove2 = (String) dataSnapshot.getValue();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        gameRef.child("score1").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                score1 = (Long) dataSnapshot.getValue();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        gameRef.child("score2").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                score2 = (Long) dataSnapshot.getValue();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
