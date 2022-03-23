package com.superpowered.playerexample.bluetooth;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import com.superpowered.playerexample.BluetoothConnectionService;
import com.superpowered.playerexample.DeviceListAdapter;
import com.superpowered.playerexample.MainActivity;
import com.superpowered.playerexample.MainFragment;
import com.superpowered.playerexample.R;

import java.util.ArrayList;
import java.util.UUID;

public class BlueTooth implements AdapterView.OnItemClickListener, ActivityCompat.OnRequestPermissionsResultCallback {

    private static final String TAG = "BLUETOOTH CLASS";

    MainFragment main;
    EstablishClocks clocks;

    //Is Connected To Another Phone
    boolean Connected;

    Button onOff;
    Button btnDiscover;
    BluetoothAdapter mBluetoothAdapter;
    Button btnEnableDisable_Discoverable;
    public ArrayList<BluetoothDevice> mBTDevices = new ArrayList<>();
    public DeviceListAdapter mDeviceListAdapter;
    ListView lvNewDevices;

    BluetoothConnectionService mBluetoothConnection;
    Button btnStartConnection;
    Button btnSend;
    EditText etSend;
    BluetoothDevice mBTDevice;

    public BlueTooth(MainFragment main) {
        InitObjects(main);
        InitLayouts();
        InitBTComponents();
        btPressed();
    }

    private void InitLayouts() {
        onOff = main.getView().findViewById(R.id.OnOff);
        lvNewDevices = (ListView) main.getView().findViewById(R.id.lvNewDevices);
        lvNewDevices.setOnItemClickListener(this);
        btnStartConnection = (Button) main.getView().findViewById(R.id.startConnection);
        btnSend = (Button) main.getView().findViewById(R.id.send);
        btnEnableDisable_Discoverable = (Button) main.getView().findViewById(R.id.btnDiscoverable_on_off);
        btnDiscover = main.getView().findViewById(R.id.btnFindUnpairedDevices);
        etSend = (EditText) main.getView().findViewById(R.id.message);


        btnStartConnection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startConnection();
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clocks.receivedTime = 0;
                clocks.timeSet(0);
            }
        });

        btnEnableDisable_Discoverable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnEnableDisable_Discoverable();
            }
        });

        btnDiscover.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                btnDiscover();
            }
        });
    }

    private void InitBTComponents() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBTDevices = new ArrayList<>();

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        main.getContext().registerReceiver(mBroadcastReceiver4, filter);
        mBTDevices = new ArrayList<>();
    }

    private void InitObjects(MainFragment main) {
        this.main = main;
        clocks = new EstablishClocks(this);
        Connected = false;
    }

    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");


    // Create a BroadcastReceiver for ACTION_FOUND
    private final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (action.equals(mBluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, mBluetoothAdapter.ERROR);

                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG, "onReceive: STATE OFF");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG, "mBroadcastReceiver1: STATE TURNING OFF");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG, "mBroadcastReceiver1: STATE ON");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG, "mBroadcastReceiver1: STATE TURNING ON");
                        break;
                }
            }
        }
    };

    /**
     * Broadcast Receiver for changes made to bluetooth states such as:
     * 1) Discoverability mode on/off or expire.
     */
    private final BroadcastReceiver mBroadcastReceiver2 = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)) {

                int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);

                switch (mode) {
                    //Device is in Discoverable Mode
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        Log.d(TAG, "mBroadcastReceiver2: Discoverability Enabled.");
                        break;
                    //Device not in discoverable mode
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Log.d(TAG, "mBroadcastReceiver2: Discoverability Disabled. Able to receive connections.");
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        Log.d(TAG, "mBroadcastReceiver2: Discoverability Disabled. Not able to receive connections.");
                        break;
                    case BluetoothAdapter.STATE_CONNECTING:
                        Log.d(TAG, "mBroadcastReceiver2: Connecting....");
                        ((MainActivity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(main.getContext(), "Connecting...", Toast.LENGTH_LONG).show();
                            }
                        });


                        break;
                    case BluetoothAdapter.STATE_CONNECTED:
                        Log.d(TAG, "mBroadcastReceiver2: Connected.");
                        ((MainActivity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(main.getContext(), "Connecting...", Toast.LENGTH_LONG).show();
                            }
                        });
                        break;
                }

            }
        }
    };

    /**
     * Broadcast Receiver for listing devices that are not yet paired
     * -Executed by btnDiscover() method.
     */
    private BroadcastReceiver mBroadcastReceiver3 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, "onReceive: ACTION FOUND.");

            if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mBTDevices.add(device);
                Log.d(TAG, "onReceive: " + device.getName() + ": " + device.getAddress());
                mDeviceListAdapter = new DeviceListAdapter(context, R.layout.device_adapter_view, mBTDevices);
                lvNewDevices.setAdapter(mDeviceListAdapter);
            }
        }
    };


    /**
     * Broadcast Receiver that detects bond state changes (Pairing status changes)
     */
    private final BroadcastReceiver mBroadcastReceiver4 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //3 cases:
                //case1: bonded already
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
                    Log.d(TAG, "BroadcastReceiver: BOND_BONDED.");
                    mBTDevice = mDevice;
