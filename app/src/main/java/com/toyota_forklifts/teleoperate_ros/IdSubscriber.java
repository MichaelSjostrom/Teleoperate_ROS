package com.toyota_forklifts.teleoperate_ros;

import android.app.Activity;
import android.widget.ArrayAdapter;
import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.ConnectedNode;
import org.ros.node.Node;
import org.ros.node.NodeMain;
import org.ros.node.topic.Subscriber;
import java.util.ArrayList;
import ar_track_alvar_msgs.AlvarMarkers;

/**
 * Created by mitch on 2016-07-04.
 */
public class IdSubscriber implements NodeMain {

    private final static String ID_SUBSCRIBER = "IdSubscriber";

    private Subscriber<ar_track_alvar_msgs.AlvarMarkers> alvarMarkerSubscriber;
    private ArrayList<Integer> idArray;
    private ArrayList<String> arrayList;
    private ArrayAdapter arrayAdapter;
    private Activity activity;

    public IdSubscriber(ArrayList arrayList, ArrayAdapter arrayAdapter, Activity activity){
        this.arrayList = arrayList;
        this.arrayAdapter = arrayAdapter;
        this.activity = activity;
    }

    @Override
    public GraphName getDefaultNodeName() {
        return null;
    }

    @Override
    public void onStart(ConnectedNode connectedNode) {

        idArray = new ArrayList<>();

        //New subscriber that listens to "/ar_pose_marker"-topic
        alvarMarkerSubscriber = connectedNode.newSubscriber("/ar_pose_marker", AlvarMarkers._TYPE);
        alvarMarkerSubscriber.addMessageListener(new MessageListener<AlvarMarkers>() {
            @Override
            public void onNewMessage(AlvarMarkers alvarMarkers) {

                //Adding ID to the list
                if(alvarMarkers.getMarkers().size() != 0){
                    for(int i = 0; i < alvarMarkers.getMarkers().size(); i++){
                        if(!idArray.contains(alvarMarkers.getMarkers().get(i).getId()))
                            idArray.add(alvarMarkers.getMarkers().get(i).getId());

                        if(!arrayList.contains(String.valueOf(alvarMarkers.getMarkers().get(i).getId()))) {
                            arrayList.add(String.valueOf(alvarMarkers.getMarkers().get(i).getId()));
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    arrayAdapter.notifyDataSetChanged();
                                }
                            });
                        }
                    }
                }
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
