package klu.at.qrcodequest.activities;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import java.util.zip.Inflater;

import klu.at.qrcodequest.R;

public class SettingsActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = (Toolbar) findViewById(R.id.myToolbar);
        toolbar.setTitle("Einstellungen");
        setSupportActionBar(toolbar);

        ListView listView = (ListView) findViewById(R.id.listViewSettings);
        String[] items = {"Design", "Alle Fortschritte zurücksetzen"};
        boolean[] arrow = {true, false};
        int[] colors = {Color.BLACK, Color.BLUE};
        listView.setAdapter(new SettingsListAdapter(this, R.layout.activity_settings, R.id.textSettings, R.id.imageArrow, items, arrow, colors));
    }

    public class SettingsListAdapter extends ArrayAdapter {

        Activity context;
        int layoutId, textViewId, imageViewId;
        String[] items;
        boolean[] arrows;
        int[] colors;
        private LayoutInflater inflater;

        public SettingsListAdapter(Activity context, int layoutId, int textViewId, int imageViewId, String[] items, boolean[] arrows, int[] colors) {
            super(context, layoutId, items);

            this.context = context;
            this.layoutId = layoutId;
            this.textViewId = textViewId;
            this.imageViewId = imageViewId;
            this.items = items;
            this.arrows = arrows;
            this.colors = colors;
        }

        public View getView(int pos, View convertView, ViewGroup parent) {
            if (inflater == null)
                inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (convertView == null)
                convertView = inflater.inflate(R.layout.list_settings, null);

            TextView label=(TextView)convertView.findViewById(textViewId);
            label.setText(items[pos]);
            label.setTextColor(colors[pos]);

            if (arrows[pos]) {
                ImageView icon=(ImageView)convertView.findViewById(imageViewId);
                icon.setImageResource(R.drawable.ic_action_chevron_right);
            }

            return convertView;
        }
    }
}
