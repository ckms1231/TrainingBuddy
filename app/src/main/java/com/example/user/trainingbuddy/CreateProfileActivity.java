package com.example.user.trainingbuddy;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

public class CreateProfileActivity extends AppCompatActivity {
    ImageView imageView2;
    Button button;
    private static final int PICK_IMAGE = 100;
    Uri imageUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_profile);

        imageView2 = (ImageView) findViewById(R.id.imageView2);
        button = (Button) findViewById(R.id.btn_image);
        //ImageView imgView = (ImageView) findViewById(R.id.imageView2);
        button.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openGallery();
                    }
                }
        );

        Button btnSubmit = (Button) findViewById(R.id.btn_submit);
        btnSubmit.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        submitUserData();
                        return;
                    }
                }
        );

    }

    private void openGallery(){
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, PICK_IMAGE);
    }

    private void submitUserData() {
        EditText edtUsername = (EditText) findViewById(R.id.edt_name);
        EditText edtGender = (EditText) findViewById(R.id.edt_gender);
        EditText edtAge = (EditText) findViewById(R.id.edt_age);
        EditText edtDesc = (EditText) findViewById(R.id.edt_desc);
        Drawable d = Drawable.createFromPath("/storage/emulated/0/Download/jake.png");
        TrainingBuddyApp app = (TrainingBuddyApp) getApplication();
        app.loadUserProfileList();
        app.addUserProfile(edtUsername.getText().toString(), edtGender.getText().toString(),
                edtDesc.getText().toString(), Double.parseDouble(edtAge.getText().toString()), d, "/storage/emulated/0/Download/jake.png");
        app.saveAppUserProfileList();
        finish();
        return;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(resultCode == RESULT_OK && requestCode == PICK_IMAGE){
            imageUri = data.getData();
            imageView2.setImageURI(imageUri);
        }
    }
}
