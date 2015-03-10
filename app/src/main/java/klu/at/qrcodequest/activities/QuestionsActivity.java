package klu.at.qrcodequest.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import klu.at.qrcodequest.Data;
import klu.at.qrcodequest.HTTPHelper;
import klu.at.qrcodequest.Node;
import klu.at.qrcodequest.Question;
import klu.at.qrcodequest.R;
import klu.at.qrcodequest.VolleySingleton;

public class QuestionsActivity extends BaseActivity {

    private static ArrayList <Question> questions;
    private SparseArray<String> answerSparseArray = new SparseArray<>();
    private int questionNumber = 0;
    private int[] questionIDs;
    private Node node;
    private List<Integer> randomKeys;
    int finishedRespones = 0;
    String postUrl = "http://193.171.127.102:8080/Quest/score/save.json";
    private ProgressBar bar;
    private TextView loadQuestionsTextView;
    private Data data;
    //46.6104597 14.3046042

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questions);

        data = (Data) getApplicationContext();
        node = data.getNode();
        questionIDs = node.getUnfinishedQuestionIDs();

        createActionBar(node.getName());

        // Progress Bar
        bar = (ProgressBar) findViewById(R.id.marker_progress);
        loadQuestionsTextView = (TextView) findViewById(R.id.loadQuestionsText);
        bar.setVisibility(View.VISIBLE);
        loadQuestionsTextView.setVisibility(View.VISIBLE);

        questions = new ArrayList<>();

        for (int i = 0; i < questionIDs.length; i++) {
            final String url = "http://193.171.127.102:8080/Quest/question/show/" + questionIDs[i] + ".json";
            final int finalI = i; // Listener benötigt final bei lokalen Variablen (innere Klasse)

            JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            System.out.println(response);
                            try {
                                boolean active = response.getBoolean("active");
                                String name = response.getString("name");
                                String descr = response.getString("description");
                                String o1 = response.getString("option1");
                                String o2 = response.getString("option2");
                                String o3 = response.getString("option3");
                                String o4 = response.getString("option4");
                                String o5 = response.getString("option5");
                                String o6 = response.getString("option6");
                                String o7 = response.getString("option7");
                                String o8 = response.getString("option8");
                                String o9 = response.getString("option9");
                                String o10 = response.getString("option10");

                                Question question = new Question(questionIDs[finalI], node.getId(), active, name, descr, o1, o2, o3, o4, o5, o6, o7, o8, o9, o10);
                                questions.add(question);

                                finishedRespones++;
                                if (finishedRespones == questionIDs.length) { // Wenn alle Requests abgearbeitet sind
                                    bar.setVisibility(View.GONE);             // Evtl mit HashMap und Flag ersetzen
                                    loadQuestionsTextView.setVisibility(View.GONE);
                                    shuffleAnswers();
                                    generateNextQuestionWithAnswers();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("Error.Response", error.toString());
                        }
                    }
            );

            // add it to the RequestQueue
            VolleySingleton.getInstance(this).addToRequestQueue(getRequest);
        }

    }

    @Override
    public void onBackPressed() {

//        if (questionNumber > 0) {
//            questionNumber--;
//            shuffleAnswers();
//            generateNextQuestionWithAnswers();
//        } else {
//        	changeActivity();
//        }
        Intent intent = new Intent(this, IdentificationActivity.class);
        startActivity(intent);
    }

    public void generateNextQuestionWithAnswers() {

        TextView questionView = (TextView) findViewById(R.id.textViewQuestion);
        ViewGroup verticalLayout = (ViewGroup) findViewById(R.id.linearLayoutQuestions);
        verticalLayout.removeAllViews(); //Removes Buttons(Answers) from last Question
        Button bt;
        View.OnClickListener buttonListener;

        // Fragetext
        questionView.setText(questions.get(questionNumber).getQuestionName());

        for (int i : randomKeys) { // Alle Antwortmöglichkeiten durchgehen
            // Wählt eine zufällige Antwort aus. Doppelte Treffer sind dank
            // der randomKeys-Arraylist nicht möglich
            String answer = answerSparseArray.get(i);

            // Button wird erstellt und zum LinearLayout hinzugefügt
            bt = new Button(this);
            bt.setText(answer);
            bt.setTextSize(20);
            bt.setBackgroundResource(R.drawable.questionbutton);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(0, 0, 15, 3);
            bt.setLayoutParams(layoutParams);
            verticalLayout.addView(bt);

            buttonListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Button b = (Button) v; // View wird als Button gespeichert
                    // Durchsucht das SparseArray nach dem String des Buttons und gibt den Key zurück
                    int key = answerSparseArray.indexOfValue(b.getText().toString());
                    if (key == 0) { // Wenn Key 0 -> richtige Antwort
                        HTTPHelper.makeJSONPost(postUrl, buildJSONObjectPost(true, questions.get(questionNumber).getId()), getApplicationContext());
                    } else {
                        HTTPHelper.makeJSONPost(postUrl, buildJSONObjectPost(false, questions.get(questionNumber).getId()), getApplicationContext());
                    }

                    // Prüft, ob es noch weitere Fragen gibt
                    if (questionNumber < questions.size()-1) {
                        questionNumber++;
                        shuffleAnswers(); // Zufallszahlen für nächste Frage neu füllen
                    } else {
                    	changeActivity();
                    }
                    generateNextQuestionWithAnswers();
                }
            };
            bt.setOnClickListener(buttonListener);
        }

    }

    /**
     * Füllt eine Arraylist mit Zahlen und mischt diese durch. Die Anzahl der Zahlen entspricht der Anzahl der Antwortmöglichkeiten
     */
    public void shuffleAnswers() {
        answerSparseArray = questions.get(questionNumber).getAnswerSparseArray();
        randomKeys = new ArrayList<>();
        for (int i = 0; i < answerSparseArray.size(); i++) {
            randomKeys.add(i);
        }

        Collections.shuffle(randomKeys); //Zufällige Keys, um die Antworten zu mischen
    }

    public JSONObject buildJSONObjectPost(boolean answeredCorrect, int questionID){
        JSONObject userQuestNode = new JSONObject();
        JSONObject scoreJSONObject = new JSONObject();
        try {
            userQuestNode.put("id", data.getUserQuestNodePk()); // TODO userQuestNode

            JSONObject question = new JSONObject();
            question.put("id", questionID);

            scoreJSONObject.put("userQuestNode", userQuestNode);
            scoreJSONObject.put("question", question);
            if (answeredCorrect) {
                scoreJSONObject.put("result", 1);
                scoreJSONObject.put("score", 500);
            } else {
                scoreJSONObject.put("result", 0);
                scoreJSONObject.put("score", 0);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return scoreJSONObject;
    }

    private void changeActivity() {
        Intent nodeIntent = new Intent (getApplicationContext(), IdentificationActivity.class);
        nodeIntent.putExtra("finished", true);
        startActivity(nodeIntent);
        finish();
    }
}
