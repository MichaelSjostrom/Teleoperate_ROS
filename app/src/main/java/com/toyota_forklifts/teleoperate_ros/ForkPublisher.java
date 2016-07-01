package com.toyota_forklifts.teleoperate_ros;

import android.content.Context;

import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Publisher;

import std_msgs.Float64;
import std_msgs.String;

/**
 * Created by mitch on 2016-07-01.
 */
public class ForkPublisher extends AbstractNodeMain{

    private Publisher<String> wordPublisher;
    private Context context;

    public ForkPublisher(Context context){
        this.context = context;
    }

    @Override
    public GraphName getDefaultNodeName() {
        return GraphName.of("talker");
    }

    @Override
    public void onStart(ConnectedNode connectedNode){
        wordPublisher = connectedNode.newPublisher(context.getString(R.string.fork_controller_topic), Float64._TYPE);

    }

    public void publish(java.lang.String str_word){
        std_msgs.String str = wordPublisher.newMessage();
        str.setData(str_word);
        wordPublisher.publish(str);
    }
}
