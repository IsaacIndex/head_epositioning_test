package com.example.bluetooth2;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.View;

import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;

import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Set;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity  implements SensorEventListener{

    //sensors
    SensorManager sm;  //用來參照【感測器管理員】
    Sensor sr;          //用來參照【加速感測器物件】
    TextView txv;       //用來參照畫面中的文字元件

    //bluetooth
    Button b1,b2,b3,b4;
    private BluetoothAdapter BA;
    private Set<BluetoothDevice>pairedDevices;
    ListView lv;

    //database
    DatabaseHelper myDB;

    static int positions,sets,rounds,tempPositions,tempSets,tempRounds;
    //private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Bundle bundle = getIntent().getExtras();
        positions = bundle.getInt("positions");
        rounds = bundle.getInt("rounds");
        sets = bundle.getInt("sets");
        tempPositions = 1;
        tempRounds = 1;
        tempSets = 1;


        sm = (SensorManager) getSystemService(SENSOR_SERVICE); //由系統服務取得感測器管理員
        sr = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER); //取得加速感測器
        txv = (TextView) findViewById(R.id.textView);     // 取得TextView元件

        b1 =(Button) findViewById(R.id.button);
        b2=(Button)findViewById(R.id.button2);
        b3=(Button)findViewById(R.id.button3);
        b4=(Button)findViewById(R.id.button4);

        BA = BluetoothAdapter.getDefaultAdapter();
        lv = (ListView)findViewById(R.id.listView);
        myDB = new DatabaseHelper(this);

        boolean restart = myDB.restart();
        if(restart==false){
            Toast.makeText(getApplicationContext(), "Error: Cannot Empty Database", Toast.LENGTH_SHORT).show();
        }

        //initialize stamping
        if(positions>0){
            tempPositions+=1;
            AddData("Position 1: ");
        }
        if(rounds>0){
            tempRounds+=1;
            AddData("   Round 1: ");
        }
        if(sets>0){
            tempSets+=1;
            AddData("       Set 1: "+ getCurrentTimeUsingDate());
        }
        showList();
    }

    //sensor stuff
    @Override
    public void onSensorChanged(SensorEvent event) {
        txv.setText(String.format("X: %1.2f, Y: %1.2f, Z: %1.2f",
                event.values[0], event.values[1], event.values[2]));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        sm.registerListener(this, sr, SensorManager.SENSOR_DELAY_NORMAL); //向加速感測器 (sr) 註冊監聽物件(this)
        showList();
    }
    @Override
    protected void onPause() {
        super.onPause();
        sm.unregisterListener(this);  //取消監聽物件(this) 的註冊
    }

    public void export(View v){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            new ExportDatabaseCSVTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            new ExportDatabaseCSVTask().execute();
        }
    }


    public  void restart(View v){
        AlertDialog.Builder alertDlg = new AlertDialog.Builder(this);
        alertDlg.setMessage("Are you sure you want to restart? All data will be lost.");
        alertDlg.setCancelable(false);
        alertDlg.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
               boolean restart = myDB.restart();
               if(restart==true){
                   Intent intent = new Intent(MainActivity.this, Start.class);
                   startActivity(intent);
               }else{
                   Toast.makeText(getApplicationContext(), "Error: Cannot Restart", Toast.LENGTH_SHORT).show();
               }
            }
        });
        alertDlg.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(getApplicationContext(), "cancelled", Toast.LENGTH_SHORT).show();
            }
        });
        alertDlg.create().show();
    }



    public void add(View view) {
        String newEntry = txv.getText().toString();
        if(newEntry.length() != 0){
            AddData("           "+newEntry);
            showList();
        }else{
            Toast.makeText(getApplicationContext(), "Sensor Data is Empty", Toast.LENGTH_SHORT).show();
        }
    }

    public void AddData(String newEntry){
        boolean insertData = myDB.addData(newEntry);

        if(insertData==true){
            Toast.makeText(getApplicationContext(), newEntry, Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(getApplicationContext(), "Error: unable to upload data", Toast.LENGTH_SHORT).show();
        }
    }

    //@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:

                //Change Chapter
                if(tempSets<=sets){
                    AddData("       Set "+tempSets+": "+ getCurrentTimeUsingDate());
                    tempSets+=1;
                    showList();

                }else if(tempSets>sets && tempRounds<=rounds){
                    AddData("   Round "+tempRounds+": ");
                    tempRounds+=1;
                    tempSets = 1;
                    AddData("       Set "+tempSets+": "+ getCurrentTimeUsingDate());
                    tempSets+=1;
                    showList();

                }else if(tempSets>sets && tempRounds>rounds && tempPositions<=positions){
                    AddData("Position "+tempPositions+": ");
                    tempPositions+=1;
                    tempRounds = 1;
                    AddData("   Round "+tempRounds+": ");
                    tempRounds+=1;
                    tempSets = 1;
                    AddData("       Set "+tempSets+": "+ getCurrentTimeUsingDate());
                    tempSets+=1;
                    showList();

                }else{
                    AddData("End of Training "+ getCurrentTimeUsingDate());
                    showList();

                }

                return true;
//            case KeyEvent.KEYCODE_ENTER:
//                String newEntry = txv.getText().toString();
//                Toast.makeText(getApplicationContext(), newEntry, Toast.LENGTH_SHORT).show();
//                if(newEntry.length() != 0){
//                    AddData(newEntry);
//                    showList();
//                    return true;
//                }else{
//                    Toast.makeText(getApplicationContext(), "Sensor Data is Empty", Toast.LENGTH_SHORT).show();
//                    return true;
//                }
            default:
            Toast.makeText(getApplicationContext(), "Error: Unregistered Button Pressed",Toast.LENGTH_SHORT).show();
                break;
        }
        return false;
        //return super.onKeyDown(keyCode, event);
    }

    public void showList(){
        ArrayList<String> theList = new ArrayList<>();
        Cursor data = myDB.getListContents();

        if(data.getCount() == 0){
            //Toast.makeText(getApplicationContext(), "The Database is Empty", Toast.LENGTH_SHORT).show();
            theList.clear();
            ListAdapter listAdapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,theList);
            lv.setAdapter(listAdapter);
        }else{
            while(data.moveToNext()){
                theList.add(data.getString(1));
                ListAdapter listAdapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,theList);
                lv.setAdapter(listAdapter);
            }
        }
    }

    public void nextStep(View view) {
        if(tempSets<=sets){
            AddData("       Set "+tempSets+": "+ getCurrentTimeUsingDate());
            tempSets+=1;
            showList();

        }else if(tempSets>sets && tempRounds<=rounds){
            AddData("   Round "+tempRounds+": ");
            tempRounds+=1;
            tempSets = 1;
            AddData("       Set "+tempSets+": "+ getCurrentTimeUsingDate());
            tempSets+=1;
            showList();

        }else if(tempSets>sets && tempRounds>rounds && tempPositions<=positions){
            AddData("Position "+tempPositions+": ");
            tempPositions+=1;
            tempRounds = 1;
            AddData("   Round "+tempRounds+": ");
            tempRounds+=1;
            tempSets = 1;
            AddData("       Set "+tempSets+": "+ getCurrentTimeUsingDate());
            tempSets+=1;
            showList();

        }else{
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            AddData("End of Training "+ timestamp);
            showList();

        }
    }


    public class ExportDatabaseCSVTask extends AsyncTask<String, Void, Boolean> {

        private final ProgressDialog dialog = new ProgressDialog(MainActivity.this);
        DatabaseHelper dbhelper;
        @Override
        protected void onPreExecute() {
            this.dialog.setMessage("Exporting database...");
            this.dialog.show();
            dbhelper = new DatabaseHelper(MainActivity.this);
        }

        protected Boolean doInBackground(final String... args) {

            File exportDir = new File(Environment.getExternalStorageDirectory(), "/Head_Repositioning_Test/");
            if (!exportDir.exists()) { exportDir.mkdirs(); }

            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            File file = new File(exportDir, "Data: "+timestamp+".csv");
            try {
                file.createNewFile();
                CSVWriter csvWrite = new CSVWriter(new FileWriter(file));
                Cursor curCSV = dbhelper.raw();
                csvWrite.writeNext(curCSV.getColumnNames());
                while(curCSV.moveToNext()) {
                    String arrStr[]=null;
                    String[] mySecondStringArray = new String[curCSV.getColumnNames().length];
                    for(int i=0;i<curCSV.getColumnNames().length;i++)
                    {
                        mySecondStringArray[i] =curCSV.getString(i);
                    }
                    csvWrite.writeNext(mySecondStringArray);
                }
                csvWrite.close();
                curCSV.close();
                return true;
            } catch (IOException e) {
                return false;
            }
        }

        protected void onPostExecute(final Boolean success) {
            if (this.dialog.isShowing()) { this.dialog.dismiss(); }
            if (success) {
                Toast.makeText(MainActivity.this, "Export successful!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Export failed", Toast.LENGTH_SHORT).show();
            }
        }
    }
    public String getCurrentTimeUsingDate() {
        Date date = new Date();
        String strDateFormat = "HH:mm:ss";
        DateFormat dateFormat = new SimpleDateFormat(strDateFormat);
        String formattedDate= dateFormat.format(date);
        return formattedDate;
    }
}

