package com.twominuteplays;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.twominuteplays.db.FirebaseStuff;

public abstract class BaseActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener {
    /* Client used to interact with Google APIs. */
    protected GoogleApiClient mGoogleApiClient;
    protected Firebase.AuthStateListener mAuthListener;
    protected Firebase mFirebaseRef;

    private boolean isNotLogin() {
        return !(this instanceof LoginActivity);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFirebaseRef = new Firebase(FirebaseStuff.FIREBASE_URL);

        /* Setup the Google API object to allow Google logins */
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        /**
         * Build a GoogleApiClient with access to the Google Sign-In API and the
         * options specified by gso.
         */

        /* Setup the Google API object to allow Google+ logins */
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        if (isNotLogin()) {
            mAuthListener = new Firebase.AuthStateListener() {
                @Override
                public void onAuthStateChanged(AuthData authData) {
                     /* The user has been logged out */
                    if (authData == null) {
                        /* Clear out shared preferences */
                        takeUserToLoginScreenOnUnAuth();
                    }
                    else {
                        postLoginCreate();
                    }
                }
            };
            mFirebaseRef.addAuthStateListener(mAuthListener);
        }
    }

    public abstract void postLoginCreate();

    @Override
    public void onDestroy() {
        super.onDestroy();
        /* Cleanup the AuthStateListener */
        if (isNotLogin()) {
            mFirebaseRef.removeAuthStateListener(mAuthListener);
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    /**
     * Logs out the user from their current session and starts LoginActivity.
     * Also disconnects the mGoogleApiClient if connected and provider is Google
     */
    protected void logout() {

        mFirebaseRef.unauth();

        /* Logout from Google+ */
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        //nothing
                    }
                });
    }

    private void takeUserToLoginScreenOnUnAuth() {
        /* Move user to LoginActivity, and remove the backstack */
        Intent intent = new Intent(BaseActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }
}
