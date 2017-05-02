package com.example.user.trainingbuddy;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore.Video.Thumbnails;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class VideoGalleryActivity extends AppCompatActivity {
    private static final String TAG = VideoGalleryActivity.class.getSimpleName();
    private ViewPager largeViewPager;
    private CustomFragmentAdapter customFragmentPageAdapter;
    private LinearLayout galleryLayout;
    private String photoPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "photos";
    private String[] fileList;
    private Uri[] mUrls;
    private int currentIndex;
    private int[]resourceImages;
    private File directoryPath;
    private final int REQUEST_CAMERA_PERMISSION = 200;
    private File storeDirectory;
    protected Bitmap resourceBitmap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_gallery);

        Intent receivedIntent = getIntent();
        final String username = receivedIntent.getStringExtra("NAME");
        ArrayList<VideoInfo> videoInfoList = new ArrayList<VideoInfo>();

        ImageButton record = (ImageButton) findViewById(R.id.buttonRecord);
        record.setOnClickListener(new View.OnClickListener() {
                                      @Override
                                      public void onClick(View view) {
                                          Intent launchIntent = new Intent(VideoGalleryActivity.this, MainActivity.class);
                                          launchIntent.putExtra("NAME", username);
                                          startActivity(launchIntent);
                                      }
                                  });

                TextView user = (TextView) findViewById(R.id.txtUniqueProfile);
        user.setText(username+"'s videos");

        TrainingBuddyApp app = (TrainingBuddyApp) getApplication();
        app.loadVideoInfoList();
        videoInfoList = app.getmVideoInfoList();
        int count =0;
        ArrayList<Integer> tags = new ArrayList<Integer>();
        for(int i=0; i<videoInfoList.size();i++) {
            if(username.equals(videoInfoList.get(i).getOwner()))
            {
                count++;
                tags.add(i);
            }
        }
        String[] videoResourcePath = new String[count];
        for (int a = 0; a<tags.size();a++){
            videoResourcePath[a] = videoInfoList.get(tags.get(a)).getPath();
        }
        Log.e("TrainingBuddyApp", "001");
        largeViewPager = (ViewPager)findViewById(R.id.large_image);
        customFragmentPageAdapter = new CustomFragmentAdapter(getSupportFragmentManager(), videoResourcePath);
        largeViewPager.setOffscreenPageLimit(3);
        largeViewPager.setAdapter(customFragmentPageAdapter);
        galleryLayout = (LinearLayout)findViewById(R.id.my_gallery);
        directoryPath = getAlbumStorageDir("images");
        Log.e("TrainingBuddyApp", "002");
        /*if(directoryPath.exists() && directoryPath.isDirectory()) {
            try {
                FileUtils.cleanDirectory(directoryPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }*/
        resourceImages = new int[]{R.drawable.one, R.drawable.one};
        for(int i = 0; i < resourceImages.length; i++){
            Log.e("TrainingBuddyApp", "021");
            resourceBitmap = resourceToImageBitmap(resourceImages[i]);
            Log.e("TrainingBuddyApp", "022");
            saveFileInExternalStorage(resourceBitmap, i);
            Log.e("TrainingBuddyApp", "023");
        }
        Log.e("TrainingBuddyApp", "003");
        /*File[] filterStoredFiles =  directoryPath.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return ((filename.endsWith(".jpg"))||(filename.endsWith(".png")));
            }
        });
        Log.e("TrainingBuddyApp", "031"); */
        /*if(filterStoredFiles.length <= 0){
            Log.e("TrainingBuddyApp", "032");
            Toast.makeText(VideoGalleryActivity.this, "Whoops!!!, There is no file saved in the external storage", Toast.LENGTH_LONG).show();
            return;
        }*/
        Log.e("TrainingBuddyApp", "004");
        fileList = videoResourcePath;
        Log.e("TrainingBuddyApp", "041");
        //for(int i = 0; i < fileList.length; i++){}
        Log.e("TrainingBuddyApp", "043");
        mUrls = new Uri[fileList.length];
        Log.e("TrainingBuddyApp", "044");
        for(int i=0; i < fileList.length; i++){
            Log.e("TrainingBuddyApp", "045");
            mUrls[i] = Uri.parse(fileList[i]);
        }
        Log.e("TrainingBuddyApp", "005");
        for(int j = 0; j < mUrls.length; j++){
            String videoAbsolutePath = mUrls[j].toString();

            Bitmap bmThumbnail;

            bmThumbnail = ThumbnailUtils.createVideoThumbnail(videoAbsolutePath, Thumbnails.MINI_KIND);

            ImageView addImageView = new ImageView(this.getApplicationContext());
            addImageView.setImageBitmap(bmThumbnail);
            //addImageView.setImageResource(R.drawable.one);
            final int indexJ = j;
            addImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    currentIndex = indexJ;
                    largeViewPager.setCurrentItem(currentIndex);
                }
            });
            galleryLayout.addView(addImageView);
        }
        Log.e("TrainingBuddyApp", "000");
    }
    /*@Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                // close the app
                Toast.makeText(VideoGalleryActivity.this, "Sorry!!!, you can't use this app without granting this permission", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }*/
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(null != galleryLayout){
            galleryLayout.removeAllViews();
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "onResume");
        if(storeDirectory.exists()){
            storeDirectory.delete();
        }
        FileOutputStream out = null;
        try {
            storeDirectory.createNewFile();
            out = new FileOutputStream(storeDirectory);
            resourceBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void saveFileInExternalStorage(Bitmap bitmap, int index){
        if(!isExternalStorageWritable() && !isExternalStorageReadable()){
            Toast.makeText(VideoGalleryActivity.this, "There is no external storage in your device or not writable", Toast.LENGTH_LONG).show();
            return;
        }
        String filename = "thumbnail" + String.valueOf(index) + ".jpg";
        storeDirectory = new File(directoryPath, filename);
        // Add permission for camera and let user grant the permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(VideoGalleryActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
            return;
        }
        if(storeDirectory.exists()){
            storeDirectory.delete();
        }
        //System.out.println("Files " + storeDirectory.getAbsolutePath());
        Log.i(TAG, "Directory not created");
        FileOutputStream out = null;
        try {
            storeDirectory.createNewFile();
            out = new FileOutputStream(storeDirectory);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private Bitmap resourceToImageBitmap(int fileResource){
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), fileResource);
        return bitmap;
    }
    private ImageView getNewImageView(String photoPath){
        Bitmap bm = decodeFile(photoPath);
        ImageView imageView = new ImageView(getApplicationContext());
        imageView.setLayoutParams(new LinearLayout.LayoutParams(300, 200));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageBitmap(bm);
        return imageView;
    }
    public static Bitmap decodeFile(String photoPath){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(photoPath, options);
        options.inJustDecodeBounds = false;
        options.inDither = false;
        options.inPreferQualityOverSpeed = true;
        return BitmapFactory.decodeFile(photoPath, options);
    }
    public File getAlbumStorageDir(String albumName) {
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), albumName);
        if (!file.mkdirs()) {
            Log.e(TAG, "Directory not created");
        }
        return file;
    }
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }
}