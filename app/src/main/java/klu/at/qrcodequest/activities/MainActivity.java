package klu.at.qrcodequest.activities;

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

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import klu.at.qrcodequest.AppDown;
import klu.at.qrcodequest.Data;
import klu.at.qrcodequest.ExpandableListViewNodes;
import klu.at.qrcodequest.HTTPHelper;
import klu.at.qrcodequest.Node;
import klu.at.qrcodequest.Quest;
import klu.at.qrcodequest.QuestMethods;
import klu.at.qrcodequest.R;

public class MainActivity extends ActionBarActivity {

    private Node[] nodes;
    private Quest quest;
    private String errorString = "";
    private ProgressBar bar;
    private int userQuestPk;
    private int userPk;
    private Data data;
    private Node node;
    private ExpandableListView list;
    ArrayList<Integer> nodeIds = new ArrayList<Integer>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AppDown.register(this);

        data = (Data)getApplicationContext();
        quest = data.getQuest();
        userPk = data.getUser().getId();
        userQuestPk = data.getUserQuestPk();
        
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
                    if (node.getRegistrationTarget1() != null && node.getRegistrationTarget1().equals(result.getContents())) {

                        int userQuestPk = (int)data.getUserQuestPk();

                        boolean exist = false;

                        for(int x = 0; x < nodeIds.size(); x++){
                            if(node.getId() == nodeIds.get(x)){
                                exist = true;
                            }
                        }
                        if(!exist){
                            new UserQuestNodeTask().execute(userQuestPk, node.getId());
                        }
                        Intent questions = new Intent(getApplicationContext(), QuestionsActivity.class);

                        Data data = (Data) getApplicationContext();

                        SparseIntArray finishedQuestions = data.getFinishedQuestions();
                        ArrayList<Integer>unfinishedQuestionsIds = new ArrayList<Integer>();
                        SparseIntArray userQuestNodePks = data.getUserQuestNodePKs();
                        System.out.println("" + finishedQuestions);

                        for(int i = 0; i < node.getQuestionIDs().length; i++) {
                            if (finishedQuestions.indexOfKey(node.getQuestionIDs()[i]) < 0) {

                                unfinishedQuestionsIds.add(node.getQuestionIDs()[i]);
                                System.out.println("" + node.getQuestionIDs()[i]);
                            }else{
                                data.setUserQuestNodePk(userQuestNodePks.get(node.getQuestionIDs()[i]));

                                System.out.println("UserQuestNodePk: " +  userQuestNodePks.get(node.getQuestionIDs()[i]));
                            }
                        }

                        int [] intArray = new int[unfinishedQuestionsIds.size()];

                        for(int x = 0; x < unfinishedQuestionsIds.size(); x++){
                            intArray[x] = unfinishedQuestionsIds.get(x);
                        }

                        node.setUnfinishedQuestionIDs(intArray);

//                        System.out.println("Länge der Node Questions: " + node.getQuestionIDs().length + "Länge der unbeantworteten Fragen: " + unfinishedQuestionsIds.size());
                        if((unfinishedQuestionsIds.size()) == 0){
                            Toast.makeText(getApplicationContext(), "Sie haben bereits alle Fragen vollständig beantwortet", Toast.LENGTH_LONG).show();
                        }else{

                            data.setNode(node);
                            startActivity(questions);
                        }
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
    	
		@Override
        protected Void doInBackground(Void... params) {

            try {
                nodes = QuestMethods.getNodes(quest.getId());

                if(userQuestPk == 0){
                    userQuestPk = QuestMethods.getUserQuestPk(userPk, quest.getId());
                    System.out.println("Das ist die UserQuestPk" + userQuestPk);

                    data.setUserQuestPk(userQuestPk);
                }

                nodeIds = QuestMethods.getFinishedNodes(userQuestPk, getApplicationContext());

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

//                ExpandableListViewNodes adapter = new ExpandableListViewNodes(getApplicationContext(), nodes, nodeIds, );
//                list.setAdapter(adapter);
//

        }
    }

    private class UserQuestNodeTask extends AsyncTask<Integer, Void, Void>{

        @Override
        protected Void doInBackground(Integer... params) {

            try {
                String json = QuestMethods.setUserQuestNode(params[0], params[1]);

                JSONObject obj = new JSONObject(json);

                int userQuestNodePk = obj.getInt("id");

                Data data = (Data) getApplicationContext();

                data.setUserQuestNodePk(userQuestNodePk);


            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }

            return null;
        }

    }
}