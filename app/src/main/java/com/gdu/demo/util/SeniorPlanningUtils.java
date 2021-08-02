package com.gdu.demo.util;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.gdu.api.GduDroneApi;
import com.gdu.api.GduInfoManager;
import com.gdu.api.GduSettingManager;
import com.gdu.api.RoutePlanning.EnumPointAction;
import com.gdu.api.RoutePlanning.EnumRoutePlanningErrStatus;
import com.gdu.api.RoutePlanning.EnumRoutePlanningOrder;
import com.gdu.api.RoutePlanning.EnumRoutePlanningRunningStatus;
import com.gdu.api.RoutePlanning.EnumRoutePlanningTaskStatus;
import com.gdu.api.RoutePlanning.OnRouteCmdListener;
import com.gdu.api.RoutePlanning.OnRoutePlanListener;
import com.gdu.api.RoutePlanning.RoutePlanBean;
import com.gdu.api.RoutePlanning.RoutePlanManager;
import com.gdu.api.RoutePlanning.RoutePlanPoint;
import com.gdu.demo.R;
import com.gdu.drone.DroneInfo;
import com.gdu.drone.GimbalType;

import java.util.ArrayList;
import java.util.List;

public class SeniorPlanningUtils
{
    private StringBuffer stringBuffer;
    private RoutePlanManager routePlanManager;
    private TextView textView;
    private Activity activity;
    private int MY_PERMISSIONS_REQUEST_WRITESDK = 1;

    private GduSettingManager mGduSettingManager;
    private GduInfoManager mGduInfoManager;

    public SeniorPlanningUtils(Activity activity, TextView textView )
    {
        stringBuffer = new StringBuffer();
        routePlanManager = GduDroneApi.getInstance().getRoutePlanManager();
        mGduSettingManager = new GduSettingManager();
        GduDroneApi.getInstance().setCurrentGimbal(GimbalType.ByrdT_EnWei_Zoom);
        mGduInfoManager = GduInfoManager.getInstance(GduDroneApi.getInstance());
        routePlanManager.setOnRoutePlanListener(onRoutePlanListener);
        this.textView = textView;
        this.activity = activity;
    }

    private Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what)
            {
                case 0:
                    stringBuffer.append("\n").append(msg.obj.toString());
                    textView.setText(stringBuffer.toString());
                    break;
                case 1:
                    stringBuffer.append("\n").append(msg.obj.toString());
                    textView.setText(stringBuffer.toString());
                    break;
            }
        }
    };

    /***********************************
     * 自动航迹的监听事件----ron
     */
    private OnRoutePlanListener onRoutePlanListener = new OnRoutePlanListener() {
        @Override
        public void beginCreateRoutePlan()
        {
            Log.e("onRoutePlanListener","beginCreateRoutePlan");
            handler.obtainMessage(0,"beginCreateRoutePlan").sendToTarget();
        }

        @Override
        public void createRoutePlanSuccess(boolean isSuccess, String routePlanningNum)
        {
            Log.e("onRoutePlanListener","isSuccess:"+ isSuccess + "," + routePlanningNum);
            handler.obtainMessage(0,"createRoutePlanSuccess:"+ isSuccess +","+ routePlanningNum).sendToTarget();
        }

        @Override
        public void beginSendRoutePlan2Drone() {
            handler.obtainMessage(0,"beginSendRoutePlan2Drone").sendToTarget();
        }

        @Override
        public void progressOfSendRoutePlanning2Drone(float progress) {
            handler.obtainMessage(0,"progressOfSendRoutePlanning2Drone:"+ progress).sendToTarget();
        }

        @Override
        public void sendRoutePlane2DroneSuccess(boolean isSuccess) {
            handler.obtainMessage(0,"sendRoutePlane2DroneSuccess:"+ isSuccess).sendToTarget();
        }

        @Override
        public void OnRoutePlanningExit(EnumRoutePlanningErrStatus errStatus) {
            Log.e("onRoutePlanListener","OnRoutePlanningExit:"+ errStatus.name());
            handler.obtainMessage(0,"OnRoutePlanningExit:"+ errStatus.name()).sendToTarget();
        }

        @Override
        public void onRoutePlanningRunningStatus(EnumRoutePlanningRunningStatus status, short num, EnumRoutePlanningTaskStatus taskStatus) {
            if (status == EnumRoutePlanningRunningStatus.FINISH) {
                handler.obtainMessage(1,"onRoutePlanningRunningStatus:"+ status.name() + ",num"+ num).sendToTarget();
            } else {
                handler.obtainMessage(0,"onRoutePlanningRunningStatus:"+ status.name() + ",num"+ num).sendToTarget();
            }

        }

        @Override
        public void onMsgLog(String msg) {
            handler.obtainMessage(0,msg).sendToTarget();
        }
    };

    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.btn_begin:
