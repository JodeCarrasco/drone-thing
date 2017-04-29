package com.example.jode.donething.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_MEDIARECORDEVENT_PICTUREEVENTCHANGED_ERROR_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM;
import com.parrot.arsdk.arcontroller.ARCONTROLLER_DEVICE_STATE_ENUM;
import com.parrot.arsdk.arcontroller.ARControllerCodec;
import com.parrot.arsdk.arcontroller.ARFrame;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.example.jode.donething.R;
import com.example.jode.donething.drone.BebopDrone;
import com.example.jode.donething.view.BebopVideoView;

/*
    This class serves as the main UI for piloting the drone. It shows the camera's views and
    gives functionality to the buttons. It interacts with the BebopDrone class. It is tied to the
    activity_bebop.xml in the res/layout/ folder
 */
public class BebopActivity extends AppCompatActivity {
    private static final String TAG = "BebopActivity";
    public static final String SCAN_DEVICE_SERVICE = "SCAN_DEVICE_SERVICE";
    protected BebopDrone mBebopDrone;

    protected ProgressDialog mConnectionProgressDialog;
    protected ProgressDialog mDownloadProgressDialog;

    protected BebopVideoView mVideoView;

    protected TextView mBatteryLabel;
    protected Button mTakeOffLandBt;
    protected Button mDownloadBt;

    protected int mNbMaxDownload;
    protected int mCurrentDownloadIndex;

    public static final String BEBOP = "com.example.jode.donething.XFER_DRONE";

    /**
     * onCreate method is required for every Activity-level class. Android does not use main methods.
     * The onCreate method is the closest approximation to the main method. All Activities have an
     * onCreate, which links to a UI xml file and does whatever Android needs to set up.
     *
     * I did not alter this code aside from comments. It is from the sample SDK
     *
     * Inherited from AppCompatActivity
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);         // Android nonsense
        setContentView(R.layout.activity_bebop);    // Links to the xml



        // Intent is where Java is telling Android what the class "intends" to do.
        Intent intent = getIntent();
        ARDiscoveryDeviceService service = intent.getParcelableExtra(DeviceListActivity.EXTRA_DEVICE_SERVICE);

        // Initializes the pilot display
        initRequired();
        initIHM();

        // BebopDrone object created
        mBebopDrone = new BebopDrone(this, service);
        mBebopDrone.addListener(mBebopListener);

    }

    /**
     * onStart method is part of Android Activities. It is the startup sequence for the Activity.
     * There is a default that will run if not overridden as it is here. In this case, the
     * additional code is checking to be sure the Bebop drone can be detected and the device
     * controller is compatible.
     *
     * I did not alter this code. It is from the sample SDK
     *
     * Inherited from AppCompatActivity.
     */
    @Override
    protected void onStart() {
        super.onStart();

        // show a loading view while the bebop drone is connecting
        if ((mBebopDrone != null) && !(ARCONTROLLER_DEVICE_STATE_ENUM.ARCONTROLLER_DEVICE_STATE_RUNNING.equals(mBebopDrone.getConnectionState())))
        {
            mConnectionProgressDialog = new ProgressDialog(this, R.style.AppCompatAlertDialogStyle);
            mConnectionProgressDialog.setIndeterminate(true);
            mConnectionProgressDialog.setMessage("Connecting ...");
            mConnectionProgressDialog.setCancelable(false);
            mConnectionProgressDialog.show();

            // if the connection to the Bebop fails, finish the activity
            if (!mBebopDrone.connect()) {
                finish();
            }
        }
    }

    /**
     * onBackPressed method is part of Android Activities. It is how the Activity will respond if
     * the user presses the back button on the Android device. There is a default that will run if not
     * overridden as it is here. In this case, the additional code is closing the connection to the
     * drone.
     *
     * I did not alter this code. It is from the sample SDK
     *
     * Inherited from AppCompatActivity.
     */
    @Override
    public void onBackPressed() {
        if (mBebopDrone != null)
        {
            mConnectionProgressDialog = new ProgressDialog(this, R.style.AppCompatAlertDialogStyle);
            mConnectionProgressDialog.setIndeterminate(true);
            mConnectionProgressDialog.setMessage("Disconnecting ...");
            mConnectionProgressDialog.setCancelable(false);
            mConnectionProgressDialog.show();

            if (!mBebopDrone.disconnect()) {
                finish();
            }
        }
    }

