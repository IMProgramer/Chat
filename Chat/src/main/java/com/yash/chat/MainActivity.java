package com.yash.chat;

import android.R;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.IOException;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends ListActivity {

    /* Change it to Swith between Server and client */
    private boolean IsServer = false;


    public static HashMap<String,BluetoothSocket> device_list = new HashMap<String, BluetoothSocket>();
    public static final String DEVICE_NAME = "server";
    public static final String UUID_string = "c23981f0-8da8-46b8-9359-cd3b4dcae8e7";
    public BluetoothAdapter bluetoothAdapter;
    private ArrayAdapter<String> adapter;
    Set<BluetoothDevice> dev;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        adapter = new ArrayAdapter<String>(this, R.layout.simple_list_item_1);


        setListAdapter(adapter);
        if (IsServer)
            new Accept().start();
        else{
             dev = bluetoothAdapter.getBondedDevices();
            for (BluetoothDevice x : dev){
                adapter.add(x.getName()+"\n"+x.getAddress());
                System.out.println(x.getName()+"\n"+x.getAddress());
            }

        }

    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        if(IsServer)
            return;
        BluetoothDevice server = (BluetoothDevice) dev.toArray()[position];
        new Connect(server).start();
    }

    class Accept extends Thread{

        BluetoothServerSocket serverSocket;
        public Accept(){
            try {
                serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord("chat", UUID.fromString(UUID_string));

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        @Override
        public void run(){
            try {
                while (true){
                    BluetoothSocket device_socket = serverSocket.accept();
                    device_list.put(device_socket.getRemoteDevice().getName(),device_socket);
                    System.out.println("devicename:" + device_socket.getRemoteDevice().getName() + " is added");
                    adapter.add(device_socket.getRemoteDevice().getName() + "\n" + device_socket.getRemoteDevice().getAddress());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    class Connect extends Thread{
        BluetoothDevice server;
        BluetoothSocket socket;
        public Connect(BluetoothDevice serv){
            server=serv;
        }

        @Override
        public void run() {
            try {
                socket = server.createRfcommSocketToServiceRecord(UUID.fromString(UUID_string));
                System.out.println(socket.getRemoteDevice().getName()+" is a Server");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onDestroy();
        if(device_list.size()>0){
            for(BluetoothSocket x : device_list.values()){
                try {
                    x.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}