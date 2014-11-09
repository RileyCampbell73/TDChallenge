package rileycampbell.example.com.tdchallenge;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
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




        }

    public void CheckInButtonClick(View view) {

        GPSService mGPSService = new GPSService(getBaseContext());
        mGPSService.getLocation();

        if (mGPSService.isLocationAvailable == false) {

            // Here you can ask the user to try again, using return; for that
            Toast.makeText(getBaseContext(), "Your location is not available, please try again.", Toast.LENGTH_SHORT).show();
            return;
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
        private ProgressDialog dialog;

        private String getJSON(String theUrl) {
            StringBuilder content = new StringBuilder();
            try {
                //making the request to google
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
            //before the call to google
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                dialog = new ProgressDialog(MainActivity.this);
                dialog.setCancelable(false);
                dialog.setMessage("Loading..");
                dialog.isIndeterminate();
                dialog.show();
            }
            //after the call to google
            @Override
            protected void onPostExecute(ArrayList<Place> result) {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                //TEST CODE for testing 1 result
                //Place temp = result.get(0);
               // result.clear();
               //result.add(temp);

                if (result.size() == 1){//ONLY 1 RESULT
                    // 1. Instantiate an AlertDialog.Builder with its constructor
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                    // 2. Chain together various setter methods to set the dialog characteristics
                    builder.setMessage("Are you currently at " + result.get(0).getName() + "? \n\nIf you select no the app will attempt to find the store your currently at again.")
                            .setTitle("Are You Here?");
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User clicked yes button
                            //TODO: LAUNCH NEW ACTIVITY WITH PLACE INFO
                        }
                    });
                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    });
                    // 3. Get the AlertDialog from create()
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                else if (result.size() > 1){//MORE THEN 1 RESULT
                    //ListView lv = (ListView)findViewById(R.id.listViewPlaces);
                    //ArrayList<String> PlaceNames = new ArrayList<String>();
                    String[] PlaceNames = new String[result.size()];
                    int counter = 0;
                    for (Place s : result){
                        PlaceNames[counter] = s.getName();
                        counter ++;
                    }
//                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
//                            MainActivity.this,
//                            android.R.layout.simple_list_item_1,
//                            PlaceNames );
//                    lv.setAdapter(arrayAdapter);
                    //INSTEAD OF PUTTING IT ON ACTIVITY. PUT IT IN DIALOG WIT THE OTHER 3 RESULT FEATURES
                    //CHANGE MAIN ACTIVITY TO JUST HAVE 1 BUTTON. THE CHECK-IN BUTTON

                    // 1. Instantiate an AlertDialog.Builder with its constructor
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                    // 2. Chain together various setter methods to set the dialog characteristics
                    builder.setTitle("Are you at any of these locations?");
                    builder.setPositiveButton("Yes", null);
                    builder.setSingleChoiceItems(PlaceNames, -1, null);
                    // 3. Get the AlertDialog from create()
                    final AlertDialog d = builder.create();
                    //set on click listener
                    d.setOnShowListener(new DialogInterface.OnShowListener() {
                        @Override
                        public void onShow(DialogInterface dialog) {
                            Button b = d.getButton(AlertDialog.BUTTON_POSITIVE);
                            b.setOnClickListener(new View.OnClickListener() {

                                @Override
                                public void onClick(View view) {
                                    ListView lw = ((AlertDialog)d).getListView();
                                    int but = lw.getCheckedItemPosition();
                                    if (lw.getCheckedItemPosition() != -1){
                                        String checkedItem = lw.getAdapter().getItem(lw.getCheckedItemPosition()).toString();
                                        Toast.makeText(getBaseContext(), checkedItem, Toast.LENGTH_SHORT).show();
                                        //Dismiss once everything is OK.
                                        d.dismiss();
                                        //TODO: LAUNCH NEW ACTIVITY WITH PLACE INFO
                                    }
                                    else{
                                    // DO NOT CLOSE DIALOG
                                        Toast.makeText(getBaseContext(), "Select something you bum", Toast.LENGTH_SHORT).show();
                                    }

                                }
                            });
                        }
                    });
                    d.show();
                }
                else{//NO RESULTS
                    // 1. Instantiate an AlertDialog.Builder with its constructor
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                    // 2. Chain together various setter methods to set the dialog characteristics
                    builder.setMessage("No store locations were found near your location. Would you like to search again?")
                           .setTitle("Where are you?");
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User clicked yes button
                            //TODO: get their latlong again and look for places again
                        }
                    });
                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                            //TODO: CLOSE DIALOG
                        }
                    });
                    // 3. Get the AlertDialog from create()
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }



            }
            //this will call google and get places at the users current latlong
            @Override
            protected ArrayList<Place> doInBackground(Void... params){
                //build the url to be sent to get results
                StringBuilder urlString = new StringBuilder(
                        "https://maps.googleapis.com/maps/api/place/search/json?");
                //may be able to limit this further. this is kinda overkill
                urlString.append("types=restaurant|cafe|bakery|bar|beauty_salon|bicycle_store|book_store|bowling_alley|clothing_store|convenience_store|" +
                        "department_store|electronics_store|pet_store|shoe_store|shopping_mall|store|furniture_store|gas_station|grocery_or_supermarket|" +
                        "hardware_store|home_goods_store|jewelry_store|liquor_store");//MAYBE REMOVE: Shopping Mall, Bar
               // urlString.append("&rankby=distance");
                urlString.append("&location=");
                urlString.append(Double.toString(latitude));
                urlString.append(",");
                urlString.append(Double.toString(longitude));
                urlString.append("&radius=325");//meters
                urlString.append("&sensor=false&key=" + "AIzaSyAsb8XP5659RBOaQSYK5e71Ta2Z0CQ_5Q4"); //THIS USES SERVER KEY
                //CANNOT HAVE 'radius' AND 'rankby' in the same statment. So will keep radius small
                //The goal it to have very few places show up.
                //if only 1 result is there then ask them if there currently their.
                //if multiple, then show list
                try {
                    //sends the request
                    String json = getJSON(urlString.toString());
                    System.out.println(json);
                    JSONObject object = new JSONObject(json);
                    JSONArray array = object.getJSONArray("results");
                    //takes the JSON array and puts into an array of Places
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
