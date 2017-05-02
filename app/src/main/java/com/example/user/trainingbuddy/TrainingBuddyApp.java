package com.example.user.trainingbuddy;

import android.app.Application;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Fortune on 4/28/2017.
 */

public class TrainingBuddyApp extends Application {

    private ArrayList<UserProfile> mUserProfileList = new ArrayList<>();
    private ArrayList<VideoInfo> mVideoInfoList = new ArrayList<>();
    private String extStorageDirectory = Environment.getExternalStorageDirectory()
            .toString();

    public void initializeUserProfileList()
    {
        //Read registered users
        //Store the IDs / locations of their profile


        String nameList[] = {"Acacia", "Birch", "Cashew", "Dogwood", "Elm", "Fig", "Guava"};
        String descList[] = {"Loves to reproduce.", "Hates long walks on the beach.", "Consistent.", "Always stands by your side.", "Tall, dark, and handsome.", "Towering"};
        String imgList[] = {"/Download/a.png", "/Download/b.png", "/Download/c.png", "/Download/d.png", "/Download/e.png",};
        String gendList[] = {"M","F"};
        Random rand = new Random();

        //create random profiles
        for (int i=0; i < 5;i++)
        {

            int randomNameIdx = rand.nextInt(nameList.length);
            int randomDescIdx = rand.nextInt(descList.length);
            int randomImgIdx = rand.nextInt(imgList.length);
            int randomGendIdx = rand.nextInt(2);
            int randomAge = rand.nextInt(50);
            Drawable d = Drawable.createFromPath(extStorageDirectory + imgList[randomImgIdx]);
            UserProfile newUserProfile = new UserProfile(nameList[randomNameIdx] + Integer.toString(i), gendList[randomGendIdx], descList[randomDescIdx],
                    randomAge, d);
            Log.e("TrainingBuddyApp", extStorageDirectory + imgList[randomImgIdx]);
            mUserProfileList.add(newUserProfile);
        }
    }

    public ArrayList<UserProfile> getmUserProfileList() {
        return mUserProfileList;
    }

    public ArrayList<VideoInfo> getmVideoInfoList() {
        return mVideoInfoList;
    }

    public boolean loadUserProfileList() {
        /* Get storage path */
        String loadPath = Environment.getExternalStorageDirectory()
                .toString();

        /* Check if file exists */
        File loadFile = new File(loadPath+"/Download/", "userlist.txt");
        if (loadFile.exists() == false) {
            Log.e("TrainingBuddyApp", "File not loaded because it does not exist");
            return false;
        }

        String contents = "";
        try {
            /* Get file input stream */
            FileInputStream fis = new FileInputStream( loadFile );

            /* Read from file input stream */
            while (fis.available() > 0) {
                byte buf[] = new byte[32];
                int bytesRead = fis.read(buf, 0, 32);
                contents += new String(buf, 0, bytesRead);
            }

            /* close file input stream */
            fis.close();
        } catch (Exception e) {
            Log.e("TrainingBuddyApp", "Exception occurred: " + e.getMessage());
            return false;
        }

        /* Display the contents in Android Monitor */
        Log.d("TrainingBuddyApp", "File Read Done:");
        Log.d("TrainingBuddyApp", "    " + contents );

        /* Parse the file contents */
        boolean result = parseUserProfilestoList(contents);

        return result;
    }

    public boolean parseUserProfilestoList(String contentsUserProfileFile) {
        /* Clear the existing friend list */
        mUserProfileList.clear();

        /* Divide the contents of the friend list file by newlines (\n)
         *  to obtain each individual line in the file */
        String lines[] = contentsUserProfileFile.split("\n");

        Drawable drawable = null;
        /* Cycle through each line */
        for (int i = 0; i < lines.length; i++) {
            /* Split the current line by commas (, ) to separate the saved pieces of info */
            String friendInfo[] = lines[i].split(",");

            /* Store each piece of information in a temporary variable */
            String username = friendInfo[0];
            String gender = friendInfo[1];
            String desc = friendInfo[2];
            String drawPath = friendInfo[3];
            double age = Double.parseDouble(friendInfo[4]);
            //Drawable d = Drawable.createFromPath(extStorageDirectory+image);
            Drawable d = Drawable.createFromPath(extStorageDirectory + friendInfo[3]);
            /* Create a Friend object from the info */
            UserProfile newUserProfile = new UserProfile(username, gender, desc, age, d);
            newUserProfile.setDrawPath(drawPath);

            /* Add the new Friend object to the Friend list */
            mUserProfileList.add(newUserProfile);
        }
        return true;
    }

