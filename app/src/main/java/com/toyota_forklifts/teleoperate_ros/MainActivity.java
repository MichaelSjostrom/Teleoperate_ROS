package com.toyota_forklifts.teleoperate_ros;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;

import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;


import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import java.io.IOException;
import java.util.ArrayList;

import com.github.rosjava.android_remocons.common_tools.apps.RosAppActivity;
import com.google.common.collect.Lists;

import org.ros.android.view.RosImageView;
import org.ros.android.view.visualization.layer.Layer;
import org.ros.android.view.visualization.layer.PathLayer;
import org.ros.node.NodeMainExecutor;
import org.ros.namespace.NameResolver;
import org.ros.node.NodeConfiguration;
import org.ros.android.BitmapFromCompressedImage;
import org.ros.android.view.visualization.layer.OccupancyGridLayer;
import org.ros.android.view.visualization.layer.LaserScanLayer;
import org.ros.android.view.visualization.layer.RobotLayer;
import org.ros.android.view.visualization.VisualizationView;

import sensor_msgs.CompressedImage;

public class MainActivity extends RosAppActivity implements AdapterView.OnItemSelectedListener {

    private static final String ROBOT_FRAME = "base_link";
    private static final int REQUEST_CAMERA = 0;

    private RosImageView<CompressedImage> cameraView;
    private VirtualJoystickView virtualJoystickView;
    private ImageButton backButton;
    private ImageButton refreshButton;
    private Spinner spinner = null;
    private ViewGroup mainLayout;
    private ViewGroup sideLayout;
    private VisualizationView mapView = null;
    private NameResolver appNameSpace = null;
    private ListView idListView;
    private ArrayList<String> idList;
    private ArrayAdapter arrayAdapter;
    private ViewControlLayer viewControlLayer;

    private OccupancyGridLayer occupancyGridLayer = null;
    private LaserScanLayer laserScanLayer = null;
    private RobotLayer robotLayer = null;
    private PathLayer pathLayer = null;
    private MapPosePublisherLayer mapPosePublisherLayer = null;
    private InitialPoseSubscriberLayer initialPoseSubscriberLayer = null;
    private ForkPublisher forkPublisher;
    private IdSubscriber idSubscriber;

    private static SeekBar seekBarFork;
    private static TextView textViewFork;
    private static SeekBar seekBarReach;
    private static TextView textViewReach;

