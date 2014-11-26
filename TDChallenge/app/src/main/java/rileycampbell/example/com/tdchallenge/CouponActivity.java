package rileycampbell.example.com.tdchallenge;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.IOException;
import java.util.Random;
import com.google.gson.Gson;


public class CouponActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coupon);
        String jsonMyObject = "";
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            jsonMyObject = extras.getString("Place");
        }
        Place place = new Gson().fromJson(jsonMyObject, Place.class);
        int but = 0;

//        Random random = new Random();
//        int range = (0 - 3) + 1;//0 = start, 3 = end
//        // compute a fraction of the range, 0 <= frac < range
//        int fraction = (range * random.nextInt());
//        int randomNumber =  (int)(fraction + 0);

        TextView text = (TextView) findViewById(R.id.textViewPlaceName);
        text.setText("Current Place: " + place.getName());

        DataBaseHelper myDbHelper;
        myDbHelper = new DataBaseHelper(this);

        try {
            myDbHelper.createDataBase();
        } catch (IOException ioe) { throw new Error("Unable to create database");}

        try {
            myDbHelper.openDataBase();
            //myDbHelper.Check_In(place);

            //Cursor c = myDbHelper.getTransactions();
            //Cursor c = myDbHelper.getPlaces();
            myDbHelper.Check_In(place);
            myDbHelper.addTransaction(15.74);
            myDbHelper.addUserTransaction(15.74);

            Cursor c = myDbHelper.getUserTransactions();
            if (c.moveToFirst())
            {
                do {
                    System.out.println("User Transactions " +c.getString(0) + " " + c.getString(1) + " " + c.getString(2) );
                } while (c.moveToNext());
            }
            c = myDbHelper.getPlaces();
            if (c.moveToFirst())
            {
                do {
                    System.out.println("Place " +c.getString(1)  );
                } while (c.moveToNext());
            }
            c = myDbHelper.getTransactions();
            if (c.moveToFirst())
            {
                do {
                    System.out.println("Transactions " +  c.getString(0)+" " +c.getString(1) + " " +  c.getString(2) );
                } while (c.moveToNext());
            }
            c = myDbHelper.getCheckins();
            if (c.moveToFirst())
            {
                do {
                    System.out.println("Check-In " +c.getString(1) + " " +  c.getString(2) );
                } while (c.moveToNext());
            }
            myDbHelper.removeeverything();
            myDbHelper.close();
            c.close();
        }catch(SQLException sqle){
            throw sqle;
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.coupon, menu);
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

    public void OnButtonClick(View view) {
        switch (view.getId()){
            case R.id.buttonTransaction:
                break;
            case R.id.buttonUserTransaction:
                break;

        }

    }
}
