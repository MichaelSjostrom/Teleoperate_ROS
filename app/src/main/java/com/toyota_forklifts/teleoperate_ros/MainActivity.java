package com.toyota_forklifts.teleoperate_ros;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import org.ros.android.view.RosImageView;
import org.ros.android.view.VirtualJoystickView;
import org.ros.node.NodeMainExecutor;
import org.ros.namespace.NameResolver;
import org.ros.node.NodeConfiguration;
import org.ros.android.BitmapFromCompressedImage;
import com.github.rosjava.android_remocons.common_tools.apps.RosAppActivity;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;

import sensor_msgs.CompressedImage;

public class MainActivity extends RosAppActivity implements AdapterView.OnItemSelectedListener{

    private RosImageView<CompressedImage> cameraView;
    private VirtualJoystickView virtualJoystickView;
    private Button backButton;
    private Spinner spinner = null;
    private ArrayList<String> spinnerArray = null;

    public MainActivity() {
        // The RosActivity constructor configures the notification title and
        // ticker messages.
        super("Teleoperate ROS", "Teleoperate ROS");
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(Bundle savedInstanceState) {

        //Dashboard is the top "navigation bar", with back button, spinner etc.
        setDashboardResource(R.id.top_bar);
        setMainWindowResource(R.layout.activity_main);
        super.onCreate(savedInstanceState);

        //The view which the robot camera feed is sent to
        cameraView = (RosImageView<sensor_msgs.CompressedImage>) findViewById(R.id.image);
        //Setting the images to be compressed
        cameraView.setMessageType(sensor_msgs.CompressedImage._TYPE);
        cameraView.setMessageToBitmapCallable(new BitmapFromCompressedImage());
        //The joystick which is used to navigate the robot remotely
        virtualJoystickView = (VirtualJoystickView) findViewById(R.id.virtual_joystick);
        //Back button in top left corner to get back to the view where connection to robot is done
        backButton = (Button) findViewById(R.id.back_button);
        //Listens to clicking on the back button
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Goes back to parent view
                onBackPressed();
            }
        });

        //Adding spinner-options to the arraylist
        spinnerArray = new ArrayList<>();
        spinnerArray.add("Camera");
        spinnerArray.add("Lidar");
        spinnerArray.add("Map");

        //The spinner for selecting different views
        spinner = (Spinner) findViewById(R.id.view_spinner);
        //Set a listener for the spinner
        spinner.setOnItemSelectedListener(this);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.views_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);


    }

    @Override
    protected void init(NodeMainExecutor nodeMainExecutor) {

        super.init(nodeMainExecutor);

        try {
            //Creates a socket with the right IP address and port
            java.net.Socket socket = new java.net.Socket(getMasterUri().getHost(), getMasterUri().getPort());
            java.net.InetAddress local_network_address = socket.getLocalAddress();
            socket.close();

            //Creates a new nodeConfiguration with the local network address and master Uri
            NodeConfiguration nodeConfiguration =
                    NodeConfiguration.newPublic(local_network_address.getHostAddress(), getMasterUri());

            //Creates string variables with the respective name from string resource file
            String joyTopic = remaps.get(getString(R.string.joystick_topic));
            String camTopic = remaps.get(getString(R.string.camera_topic));

            NameResolver appNameSpace = getMasterNameSpace();
            joyTopic = appNameSpace.resolve(joyTopic).toString();
            camTopic = appNameSpace.resolve(camTopic).toString();

            virtualJoystickView.setTopicName(joyTopic);
            cameraView.setTopicName(camTopic);

            nodeMainExecutor.execute(cameraView, nodeConfiguration
                    .setNodeName("android/camera_view"));
            nodeMainExecutor.execute(virtualJoystickView,
                    nodeConfiguration.setNodeName("android/virtual_joystick"));

        }catch(IOException e){
            //Socket error
            Log.e("TAG", e.toString());
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        menu.add(0,0,0,R.string.stop_app);
        Log.d("TAG", "Sup");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        super.onOptionsItemSelected(item);
        switch (item.getItemId()){
            case 0:
                onDestroy();
                break;
        }
        return true;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Log.d("TAG", String.valueOf(parent.getItemAtPosition(position)));


    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
