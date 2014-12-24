package klu.at.qrcodequest.activities;

import android.animation.Animator;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.transition.Explode;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewAnimationUtils;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import klu.at.qrcodequest.AppDown;
import klu.at.qrcodequest.Data;
import klu.at.qrcodequest.R;
import klu.at.qrcodequest.User;
import klu.at.qrcodequest.VolleySingleton;

public class StartActivity extends BaseActivity {

    private Intent intent;
    private Button start;
    private Typeface typeface;
    private String userID;


    @Override
	protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        ProgressBar progressBar = (ProgressBar) findViewById(R.id.marker_progress);
//        int cx = (progressBar.getLeft() + progressBar.getRight()) / 2;
//        int cy = (progressBar.getTop() + progressBar.getBottom()) / 2;
//
//        int finalRadius = Math.max(progressBar.getWidth(), progressBar.getHeight());
//        Animator anim = ViewAnimationUtils.createCircularReveal(progressBar, cx, cy, 0, finalRadius);


        TextView willkommen = (TextView) findViewById(R.id.textViewWillkommen);
        typeface = Typeface.createFromAsset(getAssets(), "fonts/TYPOGRAPH PRO Light.ttf");
        willkommen.setTypeface(typeface);

        getUser();
    }
	
	private String sha1(String s) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        assert md != null; //Programm bricht ab wenn md null
        md.update(s.getBytes());
        byte[] bytes = md.digest();
        StringBuilder buffer = new StringBuilder();
        for (byte aByte : bytes) {
            String tmp = Integer.toString((aByte & 0xff) + 0x100, 16).substring(1);
            buffer.append(tmp);
        }
        return buffer.toString();
    }

    private void getUser() {
        userID = android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
        userID = sha1(userID);

        final String url = "http://193.171.127.102:8080/Quest/user/get?userId=" + userID;
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(url, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try {
                    if (response.toString().equals("[]")) {
                        intent = new Intent(getApplicationContext(), RegistrationActivity.class);
                        intent.putExtra("userID", userID);
                        startTimer();
                    } else {
                        Gson gson = new Gson();
                        User user = gson.fromJson(response.getJSONObject(0).toString(), User.class);

                        Data data = (Data) getApplicationContext(); // Globale Datenklasse
                        data.setUser(user); // User wird Global gespeichert

                        // Wenn User existiert keine Registrierung
                        intent = new Intent(getApplicationContext(), QuestActivity.class);

                        TextView welcomeUser = (TextView) findViewById(R.id.textViewUser);
                        welcomeUser.setTypeface(typeface);
                        if (user.getFirstname().equals("unknown")) {
                            welcomeUser.setText("zurück " + user.getNickname() + "!");
                        } else {
                            welcomeUser.setText("zurück " + user.getFirstname() + "!");
                        }

                        startTimer();
                    }
                    ProgressBar progressBar = (ProgressBar) findViewById(R.id.marker_progress);
                    progressBar.setVisibility(View.INVISIBLE);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new com.android.volley.Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO Auto-generated method stub
            }
        });
        VolleySingleton.getInstance(this.getApplicationContext()).addToRequestQueue(jsonArrayRequest);
    }

    private void startTimer() {
        int SPLASH_TIME_OUT = 2500;
        new Handler().postDelayed(new Runnable() {

            /*
             * Showing splash screen with a timer. This will be useful when you
             * want to show case your app logo / company
             */

            @Override
            public void run() {
                // This method will be executed once the timer is over
                // Start your app main activity

                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.abc_fade_out);

                // close this activity
                finish();
            }
        }, SPLASH_TIME_OUT);
    }
}
