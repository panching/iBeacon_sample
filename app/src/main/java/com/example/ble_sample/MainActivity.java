package com.example.ble_sample;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.Collection;

public class MainActivity extends AppCompatActivity implements BeaconConsumer, View.OnClickListener {

    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private static final String TAG = "MainActivity";
    private static final String IBEACON_FORMAT = "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24";
    private static final String UNIQUE_ID = "UNIQUE_ID";
    private static final String CHANNEL_ID = "CHANNEL_ID";
    private static final int NOTIFY_ID = 1;
    private final Context context = this;
    private TextView tvBeaconVal, tvDistanceVal;
    private Button detectButton;
    private LinearLayout linearLayout;
    private BeaconManager beaconManager;
    private boolean isDelivered = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        detectButton = (Button) findViewById(R.id.main_detect_button);
        linearLayout = (LinearLayout) findViewById(R.id.main_wrapper);
        tvBeaconVal = (TextView) findViewById(R.id.tvBeaconVal);
        tvDistanceVal = (TextView) findViewById(R.id.tvDistanceVal);
        detectButton.setOnClickListener(this);
        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(IBEACON_FORMAT));
        beaconManager.bind(this);
        initValues();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.main_detect_button:
                startBeaconServiceConnecting();
                break;
        }
    }

    @Override
    public void onBeaconServiceConnect() {}

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, getResources().getString(R.string.permission_granted));
                } else {
                    errorBuilder(getResources().getString(R.string.notice), getResources().getString(R.string.functionality_limited));
                }
                return;
            }
        }
    }

    private void initValues() {
        tvBeaconVal.setText("0");
        tvDistanceVal.setText("0");
    }

    private boolean permissionDetector() {
        final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check 
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                errorBuilder(getResources().getString(R.string.notice), getResources().getString(R.string.location_access));
                return false;
            }
        }

        if (adapter == null) {
            errorBuilder(getResources().getString(R.string.notice), getResources().getString(R.string.bluetooth_not_found));
            return false;
        } else if (!adapter.isEnabled()) {
            errorBuilder(getResources().getString(R.string.notice), getResources().getString(R.string.bluetooth_disable));
            return false;
        } else {
            return true;
        }
    }

    private void startBeaconServiceConnecting() {
        if (!permissionDetector()) {
            return;
        }

        detectButton.setVisibility(View.GONE);
        linearLayout.setVisibility(View.VISIBLE);

        try {
            // MonitorNotifier
//            beaconManager.removeAllMonitorNotifiers();
//            beaconManager.addMonitorNotifier(new MonitorNotifier() {
//                @Override
//                public void didEnterRegion(Region region) {
//                    Log.i(TAG, getResources().getString(R.string.enter_region));
//                }
//
//                @Override
//                public void didExitRegion(Region region) {
//                    Log.i(TAG, getResources().getString(R.string.exit_region));
//                }
//
//                @Override
//                public void didDetermineStateForRegion(int state, Region region) {
//                    Log.i(TAG, getResources().getString(R.string.determine_state_for_region));
//                }
//            });
//            beaconManager.startMonitoringBeaconsInRegion(new Region(UNIQUE_ID, null, null, null));

            beaconManager.removeAllRangeNotifiers();
            // RangeNotifier
            beaconManager.addRangeNotifier(new RangeNotifier() {
                @Override
                public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                    if (beacons.size() > 0) {
                        for (Beacon beacon : beacons) {
                            // only deliver once
                            if (beacon.getDistance() <= 1.0 && !isDelivered){
                                sendNotification();
                                isDelivered = true;
                                tvBeaconVal.setText(String.valueOf(beacon.getId1()));
                                tvDistanceVal.setText(String.valueOf(beacon.getDistance()));
                                String message =
                                        "\nMac：" + beacon.getBluetoothAddress()
                                                + " \nUUID：" + beacon.getServiceUuid()
                                                + "\nId1：" + beacon.getId1()
                                                + "\nId2：" + beacon.getId2()
                                                + "\nId3：" + beacon.getId3()
                                                + "\nTxPower：" + beacon.getTxPower()
                                                + "\nDistance：" + beacon.getDistance()
                                                + "\nManufacturer：" + beacon.getManufacturer()
                                                + "\nParserIdentifier：" + beacon.getParserIdentifier()
                                                + "\nrssi：" + beacon.getRssi();
                                Log.i(TAG, message);
                            }
                        }
                    }
                }
            });

            beaconManager.startRangingBeaconsInRegion(new Region(UNIQUE_ID, null, null, null));

        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void sendNotification(){
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, CHANNEL_ID);

        notificationBuilder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_stat_fiber_new)
                .setPriority(Notification.PRIORITY_MAX)
                .setContentTitle(getResources().getString(R.string.notification_title))
                .setContentText(getResources().getString(R.string.notification_message));

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFY_ID, notificationBuilder.build());
    }

    private void errorBuilder(String title, String message) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(android.R.string.ok, null);
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
            }
        });
        builder.show();
    }
}
