package klu.at.qrcodequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Node {
	
	private int id;
	private int questPk;
	private boolean active;
	private int sequence;
	private int dtRegistration;
	private String registrationTarget1;
	private String registrationTarget2;
	private String name;
	private String description;
	private String location;
    private int[] questionIDs;
    private int[] unfinishedQuestionIDs;
	
	
	public Node(int questPk, boolean active, int sequence, int dtRegistration,
			String registrationTarget1, String registrationTarget2,
			String name, String description, String location) {
		super();
		this.questPk = questPk;
		this.active = active;
		this.sequence = sequence;
		this.dtRegistration = dtRegistration;
		this.registrationTarget1 = registrationTarget1;
		this.registrationTarget2 = registrationTarget2;
		this.name = name;
		this.description = description;
		this.location = location;
	}

	public Node(int id){
		this.id = id;
	}

	public Node(int id, int questPk, boolean active, int sequence, int dtRegistration, String registrationTarget1,
				String registrationTarget2, String name, String description, String location) {
		super();
		this.id = id;
		new Node(questPk, active, sequence, dtRegistration, registrationTarget1, registrationTarget2, name, description, location);
	}

    public int[] getUnfinishedQuestionIDs() {
        return unfinishedQuestionIDs;
    }

    public void setUnfinishedQuestionIDs(int[] unfinishedQuestionIDs) {
        this.unfinishedQuestionIDs = unfinishedQuestionIDs;
    }

	public String getRegistrationTarget1() {
		return registrationTarget1;
	}

	public String getRegistrationTarget2() {
		return registrationTarget2;
	}

    public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getQuestPk() {
		return questPk;
	}
	public void setQuestPk(int questPk) {
		this.questPk = questPk;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}

	@Override
	public String toString() {
		return "Node [id=" + id + ", questPk=" + questPk + ", active=" + active
				+ ", sequence=" + sequence + ", dtRegistration="
				+ dtRegistration + ", registrationTarget1="
				+ registrationTarget1 + ", registrationTarget2="
				+ registrationTarget2 + ", name=" + name + ", description="
				+ description + ", location=" + location + "]";
	}

    public int[] getQuestionIDs() {
        return questionIDs;
    }

    public void setQuestionIDs(JSONArray questionIDsJSON) throws JSONException {
        questionIDs = new int[questionIDsJSON.length()];
        for (int i = 0; i < questionIDsJSON.length(); i++) {
        	
        	JSONObject obj = questionIDsJSON.getJSONObject(i);
            
            questionIDs[i] = obj.getInt("id");
            
        }
    }
}