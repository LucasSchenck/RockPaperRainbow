package com.example.rockpaperrainbow;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "RockPaperRainbow";
    public static final String CURRENT_MODE = "com.example.rockpaperrainbow.MODE";
    public static final String PLAYER_ID = "PLAYER_ID";
    public static final String PLAYER_NICK = "PLAYER_NICK";
    public static final String OPPONENT_ID = "OPPONENT_ID";
    public static final String OPPONENT_NICK = "OPPONENT_NICK";
    public static final String IS_PLAYER_ONE = "IS_PLAYER_ONE";
    public static final String GAME_ID = "GAME_ID";
    public static final int PLAYER_ID_TAG = R.id.button;
    public static final int PLAYER_NICK_TAG = R.id.button2;
    public static final int GAME_ID_TAG = R.id.profile_button;

    GoogleSignInClient mGoogleSignInClient;
    GoogleSignInAccount account;
    String idToken;
    User currentUser;
    List<String> users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        users = new ArrayList<>();
        fetchUsers();

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getResources().getString(R.string.web_client_id))
                .build();
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        account = GoogleSignIn.getLastSignedInAccount(this);
        if(account != null) idToken = account.getIdToken();

        findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, 101);
            }
        });
        findViewById(R.id.profile_button).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(account != null){
                    Intent intent = new Intent(v.getContext(), ProfileActivity.class);
                    intent.putExtra(ProfileActivity.GOOGLE_ACCOUNT, account);
                    startActivity(intent);
                }
                else{
                    Toast.makeText(v.getContext(), "Sign-in required", Toast.LENGTH_SHORT).show();
                }
            }
        });

        if(account != null) onLoggedIn(account);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK)
            switch (requestCode) {
                case 101:
                    try {
                        // The Task returned from this call is always completed, no need to attach
                        // a listener.
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                        account = task.getResult(ApiException.class);
                        idToken = account.getIdToken();
                        if(isNewUser(idToken)){
                            currentUser = new User(idToken, account.getDisplayName(), 1400);
                            FirebaseDatabase.getInstance().getReference().child("users").push()
                                    .setValue(currentUser);
                            Log.d(TAG, "Created new user.");
                        }
                        Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();
                        onLoggedIn(account);
                    } catch (ApiException e) {
                        // The ApiException status code indicates the detailed failure reason.
                        Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
                    }
                    break;
            }
    }

    private void onLoggedIn(GoogleSignInAccount googleSignInAccount) {
        // Remove sign-in button
        View signInButton = findViewById(R.id.sign_in_button);
        ViewGroup parent = (ViewGroup) signInButton.getParent();
        parent.removeView(signInButton);
    }

    public void startNormalMatch(View view){

        if (account != null) {
            Intent intent = new Intent(this, InviteActivity.class);
            intent.putExtra(CURRENT_MODE, "normal");
            intent.putExtra(PLAYER_ID, idToken);
            intent.putExtra(PLAYER_NICK, account.getDisplayName());
            startActivity(intent);
        }
        else{
            Toast.makeText(this, "Sign-in required", Toast.LENGTH_SHORT).show();
        }
    }

    public void startRankedMatch(View view){
        if (account != null) {
            Intent intent = new Intent(this, InviteActivity.class);
            intent.putExtra(CURRENT_MODE, "ranked");
            intent.putExtra(PLAYER_ID, idToken);
            intent.putExtra(PLAYER_NICK, account.getDisplayName());
            startActivity(intent);
        }
        else {
            Toast.makeText(this, "Sign-in required", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isNewUser(String idToken){
        Log.d(TAG, "Checking for idToken in users string...");
        for (String id : users){
            if(id.equals(idToken)) {
                Log.d(TAG, "Found idToken in users.");
                return false;
            }
        }
        Log.d(TAG, "Didn't find idToken in users.");
        return true;
    }

    private void fetchUsers(){

        FirebaseDatabase.getInstance().getReference().child("/users")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot data : dataSnapshot.getChildren()){
                            Map<String, String> map = (HashMap) data.getValue();

                            if(!users.contains(map.get("id"))) users.add(map.get("id"));
                            Log.d(TAG, "User added: " + data.getKey());
                            Log.d(TAG, "User list: " + users.toString());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.d(TAG, "Read failed: " + databaseError.getCode());
                    }
                });
    }
}
