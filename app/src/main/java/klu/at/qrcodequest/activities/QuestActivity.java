package klu.at.qrcodequest.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.ProgressBar;
import klu.at.qrcodequest.*;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class QuestActivity extends ActionBarActivity /*implements OnItemClickListener*/ {

    private ExpandableListView list;
    private ProgressBar bar;
    private ArrayList<Quest> quests = new ArrayList<>();
    private int finished = 0;
    private User user;
    private int userId;
    private SparseIntArray userQuestMap = new SparseIntArray();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quest);

        Toolbar toolbar = (Toolbar) findViewById(R.id.myToolbar);
        toolbar.setTitle("Quests");
        setSupportActionBar(toolbar);

        Data data = (Data) getApplicationContext();

        if (savedInstanceState != null){
            userId = savedInstanceState.getInt("userId");
            data.setUser(new User(userId));
        }else{
            user = data.getUser();
            userId = data.getUser().getId();
        }

        AppDown.register(this);

        bar = (ProgressBar) findViewById(R.id.marker_progress);
        bar.setVisibility(View.VISIBLE);

        list = (ExpandableListView) findViewById(R.id.listView1);

        getQuests();
        
        list.setOnGroupExpandListener(new OnGroupExpandListener(){

			@Override
			public void onGroupExpand(int groupPosition) {
				for(int i = 0; i < quests.size(); i++){
					if(list.isGroupExpanded(i)){
						if(i != groupPosition){
							list.collapseGroup(i);
						}
						
					}
				}
			}
        	
        });
    }

    private void getQuests() {
        String url = "http://193.171.127.102:8080/Quest/quest.json";
        JsonArrayRequest jsObjRequest = new JsonArrayRequest(url, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject quest = response.getJSONObject(i);
                                String name = quest.getString("name");
                                int id = quest.getInt("id");
                                int dtRegistration = quest.getInt("dtRegistration");
                                Quest quest1 = new Quest(id, name, dtRegistration);
                                quests.add(quest1);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        getUserQuests();
                    }
                }, new com.android.volley.Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub
                        System.out.println(error);
                    }
                });
        VolleySingleton.getInstance(this.getApplicationContext()).addToRequestQueue(jsObjRequest);
    }

    private void getUserQuests() {

        for (final Quest quest : quests) {
            String url = ("http://193.171.127.102:8080/Quest/userQuest/get?userPk=" + userId + "&questPk=" + quest.getId());

            JsonArrayRequest jsObjRequest = new JsonArrayRequest(url, new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {
                	int id;
					try {
						if(response.length() != 0){
							for (int i = 0; i < response.length(); i++){
								id = response.getJSONObject(i).getInt("id");
								System.out.println("" + id);
								userQuestMap.put(quest.getId(), id);
							}
						}
						
						
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
//                    boolean existing = !response.toString().equals("[]"); // Wenn die Antwort [] ist -> false
                    
                    finished++;
                    if (finished == quests.size()) { // Wenn alle Requests abgearbeitet sind
                        drawList();
                    }
                }
            }, new com.android.volley.Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    // TODO Auto-generated method stub
                    System.out.println(error);
                }
            });
            VolleySingleton.getInstance(this.getApplicationContext()).addToRequestQueue(jsObjRequest);
        }
    }

    private void drawList() {
        ArrayList<String> values = new ArrayList<>();

        for (Quest quest : quests) {
            values.add(quest.getName()); //speichert die Namen der Quest in die ArrayList
        }

        ExpandableListAdapter adapter = new ExpandableListAdapter(getApplicationContext(), values, quests, userQuestMap, userId, this);
        list.setAdapter(adapter);

        bar.setVisibility(View.INVISIBLE);
    }


//    @Override
//    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//
//        String itemValue = (String) list.getItemAtPosition(position);
//
//
//			if(quests.get(position).getDtRegistration() == 2){
//
//
//        Intent qrreader = new Intent(getApplicationContext(), MainActivity.class);
//        qrreader.putExtra("questPk", quests.get(position).getId());
//        startActivity(qrreader);
//
//			}

//			Toast.makeText(getApplicationContext(), "" + position + " " + itemValue, Toast.LENGTH_LONG).show();
//    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt("userId", userId);
    }

}


