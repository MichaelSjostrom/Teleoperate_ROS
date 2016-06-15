package com.toyota_forklifts.teleoperate_ros;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import org.ros.android.view.RosImageView;
import org.ros.android.view.VirtualJoystickView;
import org.ros.node.NodeMainExecutor;
import org.ros.namespace.NameResolver;
import org.ros.node.NodeConfiguration;
import org.ros.address.InetAddressFactory;
import org.ros.android.BitmapFromCompressedImage;
import com.github.rosjava.android_remocons.common_tools.apps.RosAppActivity;

import sensor_msgs.CompressedImage;

public class MainActivity extends RosAppActivity {

    private RosImageView<CompressedImage> cameraView;
    private VirtualJoystickView virtualJoystickView;
    private Button backButton;
    private static final String cameraTopic = "camera/rgb/image_color/compressed_throttle";

    public MainActivity() {
        // The RosActivity constructor configures the notification title and
        // ticker
        // messages.
        super("android teleop", "android teleop");
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        //setContentView(R.layout.activity_main);
        String defaultRobotName = getString(R.string.robot_name);
        String defaultAppName = getString(R.string.app_name);

        //setDefaultRobotName(defaultRobotName);

        setDefaultAppName(defaultAppName);
        setDefaultMasterName(defaultRobotName);

        setDashboardResource(R.id.top_bar);
        setMainWindowResource(R.layout.activity_main);

        super.onCreate(savedInstanceState);

        cameraView = (RosImageView<sensor_msgs.CompressedImage>) findViewById(R.id.image);
        cameraView.setMessageType(sensor_msgs.CompressedImage._TYPE);
        cameraView.setMessageToBitmapCallable(new BitmapFromCompressedImage());
        virtualJoystickView = (VirtualJoystickView) findViewById(R.id.virtual_joystick);
        virtualJoystickView.setTopicName("/cmd_vel");
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

        NodeConfiguration nodeConfiguration = NodeConfiguration.newPublic(
                InetAddressFactory.newNonLoopback().getHostAddress(),
                getMasterUri());

        //Was 'getAppNameSpace(), not sure if getMasternameSpace() is correct.
        NameResolver appNameSpace = getMasterNameSpace();
        cameraView.setTopicName(appNameSpace.resolve(cameraTopic).toString());

        nodeMainExecutor.execute(cameraView, nodeConfiguration
                .setNodeName("android/camera_view"));
        nodeMainExecutor.execute(virtualJoystickView,
                nodeConfiguration.setNodeName("android/virtual_joystick"));

        /*nodeConfiguration = NodeConfiguration.newPublic(InetAddressFactory
                .newNonLoopback().getHostAddress(), getMasterUri());*/

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
