package com.example.rockpaperrainbow;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import static com.example.rockpaperrainbow.MainActivity.CURRENT_MODE;
import static com.example.rockpaperrainbow.MainActivity.GAME_ID;
import static com.example.rockpaperrainbow.MainActivity.GAME_ID_TAG;
import static com.example.rockpaperrainbow.MainActivity.IS_PLAYER_ONE;
import static com.example.rockpaperrainbow.MainActivity.OPPONENT_ID;
import static com.example.rockpaperrainbow.MainActivity.OPPONENT_NICK;
import static com.example.rockpaperrainbow.MainActivity.PLAYER_ID_TAG;
import static com.example.rockpaperrainbow.MainActivity.PLAYER_NICK;
import static com.example.rockpaperrainbow.MainActivity.PLAYER_ID;
import static com.example.rockpaperrainbow.MainActivity.PLAYER_NICK_TAG;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>{
    private static final String TAG = "RecyclerViewAdapter";

    private List<Room> mRoomList;
    private Context mContext;
    private Intent prev;

    public RecyclerViewAdapter(Context context, List<Room> rooms, Intent intent){
        mRoomList = rooms;
        mContext = context;
        this.prev = intent;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.invite_row, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: called.");
        Log.d(TAG, "Rooms: " + mRoomList);
            holder.nameTextView.setText(mRoomList.get(position).getPlayer1Nick());
            holder.acceptButton.setTag(PLAYER_ID_TAG, mRoomList.get(position).getPlayer1ID());
            holder.acceptButton.setTag(PLAYER_NICK_TAG, mRoomList.get(position).getPlayer1Nick());
            holder.acceptButton.setTag(GAME_ID_TAG, mRoomList.get(position).getId());
            holder.acceptButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view){
                    Log.d(TAG, "clicked on: " + mRoomList.get(position));
                    acceptInvite(view);
                }
            });
    }

    @Override
    public int getItemCount() {
        return mRoomList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView nameTextView;
        Button acceptButton;
        ConstraintLayout parentLayout;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.invite_name);
            acceptButton = itemView.findViewById(R.id.button_accept);
            parentLayout = itemView.findViewById(R.id.parent_layout);
        }
    }

    private void acceptInvite(View view){
        String id = (String) view.getTag(GAME_ID_TAG);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("/game_rooms")
                .child(id);
        ref.child("player2ID").setValue(prev.getStringExtra(PLAYER_ID));
        ref.child("player2Nick").setValue(prev.getStringExtra(PLAYER_NICK));
        ref.child("open").setValue(false);

        Intent intent = new Intent(view.getContext(), MatchActivity.class);
        intent.putExtra(CURRENT_MODE, prev.getStringExtra(CURRENT_MODE));
        intent.putExtra(PLAYER_ID, prev.getStringExtra(PLAYER_ID));
        intent.putExtra(PLAYER_NICK, prev.getStringExtra(PLAYER_NICK));
        intent.putExtra(OPPONENT_ID, (String) view.getTag(PLAYER_ID_TAG));
        intent.putExtra(OPPONENT_NICK, (String) view.getTag(PLAYER_NICK_TAG));
        intent.putExtra(IS_PLAYER_ONE, false);
        intent.putExtra(GAME_ID, id);

        Toast.makeText(view.getContext(), "Match accepted", Toast.LENGTH_SHORT).show();
        view.getContext().startActivity(intent);

    }
}
