package ortigiaenterprises.platerecognizer.sqlDb;

import android.content.ContentValues;
        import android.content.Context;
        import android.database.Cursor;
        import android.database.SQLException;
        import android.database.sqlite.SQLiteDatabase;
        import android.database.sqlite.SQLiteException;
        import android.database.sqlite.SQLiteOpenHelper;
        import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
        import android.util.Log;
        import java.io.ByteArrayOutputStream;
        import java.io.FileOutputStream;
        import java.io.IOException;
        import java.io.InputStream;
        import java.io.OutputStream;
import java.util.ArrayList;

import ortigiaenterprises.platerecognizer.OcrRes;

/**
 * Created by DionisioII on 20/07/2016.
 */
public class MyDBHandler extends SQLiteOpenHelper {

    private static String DB_PATH = "";

    private static final int DATABASE_VERSION = 1;

    private static String DB_NAME = "OcrDatabase.db";

    public static SQLiteDatabase myDataBase;
    private static MyDBHandler myDBHelper = null;

    private final Context myContext;

    private static String TABLE_DATABASE = "OcrResult";
    private static String TABLE_PARAMETRI = "Parametri";
    public static final String TAG_OCR_TEXT = "ocrtext";
    public static final String TAG_OCR_BITMAP = "ocrbitmap";
    public static final String TAG_ID = "id";
    public static final String TAG_ID_PARAMETER= "id";
    public static final String TAG_OPERATOR = "operator";
    public static final String TAG_SURVEY= "survey";
    public static final String TAG_LUNGHEZZA_MIN= "Lunghezza_minima";
    public static final String TAG_LUNGHEZZA_MAX= "Lunghezza_massima";
    public static final String TAG_DATATIME = "datetime";
    public static final String TAG_LATITUDE="latitudine";
    public static final String TAG_LONGITUDE="longitudine";
    public static final String TAG_CONFIDENCE="ocrconfidence";
    /**
     * Constructor Takes and keeps a reference of the passed context in order to
     * access to the application assets and resources.
     *
     * @param context
     */
    public MyDBHandler(Context context) {

        super(context, DB_NAME, null, 1);
        this.myContext = context;
        DB_PATH = myContext.getDatabasePath(DB_NAME).getPath();
        try {
            openDataBase();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            Log.i("ERRORE","errore");
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static MyDBHandler getInstance(Context context) {
        if (myDBHelper == null) {
            myDBHelper = new MyDBHandler(context.getApplicationContext());
        }

        return myDBHelper;
    }

    /**
     * Creates a empty database on the system and rewrites it with your own
     * database.
     * */
    public void createDataBase() throws IOException {

        boolean dbExist = checkDataBase();

        if (dbExist) {
            // do nothing - database already exist
        } else {

            // By calling this method and empty database will be created into
            // the default system path
            // of your application so we are gonna be able to overwrite that
            // database with our database.
            this.getReadableDatabase();

            try {

                copyDataBase();

            } catch (IOException e) {

                throw new Error("Error copying database");

            }
        }

    }

    /**
     * Check if the database already exist to avoid re-copying the file each
     * time you open the application.
     *
     * @return true if it exists, false if it doesn't
     */
    private boolean checkDataBase() {

        SQLiteDatabase checkDB = null;

        try {
            String myPath = DB_PATH;
            checkDB = SQLiteDatabase.openDatabase(myPath, null,
                    SQLiteDatabase.OPEN_READONLY
                            | SQLiteDatabase.NO_LOCALIZED_COLLATORS);
            int DB_EXIST_VERSION = PreferenceManager
                    .getDefaultSharedPreferences(myContext).getInt(
                            "DB_VERSION", 0);
            if (DATABASE_VERSION != DB_EXIST_VERSION) {
                checkDB = null;
            }

        } catch (SQLiteException e) {

            // database does't exist yet.

        }

        if (checkDB != null) {

            checkDB.close();

        }

        return checkDB != null ? true : false;
    }

    /**
     * Copies your database from your local assets-folder to the just created
     * empty database in the system folder, from where it can be accessed and
     * handled. This is done by transfering bytestream.
     * */
    private void copyDataBase() throws IOException {

        // Open your local db as the input stream
        InputStream myInput = myContext.getAssets().open(DB_NAME);

        // Path to the just created empty db
        String outFileName = DB_PATH;

        // Open the empty db as the output stream
        OutputStream myOutput = new FileOutputStream(outFileName);

        // transfer bytes from the inputfile to the outputfile
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer)) > 0) {
            myOutput.write(buffer, 0, length);
        }