//                setR8Action2();
                routePlanManager.sendTaskStartCmd(EnumRoutePlanningOrder.TASK_BEGIN, new OnRouteCmdListener() {
                    @Override
                    public void setSuccess(boolean isSuccess, Object object) {
                        handler.obtainMessage(0,"开启任务:"+ isSuccess + ","+ object.toString() ).sendToTarget();
                    }
                });
                return;
            case R.id.btn_pause:
                routePlanManager.sendTaskStartCmd(EnumRoutePlanningOrder.TASK_PAUSE, new OnRouteCmdListener() {
                    @Override
                    public void setSuccess(boolean isSuccess, Object object) {
                        handler.obtainMessage(0,"暂停任务:"+ isSuccess +","+ object.toString()).sendToTarget();
                    }
                });
                return;
            case R.id.btn_goon:
                routePlanManager.sendTaskStartCmd(EnumRoutePlanningOrder.TASK_GOON, new OnRouteCmdListener() {
                    @Override
                    public void setSuccess(boolean isSuccess, Object object) {
                        handler.obtainMessage(0,"继续任务:"+ isSuccess +  ","+ object.toString()).sendToTarget();
                    }
                });
                return;
            case R.id.btn_end:
                routePlanManager.sendTaskStartCmd(EnumRoutePlanningOrder.TASK_END, new OnRouteCmdListener() {
                    @Override
                    public void setSuccess(boolean isSuccess, Object object) {
                        handler.obtainMessage(0,"结束任务:"+ isSuccess +  ","+ object.toString()).sendToTarget();
                    }
                });
                return;

        }
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},MY_PERMISSIONS_REQUEST_WRITESDK
            );
            return;
        }
        List<RoutePlanPoint> data = new ArrayList<>();
