package klu.at.qrcodequest.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import klu.at.qrcodequest.*;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.google.gson.Gson;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class StartActivity extends ActionBarActivity implements OnClickListener {

    private Intent intent;
    private Button start;
    private Typeface typeface;
    private String userID;


    @Override
	protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start);
		AppDown.register(this);

//        Toolbar toolbar = findViewById()

        start = (Button) findViewById(R.id.button1);
        start.setOnClickListener(this);
        start.setClickable(false);
        TextView willkommen = (TextView) findViewById(R.id.textViewWillkommen);
        typeface = Typeface.createFromAsset(getAssets(), "fonts/TYPOGRAPH PRO Light.ttf");
        willkommen.setTypeface(typeface);

        getUser();
    }


	@Override
	public void onClick(View v) {
		
		startActivity(intent);
		
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
                        start.setClickable(true);
                    } else {
                        Gson gson = new Gson();
                        User user = gson.fromJson(response.getJSONObject(0).toString(), User.class);

                        Data data = (Data) getApplicationContext(); // Globale Datenklasse
                        data.setUser(user); // User wird Global gespeichert

                        // Wenn User existiert keine Registrierung
                        intent = new Intent(getApplicationContext(), QuestActivity.class);
                        start.setClickable(true);

                        TextView welcomeUser = (TextView) findViewById(R.id.textViewUser);
                        welcomeUser.setTypeface(typeface);
                        if (user.getFirstname().equals("unknown")) {
                            welcomeUser.setText("zurück " + user.getNickname() + "!");
                        } else {
                            welcomeUser.setText("zurück " + user.getFirstname() + "!");
                        }
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
}
