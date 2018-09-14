package com.heberle.roletarussa;

import android.content.Context;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private List<Person> mPersons;
    AlertDialog mAlertDialog;
    AlertDialog mSortedDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_title);
        setSupportActionBar(toolbar);

        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        mPersons = new ArrayList<Person>(0);
        mAdapter = new MyAdapter(mPersons);
        mRecyclerView.setAdapter(mAdapter);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enterNewPeople();
            }
        });

        startSensorListener();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_exit) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    //Exibe tela para troca de password
    protected void enterNewPeople() {
        try {
            LayoutInflater layoutInflater = LayoutInflater.from(MainActivity.this);
            View promptView = layoutInflater.inflate(R.layout.popup_people_layout, null);

            final TextInputLayout til_name = promptView.findViewById(R.id.til_new_name);

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setView(promptView);
            builder.setTitle(R.string.dialog_alert_password);
            builder.setCancelable(false);
            builder.setPositiveButton("Ok", null);
            builder.setNeutralButton("Sair", null);

            mAlertDialog = builder.create();
            mAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialogInterface) {
                    Button b = mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                    b.setOnClickListener(new View.OnClickListener(){
                        @Override
                        public void onClick(View view){
                            boolean isValid = true;
                            String pass1 = til_name.getEditText().getText().toString();

                            if(isValid){
                                addPeople(pass1);
                                til_name.getEditText().setText("");
                            }
                        }
                    });

                    Button f = mAlertDialog.getButton(AlertDialog.BUTTON_NEUTRAL);
                    f.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mAlertDialog.dismiss();
                        }
                    });
                }
            });

            mAlertDialog.show();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), R.string.hint_error, Toast.LENGTH_SHORT).show();
        }
    }

    public void addPeople(String name){
        Person newPerson = new Person(name);
        mPersons.add(newPerson);
        mAdapter.notifyDataSetChanged();
    }

    public void startSensorListener(){
        // ShakeDetector initialization
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new ShakeDetector();
        mShakeDetector.setOnShakeListener(new ShakeDetector.OnShakeListener() {

            @Override
            public void onShake(int count) {
                playAndSort();
            }
        });
    }

    public void playAndSort(){
        MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.shoot);
        mediaPlayer.start();

        if(mPersons.size() > 0){
            Person p = mPersons.get(new Random().nextInt(mPersons.size()));

            LayoutInflater layoutInflater = LayoutInflater.from(MainActivity.this);
            View promptView = layoutInflater.inflate(R.layout.popup_people_sorted, null);

            final TextView til_name = promptView.findViewById(R.id.popup_people_sorted);
            til_name.setText(p.getName());

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setView(promptView);
            builder.setTitle(R.string.dialog_lets_talk);
            builder.setCancelable(false);
            builder.setPositiveButton("OK", null);

            mSortedDialog = builder.create();
            mSortedDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialogInterface) {
                    Button b = mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                    b.setOnClickListener(new View.OnClickListener(){
                        @Override
                        public void onClick(View view){
                            mSortedDialog.dismiss();
                        }
                    });
                }
            });

            mSortedDialog.show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Add the following line to register the Session Manager Listener onResume
        mSensorManager.registerListener(mShakeDetector, mAccelerometer,	SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public void onPause() {
        // Add the following line to unregister the Sensor Manager onPause
        mSensorManager.unregisterListener(mShakeDetector);
        super.onPause();
    }
}