//        for (int i = 0 ; i < 1500 ; i ++ )
//        {
//            RoutePlanPoint pathPlanBean = new RoutePlanPoint();
//            pathPlanBean.latitude = 34.1515101 ;
//            pathPlanBean.longitude = 114.1515102;
//            pathPlanBean.droneHeadAngle = 80;
//            pathPlanBean.height = 20;
//            List<RoutePlanBean.subActionBean> subActionBeans = new ArrayList<>();
//            RoutePlanBean.subActionBean subActionBean1 = new RoutePlanBean.subActionBean(EnumPointAction.GimbalAngle, "-30");
//            RoutePlanBean.subActionBean subActionBean3 = new RoutePlanBean.subActionBean(EnumPointAction.TakePhoto, "");
//            RoutePlanBean.subActionBean subActionBean4 = new RoutePlanBean.subActionBean(EnumPointAction.GimbalAngle, "-45");
//            RoutePlanBean.subActionBean subActionBean6 = new RoutePlanBean.subActionBean(EnumPointAction.TakePhoto, "");
//            RoutePlanBean.subActionBean subActionBean7 = new RoutePlanBean.subActionBean(EnumPointAction.GimbalAngle, "-75");
//            RoutePlanBean.subActionBean subActionBean9 = new RoutePlanBean.subActionBean(EnumPointAction.TakePhoto, "");
//            subActionBeans.add(subActionBean1);
//            subActionBeans.add(subActionBean3);
//            subActionBeans.add(subActionBean4);
//            subActionBeans.add(subActionBean6);
//            subActionBeans.add(subActionBean7);
//            subActionBeans.add(subActionBean9);
//            pathPlanBean.actions = subActionBeans;
//            data.add(pathPlanBean);
//        }
//        routePlanManager.updateSeniorPlanning2Drone(createPlanPoints(),-45,20,5,2);
//        List<RoutePlanPoint> data = createPlanPoints();
        if(stringBuffer!= null && stringBuffer.length() >  0)
        stringBuffer.delete(0,stringBuffer.length()-1);
        data = createVR8();
        routePlanManager.updateSeniorPlanning2Drone(data, 0);
    }
    DroneInfo droneInfo;
    private List<RoutePlanPoint> createVR8(){
    droneInfo = mGduInfoManager.getDroneInfo();
    List<RoutePlanPoint> data = new ArrayList<>();
        RoutePlanPoint pathPlanBean = new RoutePlanPoint();
        pathPlanBean.latitude = droneInfo.getLatitude();
        pathPlanBean.longitude = droneInfo.getLongitude();
        pathPlanBean.droneHeadAngle = droneInfo.getPlaneAngle();
        pathPlanBean.height = droneInfo.getHeight();
        pathPlanBean.speed = 5;
        List<RoutePlanBean.subActionBean> subActionBeans = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            int currentRotation = -180 + i *  45;
            RoutePlanBean.subActionBean subActionBean1 = new RoutePlanBean.subActionBean(EnumPointAction.Rotation, "" + currentRotation);
            RoutePlanBean.subActionBean subActionBean11 = new RoutePlanBean.subActionBean(EnumPointAction.TakePhoto, "");
            subActionBeans.add(subActionBean1);
            subActionBeans.add(subActionBean11);
        }
        pathPlanBean.actions = subActionBeans;
        data.add(pathPlanBean);
    return data;
