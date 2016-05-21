package com.twominuteplays;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.Scope;

import java.io.IOException;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends BaseActivity implements View.OnClickListener {

    private static final int RC_SIGN_IN = 1;
    private static final String TAG = LoginActivity.class.getName();
    private GoogleSignInAccount mGoogleAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        findViewById(R.id.sign_in_button).setOnClickListener(this);
    }

    @Override
    public void postLoginCreate() {
        // TODO: refactor to use this instead?
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
        }
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            mGoogleAccount = result.getSignInAccount();
            getGoogleOAuthTokenAndLogin();

//            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//            startActivity(intent);
//            finish();
        }
        else {
            String errorMessage = result.getStatus().getStatusMessage();
            showError(errorMessage);
        }
    }

    private void showError(String errorMessage) {
        TextView errorTextView = (TextView) findViewById(R.id.errorTextView);
        errorTextView.setText(errorMessage);
    }

    private void getGoogleOAuthTokenAndLogin() {
        /* Get OAuth token in Background */
        AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {
            String mErrorMessage = null;

            @Override
            protected String doInBackground(Void... params) {
                String token = null;

                try {
                    Scope profileScope = new Scope(Scopes.PROFILE);
                    String scopeString = "oauth2:" + profileScope.toString() + " email";

                    token = GoogleAuthUtil.getToken(LoginActivity.this, mGoogleAccount.getEmail(), scopeString);
                } catch (IOException transientEx) {
                    /* Network or server error */
                    mErrorMessage = "Network error authenticating user. " + transientEx.getMessage();
                } catch (UserRecoverableAuthException e) {
                    Log.w(TAG, "Recoverable auth problem: " + e.toString());

                    /* We probably need to ask for permissions, so start the intent if there is none pending */
//                    if (!mGoogleIntentInProgress) {
//                        mGoogleIntentInProgress = true;
//                        Intent recover = e.getIntent();
//                        startActivityForResult(recover, RC_GOOGLE_LOGIN);
//                    }
                } catch (GoogleAuthException authEx) {
                    /* The call is not ever expected to succeed assuming you have already verified that
                     * Google Play services is installed. */
                    Log.e(TAG, " " + authEx.getMessage(), authEx);
                    mErrorMessage = "Network error authenticating user. " + authEx.getMessage();
                }
                return token;
            }

            @Override
            protected void onPostExecute(String token) {
//                mAuthProgressDialog.dismiss();
                if (token != null) {
                    /* Successfully got OAuth token, now login with Google */
                    loginWithGoogle(token);

                } else if (mErrorMessage != null) {
                    showError(mErrorMessage);
                }
            }
        };

        task.execute();
    }

    private void loginWithGoogle(String token) {
        mFirebaseRef.authWithOAuthToken("google", token, new Firebase.AuthResultHandler() {

            @Override
            public void onAuthenticated(AuthData authData) {
                if (authData != null) {
                    Log.i(TAG, "UID is " + authData.getUid());
                    //setAuthenticatedUserGoogle(authData);
                    /* Go to main activity */
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onAuthenticationError(FirebaseError firebaseError) {

            }
        });
    }

}

