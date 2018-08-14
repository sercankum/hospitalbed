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
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

/**
 * The Controller for RGBAModel.
 * <p>
 * As the Controller:
 * a) event handler for the View
 * b) observer of the Model (RGBAModel)
 * <p>
 * Features the Update / React Strategy.
 *
 * @author Sercan Kum
 * @version 1.0
 */
public class MainActivity extends AppCompatActivity implements Observer
        , SeekBar.OnSeekBarChangeListener {


    // CLASS VARIABLES
    //This is required for Android 6.0 (Marshmallow)
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private static final String ABOUT_DIALOG_TAG = "About";
    private static final String LOG_TAG = "RGBA";
    private static String BUTTON_1 = "button1";
    private static String BUTTON_2 = "button2";
    private static String BUTTON_3 = "button3";
    private static String BUTTON_4 = "button4";
    private static String BUTTON_5 = "button5";
    private static String BUTTON_6 = "button6";
    private static String SCENE_1 = "scene1";
    private static String SCENE_2 = "scene2";
    private static String SCENE_3 = "scene3";
    private static String SCENE_4 = "scene4";
    private static String SCENE_5 = "scene5";
    private static String SCENE_6 = "scene6";
    private int white = Color.WHITE;
    private int speedDef = 5;
    private Boolean isPlayed = false;
    private static int alpha = 255;

    // INSTANCE VARIABLES
    // Pro-tip: different naming style; the 'm' means 'member'

    private AboutDialogFragment mAboutDialog;
    private TextView mColorSwatch;
    private RGBAModel mModel;
    private SeekBar mRedSB;
    private SeekBar mGreenSB;
    private SeekBar mBlueSB;
    private SeekBar mSpeedSB;
    private SeekBar alphaSB;


    // TextViewers;
    private TextView mRedTV;
    private TextView mGreenTV;
    private TextView mBlueTV;
    private TextView alphaTV;
    private TextView mSpeedTV;
    private Button button_1, button_2, button_3, button_4, button_5, button_6, scene_1, scene_2, scene_3, scene_4, scene_5, scene_6;
    private ImageButton scenePlay;


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


    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;
    Handler sceneHandler = new Handler();


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




        ActionBar actionBar = getSupportActionBar();
        actionBar.setLogo(R.drawable.logoheader);
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setTitle("");

        SharedPreferences settings = getSharedPreferences(getResources().getString(R.string.app_name), Context.MODE_PRIVATE);

        // Instantiate a new AboutDialogFragment()
        mAboutDialog = new AboutDialogFragment();

        // Instantiate a new RGBA model
        // Initialize the model red (max), green (min), blue (min), and alpha (max)
        mModel = new RGBAModel(settings.getInt("red", 0),
                settings.getInt("green", 0),
                settings.getInt("blue", 0),
                settings.getInt("alpha", 0),
                settings.getInt("speed", 0)
        );
        // The Model is observing this Controller (class MainActivity implements Observer)
        mModel.addObserver(this);

        // reference each View
        mColorSwatch = (TextView) findViewById(R.id.colorSwatch);
        mRedSB = (SeekBar) findViewById(R.id.redSB);
        mGreenSB = (SeekBar) findViewById(R.id.greenSB);
        mBlueSB = (SeekBar) findViewById(R.id.blueSB);
        mSpeedSB = (SeekBar) findViewById(R.id.speedSB);

        // reference for all of the textViews
        mRedTV = (TextView) findViewById(R.id.redTV);
        mBlueTV = (TextView) findViewById(R.id.blueTV);
        mGreenTV = (TextView) findViewById(R.id.greenTV);
        mSpeedTV = (TextView) findViewById(R.id.speedTV);

        //All Buttons
        button_1 = (Button) findViewById(R.id.button_1);
        button_2 = (Button) findViewById(R.id.button_2);
        button_3 = (Button) findViewById(R.id.button_3);
        button_4 = (Button) findViewById(R.id.button_4);
        button_5 = (Button) findViewById(R.id.button_5);
        button_6 = (Button) findViewById(R.id.button_6);
        scene_1 = (Button) findViewById(R.id.scene_1);
        scene_2 = (Button) findViewById(R.id.scene_2);
        scene_3 = (Button) findViewById(R.id.scene_3);
        scene_4 = (Button) findViewById(R.id.scene_4);
        scene_5 = (Button) findViewById(R.id.scene_5);
        scene_6 = (Button) findViewById(R.id.scene_6);
        scenePlay = (ImageButton) findViewById(R.id.imageButton);


        scenePlay.setBackgroundResource(R.drawable.ic_play_circle_filled_black_24dp);


        // register the event handler for each <SeekBar>
        mRedSB.setOnSeekBarChangeListener(this);
        mGreenSB.setOnSeekBarChangeListener(this);
        mBlueSB.setOnSeekBarChangeListener(this);
        mSpeedSB.setOnSeekBarChangeListener(this);

        // initialize the View to the values of the Model
        this.updateView();




        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items

        switch (item.getItemId()) {
            case R.id.action_connect:
                //TODO intent into a new screen
                Intent nextScreen = new Intent(getApplicationContext(), DeviceScanActivity.class);
                startActivity(nextScreen);
                return true;
            case R.id.action_about:
                mAboutDialog.show(getFragmentManager(), ABOUT_DIALOG_TAG);
                return true;
            case R.id.action_red:
                mModel.asRed();
                return true;
            case R.id.action_green:
                mModel.asGreen();
                return true;
            case R.id.action_blue:
                mModel.asBlue();
                return true;
            case R.id.action_black:
                mModel.asBlack();
                return true;
            case R.id.action_cyan:
                mModel.asCyan();
                return true;
            case R.id.action_magenta:
                mModel.asMagenta();
                return true;
            case R.id.action_white:
                mModel.asWhite();
                return true;
            case R.id.action_yellow:
                mModel.asYellow();
                return true;
            case R.id.action_driver_1:
                Toast.makeText(this,"DIM 0% Selected",Toast.LENGTH_LONG).show();
                storeIntToPref("Driver_Type",1);
                return true;

            case R.id.action_driver_2:
                Toast.makeText(this,"DIM 50% Selected",Toast.LENGTH_LONG).show();
                storeIntToPref("Driver_Type",2);
                return true;

            case R.id.action_driver_3:
                Toast.makeText(this,"DIM 75% Selected",Toast.LENGTH_LONG).show();
                storeIntToPref("Driver_Type",3);
            default:
                Toast.makeText(this, "MenuItem: " + item.getTitle(), Toast.LENGTH_LONG).show();
                return super.onOptionsItemSelected(item);
        }
    }





    /**
     * Event handler for the <SeekBar>s: red, green, blue, and alpha.
     */
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {


        // Determine which <SeekBark> caused the event (switch + case)
        // GET the SeekBar's progress, and SET the model to it's new value

        switch (seekBar.getId()) {
            case R.id.redSB:
                mModel.setRed(mRedSB.getProgress());
                mRedTV.setText(getResources().getString(R.string.redProgress, progress));

        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // No-Operation
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    //OnClick for Preset Buttons
    public void onSave1(View view) {
        Integer color = Color.argb(alpha
                , mModel.getRed()
                , mModel.getGreen()
                , mModel.getBlue());

        button_1.setBackgroundColor(color);
        int A1 = ((ColorDrawable) button_1.getBackground()).getColor();
        if (getIntFromPref(BUTTON_1) != 0 && A1 != getIntFromPref(BUTTON_1)) {
            setPreDefinedPreset(1, true);
        } else {
            storeIntToPref(BUTTON_1, Color.argb(alpha
                    , mModel.getRed()
                    , mModel.getGreen()
                    , mModel.getBlue()));
            storeIntToPref("BUTTON_1_A", alpha);
            storeIntToPref("BUTTON_1_R", mModel.getRed());
            storeIntToPref("BUTTON_1_G", mModel.getGreen());
            storeIntToPref("BUTTON_1_B", mModel.getBlue());
        }
        button_1.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                button_1.setBackgroundColor(white);
                deleteSharePref(BUTTON_1);
                deleteSharePref("BUTTON_1_A");
                deleteSharePref("BUTTON_1_R");
                deleteSharePref("BUTTON_1_G");
                deleteSharePref("BUTTON_1_B");
                Toast.makeText(getApplicationContext(), "Preset 1 Deleted", Toast.LENGTH_SHORT).show();
                mSpeedSB.setProgress(5);
                mSpeedTV.setText("5 secs");
                return true;
            }

        });
    }
    public void onSave2(View view) {
        Integer color = Color.argb(alpha
                , mModel.getRed()
                , mModel.getGreen()
                , mModel.getBlue());

        button_2.setBackgroundColor(color);

        int A1 = ((ColorDrawable) button_2.getBackground()).getColor();
        if (getIntFromPref(BUTTON_2) != 0 && A1 != getIntFromPref(BUTTON_2)) {
            setPreDefinedPreset(2, true);
        } else {
            storeIntToPref(BUTTON_2, Color.argb(alpha
                    , mModel.getRed()
                    , mModel.getGreen()
                    , mModel.getBlue()));
            storeIntToPref("BUTTON_2_A", alpha);
            storeIntToPref("BUTTON_2_R", mModel.getRed());
            storeIntToPref("BUTTON_2_G", mModel.getGreen());
            storeIntToPref("BUTTON_2_B", mModel.getBlue());
        }
        button_2.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                button_2.setBackgroundColor(white);
                deleteSharePref(BUTTON_2);
                deleteSharePref("BUTTON_2_A");
                deleteSharePref("BUTTON_2_R");
                deleteSharePref("BUTTON_2_G");
                deleteSharePref("BUTTON_2_B");
                Toast.makeText(getApplicationContext(), "Preset 2 Deleted", Toast.LENGTH_SHORT).show();
                mSpeedSB.setProgress(5);
                mSpeedTV.setText("5 secs");
                return true;
            }
        });
    }
    public void onSave3(View view) {
        Integer color = Color.argb(alpha
                , mModel.getRed()
                , mModel.getGreen()
                , mModel.getBlue());

        button_3.setBackgroundColor(color);

        int A1 = ((ColorDrawable) button_3.getBackground()).getColor();
        if (getIntFromPref(BUTTON_3) != 0 && A1 != getIntFromPref(BUTTON_3)) {

            setPreDefinedPreset(3, true);
        } else {
            storeIntToPref(BUTTON_3, Color.argb(alpha
                    , mModel.getRed()
                    , mModel.getGreen()
                    , mModel.getBlue()));
            storeIntToPref("BUTTON_3_A", alpha);
            storeIntToPref("BUTTON_3_R", mModel.getRed());
            storeIntToPref("BUTTON_3_G", mModel.getGreen());
            storeIntToPref("BUTTON_3_B", mModel.getBlue());
        }
        button_3.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                button_3.setBackgroundColor(white);
                deleteSharePref(BUTTON_3);
                deleteSharePref("BUTTON_3_A");
                deleteSharePref("BUTTON_3_R");
                deleteSharePref("BUTTON_3_G");
                deleteSharePref("BUTTON_3_B");
                Toast.makeText(getApplicationContext(), "Preset 3 Deleted", Toast.LENGTH_SHORT).show();
                mSpeedSB.setProgress(5);
                mSpeedTV.setText("5 secs");
                return true;
            }
        });
    }
    public void onSave4(View view) {
        Integer color = Color.argb(alpha
                , mModel.getRed()
                , mModel.getGreen()
                , mModel.getBlue());

        button_4.setBackgroundColor(color);

        int A1 = ((ColorDrawable) button_4.getBackground()).getColor();
        if (getIntFromPref(BUTTON_4) != 0 && A1 != getIntFromPref(BUTTON_4)) {
            setPreDefinedPreset(4, true);
        } else {
            storeIntToPref(BUTTON_4, Color.argb(alpha
                    , mModel.getRed()
                    , mModel.getGreen()
                    , mModel.getBlue()));
            storeIntToPref("BUTTON_4_A", alpha);
            storeIntToPref("BUTTON_4_R", mModel.getRed());
            storeIntToPref("BUTTON_4_G", mModel.getGreen());
            storeIntToPref("BUTTON_4_B", mModel.getBlue());
        }
        button_4.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                button_4.setBackgroundColor(white);
                deleteSharePref(BUTTON_4);
                deleteSharePref("BUTTON_4_A");
                deleteSharePref("BUTTON_4_R");
                deleteSharePref("BUTTON_4_G");
                deleteSharePref("BUTTON_4_B");
                Toast.makeText(getApplicationContext(), "Preset 4 Deleted", Toast.LENGTH_SHORT).show();
                mSpeedSB.setProgress(5);
                mSpeedTV.setText("5 secs");
                return true;
            }
        });
    }
    public void onSave5(View view) {
        Integer color = Color.argb(alpha
                , mModel.getRed()
                , mModel.getGreen()
                , mModel.getBlue());

        button_5.setBackgroundColor(color);

        int A1 = ((ColorDrawable) button_5.getBackground()).getColor();
        if (getIntFromPref(BUTTON_5) != 0 && A1 != getIntFromPref(BUTTON_5)) {
            setPreDefinedPreset(5, true);
        } else {
            storeIntToPref(BUTTON_5, Color.argb(alpha
                    , mModel.getRed()
                    , mModel.getGreen()
                    , mModel.getBlue()));
            storeIntToPref("BUTTON_5_A", alpha);
            storeIntToPref("BUTTON_5_R", mModel.getRed());
            storeIntToPref("BUTTON_5_G", mModel.getGreen());
            storeIntToPref("BUTTON_5_B", mModel.getBlue());
        }
        button_5.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                button_5.setBackgroundColor(white);
                deleteSharePref(BUTTON_5);
                deleteSharePref("BUTTON_5_A");
                deleteSharePref("BUTTON_5_R");
                deleteSharePref("BUTTON_5_G");
                deleteSharePref("BUTTON_5_B");
                Toast.makeText(getApplicationContext(), "Preset 5 Deleted", Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }
    public void onSave6(View view) {
        Integer color = Color.argb(alpha
                , mModel.getRed()
                , mModel.getGreen()
                , mModel.getBlue());

        button_6.setBackgroundColor(color);

        int A1 = ((ColorDrawable) button_6.getBackground()).getColor();
        if (getIntFromPref(BUTTON_6) != 0 && A1 != getIntFromPref(BUTTON_6)) {
            setPreDefinedPreset(6, true);
        } else {
            storeIntToPref(BUTTON_6, Color.argb(alpha
                    , mModel.getRed()
                    , mModel.getGreen()
                    , mModel.getBlue()));
            storeIntToPref("BUTTON_6_A", alpha);
            storeIntToPref("BUTTON_6_R", mModel.getRed());
            storeIntToPref("BUTTON_6_G", mModel.getGreen());
            storeIntToPref("BUTTON_6_B", mModel.getBlue());
        }
        button_6.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                button_6.setBackgroundColor(white);
                deleteSharePref(BUTTON_6);
                deleteSharePref("BUTTON_6_A");
                deleteSharePref("BUTTON_6_R");
                deleteSharePref("BUTTON_6_G");
                deleteSharePref("BUTTON_6_B");
                Toast.makeText(getApplicationContext(), "Preset 6 Deleted", Toast.LENGTH_SHORT).show();
                mSpeedSB.setProgress(5);
                mSpeedTV.setText("5 secs");
                return true;
            }
        });
    }

    //OnClick for Scene Buttons
    public void onScene1(View view) {

        scene_1.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                Toast.makeText(getApplicationContext(), "I have been let go", Toast.LENGTH_SHORT).show();
                return true;
            }

        });
    }
    public void onScene2(View view) {
        Integer color = Color.argb(alpha
                , mModel.getRed()
                , mModel.getGreen()
                , mModel.getBlue());
        Integer speed = mModel.getSpeed();
        scene_2.setBackgroundColor(color);
        int A1 = ((ColorDrawable) scene_2.getBackground()).getColor();
        if (getIntFromPref(SCENE_2) != 0 && A1 != getIntFromPref(SCENE_2)) {
            setPreDefinedPreset(8, true);
            Log.d(LOG_TAG, String.valueOf(mModel.getSpeed()));
        } else {
            storeIntToPref(SCENE_2, Color.argb(alpha
                    , mModel.getRed()
                    , mModel.getGreen()
                    , mModel.getBlue()));
            storeIntToPref("SCENE_2_A", alpha);
            storeIntToPref("SCENE_2_R", mModel.getRed());
            storeIntToPref("SCENE_2_G", mModel.getGreen());
            storeIntToPref("SCENE_2_B", mModel.getBlue());
            storeIntToPref("SCENE_2_S", speed);
        }
        scene_2.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                scene_2.setBackgroundColor(white);
                deleteSharePref(SCENE_2);
                deleteSharePref("SCENE_2_A");
                deleteSharePref("SCENE_2_R");
                deleteSharePref("SCENE_2_G");
                deleteSharePref("SCENE_2_B");
                deleteSharePref("SCENE_2_S");
                Toast.makeText(getApplicationContext(), "Scene Colour 2 Deleted", Toast.LENGTH_SHORT).show();
                mSpeedSB.setProgress(5);
                mSpeedTV.setText("5 secs");
                return true;
            }

        });
    }
    public void onScene3(View view) {
        Integer color = Color.argb(alpha
                , mModel.getRed()
                , mModel.getGreen()
                , mModel.getBlue());
        Integer speed = mModel.getSpeed();
        scene_3.setBackgroundColor(color);
        int A1 = ((ColorDrawable) scene_3.getBackground()).getColor();
        if (getIntFromPref(SCENE_3) != 0 && A1 != getIntFromPref(SCENE_3)) {
            setPreDefinedPreset(9, true); //TODO:Change the setDefinedPreset to 9,true
            Log.d(LOG_TAG, mModel.toString());
        } else {
            storeIntToPref(SCENE_3, Color.argb(alpha
                    , mModel.getRed()
                    , mModel.getGreen()
                    , mModel.getBlue()));
            storeIntToPref("SCENE_3_A", alpha);
            storeIntToPref("SCENE_3_R", mModel.getRed());
            storeIntToPref("SCENE_3_G", mModel.getGreen());
            storeIntToPref("SCENE_3_B", mModel.getBlue());
            storeIntToPref("SCENE_3_S", speed);
        }
        scene_3.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                scene_3.setBackgroundColor(white);
                deleteSharePref(SCENE_3);
                deleteSharePref("SCENE_3_A");
                deleteSharePref("SCENE_3_R");
                deleteSharePref("SCENE_3_G");
                deleteSharePref("SCENE_3_B");
                deleteSharePref("SCENE_3_S");
                Toast.makeText(getApplicationContext(), "Scene Colour 3 Deleted", Toast.LENGTH_SHORT).show();
                mSpeedSB.setProgress(5);
                mSpeedTV.setText("5 secs");
                return true;
            }

        });
    }
    public void onScene4(View view) {
        Integer color = Color.argb(alpha
                , mModel.getRed()
                , mModel.getGreen()
                , mModel.getBlue());
        Integer speed = mModel.getSpeed();
        scene_4.setBackgroundColor(color);
        int A1 = ((ColorDrawable) scene_4.getBackground()).getColor();
        if (getIntFromPref(SCENE_4) != 0 && A1 != getIntFromPref(SCENE_4)) {
            setPreDefinedPreset(10, true);
            Log.d(LOG_TAG, mModel.toString());
        } else {
            storeIntToPref(SCENE_4, Color.argb(alpha
                    , mModel.getRed()
                    , mModel.getGreen()
                    , mModel.getBlue()));
            storeIntToPref("SCENE_4_A", alpha);
            storeIntToPref("SCENE_4_R", mModel.getRed());
            storeIntToPref("SCENE_4_G", mModel.getGreen());
            storeIntToPref("SCENE_4_B", mModel.getBlue());
            storeIntToPref("SCENE_4_S", speed);
        }
        scene_4.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                scene_4.setBackgroundColor(white);
                deleteSharePref(SCENE_4);
                deleteSharePref("SCENE_4_A");
                deleteSharePref("SCENE_4_R");
                deleteSharePref("SCENE_4_G");
                deleteSharePref("SCENE_4_B");
                deleteSharePref("SCENE_4_S");
                Toast.makeText(getApplicationContext(), "Scene Colour 4 Deleted", Toast.LENGTH_SHORT).show();
                mSpeedSB.setProgress(5);
                mSpeedTV.setText("5 secs");
                return true;
            }

        });
    }
    public void onScene5(View view) {
        Integer color = Color.argb(alpha
                , mModel.getRed()
                , mModel.getGreen()
                , mModel.getBlue());
        Integer speed = mModel.getSpeed();
        scene_5.setBackgroundColor(color);
        int A1 = ((ColorDrawable) scene_5.getBackground()).getColor();
        if (getIntFromPref(SCENE_5) != 0 && A1 != getIntFromPref(SCENE_5)) {
            setPreDefinedPreset(11, true); //TODO:Change the setDefinedPreset to 11,true
            Log.d(LOG_TAG, mModel.toString());
        } else {
            storeIntToPref(SCENE_5, Color.argb(alpha
                    , mModel.getRed()
                    , mModel.getGreen()
                    , mModel.getBlue()));
            storeIntToPref("SCENE_5_A", alpha);
            storeIntToPref("SCENE_5_R", mModel.getRed());
            storeIntToPref("SCENE_5_G", mModel.getGreen());
            storeIntToPref("SCENE_5_B", mModel.getBlue());
            storeIntToPref("SCENE_5_S", speed);
        }
        scene_5.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                scene_5.setBackgroundColor(white);
                deleteSharePref(SCENE_5);
                deleteSharePref("SCENE_5_A");
                deleteSharePref("SCENE_5_R");
                deleteSharePref("SCENE_5_G");
                deleteSharePref("SCENE_5_B");
                deleteSharePref("SCENE_5_S");
                Toast.makeText(getApplicationContext(), "Scene Colour 5 Deleted", Toast.LENGTH_SHORT).show();
                mSpeedSB.setProgress(5);
                mSpeedTV.setText("5 secs");
                return true;
            }

        });
    }
    public void onScene6(View view) {
        Integer color = Color.argb(alpha
                , mModel.getRed()
                , mModel.getGreen()
                , mModel.getBlue());
        Integer speed = mModel.getSpeed();
        scene_6.setBackgroundColor(color);
        int A1 = ((ColorDrawable) scene_6.getBackground()).getColor();
        if (getIntFromPref(SCENE_6) != 0 && A1 != getIntFromPref(SCENE_6)) {
            setPreDefinedPreset(12, true); //TODO:Change the setDefinedPreset to 11,true
            Log.d(LOG_TAG, mModel.toString());
        } else {
            storeIntToPref(SCENE_6, Color.argb(alpha
                    , mModel.getRed()
                    , mModel.getGreen()
                    , mModel.getBlue()));
            storeIntToPref("SCENE_6_A", alpha);
            storeIntToPref("SCENE_6_R", mModel.getRed());
            storeIntToPref("SCENE_6_G", mModel.getGreen());
            storeIntToPref("SCENE_6_B", mModel.getBlue());
            storeIntToPref("SCENE_6_S", speed);
        }
        scene_6.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                scene_6.setBackgroundColor(white);
                deleteSharePref(SCENE_6);
                deleteSharePref("SCENE_6_A");
                deleteSharePref("SCENE_6_R");
                deleteSharePref("SCENE_6_G");
                deleteSharePref("SCENE_6_B");
                deleteSharePref("SCENE_6_S");
                Toast.makeText(getApplicationContext(), "Scene Colour 6 Deleted", Toast.LENGTH_SHORT).show();
                mSpeedSB.setProgress(5);
                mSpeedTV.setText("5 secs");
                return true;
            }

        });
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
    public void onStart() {
        super.onStart();
        client.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        client.disconnect();
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

