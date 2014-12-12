package klu.at.qrcodequest;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.nfc.NfcAdapter;
import android.os.AsyncTask;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import klu.at.qrcodequest.activities.BestlistActivity;
import klu.at.qrcodequest.activities.GoogleMapsActivity;
import klu.at.qrcodequest.activities.MainActivity;
import klu.at.qrcodequest.activities.NFCActivity;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ExpandableListAdapter extends BaseExpandableListAdapter {

	private Context context;
	private Intent intent;
	private List<String> listParents;
	private List<Quest> quests;
	private SparseIntArray userQuestMap;
	private int userPk;
    private Data data;
	
	public ExpandableListAdapter(Data data, Context context, List<String> listParents, ArrayList<Quest> quests, SparseIntArray userQuestMap, int userPk) {
		this.context = context;
		this.listParents = listParents;
		this.quests = quests;
		this.userQuestMap = userQuestMap;
		this.userPk = userPk;
        this.data = data;
	}
	
	@Override
	public int getGroupCount() {
		return this.listParents.size();
	}

	@Override
	public int getChildrenCount(int groupPosition) {
        return 1;
	}

	@Override
	public Object getGroup(int groupPosition) {
		return this.listParents.get(groupPosition);
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return 0;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {

			String text = (String) getGroup(groupPosition);
		
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.list_group, parent, false);	
			;
			if(userQuestMap.indexOfKey(quests.get((int) getGroupId(groupPosition)).getId()) > 0 ){
				convertView.setBackgroundColor(Color.parseColor("#70FF0000"));
			}
		
	
		TextView textView = (TextView) convertView.findViewById(R.id.textView1);
		textView.setText(text);
		return convertView;
	}

	@Override
	public View getChildView(final int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		
			UserHolder2 holder;

			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.list_child, parent, false);
			
			if(userQuestMap.indexOfKey(quests.get((int) getGroupId(groupPosition)).getId()) > 0){
				convertView.setBackgroundColor(Color.parseColor("#55FF0000"));
			}
			holder = new UserHolder2();
			holder.anmelden = (Button) convertView.findViewById(R.id.sign);
			holder.bestenliste = (Button)convertView.findViewById(R.id.best);
			
			if(userQuestMap.indexOfKey(quests.get((int) getGroupId(groupPosition)).getId()) > 0){
				System.out.println("" + userQuestMap.get(quests.get((int)getGroupId(groupPosition)).getId()));
				holder.anmelden.setText("Fortsetzen");
				
			}else if(userQuestMap.indexOfKey(quests.get((int) getGroupId(groupPosition)).getId()) > 0){

				holder.anmelden.setText("Anmelden");
			}
			
			convertView.setTag(holder);
		
		final int id = (int)getGroupId(groupPosition);
		String tquest = listParents.get(id);
		
		holder.anmelden.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {

				Data data = (Data) context.getApplicationContext();
				data.setQuest(quests.get((int) getGroupId(id)));

				if(quests.get((int)getGroupId(id)).getDtRegistration() == 2){
					//QR-Code
					if(userQuestMap.indexOfKey(quests.get((int) getGroupId(groupPosition)).getId()) < 0){
						new UserQuestTask().execute(groupPosition);
						
					}else{
						data.setUserQuestPk(userQuestMap.get(quests.get((int) getGroupId(groupPosition)).getId()));
					}
					intent = new Intent(context,MainActivity.class);
				}else if(quests.get((int)getGroupId(id)).getDtRegistration() == 3){
					NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(v.getContext());
					if (nfcAdapter == null) {
						Toast.makeText(v.getContext(), "Auf diesem Gerät wird leider kein NFC unterstützt.", Toast.LENGTH_LONG).show();
						return;
					}
					if(userQuestMap.indexOfKey(quests.get((int) getGroupId(groupPosition)).getId()) < 0){
						new UserQuestTask().execute(groupPosition);
					}else{
						data.setUserQuestPk(userQuestMap.get(quests.get((int) getGroupId(groupPosition)).getId()));
					}
					intent = new Intent(context,NFCActivity.class);
				}else if(quests.get((int)getGroupId(id)).getDtRegistration() == 4){
					if(userQuestMap.indexOfKey(quests.get((int) getGroupId(groupPosition)).getId()) < 0){
						new UserQuestTask().execute(groupPosition);
					}
					intent = new Intent(context, GoogleMapsActivity.class);
					
				}
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); //dadurch kann eine neue Activity außerhalb einer Activity gestartet werden
				context.startActivity(intent);
				
			}
		});
		
		holder.bestenliste.setOnClickListener(new OnClickListener() {
		
			@Override
			public void onClick(View v) {
				
				intent = new Intent(context, BestlistActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
				intent.putExtra("questPk", quests.get((int)getGroupId(id)).getId());
				context.startActivity(intent);
				
			}
		});
	
		
		return convertView;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return false;
	}
	
	static class UserHolder2{
		Button bestenliste;
		Button anmelden;
	}
	
	 private class UserQuestTask extends AsyncTask<Integer, Void, Void> {

		@Override
		protected Void doInBackground(Integer... params) {
			try {
				QuestMethods.setUserQuest(userPk, quests.get((int)getGroupId(params[0])).getId());

//                int userQuestPk = QuestMethods.getUserQuestPk(userPk, quests.get((int)getGroupId(params[0])).getId());


//                System.out.println("" + userQuestPk);
//
//                data.setUserQuestPk(userQuestPk);

			} catch (JSONException | IOException e) {
				e.printStackTrace();
			}
			return null;
		} 
}

}
