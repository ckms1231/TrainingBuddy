package com.example.user.trainingbuddy;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Fortune on 4/28/2017.
 */

public class ImageAdapter extends BaseAdapter {
    private ArrayList<UserProfile> mImageList = null;
    private Context mContext = null;

    public ImageAdapter(Context c) {
        mContext = c;
        Log.e("TrainingBuddyApp", "A");
        return;
    }

    /*
     *  Sets the list of image file names to be downloaded from the
     *  server and loaded by this ImageGridAdapter into the GridView
     */
    public void setData(ArrayList<UserProfile> imgList) {
        mImageList = imgList;
        return;
    }

    /*
     *  Used by Android to determine what View should be loaded into a
     *  specific GridView element and what it should look like. In this
     *  case, it simply loads an image into an ImageView.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        /* Check if the list of image names has been set and is non-empty */
        if (mImageList == null) {
            return convertView;
        }

        if (mImageList.size() <= 0){
            return convertView;
        }

        /* Check if the 'position' parameter corresponds to a valid index
         *  for a data source in our list of image file names */
        UserProfile itemName = getItem(position);
        if (itemName==null) {
            return convertView;
        }

        Log.e("TrainingBuddyApp", "V");
        /* Load a layout from one of our XML files into where this GridView
         *  element should appear */
        if (convertView == null) {
            Log.e("TrainingBuddyApp", "Y");
            LayoutInflater inflater =
                    (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.grid_item, parent, false);
        }

        Log.e("TrainingBuddyApp", "W");
        /* From the loaded layout, get the ImageView that we want to load
         *  an image from the Thumbnail server into */
        ImageView imgItem = (ImageView) convertView.findViewById(R.id.grid_item_image);
        TextView txtItem = (TextView) convertView.findViewById(R.id.grid_item_text);

        Log.e("TrainingBuddyApp", "Z");

        //imgItem.setImageResource(itemName.getImage());
        imgItem.setImageDrawable(itemName.getImage());
        txtItem.setText(itemName.getUsername());

        return convertView;
    }

    /*
     *  Gets the current number of elements being displayed by
     *  this adapter in the GridView
     **/
    @Override
    public int getCount() {
        if (mImageList != null) {
            return mImageList.size();
        }
        return 0;
    }

    /*
     *  Gets the String value of the item at the specified position
     *  in this adapter's source data
     **/
    @Override
    public UserProfile getItem(int position) {
        if (mImageList != null) {
            return mImageList.get(position);
        }
        return null;
    }

    /*
     *  Gets the integer ID of the item at the specified position
     *  in this adapter's source data. Possibly redundant.
     **/
    @Override
    public long getItemId(int position) {
        if (mImageList != null) {
            if ((position > mImageList.size()) ||
                    (position < 0)) {
                return 0;
            }

            return position;
        }
        return 0;
    }

}


