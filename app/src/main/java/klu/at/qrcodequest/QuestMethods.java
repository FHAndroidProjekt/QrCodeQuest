package klu.at.qrcodequest;

import com.google.gson.Gson;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.content.Context;
import android.util.SparseIntArray;

import java.io.IOException;
import java.util.ArrayList;


public class QuestMethods {

	public static Node[] getNodes(int questId) throws JSONException, IOException {
		String json = HTTPHelper.makeGetRequest("http://193.171.127.102:8080/Quest/quest/show/" + questId + ".json");

		Gson gson = new Gson();
		Node[] nodes = gson.fromJson(new JSONObject(json).getJSONArray("nodes").toString(), Node[].class);

		for (Node node : nodes) {
			String json2 = HTTPHelper.makeGetRequest("http://193.171.127.102:8080/Quest/node/show/" + node.getId() + ".json");
			node.setQuestionIDs(new JSONObject(json2).getJSONArray("questions"));
			node.setQuestPk(questId);
		}

		return nodes;
	}
    
    public static ArrayList<Score> getScore(int questPk) throws IOException, JSONException{
    	
    	ArrayList<Score> scores = new ArrayList<Score>();
    	
    	String json = HTTPHelper.makeGetRequest("http://193.171.127.102:8080/Quest/userQuest/scores?questPk=" + questPk);
    	
    	JSONArray array = new JSONArray(json);
    	
    	for (int i = 0; i < array.length(); i++){
    		JSONObject scoreObj = array.getJSONObject(i);
    		JSONObject userObj = scoreObj.getJSONObject("user");
    		
    		int score = scoreObj.getInt("score");
    		String firstname = userObj.getString("firstname");
    		String lastname = userObj.getString("lastname");
    		String nickname = userObj.getString("nickname");
    		
    		
    		Score score1 = new Score(firstname, lastname, nickname, score);
    		
    		scores.add(score1);
    	}
    	
    	return scores;
    }
    
    public static void setUserQuest(int userId, int questId) throws JSONException, IOException{
    	
    	JSONObject user = new JSONObject();
    	
    	user.put("id", userId );
    	
    	JSONObject quest = new JSONObject();
    	
    	quest.put("id", questId);
    	
    	JSONObject userquest = new JSONObject();
    	
    	userquest.put("dtState", 1);
    	
    	userquest.put("user", user);
    	userquest.put("quest", quest);
    	
    	HTTPHelper.makeJSONPost("http://193.171.127.102:8080/Quest/userQuest/save.json", userquest.toString());

    }
    
    public static boolean getUserQuest(int userId, int questId) throws IOException{
    	
    	String json = HTTPHelper.makeGetRequest("http://193.171.127.102:8080/Quest/userQuest/get?userPk=" + userId + "&questPk=" + questId);

    	if(json.equals("[]")){
    		return false;
    	}else{
    		return true;
    	}
    }

    public static int getUserQuestPk (int userId, int questId) throws JSONException, IOException {
        String json = HTTPHelper.makeGetRequest("http://193.171.127.102:8080/Quest/userQuest/get?userPk=" + userId + "&questPk=" + questId);

        JSONObject obj = new JSONArray(json).getJSONObject(0);

        int userQuestPk = obj.getInt("id");

        return userQuestPk;
    }
    
    public static ArrayList<Integer> getFinishedNodes(int userQuestPk, Context context) throws IOException, JSONException{

        Data data = (Data) context.getApplicationContext();

        SparseIntArray finishedQuestions = new SparseIntArray();

        SparseIntArray userQuestNodePKs = new SparseIntArray();
 	   
    	String json = HTTPHelper.makeGetRequest("http://193.171.127.102:8080/Quest/userQuest/done?userQuestPk=" + userQuestPk);
    	
    	ArrayList<Integer> nodeIds = new ArrayList<Integer>();
    	
    	JSONArray array = new JSONArray(json);
    	
    	for (int i = 0; i < array.length(); i++){
    		
    		JSONObject obj = array.getJSONObject(i);
    		JSONObject nodeId = obj.getJSONObject("node");
            JSONArray scores = obj.getJSONArray("scores");

            for(int x = 0; x < scores.length(); x++){
                JSONObject scoreEintrag = scores.getJSONObject(x);
                int scoreId = scoreEintrag.getInt("id");
                JSONObject question = scoreEintrag.getJSONObject("question");
                int questionId = question.getInt("id");

                finishedQuestions.append(questionId, scoreId);
                userQuestNodePKs.append(questionId, obj.getInt("id"));
            }
    		
    		int userQuestNodePk = obj.getInt("id");

    		int nodePk = nodeId.getInt("id");
    		
    		nodeIds.add(nodePk);
    	}

        data.setFinishedQuestions(finishedQuestions);
        data.setUserQuestNodePKs(userQuestNodePKs);


        if(nodeIds == null){
            return null;
        }
    	return nodeIds;
    }

    public static String setUserQuestNode(int userQuestId, int nodeId) throws JSONException, IOException{

    	JSONObject userQuest = new JSONObject();
    	
    	userQuest.put("id", userQuestId);
    	
    	JSONObject node = new JSONObject();

    	
    	node.put("id", nodeId);
    	
    	JSONObject userQuestNode = new JSONObject();
    	
    	userQuestNode.put("userQuest", userQuest);
    	userQuestNode.put("node", node);
    	
    	String output = HTTPHelper.makeJSONPost("http://193.171.127.102:8080/Quest/userQuestNode/save.json", userQuestNode.toString());
    	
    	return output;
    	
    }

    public static int getEndScore (int userQuestId) throws JSONException, IOException {

        String json = HTTPHelper.makeGetRequest("http://193.171.127.102:8080/Quest/userQuest/score?userQuestPk=" + userQuestId);
        JSONObject obj = new JSONObject(json);

        int score = obj.getInt("score");

        return score;
    }
}