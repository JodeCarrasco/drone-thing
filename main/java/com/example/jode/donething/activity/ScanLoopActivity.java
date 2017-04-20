package com.example.jode.donething.activity;


import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.jode.donething.R;
import com.example.jode.donething.drone.BebopDrone;
import com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_MEDIARECORDEVENT_PICTUREEVENTCHANGED_ERROR_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM;
import com.parrot.arsdk.arcontroller.ARCONTROLLER_DEVICE_STATE_ENUM;
import com.parrot.arsdk.arcontroller.ARControllerCodec;
import com.parrot.arsdk.arcontroller.ARFrame;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;


/*
    Experimental activity that would execute autopilot commands. Currently incomplete.
 */
public class ScanLoopActivity extends BebopActivity {
    private static final String TAG = "ScanLoopActivity";
    private BebopDrone mBebopScan;

    //private ProgressDialog mConnectionProgressDialog;
    //private ProgressDialog mDownloadProgressDialog;

    //private TextView mBatteryLabel;
    //private Button mTakeOffLandBt;
    //private Button mDownloadBt;

    //private int mNbMaxDownload;
    //private int mCurrentDownloadIndex;

    //private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scanner);

        Intent intent = getIntent();
        ARDiscoveryDeviceService service = intent.getParcelableExtra(DeviceListActivity.EXTRA_DEVICE_SERVICE);

        initRequired();
        initScan();

        // BebopDrone object created
        mBebopScan = new BebopDrone(this, service);
        mBebopScan.addListener(mBebopListener);



    }

    @Override
    protected void onStart() {
        super.onStart();

        // show a loading view while the bebop drone is connecting
        if ((mBebopScan != null) && !(ARCONTROLLER_DEVICE_STATE_ENUM.ARCONTROLLER_DEVICE_STATE_RUNNING.equals(mBebopScan.getConnectionState())))
        {
            mConnectionProgressDialog = new ProgressDialog(this, R.style.AppCompatAlertDialogStyle);
            mConnectionProgressDialog.setIndeterminate(true);
            mConnectionProgressDialog.setMessage("Connecting ...");
            mConnectionProgressDialog.setCancelable(false);
            mConnectionProgressDialog.show();

            // if the connection to the Bebop fails, finish the activity
            if (!mBebopScan.connect()) {
                finish();
            }
        }
    }

    @Override
    public void onDestroy()
    {
        mBebopScan.dispose();
        super.onDestroy();
    }

    private void initScan() {


        findViewById(R.id.flipBt).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mBebopScan.flip();
            }
        });

        findViewById(R.id.roll_L_slowBt).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mBebopScan.setFlag((byte) 1);
                mBebopScan.timeRoll((byte) -20, 1000);
                mBebopScan.setFlag((byte) 0);
            }
        });

        findViewById(R.id.roll_R_midBt).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mBebopScan.setFlag((byte) 1);
                mBebopScan.timeRoll((byte) 50, 1000);
                mBebopScan.setFlag((byte) 0);
            }
        });

         findViewById(R.id.roll_zeroBt).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mBebopScan.setRoll((byte) 0);
                mBebopScan.setFlag((byte) 0);

            }
        });

        findViewById(R.id.yaw_L_slowBt).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mBebopScan.timeYaw((byte) -20, 1000);
            }
        });

        findViewById(R.id.yaw_R_midBt).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mBebopScan.timeYaw((byte) 50, 1000);
            }
        });

        findViewById(R.id.yaw_zeroBt).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mBebopScan.setYaw((byte) 0);
            }
        });

        findViewById(R.id.homeBt).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mBebopScan.goHome();
            }
        });
/*
        findViewById(R.id.moveBt).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mBebopScan.mover(3, 30);
                mBebopScan.getLastFlightMedias();


                mDownloadProgressDialog = new ProgressDialog(ScanLoopActivity.this, R.style.AppCompatAlertDialogStyle);
                mDownloadProgressDialog.setIndeterminate(true);
                mDownloadProgressDialog.setMessage("Fetching medias");
                mDownloadProgressDialog.setCancelable(false);
                mDownloadProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mBebopScan.cancelGetLastFlightMedias();
                    }
                });
                mDownloadProgressDialog.show();
            }
        });

        findViewById(R.id.danceBt).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //                flag      roll     pitch    yaw       gaz     time/seq
                mBebopScan.dance((byte)0,(byte)-30,(byte) 0,(byte) 40,(byte) 0,0);
                try{
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mBebopScan.dance((byte)1,(byte)0,(byte) 100,(byte) 0,(byte) 0,0);
                try{
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mBebopScan.dance((byte)0,(byte)80,(byte) 0,(byte) 0,(byte) 0,0);
                try{
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mBebopScan.dance((byte)1,(byte)0,(byte) -40,(byte) 25,(byte) 0,0);
                try{
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mBebopScan.dance((byte)0,(byte)-30,(byte) 0,(byte) 40,(byte) 0,0);
                try{
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
*/

    }

    private final BebopDrone.Listener mBebopListener = new BebopDrone.Listener() {
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
                mDownloadProgressDialog = new ProgressDialog(ScanLoopActivity.this, R.style.AppCompatAlertDialogStyle);
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
                        mBebopScan.cancelGetLastFlightMedias();
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


/*
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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

*/

}