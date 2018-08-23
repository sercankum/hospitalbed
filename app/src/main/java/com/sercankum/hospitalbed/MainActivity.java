package com.sercankum.hospitalbed;

import android.Manifest;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;



/**
 * The Controller for Hospital Bed.
 * <p>
 * As the Controller:
 * a) event handler for the View
 * b) observer of the Model (RGBAModel)
 * <p>
 * @author Sercan Kum
 * @version 1.0
 */
public class MainActivity extends AppCompatActivity implements Observer {


    // CLASS VARIABLES
    //This is required for Android 6.0 (Marshmallow)
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private static final String ABOUT_DIALOG_TAG = "About";
    private static final String LOG_TAG = "RGBA";
    private static byte driver_type = 0X0A;
    private static byte A = 0X41;
    private static byte B = 0X42;
    private static byte C = 0X43;
    private static byte D = 0X44;
    private static byte E = 0X45;
    private static byte F = 0X46;
    private static byte G = 0X47;
    private static byte H = 0X48;
    private static byte I = 0X49;
    private static byte J = 0X4A;
    private static byte K = 0X4B;
    private static byte s0 = 0X30;
    private static byte s1 = 0X31;
    private static byte s2 = 0X32;
    private static byte s3 = 0X33;
    private static byte s4 = 0X34;
    private static byte s5 = 0X35;
    private static byte s6 = 0X36;



    // INSTANCE VARIABLES
    // Pro-tip: different naming style; the 'm' means 'member'
    private SeekBar mLightSB;

    //Buttons
    private ImageButton headUp;
    private ImageButton headDown;
    private ImageButton legUp;
    private ImageButton legDown;
    private ImageButton bedUp;
    private ImageButton bedDown;
    private ImageButton openDoor;

    private final static String TAG = MainActivity.class.getSimpleName();
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private TextView mConnectionState;
    private TextView mDataField;
    private String mDeviceName;
    private String mDeviceAddress;
    private ExpandableListView mGattServicesList;
    private BluetoothLeService mBluetoothLeService;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

