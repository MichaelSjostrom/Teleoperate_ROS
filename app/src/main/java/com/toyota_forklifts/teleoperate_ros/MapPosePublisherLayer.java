package com.toyota_forklifts.teleoperate_ros;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import com.github.rosjava.android_remocons.common_tools.apps.AppParameters;
import com.google.common.base.Preconditions;
import org.ros.android.view.visualization.Color;
import org.ros.android.view.visualization.VisualizationView;
import org.ros.android.view.visualization.layer.DefaultLayer;
import org.ros.android.view.visualization.layer.PathLayer;
import org.ros.android.view.visualization.shape.PixelSpacePoseShape;
import org.ros.android.view.visualization.shape.Shape;
import org.ros.namespace.GraphName;
import org.ros.node.ConnectedNode;
import org.ros.node.Node;
import org.ros.node.topic.Publisher;
import org.ros.rosjava_geometry.Transform;
import org.ros.rosjava_geometry.Vector3;
import javax.microedition.khronos.opengles.GL10;
import geometry_msgs.PoseStamped;
import geometry_msgs.PoseWithCovarianceStamped;
import move_base_msgs.MoveBaseActionGoal;


/**
 * Created by mitch on 2016-06-23.
 */
public class MapPosePublisherLayer extends DefaultLayer {

    private Shape shape;
    private Publisher<geometry_msgs.PoseWithCovarianceStamped> initialPosePublisher;
    private Publisher<PoseStamped> androidGoalPublisher;
    private Publisher<MoveBaseActionGoal> goalPublisher;
    private PathLayer pathLayer;
    private boolean visible;
    private GestureDetector gestureDetector;
    private Transform pose;
    private Transform fixedPose;
    private ConnectedNode connectedNode;
    private int mode;
    private static final int POSE_MODE = 0;
    private static final int GOAL_MODE = 1;

    private String mapFrame;
    private String robotFrame;
    private String initialPoseTopic;
    private String simpleGoalTopic;
    private String moveBaseGoalTopic;

    public MapPosePublisherLayer(final Context context,
                                 final AppParameters params) {

        visible = false;

        this.mapFrame = (String) params.get("map_frame", context.getString(R.string.map_frame));
        this.robotFrame = (String) params.get("robot_frame", context.getString(R.string.robot_frame));

        this.initialPoseTopic = "/initialpose";
        this.simpleGoalTopic = "/move_base_simple/goal";
        this.moveBaseGoalTopic = "/move_base/goal";

        this.pathLayer = pathLayer;

    }

    public void setPoseMode() {
        mode = POSE_MODE;
    }

    public void setGoalMode() {
        mode = GOAL_MODE;
    }

    //Draws the triangle
    @Override
    public void draw(VisualizationView view, GL10 gl) {
        if(pose != null) {
            Preconditions.checkNotNull(pose);
            shape.draw(view, gl);
        }
    }

    //Calculates the angle between posevector and the finger
    private double angle(double x1, double y1, double x2, double y2) {
        double deltaX = x1 - x2;
        double deltaY = y1 - y2;
        return Math.atan2(deltaY, deltaX);
    }

