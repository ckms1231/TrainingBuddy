package com.example.user.trainingbuddy;

/**
 * Created by Fortune on 5/1/2017.
 */

public class VideoInfo {
    private String owner = "";
    private String path = "";

    public VideoInfo(String _owner, String _path){
        owner = _owner;
        path = _path;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getOwner() {
        return owner;
    }

    public String getPath() {
        return path;
    }
}
