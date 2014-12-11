package klu.at.qrcodequest;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import klu.at.qrcodequest.activities.BestlistActivity;

import java.util.ArrayList;


public class QuestCustomAdapter extends ArrayAdapter<String>{
	
	Intent intent;
	Context context;
	int layoutResourceId;
	ArrayList<String> quests = new ArrayList<String>();
	
	public QuestCustomAdapter(Context context, int layoutResourceId, ArrayList<String> quests) {
		super(context, layoutResourceId, quests);
		this.layoutResourceId = layoutResourceId;
		this.quests = quests;
		this.context = context;
	}

    @Override
	public View getView(int position,View convertView, ViewGroup parent){
		
		UserHolder holder;
		
		if(convertView == null){
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(layoutResourceId, parent, false);
			holder = new UserHolder();
			holder.text = (TextView)convertView.findViewById(R.id.textView1);
			holder.button = (Button)convertView.findViewById(R.id.button1);
			convertView.setTag(holder);
		}else{
			holder = (UserHolder) convertView.getTag();
		}
		String tquest = quests.get(position);
		holder.text.setText(tquest);
		
		holder.button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				intent = new Intent(context,BestlistActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); //dadurch kann eine neue Activity außerhalb einer Activity gestartet werden
				context.startActivity(intent);
			}
		});
	
		
		return convertView;
	
		}
	
	static class UserHolder{
			TextView text;
			Button button;
	}
	
	
	

}
