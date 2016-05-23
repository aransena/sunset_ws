package com.tcd.aransena.steer;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    private final String LOG_TAG = MainActivity.class.getSimpleName();
    private WebSocketClient mWebSocket;


    public void connect_to_server(String uri_s){

        URI uri=null;
        Log.v(LOG_TAG, "uri_s: " + uri_s);
        try{
            //uri = new URI("ws://192.168.1.102:8888/ws");
            uri = new URI(uri_s);
        }catch(URISyntaxException e){
            e.printStackTrace();
        }
        mWebSocket = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                //Log.v("Websocket", "Opened");
                mWebSocket.send("USER");
            }

            @Override
            public void onMessage(String s) {
                //Log.v("Websocket", "Received >" + s + "<");
                if(s.equals("USER")){
                    Log.v("Websocket", "Starting net comms");
                    //mHandler.postDelayed(netComms, mMetCommRate);
                }
                else if(s.equals("LOST")){
                    Log.v("Websocket", "LOST");
                    //mHandler.postDelayed(netComms, mMetCommRate);
                }

            }

            @Override
            public void onClose(int code, String s, boolean b) {
                //Log.v("Websocket", code + ": Closed " + s);

            }

            @Override
            public void onError(Exception e) {
                Log.v("Websocket", "Error " + e.getMessage());
            }
        };
        try {
            mWebSocket.connect();
        }catch(Exception e){
            Log.v("Error", "HERE");
            e.printStackTrace();

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.upper_container, new RobotCamFragment())
                   .commit();
            try {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.lower_container, new TeleopFragment())
                        .commit();
            }catch (Exception e){
                Log.v(LOG_TAG,e.toString());
            }


        }
        Context context = this;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String ip = prefs.getString(context.getString(R.string.pref_key_ip), context.getString(R.string.pref_default_ip));
        String ws = prefs.getString(context.getString(R.string.pref_key_ws), context.getString(R.string.pref_default_ws));
        String uri_s = "ws://" + ip + ":" + ws + "/ws";
        Log.v(LOG_TAG, "IP: " + ip);
        if(!ip.equals("0.0.0.0")) {
            Log.v("IP: ", "HERE " + ip);
            connect_to_server(uri_s);
        }


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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
            //Log.v("Settings: ", "launch");
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_frontdoor){
            mWebSocket.send("LOCATION,FRONT_DOOR");
        }
        else if (id == R.id.nav_charge){
            mWebSocket.send("LOCATION,CHARGE");
        }
        else if (id == R.id.nav_livingroom){
            mWebSocket.send("LOCATION,LIVING_ROOM");
        }
        else if (id == R.id.nav_kitchen){
            mWebSocket.send("LOCATION,KITCHEN");
        }
        else if (id == R.id.nav_bedroom){
            mWebSocket.send("LOCATION,BEDROOM");
        }
        else if (id == R.id.nav_frontdoor_update){
            mWebSocket.send("UPDATE,FRONT_DOOR");
        }
        else if (id == R.id.nav_charge_update){
            mWebSocket.send("UPDATE,CHARGE");
        }
        else if (id == R.id.nav_livingroom_update){
            mWebSocket.send("UPDATE,LIVING_ROOM");
        }
        else if (id == R.id.nav_kitchen_update){
            mWebSocket.send("UPDATE,KITCHEN");
        }
        else if (id == R.id.nav_bedroom_update){
            mWebSocket.send("UPDATE,BEDROOM");
        }

        /*else if (id == R.id.nav_map) {
            Log.v(LOG_TAG, "nav_map selected");
        }*/

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }




}