//    routePlanManager.updateSeniorPlanning2Drone(data, 0);
//    routePlanManager.setOnRoutePlanListener(new OnRoutePlanListener() {
//        @Override
//        public void beginCreateRoutePlan() {
//            runOnUiThread(() -> {
//
//                ToastUtils.showToast("beginCreateRoutePlan----");
//            });
//        }
//
//        @Override
//        public void createRoutePlanSuccess(boolean b, String s) {
//            runOnUiThread(() -> {
//
//                ToastUtils.showToastUI(CameraActivity.this, "createRoutePlanSuccess----b=" + b + "--s=" + s);
//            });
//        }
//
//        @Override
//        public void beginSendRoutePlan2Drone() {
//            runOnUiThread(() -> {
//
//                ToastUtils.showToastUI(CameraActivity.this, "beginSendRoutePlan2Drone----");
//            });
//        }
//
//        @Override
//        public void progressOfSendRoutePlanning2Drone(float v) {
//            runOnUiThread(() -> {
//
//                ToastUtils.showToastUI(CameraActivity.this, "progressOfSendRoutePlanning2Drone----");
//            });
//        }
//
//        @Override
//        public void sendRoutePlane2DroneSuccess(boolean b) {
//            //上传成功  去开启任务进行拍摄
//            runOnUiThread(() -> routePlanManager.sendTaskStartCmd(EnumRoutePlanningOrder.TASK_BEGIN, new OnRouteCmdListener() {
//                @Override
//                public void setSuccess(boolean isSuccess, Object object) {
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            LogUtil.e("VR8拍摄", isSuccess);
////                                    handler.obtainMessage(0,"开启任务:"+ isSuccess + ","+ object.toString() ).sendToTarget();
//                            ToastUtils.showToastUI(CameraActivity.this, "sendTaskStartCmd----" + isSuccess);
//                            routePlanManager.takeOffAndStartTask(new OnRouteCmdListener() {
//                                @Override
//                                public void setSuccess(boolean b1, Object o) {
//                                    runOnUiThread(new Runnable() {
//                                        @Override
//                                        public void run() {
//                                            LogUtil.e("开始执行任务" + b1, new Gson().toJson(o));
//                                            ToastUtils.showToastUI(CameraActivity.this, "takeOffAndStartTask+开始执行任务----" + b1);
//                                        }
//                                    });
//
//                                }
//                            });
//                        }
//                    });
//
//                }
//            }));
//
//
//        }
//
//        @Override
//        public void OnRoutePlanningExit(EnumRoutePlanningErrStatus enumRoutePlanningErrStatus) {
//            runOnUiThread(() -> {
//
//                ToastUtils.showToastUI(CameraActivity.this, "OnRoutePlanningExit----");
//            });
//        }
//
//        @Override
//        public void onRoutePlanningRunningStatus(EnumRoutePlanningRunningStatus enumRoutePlanningRunningStatus, short i, EnumRoutePlanningTaskStatus enumRoutePlanningTaskStatus) {
//            runOnUiThread(() -> {
//                ToastUtils.showToastUI(CameraActivity.this, "onRoutePlanningRunningStatus----");
//            });
//        }
//
//        @Override
//        public void onMsgLog(String s) {
//            runOnUiThread(() -> {
//
//                ToastUtils.showToastUI(CameraActivity.this, "onMsgLog----");
//            });
//        }
//    });
}

    private List<RoutePlanPoint> createPlanPoints()
    {
        List<RoutePlanPoint> data = new ArrayList<>();

        RoutePlanPoint pathPlanBean1 = new RoutePlanPoint();
        pathPlanBean1.latitude = 30.4695301d;
        pathPlanBean1.longitude = 114.4182281d;
        pathPlanBean1.height = 30;
        pathPlanBean1.speed = 5;
        List<RoutePlanBean.subActionBean> subActionBeans = new ArrayList<>();
        RoutePlanBean.subActionBean subActionBean = new RoutePlanBean.subActionBean(EnumPointAction.TakePhoto, "");
        subActionBeans.add(subActionBean);
        pathPlanBean1.actions = subActionBeans;
        pathPlanBean1.turningPoint = 1;
        data.add(pathPlanBean1);

        RoutePlanPoint pathPlanBean2 = new RoutePlanPoint();
        pathPlanBean2.latitude = 30.4695301d;
        pathPlanBean2.longitude = 114.4184265d;
        pathPlanBean2.height = 30;
        pathPlanBean2.speed = 5;
        List<RoutePlanBean.subActionBean> subActionBeans2 = new ArrayList<>();
        RoutePlanBean.subActionBean subActionBean2 = new RoutePlanBean.subActionBean(EnumPointAction.TakePhoto, "");
        subActionBeans2.add(subActionBean2);
        pathPlanBean2.actions = subActionBeans2;
        pathPlanBean2.turningPoint = 0;
        data.add(pathPlanBean2);

        RoutePlanPoint pathPlanBean3 = new RoutePlanPoint();
        pathPlanBean3.latitude = 30.4695320d;
        pathPlanBean3.longitude = 114.4186249d;
        pathPlanBean3.height = 30;
        pathPlanBean3.speed = 5;
        List<RoutePlanBean.subActionBean> subActionBeans3 = new ArrayList<>();
        RoutePlanBean.subActionBean subActionBean3 = new RoutePlanBean.subActionBean(EnumPointAction.TakePhoto, "");
        subActionBeans3.add(subActionBean3);
        pathPlanBean3.actions = subActionBeans3;
        pathPlanBean3.turningPoint = 0;
        data.add(pathPlanBean3);

        RoutePlanPoint pathPlanBean4 = new RoutePlanPoint();
        pathPlanBean4.latitude = 30.4695320d;
        pathPlanBean4.longitude = 114.4188309d;
        pathPlanBean4.height = 30;
        pathPlanBean4.speed = 5;
        List<RoutePlanBean.subActionBean> subActionBeans4 = new ArrayList<>();
        RoutePlanBean.subActionBean subActionBean4 = new RoutePlanBean.subActionBean(EnumPointAction.TakePhoto, "");
        subActionBeans4.add(subActionBean4);
        pathPlanBean4.actions = subActionBeans4;
        pathPlanBean4.turningPoint = 1;
        data.add(pathPlanBean4);

        RoutePlanPoint pathPlanBean5 = new RoutePlanPoint();
        pathPlanBean5.latitude = 30.4695339d;
        pathPlanBean5.longitude = 114.4190292d;
        pathPlanBean5.height = 30;
        pathPlanBean5.speed = 5;
        List<RoutePlanBean.subActionBean> subActionBeans5 = new ArrayList<>();
        RoutePlanBean.subActionBean subActionBean5 = new RoutePlanBean.subActionBean(EnumPointAction.TakePhoto, "");
        subActionBeans5.add(subActionBean5);
        pathPlanBean5.actions = subActionBeans5;
        pathPlanBean5.turningPoint = 0;
        data.add(pathPlanBean5);

        RoutePlanPoint pathPlanBean6 = new RoutePlanPoint();
        pathPlanBean6.latitude = 30.4695339d;
        pathPlanBean6.longitude = 114.4192276d;
        pathPlanBean6.height = 30;
        pathPlanBean6.speed = 4;
        List<RoutePlanBean.subActionBean> subActionBeans6 = new ArrayList<>();
        RoutePlanBean.subActionBean subActionBean6 = new RoutePlanBean.subActionBean(EnumPointAction.TakePhoto, "");
        subActionBeans6.add(subActionBean6);
        pathPlanBean6.actions = subActionBeans6;
        pathPlanBean6.turningPoint = 0;
        data.add(pathPlanBean6);

        RoutePlanPoint pathPlanBean7 = new RoutePlanPoint();
        pathPlanBean7.latitude = 30.4695358;
        pathPlanBean7.longitude = 114.4194260;
        pathPlanBean7.height = 30;
        pathPlanBean7.speed = 5;
        List<RoutePlanBean.subActionBean> subActionBeans7 = new ArrayList<>();
        RoutePlanBean.subActionBean subActionBean7 = new RoutePlanBean.subActionBean(EnumPointAction.TakePhoto, "");
        subActionBeans7.add(subActionBean7);
        pathPlanBean7.actions = subActionBeans7;
        pathPlanBean7.turningPoint = 1;
        data.add(pathPlanBean7);
//
//        RoutePlanPoint pathPlanBean8= new RoutePlanPoint();
//        pathPlanBean8.latitude = 30.4691124d;
//        pathPlanBean8.longitude = 114.4186707d;
//        data.add(pathPlanBean8);
//
//        RoutePlanPoint pathPlanBean9= new RoutePlanPoint();
//        pathPlanBean9.latitude = 30.4689465d;
//        pathPlanBean9.longitude = 114.4187241d;
//        data.add(pathPlanBean9);
        return data;
    }

    private void setR8Action2() {
        routePlanManager.setOnRoutePlanListener(new OnRoutePlanListener() {
            @Override
            public void beginCreateRoutePlan() {
                System.out.println("test beginCreateRoutePlan ");
//                runOnUiThread(() -> {
//                    ToastUtils.showToast("beginCreateRoutePlan----");
//                    mBinding.tvLog.append("beginCreateRoutePlan----"+"\r\n");
//                });
            }

            @Override
            public void createRoutePlanSuccess(boolean b, String s) {
                System.out.println("test beginCreateRoutePlan " + b + "  " + s);
//                runOnUiThread(() -> {
//                    ToastUtils.showToastUI(CameraActivity.this, "createRoutePlanSuccess----b=" + b + "--s=" + s);
//                    mBinding.tvLog.append("createRoutePlanSuccess----b=\" + b + \"--s=\" + s"+"\r\n");
//                });
            }

            @Override
            public void beginSendRoutePlan2Drone() {
                System.out.println("test beginSendRoutePlan2Drone ");
//                runOnUiThread(() -> {
//                    ToastUtils.showToastUI(CameraActivity.this, "beginSendRoutePlan2Drone----");
//                    mBinding.tvLog.append("beginSendRoutePlan2Drone----"+"\r\n");
//                });
            }

            @Override
            public void progressOfSendRoutePlanning2Drone(float v) {
                System.out.println("test beginSprogressOfSendRoutePlanning2Drone " + v);
//                runOnUiThread(() -> {
//                    ToastUtils.showToastUI(CameraActivity.this, "progressOfSendRoutePlanning2Drone----");
//                    mBinding.tvLog.append("progressOfSendRoutePlanning2Drone----"+"\r\n");
//                });
            }

            @Override
            public void sendRoutePlane2DroneSuccess(boolean b) {
                //上传成功  去开启任务进行拍摄
                System.out.println("test beginSsendRoutePlane2DroneSuccesse " + b);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        routePlanManager.sendTaskStartCmd(EnumRoutePlanningOrder.TASK_BEGIN, new OnRouteCmdListener() {
                            @Override
                            public void setSuccess(boolean isSuccess, Object object) {
                                handler.obtainMessage(0,"开启任务:"+ isSuccess + ","+ object.toString() ).sendToTarget();
                            }
                        });
                    }
                });
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        ToastUtils.showToastUI(CameraActivity.this, "sendRoutePlane2DroneSuccess----" + b);
//                        mBinding.tvLog.append("sendRoutePlane2DroneSuccess----"+"\r\n");
//
//                    }
//                });

            }

            @Override
            public void OnRoutePlanningExit(EnumRoutePlanningErrStatus enumRoutePlanningErrStatus) {
                System.out.println("test beginOnRoutePlanningExit " + enumRoutePlanningErrStatus);
//                runOnUiThread(() -> {
//                    mBinding.tvLog.append("OnRoutePlanningExit----"+ enumRoutePlanningErrStatus + "\r\n");
//                    ToastUtils.showToastUI(CameraActivity.this, "OnRoutePlanningExit----" +  enumRoutePlanningErrStatus);
//                });
            }

            @Override
            public void onRoutePlanningRunningStatus(EnumRoutePlanningRunningStatus enumRoutePlanningRunningStatus, short i, EnumRoutePlanningTaskStatus enumRoutePlanningTaskStatus) {
                System.out.println("test beginOonRoutePlanningRunningStatus " + enumRoutePlanningRunningStatus + " " + i);
                //                runOnUiThread(() -> {
//                    ToastUtils.showToastUI(CameraActivity.this, "onRoutePlanningRunningStatus----");
//                    mBinding.tvLog.append("onRoutePlanningRunningStatus----"+"\r\n");
//                });
            }

            @Override
            public void onMsgLog(String s) {
//                runOnUiThread(() -> {
//                    mBinding.tvLog.append("onMsgLog----"+"\r\n");
//                    ToastUtils.showToastUI(CameraActivity.this, "onMsgLog----");
//                });
            }
        });
        droneInfo = mGduInfoManager.getDroneInfo();
        List<RoutePlanPoint> data = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            RoutePlanPoint pathPlanBean = new RoutePlanPoint();
            pathPlanBean.latitude = droneInfo.getLatitude();
            pathPlanBean.longitude = droneInfo.getLongitude();
            pathPlanBean.droneHeadAngle = droneInfo.getPlaneAngle();
            pathPlanBean.height = droneInfo.getHeight();
            pathPlanBean.speed = 5;
            List<RoutePlanBean.subActionBean> subActionBeans = new ArrayList<>();
            int currentRount = -180 + 45 * i;
            RoutePlanBean.subActionBean subActionBean1 = new RoutePlanBean.subActionBean(EnumPointAction.Rotation, currentRount + "");
            RoutePlanBean.subActionBean subActionBean11 = new RoutePlanBean.subActionBean(EnumPointAction.TakePhoto, "");
            subActionBeans.add(subActionBean1);
            subActionBeans.add(subActionBean11);
            pathPlanBean.actions = subActionBeans;
            data.add(pathPlanBean);

        }
        routePlanManager.updateSeniorPlanning2Drone(data, 0);


    }
}