    private boolean mConnected = false;
    private BluetoothGattCharacteristic mNotifyCharacteristic;

    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };


    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                updateConnectionState(R.string.connected);
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                updateConnectionState(R.string.disconnected);
                invalidateOptionsMenu();

            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                //displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {

            }
        }
    };





    @TargetApi(Build.VERSION_CODES.M) // This is required for Android 6.0 (Marshmallow) to work
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //This section required for Android 6.0 (Marshmallow)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check 
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("This app needs location access ");
                builder.setMessage("Please grant location access so this app can detect devices.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    public void onDismiss(DialogInterface dialog) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                    }
                });
                builder.show();
            }

        } //End of section for Android 6.0 (Marshmallow)

        // to get the intent back from connection from DeviceScanActivity
        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);


        SharedPreferences settings = getSharedPreferences(getResources().getString(R.string.app_name), Context.MODE_PRIVATE);

        // reference each View
        mLightSB = findViewById(R.id.mLightSB);


        //All Buttons
        headUp = findViewById(R.id.headup);
        headDown = findViewById(R.id.headown);
        legUp = findViewById(R.id.legup);
        legDown = findViewById(R.id.legdown);
        bedUp = findViewById(R.id.bedup);
        bedDown = findViewById(R.id.beddown);
        openDoor = findViewById(R.id.opendoor);

        mLightSB.setProgress(0);
        // register the event handler for each <SeekBar>
        mLightSB.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            int progress = 0;
            @Override
            public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
                progress = progresValue;
             //   Toast.makeText(getApplicationContext(), "Changing seekbar's progress", Toast.LENGTH_SHORT).show();
                getProgress(progresValue);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
           //     Toast.makeText(getApplicationContext(), "Started tracking seekbar", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                getProgress(progress);
           //     Toast.makeText(getApplicationContext(), "Stopped tracking seekbar", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items

        switch (item.getItemId()) {
            case R.id.action_connect:;
                //TODO intent into a new screen
                Intent nextScreen = new Intent(getApplicationContext(), DeviceScanActivity.class);
                startActivity(nextScreen);
                return true;
        }
        return true;
    }




    public boolean getProgress(int progress){
        switch (progress){
            case 0:
                sendToBluetooth(A,driver_type);
                return true;
            case 1:
                sendToBluetooth(B,driver_type);
                return true;
            case 2:
                sendToBluetooth(C,driver_type);
                return true;
            case 3:
                sendToBluetooth(D,driver_type);
                return true;
            case 4:
                sendToBluetooth(E,driver_type);
                return true;
            case 5:
                sendToBluetooth(F,driver_type);
                return true;
            case 6:
                sendToBluetooth(G,driver_type);
                return true;
            case 7:
                sendToBluetooth(H,driver_type);
                return true;
            case 8:
                sendToBluetooth(I,driver_type);
                return true;
            case 9:
                sendToBluetooth(J,driver_type);
                return true;
            case 10:
                sendToBluetooth(K,driver_type);
                return true;
        }

        return true;
    }

    //OnClick for Preset Buttons
    public void onheadup(View view) {
      //  Toast.makeText(getApplicationContext(), "HEAD SHOULD GO UP", Toast.LENGTH_SHORT).show();
        final byte sendcommand = 0X31;
        final byte endcommand = s0;

        headUp.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        sendToBluetooth(sendcommand,driver_type);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        sendToBluetooth(endcommand,
                                driver_type);
                        break;
                }
                return false;
            }
        });
    }
    public void onheaddown(View view) {
      //  Toast.makeText(getApplicationContext(), "HEAD SHOULD GO DOWN", Toast.LENGTH_SHORT).show();
        final byte sendcommand = 0X32;
        final byte endcommand = s0;

        headDown.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        sendToBluetooth(sendcommand,driver_type);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        sendToBluetooth(endcommand,
                                driver_type);
                        break;
                }
                return false;
            }
        });
    }
    public void onlegup(View view) {

      //  Toast.makeText(getApplicationContext(), "LEG SHOULD GO UP", Toast.LENGTH_SHORT).show();
        final byte sendcommand = 0X33;
        final byte endcommand = 0x30;

        legUp.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        sendToBluetooth(sendcommand,driver_type);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        sendToBluetooth(endcommand,
                                driver_type);
                        break;
                }
                return false;
            }
        });

    }
    public void onlegdown(View view) {
        final byte sendcommand = 0X34;
        final byte endcommand = 0x30;

        legDown.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        sendToBluetooth(sendcommand,driver_type);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        sendToBluetooth(endcommand,
                                driver_type);
                        break;
                }
                return false;
            }
        });

    }
    public void onbedup( View view ){
        final byte sendcommand = 0X35;
        final byte endcommand = 0x30;

        bedUp.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        sendToBluetooth(sendcommand,driver_type);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        sendToBluetooth(endcommand,
                                driver_type);
                        break;
                }
                return false;
            }
        });
    }
    public void onbeddown( View view ){
        final byte sendcommand = 0X36;
        final byte endcommand = 0x30;

        bedDown.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        sendToBluetooth(sendcommand,driver_type);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        sendToBluetooth(endcommand,
                                driver_type);
                        break;
                }
                return false;
            }
        });
    }
    public void ondooropen(View view) {
        byte command = 0X4C;
        sendToBluetooth(command, driver_type);
    }
    @Override
    public void update(Observable observable, Object data) {

    }
    private void sendToBluetooth(byte  command, byte driver_type) {
        if(mBluetoothLeService != null) {
            mBluetoothLeService.writeCustomCharacteristic(command,driver_type);
        }
    }
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }
    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //mConnectionState.setText(resourceId);
            }
        });
    }
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }


    }
