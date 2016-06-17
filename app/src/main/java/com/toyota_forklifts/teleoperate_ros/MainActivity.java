package com.toyota_forklifts.teleoperate_ros;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import org.ros.android.view.RosImageView;
import org.ros.android.view.VirtualJoystickView;
import org.ros.node.NodeMainExecutor;
import org.ros.namespace.NameResolver;
import org.ros.node.NodeConfiguration;
import org.ros.android.BitmapFromCompressedImage;
import com.github.rosjava.android_remocons.common_tools.apps.RosAppActivity;

import java.io.IOException;

import sensor_msgs.CompressedImage;

public class MainActivity extends RosAppActivity {

    private RosImageView<CompressedImage> cameraView;
    private VirtualJoystickView virtualJoystickView;
    private Button backButton;

    public MainActivity() {
        // The RosActivity constructor configures the notification title and
        // ticker messages.
        super("android teleop", "android teleop");
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(Bundle savedInstanceState) {

        setDashboardResource(R.id.top_bar);
        setMainWindowResource(R.layout.activity_main);
        super.onCreate(savedInstanceState);

        cameraView = (RosImageView<sensor_msgs.CompressedImage>) findViewById(R.id.image);
        cameraView.setMessageType(sensor_msgs.CompressedImage._TYPE);
        cameraView.setMessageToBitmapCallable(new BitmapFromCompressedImage());
        virtualJoystickView = (VirtualJoystickView) findViewById(R.id.virtual_joystick);
        backButton = (Button) findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

    }

    @Override
    protected void init(NodeMainExecutor nodeMainExecutor) {

        super.init(nodeMainExecutor);

        try {

            java.net.Socket socket = new java.net.Socket(getMasterUri().getHost(), getMasterUri().getPort());
            java.net.InetAddress local_network_address = socket.getLocalAddress();
            socket.close();

            NodeConfiguration nodeConfiguration =
                    NodeConfiguration.newPublic(local_network_address.getHostAddress(), getMasterUri());

            String joyTopic = remaps.get(getString(R.string.joystick_topic));
            String camTopic = remaps.get(getString(R.string.camera_topic));

            NameResolver appNameSpace = getMasterNameSpace();
            joyTopic = appNameSpace.resolve(joyTopic).toString();
            camTopic = appNameSpace.resolve(camTopic).toString();

            cameraView.setTopicName(camTopic);
            virtualJoystickView.setTopicName(joyTopic);

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
}
