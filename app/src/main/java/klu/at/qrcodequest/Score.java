package klu.at.qrcodequest;

public class Score {
	
	private String firstname;
	private String lastname;
	private String nickname;
	private int score;
	
	
	
	public Score(String firstname, String lastname, String nickname, int score) {
		super();
		this.firstname = firstname;
		this.lastname = lastname;
		this.nickname = nickname;
		this.score = score;
	}

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}
	
	
	
	

}
