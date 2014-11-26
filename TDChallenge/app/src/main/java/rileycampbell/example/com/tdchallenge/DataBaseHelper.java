package rileycampbell.example.com.tdchallenge;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Riley4 on 11/25/2014.
 */
public class DataBaseHelper extends SQLiteOpenHelper {

    //The Android's default system path of your application database.
    private static String DB_PATH = "/data/data/rileycampbell.example.com.tdchallenge/databases/";

    private static String DB_NAME = "TDChallenge.db";

    private SQLiteDatabase myDataBase;

    private final Context myContext;

    /**
     * Constructor
     * Takes and keeps a reference of the passed context in order to access to the application assets and resources.
     * @param context
     */
    public DataBaseHelper(Context context) {

        super(context, DB_NAME, null, 1);
        this.myContext = context;
    }

    /**
     * Creates a empty database on the system and rewrites it with your own database.
     * */
    public void createDataBase() throws IOException {

        boolean dbExist = checkDataBase();

        if(dbExist){
            //do nothing - database already exist
        }else{

            //By calling this method and empty database will be created into the default system path
            //of your application so we are gonna be able to overwrite that database with our database.
            this.getReadableDatabase();

            try {

                copyDataBase();

            } catch (IOException e) {

                throw new Error("Error copying database");

            }
        }

    }

    /**
     * Check if the database already exist to avoid re-copying the file each time you open the application.
     * @return true if it exists, false if it doesn't
     */
    private boolean checkDataBase(){

        SQLiteDatabase checkDB = null;

        try{
            String myPath = DB_PATH + DB_NAME;
            checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);

        }catch(SQLiteException e){

            //database does't exist yet.

        }

        if(checkDB != null){

            checkDB.close();

        }

        return checkDB != null ? true : false;
    }

    /**
     * Copies your database from your local assets-folder to the just created empty database in the
     * system folder, from where it can be accessed and handled.
     * This is done by transfering bytestream.
     * */
    private void copyDataBase() throws IOException{

        //Open your local db as the input stream
        InputStream myInput = myContext.getAssets().open(DB_NAME);

        // Path to the just created empty db
        String outFileName = DB_PATH + DB_NAME;

        //Open the empty db as the output stream
        OutputStream myOutput = new FileOutputStream(outFileName);

        //transfer bytes from the inputfile to the outputfile
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer))>0){
            myOutput.write(buffer, 0, length);
        }

        //Close the streams
        myOutput.flush();
        myOutput.close();
        myInput.close();

    }

    public void openDataBase() throws SQLException {

        //Open the database
        String myPath = DB_PATH + DB_NAME;
        myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
    }
    public void removeeverything(){

        Cursor f = myDataBase.rawQuery("DELETE FROM Check_In; ",null);
        myDataBase.delete("Check_In",null,null);
        f = myDataBase.rawQuery("DELETE FROM UserTransactions; ",null);
        myDataBase.delete("UserTransactions",null,null);
        f = myDataBase.rawQuery("DELETE FROM Place; ",null);
        myDataBase.delete("Place",null,null);
        f = myDataBase.rawQuery("DELETE FROM Transactions; ",null);
        myDataBase.delete("Transactions",null,null);
    }
    public Cursor getPlaces(){
        return myDataBase.rawQuery("SELECT * FROM Place; ",null);
    }
    public Cursor getCheckins(){
        return myDataBase.rawQuery("SELECT * FROM Check_In; ",null);
    }
    public Cursor getTransactions(){
        return myDataBase.rawQuery("SELECT * FROM Transactions; ",null);
    }
    public Cursor getUserTransactions(){
        return myDataBase.rawQuery("SELECT * FROM UserTransactions; ",null);
    }
    public void addTransaction(double moneyspent){
        ContentValues values = new ContentValues();
        values = new ContentValues();
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        values.put("DateTime", dateFormat.format(date).toString());//2014/08/06 15:59:48
        values.put("spent", moneyspent);
        myDataBase.insert("Transactions",null,values);
    }
    public Cursor getLastUserTransatPlace(Place place){
        Cursor c =  myDataBase.rawQuery("Select t.DateTime, t.spent from Transactions t " +
                                        "LEFT JOIN UserTransactions u ON t.transID = u.transID " +
                                        "LEFT JOIN Place p ON u.placeID = p.placeID "+
                                        "WHERE p.GPLaceID = ?",new String[]{place.getId()});
        return c;
    }
    public void addUserTransaction(double userSpent){
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        int usertime = (Integer.parseInt(dateFormat.format(date).toString().substring(11, 13)) * 60) + Integer.parseInt(dateFormat.format(date).toString().substring(14, 16));
        //System.out.println("Usertime " + usertime);
        Cursor c =  myDataBase.rawQuery("Select * from Transactions Where instr (DateTime, ?) > 0; ",new String[]{dateFormat.format(date).toString().substring(0, 10)});

        if (c.moveToFirst())
        {
            do {
               //System.out.println("UserTransactions --- Transactions:"+c.getString(1));
                String time = c.getString(1).substring(11, c.getString(1).length());
                int transminutes =  (Integer.parseInt(time.substring(0, 2)) * 60) + Integer.parseInt(time.substring(3, 5));
                //System.out.println("transtime " + transminutes);
                //check to see if its in an appropriate time frame and see if the price sent up matches that record
                if (usertime <= (transminutes + 60) && usertime >= transminutes){
                    if (userSpent == c.getDouble(2)){
                        //add the user transaction
                        ContentValues values = new ContentValues();
                        values = new ContentValues();
                        Cursor check =  myDataBase.rawQuery("Select * from Check_In",null);
                        check.moveToLast();
                        values.put("transID", c.getInt(0));
                        values.put("placeID", check.getInt(2));
                        myDataBase.insert("UserTransactions",null,values);
                    }
                    else{
                        //Price doesnt match any transaction in the last hour
                    }
                }else{
                    //That transaction was not in the last hour
                }
            } while (c.moveToNext());
        }
        else{
            //NO TRANSACTIONS
            System.out.println("No Transactions today");
        }
    }
    public void Check_In(Place place){
        //check for place
            //if it exists, grab its id
            //if not, add new place, grab its id
       Cursor c =  myDataBase.rawQuery("Select placeID from Place Where GPlaceID = ?",new String[]{place.getId()});
        ContentValues values = new ContentValues();
        if (!(c.moveToFirst()) || c.getCount() ==0) {
            //insert new place into places table
            values.put("Name", place.getName());
            values.put("Lat", place.getLatitude());
            values.put("Long", place.getLongitude());
            values.put("GPlaceID", place.getId());
            long newPlace = myDataBase.insert("Place",null,values);
            //insert new check_in
            values = new ContentValues();
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date date = new Date();
            values.put("DateTime", dateFormat.format(date).toString());//2014/08/06 15:59:48
            values.put("placeID", newPlace);
            myDataBase.insert("Check_In",null,values);
        }else{
        //add new check in link
            values = new ContentValues();
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date date = new Date();
            values.put("DateTime", dateFormat.format(date).toString());//2014/08/06 15:59:48
            c.moveToFirst();
            values.put("placeID", c.getString(0));
            myDataBase.insert("Check_In",null,values);
        }
        c.close();

    }
    @Override
    public synchronized void close() {

        if(myDataBase != null)
            myDataBase.close();

        super.close();

    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    // Add your public helper methods to access and get content from the database.
    // You could return cursors by doing "return myDataBase.query(....)" so it'd be easy
    // to you to create adapters for your views.

}