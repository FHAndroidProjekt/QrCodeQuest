package klu.at.qrcodequest.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.SparseIntArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.ProgressBar;
import android.widget.Toast;
import klu.at.qrcodequest.*;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;


import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends ActionBarActivity {

    private Node[] nodes;
    private Quest quest;
    private String errorString = "";
    private ProgressBar bar;
    private int userQuestPk;
    private int userPk;
    private Data data;
    private ExpandableListView list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AppDown.register(this);

        data = (Data)getApplicationContext();
        quest = data.getQuest();
        userPk = data.getUser().getId();
        userQuestPk = data.getUserQuestPk();
        System.out.println("asdfasfasdf" + userQuestPk);
        
        Button btscan = (Button) findViewById(R.id.weiter);
        list = (ExpandableListView) findViewById(R.id.listView1);

        bar = (ProgressBar) findViewById(R.id.marker_progress);

        list.setOnGroupExpandListener(new OnGroupExpandListener(){

			@Override
			public void onGroupExpand(int groupPosition) {
				for(int i = 0; i < nodes.length; i++){
					if(list.isGroupExpanded(i)){
						if(i != groupPosition){
							list.collapseGroup(i);
						}

					}
				}
			}

        });

        //Thread für die Abfrage der Nodes
        new MainNodeTask().execute();

        btscan.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    //Es wird eine neues Intent aufgerufen (QR-Code Reader)
                    new IntentIntegrator(MainActivity.this).initiateScan();
//                    intent.putExtra("SCAN_MODE", "QR_CODE_MODE,PRODUCT_MODE");//es zusätzliche Optionen gesetzt werden
//                    startActivityForResult(intent, 0); //Starten der Activity, die ein Ergebnis (Result) zurückliefert
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "ERROR:" + e, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //In the same activity you'll need the following to retrieve the results:
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
//        if (requestCode == 0) {        //RequestCode dient zu identifizierung der Activity, die das Ergebnis liefert
//
//            if (resultCode == RESULT_OK) {
//
//                String result = intent.getStringExtra("SCAN_RESULT");
//                System.out.println("" + result);
//
//                for (Node node : nodes) {
//                    if (node.getRegistrationTarget1().equals(result)) {
//                        Intent questions = new Intent(getApplicationContext(), QuestionsActivity.class);
//
//                        Data data = (Data) getApplicationContext();
//                        data.setNode(node);
//
//                        startActivity(questions);
//                    }
//                }
//            }
//        }

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if(result != null) {
            if(result.getContents() == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                for (Node node : nodes) {
                    if (node.getRegistrationTarget1().equals(result.getContents())) {
                        Intent questions = new Intent(getApplicationContext(), QuestionsActivity.class);

                        Data data = (Data) getApplicationContext();
                        data.setNode(node);

                        startActivity(questions);
                    }
                }
            }
        } else {
            // This is important, otherwise the result will not be passed to the fragment
            super.onActivityResult(requestCode, resultCode, intent);
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, QuestActivity.class);
        startActivity(intent);
    }

    private class MainNodeTask extends AsyncTask<Void, Void, Void> {
    	
    	ArrayList <Integer> nodeIds = new ArrayList<Integer>();
        ArrayList<Integer> unfinishedQuestions = new ArrayList<Integer>();
    	
		@Override
        protected Void doInBackground(Void... params) {

            try {
                nodes = QuestMethods.getNodes(quest.getId());

                if(userQuestPk == 0){
                    userQuestPk = QuestMethods.getUserQuestPk(userPk, quest.getId());
                }
                ArrayList<Integer> unfinishedQuestions = new ArrayList<Integer>();
                SparseIntArray finishedQuestions = data.getFinishedQuestions();
                System.out.println("" + userQuestPk);
                ArrayList <Integer> nodeIds = QuestMethods.getFinishedNodes(userQuestPk, getApplicationContext());

                for (int i = 0; i< nodeIds.size(); i++){
                    if(finishedQuestions.indexOfKey(nodeIds.get(i)) < 0){
                        unfinishedQuestions.add(nodeIds.get(i));
                    }
                }

                System.out.println("" + nodeIds);

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                if (e.getMessage().equals("falseStatusCode")) {
                    errorString = "falseStatusCode";
                } else {
                    errorString="networkError";
                }
                return null;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            HTTPHelper.HTTPExceptionHandler(errorString, MainActivity.this);

            bar.setVisibility(View.INVISIBLE);

            if(unfinishedQuestions == null){
                Toast.makeText(getApplicationContext(),"Sie haben bereits alle Fragen beantwortet!", Toast.LENGTH_LONG).show();
            }else{
                ExpandableListViewNodes adapter = new ExpandableListViewNodes(getApplicationContext(), nodes, unfinishedQuestions);
                list.setAdapter(adapter);
            }

        }
    }
}