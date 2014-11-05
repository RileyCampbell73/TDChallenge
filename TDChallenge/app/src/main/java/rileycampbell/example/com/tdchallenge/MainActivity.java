package rileycampbell.example.com.tdchallenge;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

//todo: put the places search stuff in a async class. see craigs code or example code online
public class MainActivity extends Activity {
final String API_KEY = "AIzaSyDyQ-faomCpZDP_TIMqJm0OOBfZm12vvlw";
    ArrayList<Place> arrayList = new ArrayList<Place>();
    double latitude = 0.0;
    double longitude = 0.0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GPSService mGPSService = new GPSService(getBaseContext());
        mGPSService.getLocation();

        if (mGPSService.isLocationAvailable == false) {

            // Here you can ask the user to try again, using return; for that
            Toast.makeText(getBaseContext(), "Your location is not available, please try again.", Toast.LENGTH_SHORT).show();
            return;

            // Or you can continue without getting the location, remove the return; above and uncomment the line given below
            // address = "Location not available";
        } else {

            // Getting location co-ordinates
            latitude = mGPSService.getLatitude();
            longitude = mGPSService.getLongitude();
            Toast.makeText(getBaseContext(), "Latitude:" + latitude + " | Longitude: " + longitude, Toast.LENGTH_LONG).show();

            // make sure you close the gps after using it. Save user's battery power
            mGPSService.closeGPS();
            //Grabs all the places near user
            new GetPlaces().execute();

            }
        }



    private class GetPlaces extends AsyncTask<Void, Void,  ArrayList<Place>>{

        protected String getJSON(String url) {
            return getUrlContents(url);
        }

        private String getUrlContents(String theUrl) {
            StringBuilder content = new StringBuilder();
            try {
                URL url = new URL(theUrl);
                URLConnection urlConnection = url.openConnection();
                BufferedReader bufferedReader = new BufferedReader(
                        new InputStreamReader(urlConnection.getInputStream()), 8);
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    content.append(line + "\n");
                }
                bufferedReader.close();
            }catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
            return content.toString();
        }

            @Override
            protected void onPostExecute(ArrayList<Place> result) {
                ListView lv = (ListView)findViewById(R.id.listViewPlaces);
                ArrayList<String> PlaceNames = new ArrayList<String>();
                for (Place s : result){
                    PlaceNames.add(s.getName());
                }
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                        MainActivity.this,
                        android.R.layout.simple_list_item_1,
                        PlaceNames );
                lv.setAdapter(arrayAdapter);
            }
            @Override
            protected ArrayList<Place> doInBackground(Void... params){

                StringBuilder urlString = new StringBuilder(
                        "https://maps.googleapis.com/maps/api/place/search/json?");
                urlString.append("types=restaurant|cafe|bakery|bar|beauty_salon|bicycle_store|book_store|bowling_alley|clothing_store|convenience_store|" +
                        "department_store|electronics_store|pet_store|shoe_store|shopping_mall|store|furniture_store|gas_station|grocery_or_supermarket|" +
                        "hardware_store|home_goods_store|jewelry_store|liquor_store");//MAYBE REMOVE: Shopping Mall, Bar
                urlString.append("&location=");
                urlString.append(Double.toString(latitude));
                urlString.append(",");
                urlString.append(Double.toString(longitude));
                urlString.append("&radius=1000");
                // urlString.append("&types="+place);
                urlString.append("&sensor=false&key=" + "AIzaSyAsb8XP5659RBOaQSYK5e71Ta2Z0CQ_5Q4"); //THIS USES SERVER KEY

                try {
                    String json = getJSON(urlString.toString());
                    System.out.println(json);
                    JSONObject object = new JSONObject(json);
                    JSONArray array = object.getJSONArray("results");

                    for (int i = 0; i < array.length(); i++) {

                        try {

                            Place place = Place.jsonToPontoReferencia((JSONObject) array.get(i));
                            Log.v("Places Services ", "" + place);

                            arrayList.add(place);
                        } catch (Exception e) {
                            //ERROR HANDLE IT
                        }
                    }
                } catch (JSONException ex) {
                    Toast.makeText(getBaseContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
                    //ERROR HANDLE IT
                }
                return arrayList;
            }
}
        @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
