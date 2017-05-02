package com.example.user.trainingbuddy;

/**
 * Created by Fortune on 4/30/2017.
 */




import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class CustomFragmentAdapter extends FragmentPagerAdapter {
    private String[] videoResources;
    public CustomFragmentAdapter(FragmentManager fragmentManager, String[] videoResources){
        super(fragmentManager);
        this.videoResources = videoResources;
    }
    @Override
    public Fragment getItem(int position) {
        return ImageGalleryFragment.newInstance(position, this.videoResources);

    }
    @Override
    public int getCount() {
        return videoResources.length;
    }


}
