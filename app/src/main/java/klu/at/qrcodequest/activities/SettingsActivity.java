package klu.at.qrcodequest.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import klu.at.qrcodequest.R;

public class SettingsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        createActionBar("Einstellungen");

        ListView listView = (ListView) findViewById(R.id.listViewSettings);
        String[] items = {"Design", "Begrüßung aktivieren", "Alle Fortschritte zurücksetzen"};
        int[] extra = {1, 2, 0}; // 1:arrow, 2:switch
        int[] colors = {Color.BLACK, Color.BLACK, Color.BLUE};
        listView.setAdapter(new SettingsListAdapter(this, R.layout.activity_settings, R.id.textSettings, R.id.imageArrow, items, extra, colors));

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
                switch (position) {
                    case 0:
                        startActivity(new Intent(getApplicationContext(), ThemeActivity.class));
                        break;
                    default:
                        break;
                }
            }
        });
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
        int[] extra;
        int[] colors;
        private LayoutInflater inflater;

        public SettingsListAdapter(Activity context, int layoutId, int textViewId, int imageViewId, String[] items, int[] extra, int[] colors) {
            super(context, layoutId, items);

            this.context = context;
            this.layoutId = layoutId;
            this.textViewId = textViewId;
            this.imageViewId = imageViewId;
            this.items = items;
            this.extra = extra;
            this.colors = colors;
        }

        public View getView(int pos, View convertView, ViewGroup parent) {
            if (inflater == null)
                inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (convertView == null)
                convertView = inflater.inflate(R.layout.list_settings, null);

            TextView label=(TextView) convertView.findViewById(textViewId);
            label.setText(items[pos]);
            label.setTextColor(colors[pos]);

            ImageView icon=(ImageView) convertView.findViewById(imageViewId);
            Switch switchIntro = (Switch) convertView.findViewById(R.id.switchIntro);

            if (extra[pos] == 1) {
                switchIntro.setVisibility(View.GONE);
                icon.setVisibility(View.VISIBLE);
                icon.setImageResource(R.drawable.ic_action_chevron_right);
            } else if (extra[pos] == 2) {
                icon.setVisibility(View.GONE);
                switchIntro.setVisibility(View.VISIBLE);

                final SharedPreferences preferences = getSharedPreferences("Settings", MODE_PRIVATE);
                switchIntro.setChecked(preferences.getBoolean("Intro", true));
                switchIntro.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putBoolean("Intro", isChecked);
                        editor.apply();
                    }
                });
            }

            return convertView;
        }
    }
}
