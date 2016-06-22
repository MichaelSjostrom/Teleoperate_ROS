package com.toyota_forklifts.teleoperate_ros;

import java.util.concurrent.ExecutorService;
import android.content.Context;
import android.os.Handler;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;

import com.github.rosjava.android_remocons.common_tools.apps.AppParameters;

import org.ros.android.view.RosImageView;
import org.ros.android.view.visualization.RotateGestureDetector;
import org.ros.android.view.visualization.VisualizationView;
import org.ros.android.view.visualization.layer.CameraControlLayer;
import org.ros.android.view.visualization.layer.CameraControlListener;
import org.ros.concurrent.ListenerGroup;
import org.ros.concurrent.SignalRunnable;
import org.ros.node.ConnectedNode;
import org.ros.rosjava_geometry.FrameTransformTree;

import java.util.concurrent.ExecutorService;

import sensor_msgs.CompressedImage;

/**
 * Created by mitch on 2016-06-21.
 */
public class ViewControlLayer extends CameraControlLayer {

    private static final String ROBOT_FRAME = "base_link";
    private final Context context;
    private final ListenerGroup<CameraControlListener> listeners ;

    private GestureDetector translateGestureDetector;
    private RotateGestureDetector rotateGestureDetector;
    private ScaleGestureDetector zoomGestureDetector;

    private RosImageView<CompressedImage> cameraView;
    private VisualizationView mapView;
    private ViewGroup mainLayout;
    private ViewGroup sideLayout;
    private boolean mapViewGestureAvaiable;

    private enum ViewMode {
        CAMERA, MAP
    };
    private ViewMode viewMode;
    private String robotFrame;

    public ViewControlLayer(Context context,
                            ExecutorService executorService,
                            RosImageView<sensor_msgs.CompressedImage> cameraView,
                            VisualizationView mapView,
                            ViewGroup mainLayout,
                            ViewGroup sideLayout,
                            AppParameters params){

        this.context = context;
        listeners = new ListenerGroup<CameraControlListener>(executorService);

        this.cameraView = cameraView;
        this.mapView = mapView;
        this.mainLayout = mainLayout;
        this.sideLayout = sideLayout;

        viewMode = ViewMode.CAMERA;
        this.cameraView.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                //swapViews();
            }
        });

        this.mapView.setClickable(true);
        this.cameraView.setClickable(false);
        this.robotFrame = (String) params.get("robot_frame", context.getString(R.string.robot_frame));

        mapViewGestureAvaiable = false;
    }

    @Override
    public boolean onTouchEvent(VisualizationView view, MotionEvent event){

        if(viewMode == ViewMode.CAMERA){
            //swapViews();
            return true;
        }

        return false;
    }

    private void swapViews() {
        ViewGroup mapViewParent;
        ViewGroup cameraViewparent;

    }
}
