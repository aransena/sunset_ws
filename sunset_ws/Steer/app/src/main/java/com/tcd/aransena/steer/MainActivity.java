package com.tcd.aransena.steer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    private final String LOG_TAG = MainActivity.class.getSimpleName();
    private WebSocketClient mWebSocket;

    public void connect_to_server(String uri_s){
    // called during initialisation
    // input arg: address of websocket server to connect to

        URI uri=null;

        //Log.v(LOG_TAG, "uri_s: " + uri_s);

        try{
            uri = new URI(uri_s); // check if valid URI
        }catch(URISyntaxException e){
            e.printStackTrace();
        }

        // create a new websocket client with provided URI
        mWebSocket = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
            // called when connection to server made
                //Log.v("Websocket", "Opened");

                // Register with the websocket server.
                // This could be replaced with a device ID/username/etc.
                mWebSocket.send("USER");

            }

            @Override
            public void onMessage(String s) {
            // called whenever the client recieves a message from the server
                //Log.v("Websocket", "Received >" + s + "<");
            }

            @Override
            public void onClose(int code, String s, boolean b) {
            // called whenever the client recieves a message from the server
                //Log.v("Websocket", code + ": Closed " + s);
            }

            @Override
            public void onError(Exception e) {
            // called whenever the client experiences an error
                //Log.v("Websocket", "Error " + e.getMessage());
            }
        };

        try {
            mWebSocket.connect(); // Try use configured websocket client
        }catch(Exception e){
            //Log.v("Error", "Websocket client error");
            e.printStackTrace();

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
    // Called as part of app activity lifecycle during start up.

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // At start up, load in the required Fragments
        if (savedInstanceState == null) {
            // Find location to load fragment by ID, load Kinect view camera fragment
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.upper_container, new RobotCamFragment())
                   .commit();
            try {
                // Find location to load fragment by ID, load teleoperation fragment
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.lower_container, new TeleopFragment())
                        .commit();
            }catch (Exception e){
                Log.v(LOG_TAG,e.toString());
            }
            // If the id side_container is not found, we assume we are using a smartphone.
            // If the id is found, we assume device is a tablet and the fragment is loaded.

            if (findViewById(R.id.side_container)!=null) {
                try {
                    // Find location to load fragment by ID, load side view cameras fragment
                    getSupportFragmentManager().beginTransaction()
                            .add(R.id.side_container, new LowerCamsFragment())
                            .commit();
                } catch (Exception e) {
                    Log.v(LOG_TAG, e.toString());
                }
            }
        }

        Context context = this;

        // Retrieve Websocket URI based on IP address stored in memory.
        // IP set from Settings Activity
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String ip = prefs.getString(context.getString(R.string.pref_key_ip), context.getString(R.string.pref_default_ip));
        String ws = prefs.getString(context.getString(R.string.pref_key_ws), context.getString(R.string.pref_default_ws));
        String uri_s = "ws://" + ip + ":" + ws + "/ws";
        Log.v(LOG_TAG, "IP: " + ip);
        if(!ip.equals("0.0.0.0")) {
            Log.v("IP: ", "HERE " + ip);
            connect_to_server(uri_s); // If IP found, connect to the server
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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
    // Handle back button presses
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

        // Used to set autonomous nav goal
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
        // Used to update location of autonomous nav goal
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

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }
}