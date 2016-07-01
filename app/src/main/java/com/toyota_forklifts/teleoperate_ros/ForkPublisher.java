package com.toyota_forklifts.teleoperate_ros;

import android.content.Context;
import android.util.Log;

import org.ros.android.view.visualization.layer.DefaultLayer;
import org.ros.android.view.visualization.layer.SubscriberLayer;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.Node;
import org.ros.node.NodeMain;
import org.ros.node.topic.Publisher;

import std_msgs.Float64;
import std_msgs.String;

/**
 * Created by mitch on 2016-07-01.
 */
public class ForkPublisher implements NodeMain{

    private final static java.lang.String FORK_PUBLISHER = "ForkPublisher";
    private Publisher<std_msgs.Float64> heightPublisher;
    private Publisher<std_msgs.Float64> reachPublisher;

    private Context context;
    private ConnectedNode connectedNode;

    public ForkPublisher(){
        Log.d("TAG", "default");
    }

    @Override
    public GraphName getDefaultNodeName() {
        return null;
    }

    @Override
    public void onStart(ConnectedNode connectedNode) {
        this.connectedNode = connectedNode;
        heightPublisher = connectedNode.newPublisher("minireach/fork_position_controller/command", Float64._TYPE);
        //reachPublisher = connectedNode.newPublisher()
    }

    @Override
    public void onShutdown(Node node) {

    }

    @Override
    public void onShutdownComplete(Node node) {

    }

    @Override
    public void onError(Node node, Throwable throwable) {

    }

    //Publishing data to fork position controller topic, 0 to 1 meter
    public void publishHeightData(float data){
        Log.d(FORK_PUBLISHER, "Height value = " + data);

        std_msgs.Float64 msgsContainer = heightPublisher.newMessage();
        msgsContainer.setData(data);
        heightPublisher.publish(msgsContainer);
    }

    //Publishing data to reach position controller topic, 0 to 1 meter
    public void publishReachData(float data){
        Log.d(FORK_PUBLISHER, "Reach value = " + data);

        std_msgs.Float64 msgsContainer = reachPublisher.newMessage();
        msgsContainer.setData(data);
        reachPublisher.publish(msgsContainer);
    }
}
