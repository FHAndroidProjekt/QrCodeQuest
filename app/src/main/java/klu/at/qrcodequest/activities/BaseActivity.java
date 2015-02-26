package klu.at.qrcodequest.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
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
    protected int textColor1, textColor2, windowsBackgroundColor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Einstellungen mit dem Namen Settings werden eingelesen.
        sharedPreferences = getSharedPreferences("Settings", MODE_PRIVATE);
        theme = sharedPreferences.getInt("theme", R.style.AppTheme); // Thema wird aus den Einstellungen ausgelesen
        setTheme(theme); // Thema wird für die Activity gesetzt
        getAttributes();
        super.onCreate(savedInstanceState);

    }

    /**
     * Fügt die ToolBar als ActionBar in der aktuellen Activity hinzu.
     * @param text Der Titel in der ActionBar
     */
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
            case R.id.action_information:
                showInfo();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        if (sharedPreferences.getInt("theme", R.style.AppTheme) != theme) {
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

    /**
     * Gets the Attributes of the current applied theme
     */
    protected void getAttributes() {
        int[] attrs = {android.R.attr.textColorPrimary, android.R.attr.textColorSecondary, android.R.attr.windowBackground};
        int themeRessource = sharedPreferences.getInt("theme", R.style.AppTheme);
        @SuppressWarnings("ResourceType")
        TypedArray ta = obtainStyledAttributes(themeRessource, attrs);
        textColor1 = ta.getColor(0, Color.WHITE);
        textColor2 = ta.getColor(1, Color.BLACK);
        try {
            windowsBackgroundColor = ta.getColor(3, Color.WHITE);
        } catch (Exception e) {

        }
    }

    private void showInfo(){

        AlertDialog alertDialog = new AlertDialog.Builder(this).create();

        alertDialog.setTitle("Info");

        alertDialog.setMessage(("Programmierer:\nMessner Dominik & Kainz Alexander"));

        alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        alertDialog.show();


    }

}
