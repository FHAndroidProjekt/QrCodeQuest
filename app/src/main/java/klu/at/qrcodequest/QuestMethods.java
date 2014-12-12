package klu.at.qrcodequest;

import com.google.gson.Gson;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;


public class QuestMethods {

	public static Node[] getNodes(int questPk) throws JSONException, IOException {
		String json = HTTPHelper.makeGetRequest("http://193.171.127.102:8080/Quest/quest/show/" + questPk + ".json");

		Gson gson = new Gson();
		Node[] nodes = gson.fromJson(new JSONObject(json).getJSONArray("nodes").toString(), Node[].class);

		for (Node node : nodes) {
			String json2 = HTTPHelper.makeGetRequest("http://193.171.127.102:8080/Quest/node/show/" + node.getId() + ".json");
			node.setQuestionIDs(new JSONObject(json2).getJSONArray("questions"));
			node.setQuestPk(questPk);
			System.out.println(node);
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
    	
//    	System.out.println("" + userquest);
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

        System.out.println("" + json);
        JSONObject obj = new JSONArray(json).getJSONObject(0);

        int userQuestPk = obj.getInt("id");

        return userQuestPk;
    }
    
    public static ArrayList<Integer> getFinishedNodes(int userQuestPk) throws IOException, JSONException{
 	   
    	String json = HTTPHelper.makeGetRequest("http://193.171.127.102:8080/Quest/userQuest/done?userQuestPk=" + userQuestPk);
    	
    	ArrayList<Integer> nodeIds = new ArrayList<Integer>();
    	
    	JSONArray array = new JSONArray(json);
    	
    	for (int i = 0; i < array.length(); i++){
    		
    		JSONObject obj = array.getJSONObject(i);
    		JSONObject nodeId = obj.getJSONObject("node");
    		
    		int userQuestNodePk = obj.getInt("id");
    		
    		int nodePk = nodeId.getInt("id");
    		
    		nodeIds.add(nodePk);
    	}

        if(nodeIds == null){
            return null;
        }
    	return nodeIds;
    }
     
    
    public static String setUserQuestNode(int userquestId, int nodeId) throws JSONException, IOException{
    	
    	JSONObject userquest = new JSONObject();
    	
    	userquest.put("id", userquestId);
    	
    	System.out.println("" + userquest.toString());
    	
    	JSONObject node = new JSONObject();
    	
    	node.put("id", nodeId);
    	
    	System.out.println("" + node.toString());
    	
    	JSONObject userquestnode = new JSONObject();
    	
    	userquestnode.put("userQuest", userquest);
    	userquestnode.put("node", node);
    	
    	System.out.println("" + userquestnode.toString());
    	
    	String output = HTTPHelper.makeJSONPost("http://193.171.127.102:8080/Quest/userQuestNode/save.json", userquestnode.toString());
    	
    	return output;
    	
    }
}