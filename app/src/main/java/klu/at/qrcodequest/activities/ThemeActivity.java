package klu.at.qrcodequest.activities;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import klu.at.qrcodequest.R;

public class ThemeActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        createActionBar("Themenauswahl");

        ListView listView = (ListView) findViewById(R.id.listViewSettings);
        String[] items = {"FH Kärnten", "Material Light Green", "Material Dark"};
        int[] colors = {Color.RED, Color.GREEN, Color.DKGRAY};
        listView.setAdapter(new SettingsListAdapter(this, R.layout.activity_settings, R.id.textSettings, R.id.imageArrow, items, colors));

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
                switch (position) {
                    case 0:
                        changeTheme(R.style.AppTheme);
                        break;
                    case 1:
                        changeTheme(R.style.AppTheme_LightGreen);
                        break;
                    case 2:
                        changeTheme(R.style.AppTheme_Dark);
                        break;
                    default:
                        break;
                }
            }
        });
    }

    private void changeTheme(int styleId) {
        SharedPreferences sharedPreferences = getSharedPreferences("Settings", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("theme", styleId);
        editor.apply();
        recreate();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_empty, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public class SettingsListAdapter extends ArrayAdapter {

        Activity context;
        int layoutId, textViewId, imageViewId;
        String[] items;
        int[] colors;
        private LayoutInflater inflater;

        public SettingsListAdapter(Activity context, int layoutId, int textViewId, int imageViewId, String[] items, int[] colors) {
            super(context, layoutId, items);

            this.context = context;
            this.layoutId = layoutId;
            this.textViewId = textViewId;
            this.imageViewId = imageViewId;
            this.items = items;
            this.colors = colors;
        }

        public View getView(int pos, View convertView, ViewGroup parent) {
            if (inflater == null)
                inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (convertView == null)
                convertView = inflater.inflate(R.layout.list_settings, null);

            TextView label=(TextView)convertView.findViewById(textViewId);
            label.setText(items[pos]);
            int[] colorGradient = new int[]{colors[pos], Color.TRANSPARENT};
            GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT, colorGradient);
            if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                convertView.setBackgroundDrawable(gd);
            } else {
                convertView.setBackground(gd);
            }

            return convertView;
        }
    }
}