    public MainActivity() {
        // The RosActivity constructor configures the notification title and
        // ticker messages.

        super("Teleoperate ROS", "Teleoperate ROS");
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(Bundle savedInstanceState) {

        //Sets the app name
        String appName = getString(R.string.app_name);
        setDefaultAppName(appName);

        //Dashboard is part of top navigation bar, includes battery levels
        setDashboardResource(R.id.top_bar);
        setMainWindowResource(R.layout.activity_main);
        super.onCreate(savedInstanceState);
        seekBarFork();
        seekBarReach();

        //Asking for permissions
        checkPermissions();
        //Initializing variables
        initVariables();
        //Adds onClicklisteners
        setOnClickListeners();
        //Initializing id ListView and populates the array
        initIdListView();

        //Add layers to the mapView
        mapView.onCreate(Lists.<Layer>newArrayList(viewControlLayer, occupancyGridLayer,
                laserScanLayer,
                pathLayer, mapPosePublisherLayer,
                initialPoseSubscriberLayer));

        //Set a listener for the spinner
        spinner.setOnItemSelectedListener(this);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.views_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        mapView.getCamera().jumpToFrame(ROBOT_FRAME);

        mapView.setClickable(true);
    }

    public void checkPermissions() {
        //Asking for permission to use camera
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            // Camera permission has not been granted.
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA);
        }
    }

    @SuppressWarnings("unchecked")
    public void initVariables() {
        //Holds the two different layouts
        mainLayout = (ViewGroup) findViewById(R.id.main_layout);
        sideLayout = (ViewGroup) findViewById(R.id.frame_layout);

        //The view which the robot camera feed is sent to
        cameraView = (RosImageView<sensor_msgs.CompressedImage>) findViewById(R.id.camera_view);

        //Setting the images to be compressed
        cameraView.setMessageType(sensor_msgs.CompressedImage._TYPE);
        cameraView.setMessageToBitmapCallable(new BitmapFromCompressedImage());

        //Connects the VisualizationView to the view
        mapView = (VisualizationView) findViewById(R.id.map_view);

        //Contains all the different views and handles view changes
        viewControlLayer = new ViewControlLayer(this, cameraView, mapView, mainLayout, sideLayout, params);

        //Initializing all layers
        occupancyGridLayer = new OccupancyGridLayer("map");
        laserScanLayer = new LaserScanLayer("scan");
        robotLayer = new RobotLayer("base_link");
        pathLayer = new PathLayer("/move_base/TrajectoryPlannerROS/global_plan");
        mapPosePublisherLayer = new MapPosePublisherLayer(this, params);
        initialPoseSubscriberLayer = new InitialPoseSubscriberLayer("/initialpose", ROBOT_FRAME);

        //Initializing publisher to handle fork-height and reach functions
        forkPublisher = new ForkPublisher();

        //The joystick which is used to navigate the robot remotely
        virtualJoystickView = (VirtualJoystickView) findViewById(R.id.virtual_joystick);

        //Back button in top left corner to get back to the view where connection to robot is done
        backButton = (ImageButton) findViewById(R.id.back_button);

        //Refresh button that refreshes the map
        refreshButton = (ImageButton) findViewById(R.id.refresh_button);

        //The spinner for selecting different views
        spinner = (Spinner) findViewById(R.id.view_spinner);

        idList = new ArrayList<>();

        idListView = (ListView) findViewById(R.id.id_listview);
    }

    public void initIdListView() {

        arrayAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, idList);
        idListView.setAdapter(arrayAdapter);

        idSubscriber = new IdSubscriber(idList, arrayAdapter, this);
    }

    public void setOnClickListeners() {

        //Listens to clicking on the back button
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Goes back to parent view
                onBackPressed();
            }
        });

        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "refreshing map...",
                        Toast.LENGTH_SHORT).show();
                mapView.getCamera().jumpToFrame((String) params.get("map_frame", getString(R.string.map_frame)));
            }
        });
    }

    @Override
    protected void init(NodeMainExecutor nodeMainExecutor) {

        mapView.init(nodeMainExecutor);
        super.init(nodeMainExecutor);

        viewControlLayer.setListeners(nodeMainExecutor.getScheduledExecutorService());

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

            appNameSpace = getMasterNameSpace();

            //Resolves the namespace for each topic, e.g. /blabla/blabla/blabla
            joyTopic = appNameSpace.resolve(joyTopic).toString();
            camTopic = appNameSpace.resolve(camTopic).toString();

            //Sets the topic name for each topic
            virtualJoystickView.setTopicName(joyTopic);
            cameraView.setTopicName(camTopic);

            //Executes each node
            nodeMainExecutor.execute(cameraView, nodeConfiguration
                    .setNodeName(getString(R.string.camera_view_node)));
            nodeMainExecutor.execute(virtualJoystickView,
                    nodeConfiguration.setNodeName(getString(R.string.virtual_joystick_node)));
            nodeMainExecutor.execute(mapView,
                    nodeConfiguration.setNodeName(getString(R.string.map_view_node)));
            nodeMainExecutor.execute(forkPublisher,
                    nodeConfiguration.setNodeName(getString(R.string.fork_controller_node)));
            nodeMainExecutor.execute(idSubscriber,
                    nodeConfiguration.setNodeName(getString(R.string.id_subscriber_node)));

        } catch (IOException e) {
            //Socket error
            Log.e("TAG", e.toString());
        }
    }

    /*****
     * NOT USED FOR THE MOMENT
     *****/
    public void setPoseClicked(View view) {
        setPose();
    }

    public void setGoalClicked(View view) {
        setGoal();
    }

    private void setPose() {
        mapPosePublisherLayer.setPoseMode();
    }

    private void setGoal() {
        mapPosePublisherLayer.setGoalMode();
    }

    /************************************/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0, 0, R.string.stop_app);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case 0:
                onDestroy();
                break;
        }
        return true;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        //Lidar
        if (position == 0) {
            mapView.getCamera().jumpToFrame(getString(R.string.robot_frame));
            refreshButton.setEnabled(false);
        }

        //Camera
        if (position == 1) {
            mapView.getCamera().jumpToFrame(getString(R.string.map_frame));
            refreshButton.setEnabled(true);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (requestCode == REQUEST_CAMERA) {
            // Received permission result for camera permission.
            Context context = getApplicationContext();
            int duration = Toast.LENGTH_SHORT;
            // Check if the only required permission has been granted
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Camera permission has been granted, preview can be displayed

                CharSequence text = "Camera permission granted!";

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            } else {
                CharSequence text = "Camera permission NOT granted!";

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            }

        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public void seekBarFork() {
        final float up = 1.0f;
        final float down = -1.0f;
        final float stop = 0.0f;
        seekBarFork = (SeekBar) findViewById(R.id.seek_bar_fork);
        seekBarFork.setProgress(1);
        textViewFork = (TextView) findViewById(R.id.text_view_fork);
        textViewFork.setText("Fork Up          Down");

        seekBarFork.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                        if (progress == 0)
                            forkPublisher.publishHeightData(down);
                        else if (progress == 2)
                            forkPublisher.publishHeightData(up);
                        else
                            forkPublisher.publishHeightData(stop);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        Log.d("TAG", "onStartTracking");
                        seekBarFork.setProgress(1);
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        //textViewFork.setText("Fork : " + (float) progress_value/divide + " / " + (float) seekBarFork.getMax()/divide);
                        seekBarFork.setProgress(1);
                    }
                }
        );
    }

    public void seekBarReach() {
        final float out = 1.0f;
        final float in = -1.0f;
        final float stop = 0.0f;
        seekBarReach = (SeekBar) findViewById(R.id.seek_bar_reach);
        seekBarReach.setProgress(1);
        textViewReach = (TextView) findViewById(R.id.text_view_reach);
        textViewReach.setText("Reach In            Out");

        seekBarReach.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if (progress == 0)
                            forkPublisher.publishReachData(in);
                        else if (progress == 2)
                            forkPublisher.publishReachData(out);
                        else
                            forkPublisher.publishReachData(stop);

                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        seekBarReach.setProgress(1);
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        seekBarReach.setProgress(1);
                    }
                }
        );
    }

}














