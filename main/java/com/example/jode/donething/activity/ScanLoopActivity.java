package com.example.jode.donething.activity;


import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.example.jode.donething.R;
import com.example.jode.donething.drone.BebopDrone;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;


/*
    Experimental activity that would execute autopilot commands. Currently incomplete.
 */
public class ScanLoopActivity extends AppCompatActivity {

    private BebopDrone mBebopScan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scanner);

        // Initializes the pilot display
        initScan();

        Intent intent = getIntent();
        ARDiscoveryDeviceService service = intent.getParcelableExtra(DeviceListActivity.EXTRA_DEVICE_SERVICE);
        mBebopScan = new BebopDrone(this, service);
        //mBebopScan.addListener(mBebopListener);



    }

    private void initScan() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }

        });
    }

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
