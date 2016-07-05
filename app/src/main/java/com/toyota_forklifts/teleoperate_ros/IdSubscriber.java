package com.toyota_forklifts.teleoperate_ros;

import android.util.Log;

import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.ConnectedNode;
import org.ros.node.Node;
import org.ros.node.NodeMain;
import org.ros.node.topic.Subscriber;

import ar_track_alvar_msgs.AlvarMarker;
import ar_track_alvar_msgs.AlvarMarkers;

/**
 * Created by mitch on 2016-07-04.
 */
public class IdSubscriber implements NodeMain {

    Subscriber<ar_track_alvar_msgs.AlvarMarkers> alvarMarkerSubscriber;

    @Override
    public GraphName getDefaultNodeName() {
        return null;
    }

    @Override
    public void onStart(ConnectedNode connectedNode) {
        alvarMarkerSubscriber = connectedNode.newSubscriber("/ar_pose_marker", AlvarMarkers._TYPE);
        alvarMarkerSubscriber.addMessageListener(new MessageListener<AlvarMarkers>() {
            @Override
            public void onNewMessage(AlvarMarkers alvarMarkers) {
                Log.d("TAG", "shit shit shit ");
            }
        });
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
}
