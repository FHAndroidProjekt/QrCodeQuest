package klu.at.qrcodequest.activities;

import java.io.IOException;
import java.util.ArrayList;

import klu.at.qrcodequest.*;
import org.json.JSONException;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMyLocationChangeListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.Toast;

public class GoogleMapsActivity extends ActionBarActivity implements OnMyLocationChangeListener {

	private GoogleMap map;
    private int userId;
    private int questId;
    private int userQuestPk;
    Data data;
	private int dtRegistration = 4;
	private int userPk;
	private Node[] nodes;
	private Context context;
	private String errorString = "";
	
	private Location location;
	private double latitude;
	private double longitude;
	private double accuracy;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_google_maps);

		AppDown.register(this);

		data = (Data) getApplicationContext();
		questId = data.getQuest().getId();
		userPk = data.getUser().getId();
        userQuestPk = data.getUserQuestPk();
		
		//GoogleMaps
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        
      //Abfragen ob die Map erstellt werden konnte
        if (map == null) {
            Toast.makeText(getApplicationContext(), "Die Karte konnte nicht erstellt werden", Toast.LENGTH_LONG).show();
        } else {
            map.setMyLocationEnabled(true);
            
            abfrage();
        }
        map.getUiSettings().setCompassEnabled(false);
        map.setOnMyLocationChangeListener(this);
//        System.out.println("" + map.getMyLocation().getAccuracy());
        
        
        Location location = map.getMyLocation();
        
        if(location != null){
        	double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            
            setCameraPosition(latitude, longitude);
        }
        
        new MainNodeTask().execute();
	}
	
	@Override
    public void onBackPressed() {
        Intent intent = new Intent(this, QuestActivity.class);
        startActivity(intent);
    }
	
	public void abfrage() {
        EnableGPSorWLAN enable = new EnableGPSorWLAN(this);

        if (!enable.isGPSenabled() && enable.isWIFIEnabled()) {
            enable.enableGPS();
        }
        if (!enable.isWIFIEnabled() && enable.isGPSenabled()) {
            enable.enableNetwork();
        }
        if (!enable.isWIFIEnabled() && !enable.isGPSenabled()) {
            enable.enableAll();
        }
    }
	
	private void placeMarker(double latitude, double longitude, String title) {

        MarkerOptions marker = new MarkerOptions().position(new LatLng(latitude, longitude));
        marker.title(title);
        marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        map.addMarker(marker);
    }
	
	private void setCameraPosition(double latitude, double longitude){
		CameraPosition cameraPosition = new CameraPosition.Builder().target(
                new LatLng(latitude, longitude)).zoom(12).build();
 
		map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
	}

	@Override
	public void onMyLocationChange(Location location) {
		Toast.makeText(getApplicationContext(), "Latitude: " + location.getLatitude() + "Longitude: " + location.getLongitude() + "Genauigkeit: " + location.getAccuracy(), Toast.LENGTH_SHORT).show();
		
		latitude = location.getLatitude();
		longitude = location.getLongitude();
		accuracy = location.getAccuracy();
		
		double d;
		
		if(nodes != null && accuracy <=200){
			for (Node node : nodes) {

				double dx = 71.5 * (latitude - Double.parseDouble(node.getRegistrationTarget1()));
				double dy = 111.3 * (longitude - Double.parseDouble(node.getRegistrationTarget2()));

				d = Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2)) * 1000;

				if (d <= 100) {
					Intent questions = new Intent(getApplicationContext(), QuestionsActivity.class);
					data.setNode(node);
					startActivity(questions);
				}
				Toast.makeText(getApplicationContext(), "" + d, Toast.LENGTH_SHORT).show();


			}
		}
	}
	
	private class MainNodeTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
            try {
                nodes = QuestMethods.getNodes(questId);


            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                if (e.getMessage().equals("falseStatusCode")) {
                    errorString = "falseStatusCode";
                } else {
                    errorString="networkError";
                }
                return null;
            }
            
            
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);

			for (Node node : nodes) {
				double latitude = Double.parseDouble(node.getRegistrationTarget1());
				double longitude = Double.parseDouble(node.getRegistrationTarget2());
				String title = node.getDescription();
				placeMarker(latitude, longitude, title);
			}
		}
		
		
	}
}
