package klu.at.qrcodequest;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

public class ExpandableListViewNodes extends BaseExpandableListAdapter{
	
	private Node[] nodes;
	private Context context;
	private ArrayList <Integer> nodeIds;
	
	public ExpandableListViewNodes(Context context, Node[] nodes, ArrayList<Integer>nodeIds) {
		this.nodes = nodes;
		this.context = context;
		this.nodeIds = nodeIds;
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

		String nodeName = (String) getGroup(groupPosition);
		
	
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		convertView = inflater.inflate(R.layout.list_group, parent, false);	
		
		System.out.println("" + nodeIds);
		
		for (int x = 0; x < nodeIds.size(); x++){
			if (nodes[(int)getGroupId(groupPosition)].getId() == nodeIds.get(x)){
				convertView.setBackgroundColor(Color.parseColor("#70FF0000"));
		}
			
		}
		
		TextView textView = (TextView) convertView.findViewById(R.id.textView1);
		textView.setText(nodeName);
		
		return convertView;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		convertView = inflater.inflate(R.layout.list_child_nodes, parent, false);	
		
		String location = nodes[groupPosition].getLocation();
		String beschreibung = nodes[groupPosition].getDescription();
		
		TextView textLocation = (TextView) convertView.findViewById(R.id.textView3);
		TextView textBeschreibung = (TextView) convertView.findViewById(R.id.textView4);
		
		textLocation.setText(location);
		textBeschreibung.setText(beschreibung);
		return convertView;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return false;
	}
	
	

}