        // Close the streams
        myOutput.flush();
        myOutput.close();
        myInput.close();
        PreferenceManager.getDefaultSharedPreferences(myContext).edit()
                .putInt("DB_VERSION", DATABASE_VERSION).commit();
    }

    public void openDataBase() throws SQLException, IOException {
        createDataBase();
        // Open the database
        String myPath = DB_PATH;
        myDataBase = SQLiteDatabase.openDatabase(myPath, null,
                SQLiteDatabase.OPEN_READWRITE
                        | SQLiteDatabase.NO_LOCALIZED_COLLATORS);

    }

    @Override
    public synchronized void close() {

        if (myDataBase != null)
            myDataBase.close();
        myDataBase = null;
        myDBHelper = null;

        super.close();

    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    /*********************************************
     * Method to execute a SQL statemtn and return a custom adapter. Use only to
     * search Station Codes
     *********************************************/
    public Cursor executeSQLStatement(String SQLStatement) {

        Cursor c = null;
        try {
            if (myDataBase != null) {
                c = myDataBase.rawQuery(SQLStatement, new String[] {});
                if (c != null)
                    c.moveToFirst();

            }
        }

        catch (Exception e) {
            e.printStackTrace();

        }

        return c;
    }

    public static byte[] getBitmapAsByteArray(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
        return outputStream.toByteArray();
    }


    public void insertTodb(OcrRes result) {

        Bitmap image= result.getBp();
        String text= result.getText();
        String survey=result.getSurvey();
        String operator=result.getOperator();
        int confidence=result.getMeanConfidence();
        String datatime=result.getDateTime();
        Double latitudine=result.getLatitude();
        Double longitudine= result.getLongitude();

        byte[] data = getBitmapAsByteArray(image);

        ContentValues values = new ContentValues();

        values.put("ocrbitmap", data);
        values.put("ocrtext",text);
        values.put("survey",survey);
        values.put("operator",operator);
        values.put("datetime",datatime);
        values.put("latitudine",latitudine);
        values.put("longitudine",longitudine);
        values.put("ocrconfidence",confidence);



        SQLiteDatabase db = this.getWritableDatabase();

        db.insert(TABLE_DATABASE, null, values);
        db.close();
    }



    public ArrayList<OcrRes> getDataFromDb()
    {   SQLiteDatabase db = this.getReadableDatabase();
        //String query= "SELECT * FROM "+TABLE_DATABASE+"";
        String query= "SELECT * FROM "+TABLE_DATABASE+" ORDER BY id DESC";
        ArrayList<OcrRes> risultato=new ArrayList<OcrRes>();

        Cursor c = executeSQLStatement(query);
        if(c.moveToFirst())
        { do
        {
            OcrRes res=new OcrRes(
                    BitmapFactory.decodeByteArray(c.getBlob(2), 0, c.getBlob(2).length),
                    c.getString(1),
                    c.getDouble(6),
                    c.getDouble(7),
                    c.getInt(8),
                    c.getString(4),
                    c.getString(3),
                    c.getString(5)
            );
            risultato.add(res);


        } while(c.moveToNext());
        }
        c.close();
        db.close();
        return risultato;

    }

    public Object[] getParameters()
    {
        SQLiteDatabase db = this.getReadableDatabase();
        String query= "SELECT * FROM "+TABLE_PARAMETRI+"";
        Object[] par= new Object[4];
        Cursor c = executeSQLStatement(query);
        if(c.moveToFirst())
        { do
        {
            par[0]= c.getString(0);
            par[1] = c.getString(1);
            par[2] = c.getInt(2);
            par[3] = c.getInt(3);

        } while(c.moveToNext());

        }
        c.close();
        db.close();
        return par;

    }






  //UPDATE PARAMETRI
  public void updateParametri(String operatore,String survey,int minlength,int maxlength)
  {
      SQLiteDatabase db = this.getWritableDatabase();

      //String query = "UPDATE "+TABLE_PARAMETRI+" SET "+TAG_OPERATOR+"='"+operatore+"', "+TAG_SURVEY+"='"+survey+"', "+TAG_LUNGHEZZA_MIN+"='"+minlength+"',"+TAG_LUNGHEZZA_MAX+"='"+maxlength+"' WHERE "+TAG_ID_PARAMETER+" = '1'";
      String query = "UPDATE "+TABLE_PARAMETRI+" SET "+TAG_OPERATOR+"='"+operatore+"', "+TAG_SURVEY+"='"+survey+"', "+TAG_LUNGHEZZA_MIN+"='"+minlength+"',"+TAG_LUNGHEZZA_MAX+"='"+maxlength+"' WHERE "+TAG_ID_PARAMETER+" = '1'";
      Log.d("cazzoduro",query);
      Cursor c = executeSQLStatement(query);

      c.close();
      db.close();
  }

    public void deleteTodb(int id) {
        String where = TAG_ID+"=?";
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_DATABASE, where, null);
        db.close();

    }
    public void deleteTodb(String text) {
        String where = TAG_OCR_TEXT+"=?";
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_DATABASE,where,null);
        db.close();

    }


    public void deleteTodb(String text,String datetime) {
        String query= "DELETE  FROM "+TABLE_DATABASE+" WHERE "+TAG_OCR_TEXT+"='"+text+"' AND "+TAG_DATATIME+"='"+datetime+"'";
        //String where = TAG_OCR_TEXT+"="+text+" AND "+TAG_DATATIME+"='"+datetime+"'";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = executeSQLStatement(query);
        //db.delete(TABLE_DATABASE,where,null);
        db.close();

    }

    public OcrRes getSingleOcrRes(String text,String datetime) {
        String query= "SELECT *  FROM "+TABLE_DATABASE+" WHERE "+TAG_OCR_TEXT+"='"+text+"' AND "+TAG_DATATIME+"='"+datetime+"'";
        //String where = TAG_OCR_TEXT+"="+text+" AND "+TAG_DATATIME+"='"+datetime+"'";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = executeSQLStatement(query);
        OcrRes risultato = null;
        if(c.moveToFirst())

        {
            risultato=new OcrRes(
                    BitmapFactory.decodeByteArray(c.getBlob(2), 0, c.getBlob(2).length),
                    c.getString(1),
                    c.getDouble(6),
                    c.getDouble(7),
                    c.getInt(8),
                    c.getString(4),
                    c.getString(3),
                    c.getString(5)
            );



        }
        c.close();
        db.close();
        return risultato;

    }



}

