package com.example.jode.donething.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parrot.arsdk.ARSDK;
import com.parrot.arsdk.ardiscovery.ARDISCOVERY_PRODUCT_ENUM;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.parrot.arsdk.ardiscovery.ARDiscoveryService;
import com.example.jode.donething.R;
import com.example.jode.donething.discovery.DroneDiscoverer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/*
    This is the first class (activity) to run as defined by the manifest. It serves as the UI for
    the DroneDiscoverer class, and is tied to the activity_device_list.xml in the res/layout/ folder.
    It generates a list of Parrot drones it detects and allows the user to select one.
 */
public class DeviceListActivity extends AppCompatActivity {
    public static final String EXTRA_DEVICE_SERVICE = "EXTRA_DEVICE_SERVICE";

    private static final String TAG = "DeviceListActivity";

    /** List of runtime permission we need. */
    private static final String[] PERMISSIONS_NEEDED = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
    };

    /** Code for permission request result handling. */
    private static final int REQUEST_CODE_PERMISSIONS_REQUEST = 1;

    // Declare DroneDiscoverer object. DroneDiscoverer is what actually finds the drone
    public DroneDiscoverer mDroneDiscoverer;

    private final List<ARDiscoveryDeviceService> mDronesList = new ArrayList<>();

    // this block loads the native libraries
    // it is mandatory
    static {
        ARSDK.loadSDKLibs();
    }

    /**
     * onCreate method is required for every Activity-level class. Android does not use main methods.
     * The onCreate method is the closest approximation to the main method. All Activities have an
     * onCreate, which links to a UI xml file and does whatever Android needs to set up.
     *
     * This is from the sample SDK
     *
     * Inherited from AppCompatActivity
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);                             // Android gobbledygook
        setContentView(R.layout.activity_device_list);                  // Links to the xml file
        final ListView listView = (ListView) findViewById(R.id.list);   // Establishes an Android List

        // Assign adapter to ListView
        listView.setAdapter(mAdapter);

        // This is where the Intent is set via an item selection from the listview.
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                // Initialize the intent to null.
                Intent intent = null;

                // Drone discovery.
                ARDiscoveryDeviceService service = (ARDiscoveryDeviceService)mAdapter.getItem(position);
                ARDISCOVERY_PRODUCT_ENUM product = ARDiscoveryService.getProductFromProductID(service.getProductID());

                // Switch statement runs the discovered product against the expected Parrot drones.
                // Since we got the Bebop, I wasn't sure if the product would be ARDRONE or BEBOP_2
                // so I kept those options in. Removed the others.
                switch (product) {
                    case ARDISCOVERY_PRODUCT_ARDRONE:
                    case ARDISCOVERY_PRODUCT_BEBOP_2:
                        intent = new Intent(DeviceListActivity.this, BebopActivity.class);
                        break;

                    default:
                        Log.e(TAG, "The type " + product + " is not supported by this sample");
                }

                if (intent != null) {
                    intent.putExtra(EXTRA_DEVICE_SERVICE, service);
                    startActivity(intent);
                }
            }
        });

        // Create DroneDiscoverer object.
        mDroneDiscoverer = new DroneDiscoverer(this);

        // To get permissions for use on Android device
        Set<String> permissionsToRequest = new HashSet<>();
        for (String permission : PERMISSIONS_NEEDED) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                    Toast.makeText(this, "Please allow permission " + permission, Toast.LENGTH_LONG).show();
                    finish();
                    return;
                } else {
                    permissionsToRequest.add(permission);
                }
            }
        }
        if (permissionsToRequest.size() > 0) {
            ActivityCompat.requestPermissions(this,
                    permissionsToRequest.toArray(new String[permissionsToRequest.size()]),
                    REQUEST_CODE_PERMISSIONS_REQUEST);
        }
    }

    /**
     * onResume method is part of Android Activities. It defines what the Activity does when it
     * takes over the foreground, the main app the user sees. There is a default that will run if not
     * overridden as it is here. In this case, the additional code sets up a listener of the
     * DroneDiscoverer object, and starts the discovery.
     *
     * I did not alter this code. It is from the sample SDK
     *
     * Inherited from AppCompatActivity.
     */
    @Override
    protected void onResume()
    {
        super.onResume();

        // setup the drone discoverer and register as listener
        mDroneDiscoverer.setup();
        mDroneDiscoverer.addListener(mDiscovererListener);

        // start discovering
        mDroneDiscoverer.startDiscovering();
    }

    /**
     * onPause method is part of Android Activities. It defines what the Activity does when it
     * is pushed to the background. There is a default that will run if not
     * overridden as it is here. In this case, the additional code cleans up and shuts down the
     * objects related to drone discovery.
     *
     * I did not alter this code. It is from the sample SDK
     *
     * Inherited from AppCompatActivity.
     */
    @Override
    protected void onPause()
    {
        super.onPause();

        // clean the drone discoverer object
        mDroneDiscoverer.stopDiscovering();
        mDroneDiscoverer.cleanup();
        mDroneDiscoverer.removeListener(mDiscovererListener);
    }

    /**
     * onRequestPermissionResult is a part of Android Activities. It determines if all permissions
     * required by the app have been approved, and if not will shut down the app.
     *
     * I did not alter this code. It is from the sample SDK
     *
     * Inherited from AppCompatActivity.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        boolean denied = false;
        if (permissions.length == 0) {
            // canceled, finish
            denied = true;
        } else {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    denied = true;
                }
            }
        }

        if (denied) {
            Toast.makeText(this, "At least one permission is missing.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    /**
     * mDiscovererListener is a DroneDiscoverer.Listener object. Listener in this case is an
     * interface defined within DroneDiscoverer. As the list is updated, this object clears the list
     * then repopulates it with the new complete list of drones.
     *
     * For example, if there is a Bebop and a Disco present, the discoverer might detect the Bebop
     * first, then add it to the list. Soon after, it detects the Disco. This object would clear the
     * list, then add the Bebop back, in addition to the Disco.
     *
     * From the sample SDK
     */
    private final DroneDiscoverer.Listener mDiscovererListener = new  DroneDiscoverer.Listener() {

        @Override
        public void onDronesListUpdated(List<ARDiscoveryDeviceService> dronesList) {
            mDronesList.clear();
            mDronesList.addAll(dronesList);

            mAdapter.notifyDataSetChanged();
        }
    };

    // Necessary to display list items in an interactive list
    static class ViewHolder {
        public TextView text;
    }

    /**
     * mAdapter is a BaseAdapter object. The methods defined within the object are from the
     * interface Adapter, an Android widget. These methods basically enable the data the List is
     * pointing to to have meaningful interaction with the user.
     *
     * from the sample SDK
     */
    private final BaseAdapter mAdapter = new BaseAdapter()
    {
        @Override
        public int getCount()
        {
            return mDronesList.size();
        }

        @Override
        public Object getItem(int position)
        {
            return mDronesList.get(position);
        }

        @Override
        public long getItemId(int position)
        {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            View rowView = convertView;
            // reuse views
            if (rowView == null) {
                LayoutInflater inflater = getLayoutInflater();
                rowView = inflater.inflate(android.R.layout.simple_list_item_1, null);
                // configure view holder
                ViewHolder viewHolder = new ViewHolder();
                viewHolder.text = (TextView) rowView.findViewById(android.R.id.text1);
                rowView.setTag(viewHolder);
            }

            // fill data
            ViewHolder holder = (ViewHolder) rowView.getTag();
            ARDiscoveryDeviceService service = (ARDiscoveryDeviceService)getItem(position);
            holder.text.setText(service.getName() + " on " + service.getNetworkType());

            return rowView;
        }
    };

}
