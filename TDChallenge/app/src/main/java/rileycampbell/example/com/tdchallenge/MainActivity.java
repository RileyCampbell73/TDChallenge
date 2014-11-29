package rileycampbell.example.com.tdchallenge;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.google.gson.Gson;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class MainActivity extends Activity {
    final String API_KEY = "AIzaSyDyQ-faomCpZDP_TIMqJm0OOBfZm12vvlw";
    final String Google_Places_API_Key = "AIzaSyAsb8XP5659RBOaQSYK5e71Ta2Z0CQ_5Q4";
    ArrayList<Place> arrayList = new ArrayList<Place>();
    double latitude = 0.0;
    double longitude = 0.0;
    GetPlaces asyncTask;
    int devtappcounter = 0;
    String dev_range = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        Spinner spinner = (Spinner)findViewById(R.id.spinnerRange);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                dev_range = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        }

    public void CheckInButtonClick(View view) {

        GPSService mGPSService = new GPSService(getBaseContext());
        mGPSService.getLocation();

        if (!mGPSService.isLocationAvailable) {
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
            //Grabs all the places near user, and places them into an arraylist
            asyncTask = new GetPlaces();
            asyncTask.execute();
        }
    }

    public void TdLogoTap(View view) {
        if (devtappcounter < 2){
            devtappcounter ++;
        }
        else if (devtappcounter >= 2){
            //Show Slider
            TextView text = (TextView)findViewById(R.id.textViewRange);
            text.setVisibility(View.VISIBLE);
            Spinner SR = (Spinner)findViewById(R.id.spinnerRange);
            SR.setVisibility(View.VISIBLE);
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

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                arrayList = new ArrayList<Place>();
                //before the call to google, open a dialog that shows a loading message
                dialog = new ProgressDialog(MainActivity.this);
                dialog.setCancelable(false);
                dialog.setMessage("Loading..");
                dialog.isIndeterminate();
                dialog.show();
            }

            @Override
            protected void onPostExecute(final ArrayList<Place> result) {
                //after the call to google
                //get rid of the loading dialog
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }

                if (result.size() == 1){//ONLY 1 RESULT
                    // 1. Instantiate an AlertDialog.Builder with its constructor
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                    // 2. Chain together various setter methods to set the dialog characteristics
                    builder.setMessage("Are you currently at " + result.get(0).getName() + "? \n\nIf you select no the app will attempt to find the store your currently at again.")
                            .setTitle("Are You Here?");
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User clicked yes button
                            Intent i = new Intent(MainActivity.this ,CouponActivity.class);
                            i.putExtra("Place",new Gson().toJson(result.get(0)));
                            startActivity(i);
                        }
                    });
                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                            asyncTask = new GetPlaces();
                            asyncTask.execute();
                        }
                    });
                    // 3. Get the AlertDialog from create()
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                else if (result.size() > 1){//MORE THEN 1 RESULT
                    //make a string array of the names of the places recieved
                    String[] PlaceNames = new String[result.size()];
                    int counter = 0;
                    //populate the string array
                    for (Place s : result){
                        PlaceNames[counter] = s.getName();
                        counter ++;
                    }

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
                                    if (lw.getCheckedItemPosition() != -1){
                                        String checkedItem = lw.getAdapter().getItem(lw.getCheckedItemPosition()).toString();
                                        Toast.makeText(getBaseContext(), checkedItem, Toast.LENGTH_SHORT).show();
                                        //Dismiss once everything is OK.
                                        //LAUNCH NEW ACTIVITY WITH PLACE INFO
                                        Intent i = new Intent(MainActivity.this ,CouponActivity.class);

                                        //get the place based on the name of the place they picked
                                        for (Place p : result){
                                            if (p.getName().equals(checkedItem))
                                                //put the place object in the bundle as a JSON string
                                            i.putExtra("Place",new Gson().toJson(p));
                                        }
                                        //dismiss the dialog
                                        d.dismiss();
                                        //launch the activity
                                        startActivity(i);
                                    }
                                    else{
                                    // DO NOT CLOSE DIALOG
                                        Toast.makeText(getBaseContext(), "Please select an option.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    });
                    //show the dialog
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
                            asyncTask = new GetPlaces();
                            asyncTask.execute();
                        }
                    });
                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                            dialog.cancel();
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
                urlString.append("&location=");
                urlString.append(Double.toString(latitude));
                urlString.append(",");
                urlString.append(Double.toString(longitude));
                if (devtappcounter == 2){
                    //Toast.makeText(getBaseContext(), dev_range, Toast.LENGTH_LONG).show();
                    urlString.append("&radius=" + dev_range);//meters
                }
                urlString.append("&radius=1000");//meters
                urlString.append("&sensor=false&key=" + Google_Places_API_Key);
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
                            // 1. Instantiate an AlertDialog.Builder with its constructor
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                            // 2. Chain together various setter methods to set the dialog characteristics
                            builder.setMessage("There was an error when attempting to get any places. Please try again later.")
                                    .setTitle("There was an Error.");
                            builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // User clicked yes button
                                    asyncTask.cancel(true);
                                }
                            });
                            // 3. Get the AlertDialog from create()
                            AlertDialog dialog = builder.create();
                            dialog.show();
                        }
                    }
                } catch (JSONException ex) {
                    Toast.makeText(getBaseContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
                    // 1. Instantiate an AlertDialog.Builder with its constructor
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                    // 2. Chain together various setter methods to set the dialog characteristics
                    builder.setMessage("There was an error when attempting to get any places. Please try again later.")
                            .setTitle("There was an Error.");
                    builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User clicked yes button
                            asyncTask.cancel(true);
                        }
                    });
                    // 3. Get the AlertDialog from create()
                    AlertDialog dialog = builder.create();
                    dialog.show();
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
