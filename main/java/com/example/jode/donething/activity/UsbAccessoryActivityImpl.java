package com.example.jode.donething.activity;

import com.parrot.arsdk.ardiscovery.UsbAccessoryActivity;
/*
    This was part of the SDK sample. It is a required part of the app, but I'm not entirely sure
    what it does. Clearly, it has something to do with the USB connection, but not sure what.

    It seems to have little bearing on our intent, and I have not seen any calls to this class,
    but I'm not about to remove a class the makers claim is necessary.
 */
public class UsbAccessoryActivityImpl extends UsbAccessoryActivity
{
    @Override
    protected Class getBaseActivity() {
        return DeviceListActivity.class;
    }
}
