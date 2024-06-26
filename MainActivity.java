package com.example.authent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.Firebase;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 20;
    ImageView gbtn;
    FirebaseAuth auth;
    FirebaseDatabase database;
    GoogleSignInClient gsc;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


          gbtn=findViewById(R.id.googbtn);
          auth=FirebaseAuth.getInstance();
          database=FirebaseDatabase.getInstance();
          GoogleSignInOptions gso=new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).
                  requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build();
          gsc=GoogleSignIn.getClient(this,gso);
          gbtn.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                  googlesignin();
              }
          });
    }

    private void googlesignin() {
        Intent intent=gsc.getSignInIntent();
        startActivityForResult(intent,RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==RC_SIGN_IN){
           Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
           try{
              GoogleSignInAccount acc=task.getResult(ApiException.class);
              firebaseAuth(acc.getIdToken());
           }
           catch(Exception e){
               Intent intent=new Intent(MainActivity.this, Googlesignin.class);
               startActivity(intent);
           }
        }
    }

    private void firebaseAuth(String idToken) {
        AuthCredential credential= GoogleAuthProvider.getCredential(idToken,null);
        auth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    FirebaseUser user=auth.getCurrentUser();
                    HashMap<String,Object>map=new HashMap<>();
                    map.put("id",user.getUid());
                    map.put("name",user.getDisplayName());
                    map.put("profile",user.getPhotoUrl().toString());
                    database.getReference().child("users").child(user.getUid()).setValue(map);
                    Toast.makeText(MainActivity.this,"Login unsuccessful",Toast.LENGTH_SHORT).show();
                    Intent intent=new Intent(MainActivity.this, Googlesignin.class);
                    startActivity(intent);
                }
                else{
                    Toast.makeText(MainActivity.this,"Login unsuccessful",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}