    public boolean saveAppUserProfileList() {

        String filePath = extStorageDirectory + "/Download";
        File file = new File(filePath, "userlist.txt");
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(file);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        String fileContents = "";
        for (int i = 0; i < mUserProfileList.size(); i++) {
            UserProfile msg = mUserProfileList.get(i);

            fileContents += msg.getUsername() + "," +
                    msg.getGender() + "," + msg.getDesc()  + "," + msg.getDrawPath() + "," + msg.getAge() + "," + "\n";
        }

        try {
            outputStream.write(fileContents.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //return writeFile(filename, fileContents);
        return true;
    }

    public boolean saveVideoInfoList() {

        String filePath = extStorageDirectory + "/Download";
        File file = new File(filePath, "videoinfo.txt");
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(file);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        String fileContents = "";
        for (int i = 0; i < mVideoInfoList.size(); i++) {
            VideoInfo msg = mVideoInfoList.get(i);

            fileContents += msg.getOwner() + "," +
                    msg.getPath() + "," + 0 + "\n";
        }

        try {
            outputStream.write(fileContents.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //return writeFile(filename, fileContents);
        return true;
    }

    private String getStoragePath() {
        String storagePath = getFilesDir().toString();
        return storagePath;
    }

    public boolean writeFile(String fileName, String data) {
        String filePath = extStorageDirectory + "/Download";
        File file = new File(filePath, fileName);
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(file);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }



        try {
            outputStream.write(data.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

    public boolean loadVideoInfoList() {
        /* Get storage path */
        String loadPath = Environment.getExternalStorageDirectory()
                .toString();

        /* Check if file exists */
        File loadFile = new File(loadPath+"/Download/", "videoinfo.txt");
        if (loadFile.exists() == false) {
            Log.e("TrainingBuddyApp", "File not loaded because it does not exist");
            return false;
        }

        String contents = "";
        try {
            /* Get file input stream */
            FileInputStream fis = new FileInputStream( loadFile );

            /* Read from file input stream */
            while (fis.available() > 0) {
                byte buf[] = new byte[32];
                int bytesRead = fis.read(buf, 0, 32);
                contents += new String(buf, 0, bytesRead);
            }

            /* close file input stream */
            fis.close();
        } catch (Exception e) {
            Log.e("TrainingBuddyApp", "Exception occurred: " + e.getMessage());
            return false;
        }

        /* Display the contents in Android Monitor */
        Log.d("TrainingBuddyApp", "File Read Done:");
        Log.d("TrainingBuddyApp", "    " + contents );

        /* Parse the file contents */
        boolean result = parseVideoInfoToList(contents);

        return result;
    }

    public boolean addUserProfile(String username, String gender, String desc, double age, Drawable d, String _drawPath){
        UserProfile user = new UserProfile(username, gender, desc, age, d);
        user.setDrawPath(_drawPath);
        mUserProfileList.add(user);
        return true;
    }

    public boolean addVideoInfo(String owner, String path){
        VideoInfo video = new VideoInfo(owner, path);
        mVideoInfoList.add(video);
        return true;
    }

    public boolean parseVideoInfoToList(String contentsUserProfileFile) {
        /* Clear the existing friend list */
        mVideoInfoList.clear();

        /* Divide the contents of the friend list file by newlines (\n)
         *  to obtain each individual line in the file */
        String lines[] = contentsUserProfileFile.split("\n");

        Drawable drawable = null;
        /* Cycle through each line */
        for (int i = 0; i < lines.length; i++) {
            /* Split the current line by commas (, ) to separate the saved pieces of info */
            String friendInfo[] = lines[i].split(",");

            /* Store each piece of information in a temporary variable */
            String owner = friendInfo[0];
            String path = friendInfo[1];
            String dummy = friendInfo[2];
            /* Create a Friend object from the info */
            VideoInfo newVideoInfo = new VideoInfo(owner, path);

            /* Add the new Friend object to the Friend list */
            mVideoInfoList.add(newVideoInfo);
        }
        return true;
    }
}
