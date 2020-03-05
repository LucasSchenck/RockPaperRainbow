package com.example.rockpaperrainbow;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.example.rockpaperrainbow.MainActivity.CURRENT_MODE;
import static com.example.rockpaperrainbow.MainActivity.GAME_ID;
import static com.example.rockpaperrainbow.MainActivity.GAME_ID_TAG;
import static com.example.rockpaperrainbow.MainActivity.IS_PLAYER_ONE;
import static com.example.rockpaperrainbow.MainActivity.OPPONENT_ID;
import static com.example.rockpaperrainbow.MainActivity.OPPONENT_NICK;
import static com.example.rockpaperrainbow.MainActivity.PLAYER_ID;
import static com.example.rockpaperrainbow.MainActivity.PLAYER_ID_TAG;
import static com.example.rockpaperrainbow.MainActivity.PLAYER_NICK;
import static com.example.rockpaperrainbow.MainActivity.PLAYER_NICK_TAG;

public class InviteActivity extends AppCompatActivity {
    private static final String TAG = "InviteActivity";
    private List<Room> rooms;
    RecyclerView roomList;
    private DatabaseReference gameRef;
    private String gameID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite);
        rooms = new ArrayList<>();

        fetchOpenRooms();
        removeClosedRooms();
        Log.d(TAG, "Room list: " + rooms.toString());
    }

    @Override
    protected void onStart(){
        super.onStart();
        removeClosedRooms();
    }

    private void fetchOpenRooms(){
        initRecycleView();

        FirebaseDatabase.getInstance().getReference().child("/game_rooms")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                            Room room = data.getValue(Room.class);

//                            Room temp = new Room(data.getKey(), room.getPlayer1ID(), room.getPlayer1Nick(),
//                                    room.getPlayer2ID(), room.getPlayer2Nick());
                            if(isNewRoom(room) && room.getOpen()) rooms.add(room);
                            removeClosedRooms();

                            Log.d(TAG, "Room added: " + data.getKey());
                            Log.d(TAG, "Room list: " + rooms.toString());
                            initRecycleView();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.d(TAG, "Read failed: " + databaseError.getCode());
                    }
                });
    }

    private void initRecycleView(){
        Log.d(TAG, "Initializing RecycleView...");
        roomList = findViewById(R.id.invite_list);
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(this, rooms, getIntent());
        roomList.setAdapter(adapter);
        roomList.setLayoutManager(new LinearLayoutManager(this));
        Log.d(TAG, "Finished initializing RecycleView.");
    }

    public void acceptInvite(View view){
        Intent prev = getIntent();

        String id = (String) view.getTag(GAME_ID_TAG);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("/game_rooms")
                .child(id);
        ref.child("player2ID").setValue(prev.getStringExtra(PLAYER_ID));
        ref.child("player2Nick").setValue(prev.getStringExtra(PLAYER_NICK));
        ref.child("open").setValue(false);
        removeClosedRooms();

        Intent intent = new Intent(this, MatchActivity.class);
        intent.putExtra(CURRENT_MODE, prev.getStringExtra(CURRENT_MODE));
        intent.putExtra(PLAYER_ID, prev.getStringExtra(PLAYER_ID));
        intent.putExtra(PLAYER_NICK, prev.getStringExtra(PLAYER_NICK));
        intent.putExtra(OPPONENT_ID, (String) view.getTag(PLAYER_ID_TAG));
        intent.putExtra(OPPONENT_NICK, (String) view.getTag(PLAYER_NICK_TAG));
        intent.putExtra(IS_PLAYER_ONE, false);
        intent.putExtra(GAME_ID, id);

        Toast.makeText(this, "Match accepted", Toast.LENGTH_SHORT).show();
        startActivity(intent);
    }

    public void createRoom(View view){
        Intent prev = getIntent();
        gameRef = FirebaseDatabase.getInstance().getReference().child("/game_rooms").push();
        gameID = gameRef.getKey();

        gameRef.setValue(new Room(gameID, prev.getStringExtra(PLAYER_ID), prev.getStringExtra(PLAYER_NICK),
                        null, null));
        Log.d(TAG, "Created new room with Player 1: " + prev.getStringExtra(PLAYER_NICK));

        gameRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        Room room = dataSnapshot.getValue(Room.class);
                        if(room != null && room.getPlayer2ID() != null && room.getPlayer2Nick() != null && room.getOpen()){
                            Intent intent = new Intent(view.getContext(), MatchActivity.class);
                            intent.putExtra(CURRENT_MODE, prev.getStringExtra(CURRENT_MODE));
                            intent.putExtra(PLAYER_ID, room.getPlayer1ID());
                            intent.putExtra(PLAYER_NICK, room.getPlayer1Nick());
                            intent.putExtra(OPPONENT_ID, room.getPlayer2ID());
                            intent.putExtra(OPPONENT_NICK, room.getPlayer2Nick());
                            intent.putExtra(IS_PLAYER_ONE, true);
                            intent.putExtra(GAME_ID, gameID);

                            removeClosedRooms();
                            Toast.makeText(view.getContext(), "Opponent found", Toast.LENGTH_SHORT).show();
                            startActivity(intent);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.d(TAG, "Read failed: " + databaseError.getCode());
                    }
                });
    }

    private boolean isNewRoom(Room current){
        for (Room room : rooms){
            if(room.getId().equals(current.getId())) return false;
        }
        return true;
    }

    private void removeClosedRooms(){
        Iterator<Room> itr = rooms.iterator();

        Log.d(TAG, "Checking for closed rooms...");
        while (itr.hasNext()){
            Room temp = itr.next();
            if(!temp.getOpen()) {
                Log.d(TAG, "Found closed room (ID: " + temp.getId() + "). Removing...");
                itr.remove();
            }
        }
        Log.d(TAG, "Finished checking for closed rooms.");
    }
}
