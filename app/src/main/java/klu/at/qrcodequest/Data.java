package klu.at.qrcodequest;

import android.app.Application;

import java.util.ArrayList;

public class Data extends Application {
    private User user;
    private Quest quest;
    private Node node;
    private int userQuestPk;
    private int userQuestNodePk;
    private ArrayList<Node> answeredNodeList = new ArrayList<Node>();
    private ArrayList<Node> nodeList = new ArrayList<Node>();

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Quest getQuest() {
        return quest;
    }
    public void setQuest(Quest quest) {
        this.quest = quest;
    }

    public ArrayList<Node> getNodeList() {
        return nodeList;
    }

    public void setNodeList(ArrayList<Node> nodeList) {
        this.nodeList = nodeList;
    }

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }

	public ArrayList<Node> getAnsweredNodeList() {
		return answeredNodeList;
	}

	public void setAnsweredNodeList(ArrayList<Node> answeredNodeList) {
		this.answeredNodeList = answeredNodeList;
	}

	public int getUserQuestPk() {
		return userQuestPk;
	}

	public void setUserQuestPk(int userQuestPk) {
		this.userQuestPk = userQuestPk;
	}

	public int getUserQuestNodePk() {
		return userQuestNodePk;
	}

	public void setUserQuestNodePk(int userQuestNodePk) {
		this.userQuestNodePk = userQuestNodePk;
	}
	
	
	
    
}
