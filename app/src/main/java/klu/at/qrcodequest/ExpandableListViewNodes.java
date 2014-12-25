package klu.at.qrcodequest;

import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class ExpandableListViewNodes extends BaseExpandableListAdapter{
	
	private Node[] nodes;
	private Context context;
	private ArrayList <Integer> nodeIds;
    private ArrayList<Integer> finishedNodeIds;
    private int textColor;
	
	public ExpandableListViewNodes(Context context, Node[] nodes, ArrayList<Integer>nodeIds, ArrayList<Integer>finishedNodeIds) {
		this.nodes = nodes;
		this.context = context;
		this.nodeIds = nodeIds;
        this.finishedNodeIds = finishedNodeIds;

        int[] attrs = {android.R.attr.textColorSecondary};
        SharedPreferences sharedPreferences = context.getSharedPreferences("Settings", Context.MODE_PRIVATE);
        int themeRessource = sharedPreferences.getInt("theme", R.style.AppTheme);
        @SuppressWarnings("ResourceType")
        TypedArray ta = context.obtainStyledAttributes(themeRessource, attrs);
        textColor = ta.getColor(0, Color.WHITE);
	}
	

	@Override
	public int getGroupCount() {
		return this.nodes.length;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return 1;
	}

	@Override
	public Object getGroup(int groupPosition) {
		return this.nodes[groupPosition].getName();
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return 0;
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {

		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		convertView = inflater.inflate(R.layout.list_group, parent, false);

        String nodeName = (String) getGroup(groupPosition);
        ImageView imageView = (ImageView) convertView.findViewById(R.id.imageView);
		System.out.println("" + nodeIds);
		
		for (int x = 0; x < nodeIds.size(); x++){
			if (nodes[(int)getGroupId(groupPosition)].getId() == nodeIds.get(x)){
				convertView.setBackgroundColor(Color.parseColor("#70FF0000"));
		    }
		}
        imageView.setVisibility(View.INVISIBLE);
        for(int i = 0; i< finishedNodeIds.size(); i++){
            if (nodes[(int)getGroupId(groupPosition)].getId() == finishedNodeIds.get(i)){
//                convertView.setBackgroundColor(Color.parseColor("#900000FF"));
                imageView.setVisibility(View.VISIBLE);
            }
        }
		
		TextView textView = (TextView) convertView.findViewById(R.id.textViewLocationLabel);
		textView.setText(nodeName);
        textView.setTextColor(textColor);
		
		return convertView;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		convertView = inflater.inflate(R.layout.list_child_nodes, parent, false);	
		
		String location = nodes[groupPosition].getLocation();
		String beschreibung = nodes[groupPosition].getDescription();
		
		TextView textLocation = (TextView) convertView.findViewById(R.id.textViewLocation);
		TextView textBeschreibung = (TextView) convertView.findViewById(R.id.textViewDescription);
		
		textLocation.setText(location);
		textBeschreibung.setText(beschreibung);

        TextView locactionLabel = (TextView) convertView.findViewById(R.id.textViewLocationLabel);
        TextView descriptionLabel = (TextView) convertView.findViewById(R.id.textViewDescriptionLabel);
        locactionLabel.setTextColor(textColor);
        descriptionLabel.setTextColor(textColor);
        textLocation.setTextColor(textColor);
        textBeschreibung.setTextColor(textColor);
		return convertView;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return false;
	}
	
	

}
