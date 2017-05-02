package com.example.user.trainingbuddy;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import java.util.ArrayList;

public class ProfileGalleryActivity extends AppCompatActivity {

    private ArrayList<UserProfile> userProfileList = new ArrayList<UserProfile>();
    private ImageAdapter userProfileAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_gallery);

        TextView txv_welcome = (TextView) findViewById(R.id.txtProfile);
        txv_welcome.setText("Gallery");

        //Create adapter
        userProfileAdapter =
                new ImageAdapter(this);
/*
        //Connect adapter to widget
        GridView listprofile = (GridView) findViewById(R.id.list_profile);
        listprofile.setAdapter(userProfileAdapter);
        listprofile.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent launchIntent = new Intent(ProfileGalleryActivity.this, VideoGalleryActivity.class);
                        startActivity(launchIntent);
                        return;
                    }
                }
        );*/

        GridView gridview = (GridView) findViewById(R.id.list_profile);
        gridview.setAdapter(userProfileAdapter);

        gridview.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent launchIntent = new Intent(ProfileGalleryActivity.this, VideoGalleryActivity.class);
                        launchIntent.putExtra("NAME", userProfileList.get(position).getUsername());
                        startActivity(launchIntent);
                        return;
                    }
                }
        );

        //Fill in user profile list
        TrainingBuddyApp app = (TrainingBuddyApp) getApplication();
            app.loadUserProfileList();

        userProfileList = app.getmUserProfileList();
        userProfileAdapter.setData(userProfileList);
        userProfileAdapter.notifyDataSetChanged();

        Log.e("TrainingBuddyApp", "" + userProfileAdapter.getCount());
        return;


    }
}
