package braingalore.loginmodes;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.login.widget.ProfilePictureView;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * A login screen that offers G+, Facebook sign in option
 */
public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, OnClickListener {

    private static final String TAG = LoginActivity.class.getName();
    private GoogleApiClient mGoogleApiClient;
    private ImageView imgProfilePic;
    private TextView txtName, txtEmail;
    private TextView fbData;
    private LinearLayout llProfileLayout;
    private LinearLayout fbProfileLayout;
    private ProgressDialog mProgressDialog;
    private SignInButton signInButton;
    private Button signOutButton;
    private LoginButton loginButton;
    private CallbackManager callbackManager;
    private AccessTokenTracker accessTokenTracker;
    private AccessToken accessToken;
    private ProfileTracker profileTracker;
    private ProfilePictureView profile;
    SharedPreferences sharedpreferences;
    public static final String MyPREFERENCES = "MyPrefs";
    public static final String LOGIN_VIA_FB = "LoginFB";
    public static final String LOGIN_VIA_GOOGLE = "LoginGoogle";
    private String facebook_id, f_name, m_name, l_name, gender, profile_image, full_name, email_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.setApplicationId(getString(R.string.facebook_app_id));
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_login);
        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        callbackManager = CallbackManager.Factory.create();
        imgProfilePic = (ImageView) findViewById(R.id.imgProfilePic);
        txtName = (TextView) findViewById(R.id.txtName);
        txtEmail = (TextView) findViewById(R.id.txtEmail);
        fbData = (TextView) findViewById(R.id.fbData);
        llProfileLayout = (LinearLayout) findViewById(R.id.llProfile);
        fbProfileLayout = (LinearLayout) findViewById(R.id.fbProfile);
        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(
                    AccessToken oldAccessToken,
                    AccessToken currentAccessToken) {
                if (currentAccessToken != null) {
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.putBoolean(LOGIN_VIA_FB, true);
                    editor.putBoolean(LOGIN_VIA_GOOGLE, false);
                    editor.commit();
                    updateUI(true, false);
                }
                if (currentAccessToken == null) {
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.putBoolean(LOGIN_VIA_FB, false);
                    editor.commit();
                    updateUI(false, false);
                }
            }
        };
        // If the access token is available already assign it.
        accessToken = AccessToken.getCurrentAccessToken();

        profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(
                    Profile oldProfile,
                    Profile currentProfile) {
                // App code
            }
        };

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        // Set the dimensions of the sign-in button.
        signInButton = (SignInButton) findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        signInButton.setScopes(gso.getScopeArray());

        signInButton.setOnClickListener(this);

        signOutButton = (Button) findViewById(R.id.btn_sign_out);
        signOutButton.setOnClickListener(this);

        loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions("email");

        profile = (ProfilePictureView) findViewById(R.id.picture);

        // If using in a fragment
        //loginButton.setFragment(this);
        // Callback registration
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                facebook_id = f_name = m_name = l_name = gender = profile_image = full_name = email_id = "";
                updateUI(true, false);
            }

            @Override
            public void onCancel() {
                // App code
            }

            @Override
            public void onError(FacebookException exception) {

            }
        });

    }

    public void RequestData() {
        GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {

                JSONObject json = response.getJSONObject();
                System.out.println("Json data :" + json);
                try {
                    if (json != null) {
                        String text = "<b>Name :</b> " + json.getString("name") + "<br><br><b>Email :</b> " + json.getString("email") + "<br><br><b>Profile link :</b> " + json.getString("link");
                        fbData.setText(Html.fromHtml(text));
                        profile.setProfileId(json.getString("id"));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,link,email,picture");
        request.setParameters(parameters);
        request.executeAsync();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        /*accessTokenTracker.stopTracking();
        profileTracker.stopTracking();*/
    }

    private int RC_SIGN_IN = 987;

    /**
     * Call to sign in to Google account
     */
    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    /**
     * API call to sign out from Google account
     */
    private void signOut() {
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putBoolean(LOGIN_VIA_GOOGLE, false);
        editor.commit();
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        updateUI(false, false);
                    }
                });
    }

    private void revokeAccess() {
        Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        updateUI(false, false);
                    }
                });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id) {
            case R.id.sign_in_button:
                signIn();
                break;

            case R.id.btn_sign_out:
                signOut();
                break;

            /*case R.id.btn_revoke_access:
                revokeAccess();
                break;*/
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putBoolean(LOGIN_VIA_FB, false);
            editor.putBoolean(LOGIN_VIA_GOOGLE, true);
            editor.commit();
            GoogleSignInAccount acct = result.getSignInAccount();
            Log.e(TAG, "display name: " + acct.getDisplayName());

            String personName = acct.getDisplayName();
            String personPhotoUrl = acct.getPhotoUrl().toString();
            String email = acct.getEmail();

            Log.e(TAG, "Name: " + personName + ", email: " + email
                    + ", Image: " + personPhotoUrl);

            txtName.setText(personName);
            txtEmail.setText(email);
            Glide.with(getApplicationContext()).load(personPhotoUrl)
                    .thumbnail(0.5f)
                    .crossFade()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(imgProfilePic);
            updateUI(false, true);
        } else {
            // Signed out, show unauthenticated UI.
            updateUI(false, false);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        if (sharedpreferences.getBoolean(LOGIN_VIA_FB, false) == true) {
            updateUI(true, false);
        } else if (sharedpreferences.getBoolean(LOGIN_VIA_GOOGLE, false) == true) {
            OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
            if (opr.isDone()) {
                // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
                // and the GoogleSignInResult will be available instantly.
                Log.d(TAG, "Got cached sign-in");
                GoogleSignInResult result = opr.get();
                handleSignInResult(result);
            } else {
                // If the user has not previously signed in on this device or the sign-in has expired,
                // this asynchronous branch will attempt to sign in the user silently.  Cross-device
                // single sign-on will occur in this branch.
                showProgressDialog();
                opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                    @Override
                    public void onResult(GoogleSignInResult googleSignInResult) {
                        hideProgressDialog();
                        handleSignInResult(googleSignInResult);
                    }
                });
            }
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }

    /**
     * Updates UI after login status changed
     *
     * @param isSignedInViaFB
     * @param isSignedInViaGoogle
     */
    private void updateUI(boolean isSignedInViaFB, boolean isSignedInViaGoogle) {
        if (isSignedInViaFB) {
            signInButton.setVisibility(View.GONE);
            loginButton.setVisibility(View.VISIBLE);
            fbProfileLayout.setVisibility(View.VISIBLE);
            if (AccessToken.getCurrentAccessToken() != null) {
                RequestData();
                Profile profile = Profile.getCurrentProfile();
                if (profile != null) {
                    facebook_id = profile.getId();
                    Log.e("facebook_id", facebook_id);
                    f_name = profile.getFirstName();
                    Log.e("f_name", f_name);
                    m_name = profile.getMiddleName();
                    Log.e("m_name", m_name);
                    l_name = profile.getLastName();
                    Log.e("l_name", l_name);
                    full_name = profile.getName();
                    Log.e("full_name", full_name);
                    profile_image = profile.getProfilePictureUri(400, 400).toString();
                    Glide.with(getApplicationContext()).load(profile_image)
                            .thumbnail(0.5f)
                            .crossFade()
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(imgProfilePic);
                    Log.e("profile_image", profile_image);
                }
            }
        } else if (isSignedInViaGoogle) {
            signInButton.setVisibility(View.GONE);
            loginButton.setVisibility(View.GONE);
            llProfileLayout.setVisibility(View.VISIBLE);
        } else {
            signInButton.setVisibility(View.VISIBLE);
            loginButton.setVisibility(View.VISIBLE);
            llProfileLayout.setVisibility(View.GONE);
            fbProfileLayout.setVisibility(View.GONE);
        }
    }
}

