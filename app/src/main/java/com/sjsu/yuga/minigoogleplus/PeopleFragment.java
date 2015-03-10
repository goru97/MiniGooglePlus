package com.sjsu.yuga.minigoogleplus;

import android.app.ProgressDialog;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.plus.model.people.Person;

import java.io.InputStream;
import java.net.URL;

import Constants.Constants;

/**
 * Created by SCS_USER on 3/9/2015.
 */

public class PeopleFragment extends Fragment {
    ImageView img;
    Bitmap bitmap;
    ProgressDialog pDialog;
    TextView profile_name;
    TextView organization;
    public PeopleFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_logged_in, container, false);
        img = (ImageView)rootView.findViewById(R.id.profile_image);
        profile_name = (TextView)rootView.findViewById(R.id.profile_name);
        organization = (TextView)rootView.findViewById(R.id.organization);
        setProfile();
        new LoadImage().execute(((Person.Image)(((LoggedInActivity)getActivity()).getProfileInfo()).get(Constants.PERSON_PHOTO)).getUrl());
        return rootView;
    }

    private void setProfile(){
        LoggedInActivity loggedInActivity = (LoggedInActivity)getActivity();
        profile_name.setText((String) (loggedInActivity.getProfileInfo()).get(Constants.PERSON_NAME));
        profile_name.setTextSize(15);
        organization.setText((String) (loggedInActivity.getProfileInfo()).get(Constants.ORGANIZATION));
    }
    private class LoadImage extends AsyncTask<String, String, Bitmap> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(getActivity());
            pDialog.setMessage("Loading Profile ....");
            pDialog.show();
        }
        protected Bitmap doInBackground(String... args) {
            try {
                bitmap = BitmapFactory.decodeStream((InputStream) new URL(args[0]).getContent());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmap;
        }
        protected void onPostExecute(Bitmap image) {
            if(image != null){
                img.setImageBitmap(image);
                pDialog.dismiss();
            }else{
                pDialog.dismiss();
                Toast.makeText(getActivity(), "Image Does Not exist or Network Error", Toast.LENGTH_SHORT).show();
            }
        }
    }
}