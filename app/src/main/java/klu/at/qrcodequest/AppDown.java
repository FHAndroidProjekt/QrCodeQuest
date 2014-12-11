package klu.at.qrcodequest;

import java.util.ArrayList;

import android.app.Activity;

public class AppDown {
	
	private static final ArrayList<Activity> activities = new ArrayList<Activity>();
	
	public static void register(Activity activity){
		
		activities.add(activity);
		
	}
	
	public static void allDown(){
        for (Activity activity : activities) {
            activity.finish();
        }
	}

}

