package klu.at.qrcodequest.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import klu.at.qrcodequest.*;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

public class QuestionsActivity extends ActionBarActivity {

    private static ArrayList <Question> questions;
    private SparseArray<String> answerSparseArray = new SparseArray<>();
    private int questionNumber = 0;
    private int[] questionIDs;
    private Node node;
    private Quest quest;
    private List<Integer> randomKeys;
    int finishedRespones = 0;
    String postUrl = "http://193.171.127.102:8080/Quest/score/save.json";
    private ProgressBar bar;
    private TextView loadQuestionsTextView;
    private Data data;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questions);
        AppDown.register(this);

        data = (Data) getApplicationContext();
        node = data.getNode();
        quest = data.getQuest();
        questionIDs = node.getUnfinishedQuestionIDs();

        TextView nodeText = (TextView) findViewById(R.id.textViewNode);
        nodeText.setText(node.getName());
        // Progress Bar
        bar = (ProgressBar) findViewById(R.id.marker_progress);
        loadQuestionsTextView = (TextView) findViewById(R.id.loadQuestionsText);
        bar.setVisibility(View.VISIBLE);
        loadQuestionsTextView.setVisibility(View.VISIBLE);

        questions = new ArrayList<>();

        for (int i = 0; i < questionIDs.length; i++) {
            final String url = "http://193.171.127.102:8080/Quest/question/show/" + questionIDs[i] + ".json";
            final int finalI = i;

            // prepare the Request
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
        verticalLayout.removeAllViews(); //Removes Buttons from last Question
        Button bt;
        View.OnClickListener buttonListener;

        questionView.setText(questions.get(questionNumber).getQuestionName());

        for (int i : randomKeys) {
            String answer = answerSparseArray.get(i);

            bt = new Button(this);
            bt.setText(answer);
            bt.setTextSize(20);
            bt.setBackgroundResource(R.drawable.questionbutton);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(0, 0, 15, 3);
            bt.setLayoutParams(layoutParams);
            verticalLayout.addView(bt);

            buttonListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Button b = (Button) v;
                    if (answerSparseArray.indexOfValue(b.getText().toString()) == 0) {
                        HTTPHelper.makeJSONPost(postUrl, buildJSONObjectPost(true, questions.get(questionNumber).getId()), getApplicationContext());
                    } else {
                    	System.out.println("" + buildJSONObjectPost(true, questions.get(questionNumber).getId()));
                        HTTPHelper.makeJSONPost(postUrl, buildJSONObjectPost(false, questions.get(questionNumber).getId()), getApplicationContext());
                    }

                    if (questionNumber < questions.size()-1) {
                        questionNumber++;
                        shuffleAnswers();
                    } else {
                    	changeActivity();
                    }
                    generateNextQuestionWithAnswers();
                }
            };
            bt.setOnClickListener(buttonListener);
        }

    }

    public void shuffleAnswers() {
        answerSparseArray = questions.get(questionNumber).getAnswerSparseArray();
//        Integer[] numbers = new Integer[answerSparseArray.size()];
        randomKeys = new ArrayList<>(answerSparseArray.size()+5);
        for (int i = 0; i < answerSparseArray.size(); i++) {
//            numbers[i] = i;
            randomKeys.add(i);
        }

//        randomKeys = Arrays.asList(numbers);
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

    private void changeActivity(Class newActivity) {
        Intent nodeIntent = new Intent (getApplicationContext(), newActivity);
        nodeIntent.putExtra("finished", true);
        startActivity(nodeIntent);
    }

    private void changeActivity() {

        changeActivity(IdentificationActivity.class);
    }
}