    //Listens to touch events
    @Override
    public boolean onTouchEvent(VisualizationView view, MotionEvent event) {
        //touchEvents are only handled when the when a longPress has occurred and
        //visible == true
        if (visible) {
            Preconditions.checkNotNull(pose);

            Vector3 poseVector;
            Vector3 pointerVector;

            //ACTION_MOVE when screen is pressed and finger is moved to another position
            if (event.getAction() == MotionEvent.ACTION_MOVE) {

                poseVector = pose.apply(Vector3.zero());
                pointerVector = view.getCamera().toCameraFrame((int) event.getX(),
                        (int) event.getY());

                double angle = angle(pointerVector.getX(),
                        pointerVector.getY(), poseVector.getX(),
                        poseVector.getY());
                pose = Transform.translation(poseVector).multiply(
                        Transform.zRotation(angle));

                shape.setTransform(pose);
                return true;
            }
            //ACION_UP when finger is released from the screen
            if (event.getAction() == MotionEvent.ACTION_UP) {

                PoseStamped poseStamped;
                switch (mode) {
                    case POSE_MODE:

                        view.getCamera().setFrame(mapFrame);
                        poseVector = fixedPose.apply(Vector3.zero());
                        pointerVector = view.getCamera().toCameraFrame(
                                (int) event.getX(), (int) event.getY());
                        double angle2 = angle(pointerVector.getX(),
                                pointerVector.getY(), poseVector.getX(),
                                poseVector.getY());
                        fixedPose = Transform.translation(poseVector).multiply(
                                Transform.zRotation(angle2));
                        view.getCamera().setFrame(robotFrame);
                        poseStamped = fixedPose.toPoseStampedMessage(
                                GraphName.of(robotFrame),
                                connectedNode.getCurrentTime(),
                                androidGoalPublisher.newMessage());

                        PoseWithCovarianceStamped initialPose = initialPosePublisher.newMessage();
                        initialPose.getHeader().setFrameId(mapFrame);
                        initialPose.getPose().setPose(poseStamped.getPose());
                        double[] covariance = initialPose.getPose().getCovariance();
                        covariance[6 * 0 + 0] = 0.5 * 0.5;
                        covariance[6 * 1 + 1] = 0.5 * 0.5;
                        covariance[6 * 5 + 5] = (float) (Math.PI / 12.0 * Math.PI / 12.0);

                        initialPosePublisher.publish(initialPose);
                        break;
                    //GOAL_MODE is used the set a goal with a specified pose(position and angle)
                    case GOAL_MODE:

                        poseStamped = pose.toPoseStampedMessage(
                                GraphName.of(mapFrame),
                                connectedNode.getCurrentTime(),
                                androidGoalPublisher.newMessage());
                        androidGoalPublisher.publish(poseStamped);

                        move_base_msgs.MoveBaseActionGoal message = goalPublisher.newMessage();
                        message.setHeader(poseStamped.getHeader());
                        message.getGoalId().setStamp(connectedNode.getCurrentTime());
                        message.getGoalId().setId("move_base/move_base_client_android"
                                + connectedNode.getCurrentTime().toString());
                        message.getGoal().setTargetPose(poseStamped);
                        goalPublisher.publish(message);
                        break;
                }
                visible = false;
                return true;
            }
        }
        gestureDetector.onTouchEvent(event);
        return false;
    }

    @Override
    public void onStart(final VisualizationView view, ConnectedNode connectedNode) {
        this.connectedNode = connectedNode;
        shape = new PixelSpacePoseShape();
        Color color = new Color(0.6f,0.2f,0.5f,0.8f);
        shape.setColor(color);

        mode = GOAL_MODE;


        initialPosePublisher = connectedNode.newPublisher("/initialpose",
                "geometry_msgs/PoseWithCovarianceStamped");
        androidGoalPublisher = connectedNode.newPublisher("/move_base_simple/goal",
                "geometry_msgs/PoseStamped");
        goalPublisher = connectedNode.newPublisher("/move_base/goal",
                "move_base_msgs/MoveBaseActionGoal");

        //Runs a new thread that listens to gestures, in this case onLongPress
        view.post(new Runnable() {
            @Override
            public void run() {
                gestureDetector = new GestureDetector(view.getContext(),
                        new GestureDetector.SimpleOnGestureListener() {

                            @Override
                            public void onLongPress(MotionEvent e) {
                                pose = Transform.translation(view.getCamera().toCameraFrame(
                                        (int) e.getX(), (int) e.getY()));
                                shape.setTransform(pose);
                                view.getCamera().setFrame(mapFrame);
                                fixedPose = Transform.translation(view.getCamera().toCameraFrame(
                                        (int) e.getX(), (int) e.getY()));
                                //view.getCamera().setFrame(robotFrame);
                                visible = true;
                            }
                        });
            }
        });
    }

    @Override
    public void onShutdown(VisualizationView view, Node node) {
        initialPosePublisher.shutdown();
        androidGoalPublisher.shutdown();
        goalPublisher.shutdown();
    }

}