//                    System.out.println(mDeviceListAdapter);
//                    if(mDeviceListAdapter != null){
//                        mDeviceListAdapter.setOnConnectionListener(new DeviceListAdapter.ConnectionListener() {
//                            @Override
//                            public void onConnected(View v) {
//                                TextView t = v.findViewById(R.id.connection);
//                                t.setText("Connected");
//                            }
//                        });
//                    }


                }
                //case2: creating a bone
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDING) {
                    Log.d(TAG, "BroadcastReceiver: BOND_BONDING.");
                }
                //case3: breaking a bond
                if (mDevice.getBondState() == BluetoothDevice.BOND_NONE) {
                    Log.d(TAG, "BroadcastReceiver: BOND_NONE.");
                    if (mDeviceListAdapter != null) {
                        mDeviceListAdapter.setOnConnectionListener(new DeviceListAdapter.ConnectionListener() {
                            @Override
                            public void onConnected(View v) {
                                TextView t = v.findViewById(R.id.connection);
                                t.setText("Not Connected");
                            }
                        });
                    }
                }
            }
        }
    };


    //create method for starting connection
//***remember the conncction will fail and app will crash if you haven't paired first
    public void startConnection() {
        startBTConnection(mBTDevice, MY_UUID_INSECURE);
    }

    /**
     * starting chat service method
     */
    public void startBTConnection(BluetoothDevice device, UUID uuid) {
        Log.d(TAG, "startBTConnection: Initializing RFCOM Bluetooth Connection.");

        mBluetoothConnection.startClient(device, uuid);
    }

    private void btPressed() {

        onOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBluetoothAdapter == null) {
                    Log.d(TAG, "enableDisableBT: Does not have BT capabilities.");
                }
                if (!mBluetoothAdapter.isEnabled()) {
                    Log.d(TAG, "enableDisableBT: enabling BT.");
                    Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    main.startActivity(enableBTIntent);

                    IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
                    main.getContext().registerReceiver(mBroadcastReceiver1, BTIntent);
                }
                if (mBluetoothAdapter.isEnabled()) {
                    Log.d(TAG, "enableDisableBT: disabling BT.");
                    mBluetoothAdapter.disable();
                    mDeviceListAdapter = new DeviceListAdapter(v.getContext(), R.layout.device_adapter_view, new ArrayList<BluetoothDevice>());
                    lvNewDevices.setAdapter(mDeviceListAdapter);

                    IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
                    main.getContext().registerReceiver(mBroadcastReceiver1, BTIntent);
                }
            }
        });

    }

    public void btnEnableDisable_Discoverable() {
        Log.d(TAG, "btnEnableDisable_Discoverable: Making device discoverable for 300 seconds.");

        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        main.startActivity(discoverableIntent);

        IntentFilter intentFilter = new IntentFilter(mBluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        main.getContext().registerReceiver(mBroadcastReceiver2, intentFilter);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void btnDiscover() {
        Log.d(TAG, "btnDiscover: Looking for unpaired devices.");

        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();

            mDeviceListAdapter = new DeviceListAdapter(main.getContext(), R.layout.device_adapter_view, new ArrayList<BluetoothDevice>());
            lvNewDevices.setAdapter(mDeviceListAdapter);
            Log.d(TAG, "btnDiscover: Canceling discovery.");

            //check BT permissions in manifest
            checkBTPermissions();

            mBluetoothAdapter.startDiscovery();
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            main.getContext().registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);
        }
        if (!mBluetoothAdapter.isDiscovering()) {

            //check BT permissions in manifest
            checkBTPermissions();

            mBluetoothAdapter.startDiscovery();
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            main.getContext().registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);
        }
    }

    /**
     * This method is required for all devices running API23+
     * Android must programmatically check the permissions for bluetooth. Putting the proper permissions
     * in the manifest is not enough.
     * <p>
     * NOTE: This will only execute on versions > LOLLIPOP because it is not needed otherwise.
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkBTPermissions() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            int permissionCheck = main.getContext().checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += main.getContext().checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            if (permissionCheck != 0) {

                main.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
            }
        } else {
            Log.d(TAG, "checkBTPermissions: No need to check permissions. SDK version < LOLLIPOP.");
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //first cancel discovery because its very memory intensive.
        mBluetoothAdapter.cancelDiscovery();

        Log.d(TAG, "onItemClick: You Clicked on a device.");
        String deviceName = mBTDevices.get(position).getName();
        String deviceAddress = mBTDevices.get(position).getAddress();

        Log.d(TAG, "onItemClick: deviceName = " + deviceName);
        Log.d(TAG, "onItemClick: deviceAddress = " + deviceAddress);

        //create the bond.
        //NOTE: Requires API 17+? I think this is JellyBean
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
            Log.d(TAG, "Trying to pair with " + deviceName);
            mBTDevices.get(position).createBond();

            mBTDevice = mBTDevices.get(position);
            mBluetoothConnection = new BluetoothConnectionService(main.getContext(), new BluetoothConnectionService.StartMetronomeListener() {
                @Override
                public void startMetronome(final View v, final String time) {
                    char messageType = time.charAt(0);

                    if ('s' == messageType) {
                        clocks.setPhoneTimeDif(Long.parseLong(time.substring(1)));
                        setConnected(true);
                    } else if ('p' == messageType) {
                        main.getBeatLoop().BTActivateMetronome(Long.parseLong(time.substring(1)));
                    } else {
                        clocks.timeSet(Long.parseLong(time));
                    }
                }
            }, view);
        }
    }

    @Override
    public void onRequestPermissionsResult(int i, @NonNull String[] strings, @NonNull int[] ints) {

    }

    public boolean getConnected() {
        return Connected;
    }

    public void setConnected(boolean bool) {
        Connected = bool;
    }


    public void onDestroy() {
        main.getContext().unregisterReceiver(mBroadcastReceiver1);
        main.getContext().unregisterReceiver(mBroadcastReceiver2);
        main.getContext().unregisterReceiver(mBroadcastReceiver3);
        main.getContext().unregisterReceiver(mBroadcastReceiver4);
    }

    public BluetoothConnectionService getBluetoothConnection() {
        return mBluetoothConnection;
    }

    public EstablishClocks getClocks(){
        return clocks;
    }
}
