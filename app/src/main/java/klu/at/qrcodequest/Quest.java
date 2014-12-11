package klu.at.qrcodequest;

public class Quest {
	
	private int id;
	private int active;					
	private int sequence;
	private int dtOwner;				//1 = FH, 2 = HTL 
	private int dtRegistration;			//1 = Nothing, 2 = QR-Code, 3 = NFC, 4 = GPS
	private String name;				//Name der Quest
	private String description;			//Questbeschreibung
	public Quest(){
	}
	public Quest(int id, int active, int sequence, int dtOwner,
			int dtRegistration, String name, String description) {
		super();
		this.id = id;
		this.active = active;
		this.sequence = sequence;
		this.dtOwner = dtOwner;
		this.dtRegistration = dtRegistration;
		this.name = name;
		this.description = description;
	}
	public Quest(String name){
		this.name = name;
	}

    public Quest(int id) {
        super();
        this.id = id;
    }

	public Quest(int id, String name, int dtRegistration) {
		super();
		this.id = id;
		this.name = name;
		this.dtRegistration = dtRegistration;
	}
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getActive() {
		return active;
	}

	public void setActive(int active) {
		this.active = active;
	}

	public int getSequence() {
		return sequence;
	}

	public void setSequence(int sequence) {
		this.sequence = sequence;
	}

	public int getDtOwner() {
		return dtOwner;
	}

	public void setDtOwner(int dtOwner) {
		this.dtOwner = dtOwner;
	}

	public int getDtRegistration() {
		return dtRegistration;
	}

	public void setDtRegistration(int dtRegistration) {
		this.dtRegistration = dtRegistration;
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

	@Override
	public String toString() {
		return "Quest [id=" + id + ", active=" + active
				+ ", sequence=" + sequence + ", dtOwner=" + dtOwner
				+ ", dtRegistration=" + dtRegistration + ", name=" + name
				+ ", description=" + description + "]";
	}
	
	
	
	
	
	

}
