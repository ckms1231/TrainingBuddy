package com.example.user.trainingbuddy;

import android.graphics.drawable.Drawable;

/**
 * Created by Fortune on 4/28/2017.
 */

public class UserProfile {

    private String username = "";
    private String gender = "";
    private String desc = "";
    private double age = 0;
    private Drawable image = null;
    private String drawPath = "";

    public UserProfile(String _username, String _gender, String _desc, double _age, Drawable _image)
    {
        username = _username;
        gender = _gender;
        desc = _desc;
        age = _age;
        image = _image;
    }

    public String getUsername() {
        return username;
    }

    public String getGender() {
        return gender;
    }

    public String getDesc() {
        return desc;
    }

    public double getAge() {
        return age;
    }

    public Drawable getImage() {
        return image;
    }

    public String getDrawPath() {return drawPath;}

    public void setDrawPath(String drawPath) {
        this.drawPath = drawPath;
    }

    public String toString() {
        return getUsername() + "\n";
    }
}
