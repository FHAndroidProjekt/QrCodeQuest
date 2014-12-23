package klu.at.qrcodequest.activities;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap.OnMyLocationChangeListener;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import klu.at.qrcodequest.AppDown;
import klu.at.qrcodequest.Data;
import klu.at.qrcodequest.EnableGPSorWLAN;
import klu.at.qrcodequest.ExpandableListViewNodes;
import klu.at.qrcodequest.HTTPHelper;
import klu.at.qrcodequest.Node;
import klu.at.qrcodequest.QuestMethods;
import klu.at.qrcodequest.R;

public class IdentificationActivity extends BaseActivity implements OnMyLocationChangeListener  {

    //Variablen
    private Context context;
    private Data data;
    private String errorString = "";
    private ExpandableListViewNodes adapter;
    private ExpandableListView list;
    private ProgressBar bar;

    //Variablen für die Kommunikation
    private Node[] nodes;
    private int userId;
    private int questId;
    private int userQuestId;
    private int dtRegistration;
    private ArrayList<Integer> nodeIds = new ArrayList<Integer>();
    private ArrayList<Integer> finishedNodeIds = new ArrayList<Integer>();

    //Variablen spezifisch für NFC
    public final String MIME_TEXT_PLAIN = "text/plain";
    private PendingIntent mPendingIntent;
    private IntentFilter[] intentFilter;
    private NfcAdapter nfcAdapter;
    private String[][] mNFCTechLists;

    //Variablen spezifisch für QR
    private Button btscan;

    //Variablen spezifisch für Google Maps
    private GoogleMap map;
    private Location location;
    private double latitude; //Breitengrad
    private double longitude; //Längengrad
    private double accuracy; //Genauigkeit
    private int finished = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initiateData();