    /**
     * onDestroy method is part of Android Activities. It is cleanup for when the Activity is closed.
     * There is a default that will run if not overridden as it is here. In this case, the additional
     * code is cleaning the BebopDrone object from memory before cleaning the app.
     *
     * I did not alter this code. It is from the sample SDK
     *
     * Inherited from AppCompatActivity.
     */
    @Override
    public void onDestroy()
    {
        mBebopDrone.dispose();
        super.onDestroy();
    }

    /**
     * initIHM initializes pilot view. It defines what each button does and ties to xml. It also
     * initializes the VideoView. I have been experimenting with setting an autopilot path within
     * this method using a new button I added to the pilot view. Ultimately, I hope to change the
     * function of the button to create a new Activity which will serve as a basis for the auto-
     * pilot and other commands.
     *
     * I have altered this code from the sample SDK
     *
     */

    protected void initRequired(){
        // Defines the Emergency button, which cuts the rotors instantly
        findViewById(R.id.emergencyBt).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mBebopDrone.emergency();
            }
        });

        // Defines the Take Off/Land button.
        mTakeOffLandBt = (Button) findViewById(R.id.takeOffOrLandBt);
        mTakeOffLandBt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                // Switch statement detects the drone's flying state. If landed, the button will
                // cause the drone to take off. If hovering or flying, the button will cause the
                // drone to land.
                switch (mBebopDrone.getFlyingState()) {
                    case ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_LANDED:
                        mBebopDrone.takeOff();
                        break;
                    case ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_FLYING:
                    case ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_HOVERING:
                        mBebopDrone.land();
                        break;
                    default:
                }
            }
        });

        // The label for the battery indicator
        mBatteryLabel = (TextView) findViewById(R.id.batteryLabel);
    }

    private void initIHM() {
        mVideoView = (BebopVideoView) findViewById(R.id.videoView); // Main video screen in pilot view



        // Defines the take picture button.
        findViewById(R.id.takePictureBt).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mBebopDrone.takePicture();
            }
        });

        // Defines download button.
        mDownloadBt = (Button)findViewById(R.id.downloadBt);
        mDownloadBt.setEnabled(false);
        mDownloadBt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mBebopDrone.getLastFlightMedias();


                mDownloadProgressDialog = new ProgressDialog(BebopActivity.this, R.style.AppCompatAlertDialogStyle);
                mDownloadProgressDialog.setIndeterminate(true);
                mDownloadProgressDialog.setMessage("Fetching medias");
                mDownloadProgressDialog.setCancelable(false);
                mDownloadProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mBebopDrone.cancelGetLastFlightMedias();
                    }
                });
                mDownloadProgressDialog.show();
            }
        });

        // Defines the start scan button. Currently experimental. Played with possible autopilot
        // commands, and now trying to start new Activity from this button.
        findViewById(R.id.start_scanBt).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {


                mBebopDrone.videoOnOff((byte)0);

                mBebopDrone.setRotateSpeed(45);
                for(int i = 0; i < 8; i++) {
                    mBebopDrone.timeYaw((byte)50,1000);
                    mBebopDrone.timeYaw((byte)0,500);
                    mBebopDrone.flatTrim();
                    mBebopDrone.takePicture();
                }
                mBebopDrone.setYaw((byte)0);
                mBebopDrone.flatTrim();
                mBebopDrone.videoOnOff((byte)1);

/*
                Intent intent = new Intent(BebopActivity.this, ScanLoopActivity.class);
                startActivity(intent);
*/
            }
        });

        //HomeButton
        findViewById(R.id.homeBt).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mBebopDrone.goHome();
            }
        });

        // Defines the gaz up (Z-axis/vertical ascent) button
        findViewById(R.id.gazUpBt).setOnTouchListener(new View.OnTouchListener() {

            // "onTouch" is essentially "click and hold". You touch and hold the gaz up, and then
            // tilt your android device (the MotionEvent) to act as throttle. All other drone
            // control buttons (gaz down, yaw, roll, pitch) work this way.
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setPressed(true);
                        mBebopDrone.setGaz((byte) 50);
                        break;

                    case MotionEvent.ACTION_UP:
                        v.setPressed(false);
                        mBebopDrone.setGaz((byte) 0);
                        break;

                    default:

                        break;
                }

                return true;
            }
        });

        // Define the gaz down (Z-axis/vertical descent) button
        findViewById(R.id.gazDownBt).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setPressed(true);
                        mBebopDrone.setGaz((byte) -50);
                        break;

                    case MotionEvent.ACTION_UP:
                        v.setPressed(false);
                        mBebopDrone.setGaz((byte) 0);
                        break;

                    default:

                        break;
                }

                return true;
            }
        });

        // Defines the yaw (drone rotation/facing) left button
        findViewById(R.id.yawLeftBt).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setPressed(true);
                        mBebopDrone.setYaw((byte) -50);
                        break;

                    case MotionEvent.ACTION_UP:
                        v.setPressed(false);
                        mBebopDrone.setYaw((byte) 0);
                        break;

                    default:

                        break;
                }

                return true;
            }
        });

        // Defines the yaw (drone rotation/facing) right button
        findViewById(R.id.yawRightBt).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setPressed(true);
                        mBebopDrone.setYaw((byte) 50);
                        break;

                    case MotionEvent.ACTION_UP:
                        v.setPressed(false);
                        mBebopDrone.setYaw((byte) 0);
                        break;

                    default:

                        break;
                }

                return true;
            }
        });

        // Defines the forward button.
        // Forward/backward motion is determined by the pitch, the drone's up or down angle
        findViewById(R.id.forwardBt).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setPressed(true);
                        mBebopDrone.setPitch((byte) 50);
                        mBebopDrone.setFlag((byte) 1);
                        break;

                    case MotionEvent.ACTION_UP:
                        v.setPressed(false);
                        mBebopDrone.setPitch((byte) 0);
                        mBebopDrone.setFlag((byte) 0);
                        break;

                    default:

                        break;
                }

                return true;
            }
        });

        // Defines the back button.
        // Forward/backward motion is determined by the pitch, the drone's up or down angle
        findViewById(R.id.backBt).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setPressed(true);
                        mBebopDrone.setPitch((byte) -50);
                        mBebopDrone.setFlag((byte) 1);
                        break;

                    case MotionEvent.ACTION_UP:
                        v.setPressed(false);
                        mBebopDrone.setPitch((byte) 0);
                        mBebopDrone.setFlag((byte) 0);
                        break;

                    default:

                        break;
                }

                return true;
            }
        });

        // Defines the roll left (move to the left) button
        findViewById(R.id.rollLeftBt).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setPressed(true);
                        mBebopDrone.setRoll((byte) -50);
                        mBebopDrone.setFlag((byte) 1);
                        break;

                    case MotionEvent.ACTION_UP:
                        v.setPressed(false);
                        mBebopDrone.setRoll((byte) 0);
                        mBebopDrone.setFlag((byte) 0);
                        break;

                    default:

                        break;
                }

                return true;
            }
        });

        // Defines the roll right (move to the right) button
        findViewById(R.id.rollRightBt).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setPressed(true);
                        mBebopDrone.setRoll((byte) 50);
                        mBebopDrone.setFlag((byte) 1);
                        break;

                    case MotionEvent.ACTION_UP:
                        v.setPressed(false);
                        mBebopDrone.setRoll((byte) 0);
                        mBebopDrone.setFlag((byte) 0);
                        break;

                    default:

                        break;
                }

                return true;
            }
        });

    }

    /**
     * mBebopListener. Is this even a method? It looks like a really big object declaration.
     * It defines the methods of BebopDrone's Listener interface, which is declared inside the
     * BebopDrone class. That way, this class (BebopActivity) avoids implementing the interface.
     *
     * Whatever it is, it governs the data that updates live on the
     * pilot view, like battery level, progress bars, and flight status. If I'm reading the
     * documentation correctly, this stuff is being automatically checked every 50 milliseconds.
     *
     * From the sample SDK. I have commented for clarity.
     *
     */
    protected final BebopDrone.Listener mBebopListener = new BebopDrone.Listener() {
        // Checks to make sure device connection is still active.
        @Override
        public void onDroneConnectionChanged(ARCONTROLLER_DEVICE_STATE_ENUM state) {
            switch (state)
            {
                case ARCONTROLLER_DEVICE_STATE_RUNNING:
                    mConnectionProgressDialog.dismiss();
                    break;

                case ARCONTROLLER_DEVICE_STATE_STOPPED:
                    // if the deviceController is stopped, go back to the previous activity
                    mConnectionProgressDialog.dismiss();
                    finish();
                    break;

                default:
                    break;
            }
        }

        // Checks the drone's battery level, and updates it to the UI.
        @Override
        public void onBatteryChargeChanged(int batteryPercentage) {
            mBatteryLabel.setText(String.format("%d%%", batteryPercentage));
        }

        // Checks the piloting state and updates the Take Off/Land button to say either "Take Off"
        // or "Land" as appropriate.
        @Override
        public void onPilotingStateChanged(ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM state) {
            switch (state) {
                case ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_LANDED:
                    mTakeOffLandBt.setText("Take off");
                    mTakeOffLandBt.setEnabled(true);
                    mDownloadBt.setEnabled(true);
                    break;
                case ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_FLYING:
                case ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_HOVERING:
                    mTakeOffLandBt.setText("Land");
                    mTakeOffLandBt.setEnabled(true);
                    mDownloadBt.setEnabled(false);
                    break;
                default:
                    mTakeOffLandBt.setEnabled(false);
                    mDownloadBt.setEnabled(false);
            }
        }

        // Checks when a picture has been taken
        @Override
        public void onPictureTaken(ARCOMMANDS_ARDRONE3_MEDIARECORDEVENT_PICTUREEVENTCHANGED_ERROR_ENUM error) {
            Log.i(TAG, "Picture has been taken");
        }

        // Configures the video decoder. I assume that's important.
        @Override
        public void configureDecoder(ARControllerCodec codec) {
            mVideoView.configureDecoder(codec);
        }

        // Receives a frame of video from the drone.
        @Override
        public void onFrameReceived(ARFrame frame) {
            mVideoView.displayFrame(frame);
        }

        // Method that runs before media is actually downloaded. Includes a cancel button
        @Override
        public void onMatchingMediasFound(int nbMedias) {
            mDownloadProgressDialog.dismiss();

            mNbMaxDownload = nbMedias;
            mCurrentDownloadIndex = 1;

            if (nbMedias > 0) {
                mDownloadProgressDialog = new ProgressDialog(BebopActivity.this, R.style.AppCompatAlertDialogStyle);
                mDownloadProgressDialog.setIndeterminate(false);
                mDownloadProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mDownloadProgressDialog.setMessage("Downloading medias");
                mDownloadProgressDialog.setMax(mNbMaxDownload * 100);
                mDownloadProgressDialog.setSecondaryProgress(mCurrentDownloadIndex * 100);
                mDownloadProgressDialog.setProgress(0);
                mDownloadProgressDialog.setCancelable(false);
                mDownloadProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mBebopDrone.cancelGetLastFlightMedias();
                    }
                });
                mDownloadProgressDialog.show();
            }
        }

        // The progress of the current download. Ranges from 0% to 100%
        @Override
        public void onDownloadProgressed(String mediaName, int progress) {
            mDownloadProgressDialog.setProgress(((mCurrentDownloadIndex - 1) * 100) + progress);
        }

        // Called when download ends. Dismisses the progress dialog and cleans up
        @Override
        public void onDownloadComplete(String mediaName) {
            mCurrentDownloadIndex++;
            mDownloadProgressDialog.setSecondaryProgress(mCurrentDownloadIndex * 100);

            if (mCurrentDownloadIndex > mNbMaxDownload) {
                mDownloadProgressDialog.dismiss();
                mDownloadProgressDialog = null;
            }
        }
    };
}