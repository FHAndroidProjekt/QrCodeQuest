package klu.at.qrcodequest.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import klu.at.qrcodequest.Data;
import klu.at.qrcodequest.R;

public abstract class BaseActivity extends ActionBarActivity {

    protected Toolbar toolbar;
    protected Data data;
    protected int theme = R.style.AppTheme;
    private SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPreferences = getSharedPreferences("Settings", MODE_PRIVATE);
        theme = sharedPreferences.getInt("theme", R.style.AppTheme);
        setTheme(theme);
        super.onCreate(savedInstanceState);

    }

    protected void createActionBar(String text) {
        toolbar = (Toolbar) findViewById(R.id.myToolbar);
        toolbar.setTitle(text);
        setSupportActionBar(toolbar);

        data = (Data) getApplicationContext();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        if (!getLocalClassName().equals("activities.SettingsActivity") && !getLocalClassName().equals("activities.ThemeActivity")) { // Wenn bereits in den Einstellungen kein Icon mehr
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.menu_actionbar, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        if (sharedPreferences.getInt("theme", R.style.AppTheme) != theme) {
            System.out.println("Now");
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    recreate();
                    theme = sharedPreferences.getInt("theme", R.style.AppTheme);
                }
            }, 1);
        }
    }

    protected void getAttributes() {
        int[] attrs = {R.attr.colorPrimary};
        @SuppressWarnings("ResourceType")
        TypedArray ta = obtainStyledAttributes(R.style.AppTheme_LightGreen, attrs);
        ta.getColor(0, Color.RED);
    }
}