        //Layout für NFC
        if(dtRegistration == 3){
            setContentView(R.layout.activity_nfc);
        }
        //Layout für QR
        if(dtRegistration == 2){
            setContentView(R.layout.activity_main);
            btscan = (Button)findViewById(R.id.weiter);

            //setOnClickListener für den Scannnen-Button
            btscan.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        //Es wird eine neues Intent aufgerufen (QR-Code Reader)
                        new IntentIntegrator(IdentificationActivity.this).initiateScan();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "ERROR:" + e, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        //Layout für Google Maps
        if(dtRegistration == 4){
            setContentView(R.layout.activity_google_maps);

            initialiseGoogleMaps();
        }

        createActionBar(data.getQuest().getName());

        context = this;
        AppDown.register(this); //Methode für das Beenden der Activity

        if(dtRegistration == 3 || dtRegistration == 2){
            bar = (ProgressBar) findViewById(R.id.marker_progress);
            list = (ExpandableListView) findViewById(R.id.listView1);

            list.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener(){

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
        }

        new MainNodeTask().execute();

        if(dtRegistration == 3){
            setUpNFC();
        }
    }

    private void initiateData() {

        data = (Data) getApplicationContext();

        questId = data.getQuest().getId();
        userId = data.getUser().getId();
        userQuestId = data.getUserQuestPk();
        dtRegistration = data.getQuest().getDtRegistration();
    }

    private void initialiseGoogleMaps(){
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        map.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        //Abfragen ob die Map erstellt werden konnte
        if (map == null) {
            Toast.makeText(getApplicationContext(), "Die Karte konnte nicht erstellt werden", Toast.LENGTH_LONG).show();
        } else {
            map.setMyLocationEnabled(true);
        }
        map.getUiSettings().setCompassEnabled(false);
        map.setOnMyLocationChangeListener(this);

        Location location = map.getMyLocation();

        if(location != null){
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();

            setCameraPosition(latitude, longitude);
        }
    }

    //Methoden & KLassen spezifisch für NFC
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
                        returnUnfinishedQuestions(node);
                    }
                }
            }
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

    private void setUpNFC(){
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


    //Methoden & Klassen spezifisch für QR

    public void onActivityResult(int requestCode, int resultCode, Intent intent){
        IntentResult result = IntentIntegrator.parseActivityResult (requestCode, resultCode, intent);

        if(result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                for (Node node : nodes) {
                    if (node.getRegistrationTarget1() != null && node.getRegistrationTarget1().equals(result.getContents())) {

                        returnUnfinishedQuestions(node);
                    }
                }
            }
        }else{
            // This is important, otherwise the result will not be passed to the fragment
            super.onActivityResult(requestCode, resultCode, intent);
        }
    }

    //Methoden & Klassen spezfisch für Google Maps
    @Override
    public void onMyLocationChange(Location location) {
        Toast.makeText(getApplicationContext(), "Latitude: " + location.getLatitude() + "Longitude: " + location.getLongitude() + "Genauigkeit: " + location.getAccuracy(), Toast.LENGTH_SHORT).show();

        latitude = location.getLatitude();
        longitude = location.getLongitude();
        accuracy = location.getAccuracy();

        double d;

        if(nodes != null && accuracy <=200 && finished == 1){
            for (Node node : nodes) {

                double dx = 71.5 * (latitude - Double.parseDouble(node.getRegistrationTarget1()));
                double dy = 111.3 * (longitude - Double.parseDouble(node.getRegistrationTarget2()));

                d = Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2)) * 1000;

                if (d <= 100) {

                   returnUnfinishedQuestions(node);
                }
                Toast.makeText(getApplicationContext(), "" + d, Toast.LENGTH_SHORT).show();


            }
        }
    }

    private void placeMarker(double latitude, double longitude, String title) {

        MarkerOptions marker = new MarkerOptions().position(new LatLng(latitude, longitude));
        marker.title(title);
        marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        map.addMarker(marker);
    }

    private void setCameraPosition(double latitude, double longitude){
        CameraPosition cameraPosition = new CameraPosition.Builder().target(
                new LatLng(latitude, longitude)).zoom(12).build();

        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    //Methoden & Klassen unabhängig von dtRegistration (NFC||QR||Google Maps)
    private class UserQuestNodeTask extends AsyncTask<Integer, Void, Void>{

        @Override
        protected Void doInBackground(Integer... params) {
            // TODO Auto-generated method stub

            try {
                //param[0] = userQuestPk
                //param[1] = nodePk
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

            if(dtRegistration == 3 || dtRegistration == 2){
                bar.setVisibility(View.VISIBLE);
            }

        }

        @Override
        protected Void doInBackground(Void... params) {

            try {
                nodes = QuestMethods.getNodes(questId);

                //Meldet sich der Benutzer bei einer Activity an, wird ein UserQuest-Eintrag gesetzt.
                // Daher ist jedoch die UserQuestPk, zuvor noch nicht bekannt und muss daher noch abgefragt werden.
                if(userQuestId == 0){
                    userQuestId = QuestMethods.getUserQuestPk(userId, questId);
                    data.setUserQuestPk(userQuestId);
                }

                nodeIds = QuestMethods.getFinishedNodes(userQuestId, getApplicationContext());



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
            HTTPHelper.HTTPExceptionHandler(errorString, IdentificationActivity.this);
            finishedNodeIds = getFinishedNodes(nodes);
            System.out.println("Fertige Nodes!!!!!!!!" + finishedNodeIds);

            if(dtRegistration == 2 || dtRegistration == 3){
                bar.setVisibility(View.INVISIBLE);
                adapter = new ExpandableListViewNodes(getApplicationContext(), nodes, nodeIds, finishedNodeIds);
                list.setAdapter(adapter);
            }
            if(dtRegistration == 4){
                for (Node node : nodes) {
                    double latitude = Double.parseDouble(node.getRegistrationTarget1());
                    double longitude = Double.parseDouble(node.getRegistrationTarget2());
                    String title = node.getDescription();
                    placeMarker(latitude, longitude, title);
                }
                finished = 1;
            }
        }
    }

    //Übergibt der Question-Acitivity nur jene Fragen, die noch nicht beantwortet wurden.
    //Sind alle Fragen beantwortet, wird der Benutzer durch eine Toast-Message darauf hingewiesen.
    //Existiert noch kein Eintrag (UserQuestNode), wird dieser angelegt.
    private void returnUnfinishedQuestions(Node node){

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

    //Methoden - Android Lifecycle
    @Override
    protected void onNewIntent(Intent intent) {
        // TODO Auto-generated method stub
        super.onNewIntent(intent);

        if(dtRegistration == 3){
            handleIntent(intent);
        }

    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();

        if(dtRegistration == 3 && nfcAdapter != null){
            nfcAdapter.disableForegroundDispatch(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppDown.register(this);
        if(dtRegistration == 3){
            nfcAdapter.enableForegroundDispatch(this, mPendingIntent, intentFilter, mNFCTechLists);
        }
    }


//    @Override
//       protected void onRestart(){
//        super.onRestart();
//
//        System.out.println("Die Quest sollte eigentlich jetzt beendet werden");
//        Intent restart = new Intent(this, StartActivity.class);
//        finish();
//        startActivity(restart);
//
//    }

    private ArrayList<Integer> getFinishedNodes(Node [] nodes){

        Data data = (Data)getApplicationContext();

        SparseIntArray finishedQuestions = data.getFinishedQuestions();
        ArrayList<Integer> finishedNodeIds = new ArrayList<Integer>();

        for(Node node: nodes){

            ArrayList<Integer>unfinishedQuestionsIds = new ArrayList<Integer>();

            for(int i = 0; i < node.getQuestionIDs().length; i++) {
                if (finishedQuestions.indexOfKey(node.getQuestionIDs()[i]) < 0) {
                    unfinishedQuestionsIds.add(node.getQuestionIDs()[i]);
                }
            }
            if(unfinishedQuestionsIds.size() == 0){
                finishedNodeIds.add(node.getId());
            }
            System.out.println("unfinishedQuestionsCount1" + unfinishedQuestionsIds.size());
        }
        return finishedNodeIds;

    }

}



