package klu.at.qrcodequest;

import android.app.Application;
import android.util.SparseIntArray;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class Data extends Application {
    @Expose private User user;
    private Quest quest;
    private Node node;
    private int userQuestPk;
    private int userQuestNodePk;
    @Expose private ArrayList<Node> answeredNodeList = new ArrayList<Node>();
    private ArrayList<Node> nodeList = new ArrayList<Node>();
    @Expose private SparseIntArray finishedQuestions;
    private SparseIntArray userQuestNodePKs;

    public SparseIntArray getUserQuestNodePKs() {
        return userQuestNodePKs;
    }

    public void setUserQuestNodePKs(SparseIntArray userQuestNodePKs) {
        this.userQuestNodePKs = userQuestNodePKs;
        saveData();
    }

    public User getUser() {
        isDataStillExisting();
        return user;
    }

    public void setUser(User user) {
        this.user = user;
        saveData();
    }

    public Quest getQuest() {
        isDataStillExisting();
        return quest;
    }
    public void setQuest(Quest quest) {
        this.quest = quest;
        saveData();
    }

    public ArrayList<Node> getNodeList() {
        isDataStillExisting();
        return nodeList;
    }

    public void setNodeList(ArrayList<Node> nodeList) {
        this.nodeList = nodeList;
        saveData();
    }

    public Node getNode() {
        isDataStillExisting();
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
        saveData();
    }

	public ArrayList<Node> getAnsweredNodeList() {
		return answeredNodeList;
	}

	public void setAnsweredNodeList(ArrayList<Node> answeredNodeList) {
		this.answeredNodeList = answeredNodeList;
	}

	public int getUserQuestPk() {
        isDataStillExisting();
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

    public SparseIntArray getFinishedQuestions() {
        return finishedQuestions;
    }

    public void setFinishedQuestions(SparseIntArray finishedQuestions) {
        this.finishedQuestions = finishedQuestions;
    }

    private void isDataStillExisting() {
        if (user == null) {
            Gson gson = new Gson();
            Data data = (Data) getApplicationContext();
            FileInputStream fileInputStream = null;
            try {
                fileInputStream = openFileInput("data");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            assert fileInputStream != null;
            try {
                data = gson.fromJson(String.valueOf(fileInputStream.read()), Data.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private void saveData() {
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = openFileOutput("data", MODE_PRIVATE);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            ObjectOutputStream oos = new ObjectOutputStream(openFileOutput("asd", MODE_PRIVATE)); //Select where you wish to save the file...
            oos.writeObject(this); // write the class as an 'object'
            oos.flush(); // flush the stream to insure all of the information was written to 'save_object.bin'
            oos.close();// close the stream
        } catch(Exception ex) {
            ex.printStackTrace();
        }

        assert fileOutputStream != null;
        Data data = (Data) getApplicationContext();
        System.out.println(data);
        try {
            fileOutputStream.write(gson.toJson(data).getBytes());
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
