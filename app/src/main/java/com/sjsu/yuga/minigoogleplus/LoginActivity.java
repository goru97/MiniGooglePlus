package com.sjsu.yuga.minigoogleplus;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.plus.People;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.google.android.gms.plus.model.people.PersonBuffer;

import java.util.HashMap;

import Constants.Constants;

public class LoginActivity extends Activity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,ResultCallback<People.LoadPeopleResult> {
private static final String TAG = "Login Activity class";
private HashMap profileInfo  = new HashMap();
private PersonBuffer circles;
    /* Request code used to invoke sign in user interactions. */
    private static final int RC_SIGN_IN = 0;

    /* Client used to interact with Google APIs. */
    private GoogleApiClient mGoogleApiClient;

    /* A flag indicating that a PendingIntent is in progress and prevents
     * us from starting further intents.
     */
    private boolean mIntentInProgress;

    /* Track whether the sign-in button has been clicked so that we know to resolve
     * all issues preventing sign-in without waiting.
     */
    private boolean mSignInClicked;

    /* Store the connection result from onConnectionFailed callbacks so that we can
     * resolve them when the user clicks sign-in.
     */
    private ConnectionResult mConnectionResult;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API,new Plus.PlusOptions.Builder().build()) // prev. .addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .build();
        setContentView(R.layout.activity_login);
        findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Log.d("","Clicked");
                if (!mGoogleApiClient.isConnecting()) {

                    mSignInClicked = true;
                    resolveSignInError();
                }
            }
        });
    }


    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    protected void onStop() {
        super.onStop();

        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }
//    @Override
  /*  protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }*/
public void onConnectionFailed(ConnectionResult result) {
    if (!mIntentInProgress) {
        // Store the ConnectionResult so that we can use it later when the user clicks
        // 'sign-in'.
        mConnectionResult = result;

        if (mSignInClicked) {
            // The user has already clicked 'sign-in' so we attempt to resolve all
            // errors until the user is signed in, or they cancel.
            resolveSignInError();
        }
    }
}

    public void onConnected(Bundle connectionHint) {
        // We've resolved any connection errors.  mGoogleApiClient can be used to
        // access Google APIs on behalf of the user.
        Log.d(TAG,"Connected");
        if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {
            Person currentPerson = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
            String personName = currentPerson.getDisplayName();
            Person.Image personPhoto = currentPerson.getImage();
            String personGooglePlusProfile = currentPerson.getUrl();

            profileInfo.put(Constants.CURRENT_PERSON,currentPerson);
            profileInfo.put(Constants.PERSON_NAME,personName);
            profileInfo.put(Constants.PERSON_PHOTO,personPhoto);
            profileInfo.put(Constants.PERSON_GOOGLE_PLUS_PROFILE,personGooglePlusProfile);
        }

        Plus.PeopleApi.loadVisible(mGoogleApiClient, null)
               .setResultCallback(this);
       /* Intent intent =  new Intent(this,LoggedInActivity.class);
        intent.putExtra(Constants.CONNECTED, Constants.YES);
        startActivity(intent); */

    }

    protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
        if (requestCode == RC_SIGN_IN) {
            mIntentInProgress = false;

            if (!mGoogleApiClient.isConnecting()) {
                mGoogleApiClient.connect();
            }
        }
    }


    private void resolveSignInError() {

        if (mConnectionResult.hasResolution()) {
            try {
                mIntentInProgress = true;
                startIntentSenderForResult(mConnectionResult.getResolution().getIntentSender(),
                        RC_SIGN_IN, null, 0, 0, 0);
            } catch (IntentSender.SendIntentException e) {
                // The intent was canceled before it was sent.  Return to the default
                // state and attempt to connect to get an updated ConnectionResult.
                mIntentInProgress = false;
                mGoogleApiClient.connect();
            }
        }
    }

    @Override
    public void onResult(People.LoadPeopleResult peopleData) {
        if (peopleData.getStatus().getStatusCode() == CommonStatusCodes.SUCCESS) {
            PersonBuffer personBuffer = peopleData.getPersonBuffer();
            circles = personBuffer;
            try {
                int count = personBuffer.getCount();
                for (int i = 0; i < count; i++) {
                    Log.d(TAG, "Display name: " + personBuffer.get(i).getDisplayName());
                }
            } finally {
                personBuffer.close();
            }
        } else {
            Log.e(TAG, "Error requesting visible circles: " + peopleData.getStatus());
        }

        //Start New Activity from here
        Intent intent =  new Intent(this,LoggedInActivity.class);
        intent.putExtra(Constants.CONNECTED, Constants.YES);
        intent.putExtra(Constants.PROFILE_INFO, profileInfo);
        startActivity(intent);
    }
    public void onConnectionSuspended(int cause) {
        mGoogleApiClient.connect();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
