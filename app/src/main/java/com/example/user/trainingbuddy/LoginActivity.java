package com.example.user.trainingbuddy;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


public class LoginActivity extends Activity  {
    Button b1,b2,b3;
    EditText ed1,ed2;

    TextView tx1;
    int counter = 3;
    ArrayList<UserProfile> users = new ArrayList<UserProfile>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        b1 = (Button)findViewById(R.id.button);
        ed1 = (EditText)findViewById(R.id.editText);
        ed2 = (EditText)findViewById(R.id.editText2);

        b2 = (Button)findViewById(R.id.button2);
        tx1 = (TextView)findViewById(R.id.textView3);
        tx1.setVisibility(View.GONE);

        b3 = (Button) findViewById(R.id.btn_register);
        TrainingBuddyApp app = (TrainingBuddyApp) getApplication();
        app.loadUserProfileList();
        users = app.getmUserProfileList();

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkUserExists(ed1.getText().toString())) {
                    Toast.makeText(getApplicationContext(),
                            "Redirecting...",Toast.LENGTH_SHORT).show();
                    Intent launchIntent = new Intent(LoginActivity.this, ProfileGalleryActivity.class);
                    startActivity(launchIntent);
                }else{
                    Toast.makeText(getApplicationContext(), "Wrong Credentials",Toast.LENGTH_SHORT).show();

                    tx1.setVisibility(View.VISIBLE);
                    tx1.setBackgroundColor(Color.RED);
                    counter--;
                    tx1.setText(Integer.toString(counter));

                    if (counter == 0) {
                        b1.setEnabled(false);
                    }
                }
            }
        });

        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        b3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent launchintent = new Intent(LoginActivity.this, CreateProfileActivity.class);
                startActivity(launchintent);
            }
        });
    }

    public boolean checkUserExists(String username){
        boolean checkFlag = false;
        for(int i = 0; i < users.size(); i++){
            if(username.equals(users.get(i).getUsername()) == true){
                checkFlag = true;
                break;
            }
        }

        return checkFlag;
    }


}