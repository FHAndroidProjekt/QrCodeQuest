package klu.at.qrcodequest.activities;

import java.io.IOException;
import java.io.UnsupportedEncodingException;


import java.util.ArrayList;

import klu.at.qrcodequest.*;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.ExpandableListView.OnGroupExpandListener;


public class NFCActivity extends ActionBarActivity {
	
	public final String MIME_TEXT_PLAIN = "text/plain";
	
	private Context context;
	private PendingIntent mPendingIntent;
	private IntentFilter[] intentFilter;
	private NfcAdapter nfcAdapter;
	private String[][] mNFCTechLists;
	
	private int dtRegistration = 3; //NFC_dtRegistration = 3
	private Node[] nodes;
	private Node node;
	private String errorString="";
    private int userId;
    private int questId;
	private int userQuestPk;
	Data data;
	
	private ExpandableListViewNodes adapter;
	private ExpandableListView list;
	ArrayList<Integer> nodeIds = new ArrayList<Integer>();
	
	private ProgressBar bar;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_nfc);
		
		data = (Data) getApplicationContext();

        if(savedInstanceState != null){
            questId = savedInstanceState.getInt("questId");
            userId = savedInstanceState.getInt("userId");
            userQuestPk = savedInstanceState.getInt("userQuestPk");

            int dtRegistration = savedInstanceState.getInt("dtRegistration");

            Quest quest = new Quest();
            quest.setDtRegistration(dtRegistration);
            quest.setId(questId);

            data.setUserQuestPk(userQuestPk);
            data.setQuest(quest);
            data.setUser(new User(userId));
        } else {
            questId = data.getQuest().getId();
            userId = data.getUser().getId();
            userQuestPk = data.getUserQuestPk();
        }


		AppDown.register(this); // Methode für das Beenden der Applikation
		
		list = (ExpandableListView) findViewById(R.id.listView1);
		
		bar = (ProgressBar) findViewById(R.id.marker_progress);
		new MainNodeTask().execute();
		
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
		
		context = this;
		//Laden des NFC Adapters. Wird verwendet um die Verfügbarkeit zu überprüfen
		nfcAdapter = NfcAdapter.getDefaultAdapter(this);
		
		//Abfrage, ob das Gerät NFC unterstützt
		if(nfcAdapter == null){
			Toast.makeText(getApplicationContext(), "This device doesn't support NFC", Toast.LENGTH_LONG).show();
		}else{
			//Abfrage, ob NFC eingeschaltet ist
			if(!nfcAdapter.isEnabled()){
				enableNFC(); //Starten eines Allert Dialogs, um NFC einzuschalten
			}
		}
		
		// create an intent with tag data and deliver to this activity
				mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
						getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
		
		// set an intent filter for all MIME data
				IntentFilter ndefIntent = new IntentFilter(
						NfcAdapter.ACTION_NDEF_DISCOVERED);
				try {
					ndefIntent.addDataType("*/*");
					intentFilter = new IntentFilter[] { ndefIntent };
				} catch (Exception e) {
					Log.e("TagDispatch", e.toString());
				}
				mNFCTechLists = new String[][] { new String[] { Ndef.class.getName() } };
				
	}
	
	private void enableNFC(){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setTitle("Einstellungen");
		builder.setMessage("NFC ist ausgeschaltet. Bitte gehe zu den Einstellungen.");

		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (android.os.Build.VERSION.SDK_INT >= 16) {
					Intent intent = new Intent(
							android.provider.Settings.ACTION_NFC_SETTINGS);
					context.startActivity(intent);
				} else {
					startActivity(new Intent(
							android.provider.Settings.ACTION_WIRELESS_SETTINGS));
				}
			}
		});
		builder.setNegativeButton("Abbrechen",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

					}
				});
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	
	@Override
	protected void onResume() {
		super.onResume();
		AppDown.register(this);
		nfcAdapter.enableForegroundDispatch(this, mPendingIntent, intentFilter, mNFCTechLists);
	}

	
	@Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		super.onNewIntent(intent);
		
		handleIntent(intent);
	}

	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		
		if(nfcAdapter != null){
			nfcAdapter.disableForegroundDispatch(this);
		}
	}
	
	private void handleIntent(Intent intent) {
		String action = intent.getAction();
		if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {

			String type = intent.getType();
			System.out.println("" + type);
			if (MIME_TEXT_PLAIN.equals(type)) {

				Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
				new NdefReaderTask().execute(tag);

			} else {
				Log.d("Tag", "Wrong mime type: " + type);
			}
		} else if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {

			// In case we would still use the Tech Discovered Intent
			Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
			String[] techList = tag.getTechList();
			String searchedTech = Ndef.class.getName();

			for (String tech : techList) {
				if (searchedTech.equals(tech)) {
					new NdefReaderTask().execute(tag);
					break;
				}
			}
		}
	}
	
	private class NdefReaderTask extends AsyncTask<Tag, Void, String> {

		@Override
		protected String doInBackground(Tag... params) {

			Tag tag = params[0];

			Ndef ndef = Ndef.get(tag);
			if (ndef == null) {
				// NDEF is not supported by this Tag.
				Toast.makeText(context, "Der NFC-Tag konnte leider nicht gelesenw werden", Toast.LENGTH_SHORT).show();
				return null;
			}

			NdefMessage ndefMessage = ndef.getCachedNdefMessage();

			NdefRecord[] records = ndefMessage.getRecords();

			NdefRecord ndefRecord = records[0];

			if (ndefRecord.getTnf() == NdefRecord.TNF_MIME_MEDIA
					|| ndefRecord.getTnf() == NdefRecord.TNF_WELL_KNOWN) {
				
				try {
//					System.out.println("Hier bini ich");
					
					return readText(ndefRecord);

				} catch (UnsupportedEncodingException e) {
					Log.e("Tag", "Unsupported Encoding", e);
				}
				// }
			} else {

			}

			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			if (result != null) {
				
//				System.out.println("Read content: " + result);
				
				for (final Node node : nodes) {
                    if (node.getRegistrationTarget1()!= null && node.getRegistrationTarget1().equals(result)) {

							int userQuestPk = (int)data.getUserQuestPk();
							
							boolean exist = false;
							for (int x = 0; x < nodeIds.size(); x++){
								if (node.getId() == nodeIds.get(x)){
										exist = true;
									}
								}
							if (exist == false){
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

                            System.out.println("Länge der Node Questions: " + node.getQuestionIDs().length + "Länge der unbeantworteten Fragen: " + unfinishedQuestionsIds.size());
                            if((unfinishedQuestionsIds.size()) == 0){
                                Toast.makeText(getApplicationContext(), "Sie haben bereits alle Fragen vollständig beantwortet", Toast.LENGTH_LONG).show();
                            }else{

                                data.setNode(node);
                                startActivity(questions);
                            }



                    }
			}
		}
	}
	}
	
	private class UserQuestNodeTask extends AsyncTask<Integer, Void, Void>{

		@Override
		protected Void doInBackground(Integer... params) {
			// TODO Auto-generated method stub
			
			try {
				String json = QuestMethods.setUserQuestNode(params[0], params[1]);
				
				JSONObject obj = new JSONObject(json);
				
				int userQuestNodePk = obj.getInt("id");
				
				Data data = (Data) getApplicationContext();
				
				data.setUserQuestNodePk(userQuestNodePk);
				
						
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return null;
		}
	}
	
	private class MainNodeTask extends AsyncTask<Void, Void, Void> {


        @Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			
			bar.setVisibility(View.VISIBLE);
		}

		@Override
        protected Void doInBackground(Void... params) {

            try {
                nodes = QuestMethods.getNodes(questId);

                if(userQuestPk == 0){
                    userQuestPk = QuestMethods.getUserQuestPk(userId, questId);
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
            HTTPHelper.HTTPExceptionHandler(errorString, NFCActivity.this);
            bar.setVisibility(View.INVISIBLE);

//            if(unfinishedQuestions == null){
//                Toast.makeText(getApplicationContext(),"Sie haben bereits alle Fragen beantwortet!", Toast.LENGTH_LONG).show();
//            }else{
//                adapter = new ExpandableListViewNodes(getApplicationContext(), nodes, nodeIds);
//                list.setAdapter(adapter);
//            }

        }
    }
	
	private String readText(NdefRecord record) throws UnsupportedEncodingException{
		
		//Einlesen des Payloads
		byte[]payload = record.getPayload();
		
		//Erfassen der Codierung
		String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";
		
		//byte[], Encoding - z.B. UTF-8
		return new String(payload, textEncoding);
		
	}

	@Override
    public void onBackPressed() {
        Intent intent = new Intent(this, QuestActivity.class);
        startActivity(intent);
    }
	
	public void abfrage() {
        EnableGPSorWLAN enable = new EnableGPSorWLAN(this);

        if (!enable.isGPSenabled() && enable.isWIFIEnabled()) {
            enable.enableGPS();
        }
        if (!enable.isWIFIEnabled() && enable.isGPSenabled()) {
            enable.enableNetwork();
        }
        if (!enable.isWIFIEnabled() && !enable.isGPSenabled()) {
            enable.enableAll();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putInt("userQuestPk", userQuestPk);
        savedInstanceState.putInt("questId", questId);
        savedInstanceState.putInt("userId", userId);
        savedInstanceState.putInt("dtRegistration", data.getQuest().getDtRegistration());
    }

}
