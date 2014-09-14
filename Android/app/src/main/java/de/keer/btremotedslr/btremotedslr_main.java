package de.keer.btremotedslr;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;

import android.widget.Toast;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.ArrayAdapter;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;



public class btremotedslr_main extends Activity {

    public static final String TAG = "BTRemoteDSLR_Main";

    // static variable to save user specific values
    public static final String PREFS_NAME = "MyPrefsFile";
    public static final String PREFS_BT_DEVICE = "BT_Device";
    public static final String PREFS_TIME_INTERVAL = "TimeInterval";
    public static final String PREFS_PICTURE_COUNT = "PictureCount";

    // bluetooth
    private BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
    private Set<BluetoothDevice> pairedDevices;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // settings
    private SharedPreferences settings;

    // gui
    private Spinner deviceList;
    private EditText editTimeInterval;
    private EditText editPictureCount;
    private Button btnStart;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_btremotedslr_main);

        // init. control elements and gui
        deviceList = (Spinner)findViewById(R.id.deviceList);
        btnStart = (Button)findViewById(R.id.btn_start);
        editTimeInterval = (EditText)findViewById(R.id.editTimeInterval);
        editPictureCount = (EditText)findViewById(R.id.editPictureCount);

        // load preferences
        settings = this.getSharedPreferences(PREFS_NAME, 0);
        final String selectedBtDevice = settings.getString(PREFS_BT_DEVICE, "");
        String chosenTimerInterval = settings.getString(PREFS_TIME_INTERVAL, "5");
        String chosenPictureCount = settings.getString(PREFS_PICTURE_COUNT, "3");

        // init. bluetooth utils
        btAdapter = BluetoothAdapter.getDefaultAdapter();

        // Bluetooth support?
        if (btAdapter != null) {
            pairedDevices = btAdapter.getBondedDevices();

            // show possible devices at the list view
            ArrayList<String> list = new ArrayList<String>();
            for(BluetoothDevice bt : pairedDevices)
            {
                list.add(bt.getName() + "\n" + bt.getAddress());
            }

            final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list);
            deviceList.setAdapter(adapter);

            // change the values to the last state
            if(selectedBtDevice.length() > 0)
            {
                for(int i = 0; i < deviceList.getAdapter().getCount(); i++)
                {
                    if(deviceList.getItemAtPosition(i).toString().equalsIgnoreCase(selectedBtDevice))
                    {
                        deviceList.setSelection(i);
                    }
                }
            }

            editTimeInterval.setText("");
            editPictureCount.setText("");
            editTimeInterval.setText(chosenTimerInterval);
            editPictureCount.setText(chosenPictureCount);

            // only activate button, if exist more then one paired bluetooth device
            if(list.size() > 0)
                btnStart.setEnabled(true);
            else
                btnStart.setEnabled(false);
        }
        else
        {
            Log.d(TAG, "Device doesn't support Bluetooth");
            Toast.makeText(getApplicationContext(),"Device doesn't support Bluetooth"
                    ,Toast.LENGTH_LONG).show();
            btnStart.setEnabled(false);
        }

        // add onClickListener
        btnStart.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // information:
                // BluetoothAdapter -> BluetoothDevice -> BluetoothSocket -> OutputStream
                // ----- transmit data --> close


                //0. get bluetooth adapter and selected paired device
                if(null == btAdapter)
                {
                    btAdapter = BluetoothAdapter.getDefaultAdapter();

                    if(btAdapter == null)
                    {
                        Toast.makeText(getApplicationContext(),"Device doesn't support Bluetooth"
                                ,Toast.LENGTH_LONG).show();
                        return;
                    }
                }

                BluetoothDevice btSelectedDevice = null;
                String selectedDevice = deviceList.getSelectedItem().toString();

                for(BluetoothDevice bt : pairedDevices)
                {
                    if(selectedDevice.equalsIgnoreCase(bt.getName() + "\n" + bt.getAddress()))
                    {
                        btSelectedDevice = bt;
                    }
                }

                if(btSelectedDevice == null)
                {
                    Toast.makeText(getApplicationContext(),"no bluetooth device selected"
                            ,Toast.LENGTH_LONG).show();
                    return;
                }

                //1. Bluetooth active?
                if (!btAdapter.isEnabled()) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, 1);
                }

                //2. create socket
                BluetoothSocket btSocket;
                try {
                    // MY_UUID is the app's UUID string, also used by the server code
                    btSocket = btSelectedDevice.createRfcommSocketToServiceRecord(MY_UUID);
                    Log.d(TAG, "create socket to " + btSelectedDevice.getName() + "\n" + btSelectedDevice.getAddress());
                } catch (IOException connectException) {
                    Log.d(TAG, "can't create socket to " + btSelectedDevice.getName() + "\n" + btSelectedDevice.getAddress());
                    Log.d(TAG, connectException.getMessage());
                    return;
                }

                //3. connect to socket
                try{
                    btAdapter.cancelDiscovery();
                    btSocket.connect();
                    Log.d(TAG, "connected to " + btSelectedDevice.getName() + "(" + btSelectedDevice.getAddress() + ")");
                } catch (IOException connectException) {
                    try {
                        btSocket.close();
                    } catch (IOException e) {
                        return;
                    }
                    Toast.makeText(getApplicationContext(), "can't connect to " + btSelectedDevice.getName() + "\n" + btSelectedDevice.getAddress()
                            ,Toast.LENGTH_LONG).show();

                    Log.d(TAG, "can't connect to " + btSelectedDevice.getName() + "(" + btSelectedDevice.getAddress() + ")");
                    Log.d(TAG, connectException.getMessage());
                    return;
                }

                //4. send informations
                byte[] sendInformations= new byte[3];
                sendInformations[0] = (byte)Integer.parseInt(editTimeInterval.getText().toString());
                byte[] helpBuffer = ByteBuffer.allocate(4).putInt(Integer.parseInt(editPictureCount.getText().toString())).array();
                sendInformations[1] = helpBuffer[2];
                sendInformations[2] =  helpBuffer[3];

                OutputStream btOutStream;
                try {
                    btOutStream = btSocket.getOutputStream();
                    btOutStream.write(sendInformations);
                    Log.d(TAG, "send to " + btSelectedDevice.getName() + ": " + String.format("0x%x, 0x%x, 0x%x",
                            sendInformations[0], sendInformations[1], sendInformations[2]));

                }catch (IOException e){
                    Log.d(TAG, "can't send bytes to " + btSelectedDevice.getName() + "(" + btSelectedDevice.getAddress() + ")");
                    Log.d(TAG, e.getMessage());
                }

                //5. disconnect
                try {
                    btSocket.close();
                    btSocket = null;
                    Log.d(TAG, "disconnect of " + btSelectedDevice.getName() + "(" + btSelectedDevice.getAddress() + ")");
                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(), "can't close the connecting to " + btSelectedDevice.getName() + "\n" + btSelectedDevice.getAddress()
                            ,Toast.LENGTH_LONG).show();
                    Log.d(TAG, "can't close the connecting to " + btSelectedDevice.getName() + "(" + btSelectedDevice.getAddress() + ")");
                    return;
                }
            }
        });
    }
    @Override
    protected void onStop(){
        super.onStop();

        SharedPreferences.Editor ed = settings.edit();
        ed.putString(PREFS_BT_DEVICE, deviceList.getSelectedItem().toString());
        ed.putString(PREFS_TIME_INTERVAL, editTimeInterval.getText().toString());
        ed.putString(PREFS_PICTURE_COUNT, editPictureCount.getText().toString());
        ed.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.btremotedslr_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
