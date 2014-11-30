package rileycampbell.example.com.tdchallenge;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import java.io.IOException;

import com.google.gson.Gson;


public class CouponActivity extends Activity {
    private double userVal = 0.0;
    private double transactionsTotal = 0.0;
    private String couponTier = "Current Coupon Tier: Bronze";
    private String coupon = "Coupon: 15% off purchase";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_coupon);
        String jsonMyObject = "";
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            jsonMyObject = extras.getString("Place");
        }
        if ( extras.getBoolean("DevOptions")){
            Button btn = (Button)findViewById(R.id.buttonTransaction);
            btn.setVisibility(View.VISIBLE);
        }
        Place place = new Gson().fromJson(jsonMyObject, Place.class);

        TextView text = (TextView) findViewById(R.id.textViewPlaceName);
        text.setText("Currently at: " + place.getName());

        DataBaseHelper myDbHelper;
        myDbHelper = new DataBaseHelper(this);

        try {
            myDbHelper.createDataBase();
        } catch (IOException ioe) { throw new Error("Unable to create database");}

        try {
            myDbHelper.openDataBase();
            //myDbHelper.Check_In(place);
           // myDbHelper.removeeverything();
            //Cursor c = myDbHelper.getTransactions();
            //Cursor c = myDbHelper.getPlaces();
            myDbHelper.Check_In(place);
           //myDbHelper.addTransaction(15.74);
            //myDbHelper.addUserTransaction(15.74);

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
                    // sum their transactions so that we'll know what coupon tier they're in
                } while (c.moveToNext());
            }
            c = myDbHelper.getLastUserTransatPlace(place);
            if (c.moveToFirst())
            {
                do {
                    transactionsTotal += Double.valueOf(c.getString(1));
                } while (c.moveToNext());
                c.moveToLast();
                System.out.println("Last check-in's" +c.getString(0) + " " +  c.getString(1) );
                //display last check in at this location
                TextView tv = (TextView)findViewById(R.id.textViewLastCheckin);
                tv.setText("Welcome back! You were last here " + c.getString(0) + " and you spent $"+c.getString(1) + "." );
                tv.setVisibility(View.VISIBLE);
            }
            else{
                //no last check in
            }
            myDbHelper.close();
            c.close();
        }catch(SQLException sqle){
            throw sqle;
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }

        // figure out what coupon tier they're at for this location
        // Coupons are completely stubbed out, the idea is that they'll get greater benefits based on their coupon tier
        if( transactionsTotal >= 250.00 && transactionsTotal < 500.00 ) {
            couponTier = "Current Coupon Tier: Silver";
            coupon = "Coupon: 25% off purchase";
        }
        else if( transactionsTotal >= 500.00 ) {
            couponTier = "Current Coupon Tier: Gold";
            coupon = "Coupon: 35% off purchase";
        }
        TextView tvCouponTier = (TextView)findViewById(R.id.textViewCurrentCouponTier);
        tvCouponTier.setText(couponTier);

        TextView tvCoupon = (TextView)findViewById(R.id.textViewCurrentCoupon);
        tvCoupon.setText(coupon);

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
    public void OpenTextDialog(final boolean isUserTrans){
        final Context context = this;

        LayoutInflater li = LayoutInflater.from(context);
        View promptsView = li.inflate(R.layout.text_field_dialog, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                context);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final EditText userInput = (EditText) promptsView
                .findViewById(R.id.editTextDialogUserInput);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                if (!userInput.getText().toString().isEmpty()) {
                                    // get user input and send it to DB
                                    userVal = Double.parseDouble(userInput.getText().toString());

                                    DataBaseHelper myDbHelper;
                                    myDbHelper = new DataBaseHelper(CouponActivity.this);
                                    try {
                                        myDbHelper.createDataBase();
                                    } catch (IOException ioe) {
                                        throw new Error("Unable to create database");
                                    }
                                    try {
                                        myDbHelper.openDataBase();
                                        if (isUserTrans) {
                                            String result = myDbHelper.addUserTransaction(userVal);
                                            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(CouponActivity.this);
                                            alertDialogBuilder.setMessage(result);
                                            alertDialogBuilder.setPositiveButton("Got it!",
                                                    new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int arg1) {
                                                            dialog.dismiss();
                                                        }
                                                    });
                                            AlertDialog alert = alertDialogBuilder.create();
                                            alert.show();
                                            Cursor c = myDbHelper.getUserTransactions();
                                            if (c.moveToFirst()) {
                                                do {
                                                    System.out.println("User Transactions " + c.getString(0) + " " + c.getString(1) + " " + c.getString(2));
                                                } while (c.moveToNext());
                                                c.close();
                                            }
                                        } else {
                                            myDbHelper.addTransaction(userVal);
                                        }

                                        myDbHelper.close();
                                    } catch (SQLException sqle) {
                                        throw sqle;
                                    } catch (java.sql.SQLException e) {
                                        e.printStackTrace();
                                    }
                                }
                                else{
                                    Toast.makeText(getBaseContext(), "Please Enter an Amount.", Toast.LENGTH_LONG).show();
                                }
                            }

                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }
    public void OnButtonClick(View view) {

        switch (view.getId()){
            case R.id.buttonTransaction:
                userVal = 0.0;
                OpenTextDialog(false);
                break;
            case R.id.buttonUserTransaction:
                userVal = 0.0;
                OpenTextDialog(true);
                break;
            case R.id.imageViewInfo:
                final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setMessage("Coupon tiers are determined by the amount of money you've spent at your favourite shops.  The higher the coupon tier, the higher the rewards!");
                alertDialogBuilder.setPositiveButton("Got it!",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int arg1) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alert = alertDialogBuilder.create();
                alert.show();
                break;
        }

    }
}
