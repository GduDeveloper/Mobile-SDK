package com.gdu.demo.flight.airlink;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.gdu.config.GduAppEnv;
import com.gdu.config.GduConfig;
import com.gdu.config.GlobalVariable;
import com.gdu.config.UavStaticVar;
import com.gdu.socket.GduFrame3;
import com.gdu.socket.GduFrameUtil3;
import com.gdu.socket.SocketCallBack3;
import com.gdu.socketmodel.GduSocketConfig3;
import com.gdu.util.ByteUtilsLowBefore;
import com.gdu.util.DataUtil;
import com.gdu.util.SPUtils;
import com.gdu.util.StringUtils;
import com.gdu.util.logger.MyLogUtils;
import com.gdu.util.logs.RonLog2FileLET;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;

/**
 * 备份图传管理类
 */
public class BackupsAirlinkManager implements IBackupsAirlink {

    private final static String TAG = "BackupsAirlinkManager";

    private final byte LOG_SYS_ID = GduSocketConfig3.ONBOARD_SYSTEMS_ID;  //打印log的系统id

    private final byte LOG_COM_ID = GduSocketConfig3.ONBOARD_COMP_FC_ID;  //打印log的组件id

    private final short LOG_MSG_ID = 0x00D1;      //打印log的消息id


    private final int RECEIVE_MQTT_MSG = 101;

    private int pushNum = 0;

    private int receiveNum = 0;

    private static BackupsAirlinkManager mBackupsAirlinkManager;

    private MqttAndroidClient mMqttAndroidClient;
    private MqttConnectOptions mqttConnectOptions;
    private Handler mHandler;
    private String mServerURI;

    private boolean isConnecting = false;

    private long lastSendRockerInfoTime;
    private long lastSendRcConnectTime;

    private SocketCallBack3 socketCallBack3;
    private byte packageNum = 0;
    private long sendRegisterTime = 0;

    private Thread  mReadThread;

    private static final int TIME_OUT = 1;

    private volatile boolean npsTimeoutRunning = false;

    private final BlockingQueue<byte[]> mDataQueue = new LinkedBlockingDeque<>(200);


    public static BackupsAirlinkManager getInstance(){
        if (mBackupsAirlinkManager == null) {
            mBackupsAirlinkManager = new BackupsAirlinkManager();
        }
        return mBackupsAirlinkManager;
    }

    private BackupsAirlinkManager(){

        getServerUri();
        initHandler();
        initRockerInfo();
        Observable.create((ObservableOnSubscribe<Integer>) emitter -> {
                    emitter.onNext(1);})
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(i ->{
                    initThread();
                });
//        GduApplication.getSingleApp().gduCommunication.getGduSocket().setOnGduUdpSocketListener(new GduUDPSocket3.OnGduUdpSocketListener() {
//            @Override
//            public void onLTEDataGot(byte[] data) {
//                pushData(data);
//            }
//        });
    }

    private final Runnable mReadRun = new Runnable() {
        @Override
        public void run() {

            try {
                while (true) {
                    byte[] data = mDataQueue.poll(TIME_OUT, TimeUnit.SECONDS);
                    if (data == null) {
                        continue;
                    }

                    if (isRightData(data)) {
                        receiveNum++;
//                        GduApplication.getSingleApp().gduCommunication.getGduSocket().handlerReceiveData(data, true);
                    }

                }

            } catch (Exception e) {
            }

        }
    };

    private boolean isRightData(byte[] data) {
        if (data.length < 11) {
            return false;
        }
        return (data[0] & 0xFF) == 0xFE && (data[1] & 0xFF) == 0x55;
    }


    private void getServerUri() {
        String url = SPUtils.getString(GduAppEnv.application, SPUtils.BACK_AIR_LINK_URL);
        if (TextUtils.isEmpty(url)) {
            mServerURI = "tcp://" + SERVER_IP + ":" + SERVER_PORT;
        } else {
            mServerURI = url;
        }
        MyLogUtils.i("getServerUri() mServerURI = " + mServerURI);
        GlobalVariable.BackAirLinkUrl = mServerURI;
    }


    private void checkNpsCallBack(byte[] data) {
        short msgId = ByteUtilsLowBefore.byte2short(data, 8);
        if (data[4] == GduSocketConfig3.ONBOARD_SYSTEMS_ID && data[5] == GduSocketConfig3.ONBOARD_COMP_NPC
                && data[6] == 0x04 && data[7] == 0x01 && msgId == 0x000A) {
            //需要取消超时监听，否则导致错误逻辑
            cancelNpsTimeout();
            if (socketCallBack3 != null && data[10] == packageNum) {
                socketCallBack3.callBack(GduSocketConfig3.SUCCESS_CODE, null);
            }
        }
    }


    public void initThread() {

        if (mReadThread == null) {
            mReadThread = new Thread(mReadRun);
            mReadThread.start();
        }

        if (mReadThread != null && !mReadThread.isAlive()) {
            mReadThread.interrupt();
            mReadThread = null;
            mReadThread = new Thread(mReadRun);
            mReadThread.start();
        }
    }


    private void initRockerInfo() {
//        GduApplication.getSingleApp().rcCustomKeyManager.addSocketCallBack3((code, gduFrame) -> {
//            if (gduFrame == null || gduFrame.frameContent == null) {
//                return;
//            }
//            if (!GlobalVariable.isUseBackupsAirlink) {
//                return;
//            }
//            // 上传航迹时减少发送遥感数据
//            if (GlobalVariable.isUploadFile) {
//                if (System.currentTimeMillis() - lastSendRockerInfoTime <= 1000 ) {
//                    return;
//                }
//                sendRockerData(gduFrame);
//                lastSendRockerInfoTime = System.currentTimeMillis();
//            } else {
//                sendRockerData(gduFrame);
//            }
//
//            if (GlobalVariable.connStateEnum == ConnStateEnum.Conn_Sucess) {
//                if (System.currentTimeMillis() - lastSendRcConnectTime <= 2000) {
//                    return;
//                }
//
//                GduApplication.getSingleApp().gduCommunication.setRockerConnectState((byte) 1, new SocketCallBack3() {
//                    @Override
//                    public void callBack(int code, GduFrame3 bean) {
//
//                    }
//                });
//                GduApplication.getSingleApp().gduCommunication.setFlyStateInfo(GlobalVariable.droneRawFlyState, null);
//                lastSendRcConnectTime = System.currentTimeMillis();
//            }
//
//        });
    }

    /**
     * 通过MQTT发送摇杆数据到飞机
     * @param gduFrame
     */
    private void sendRockerData(GduFrame3 gduFrame) {

        gduFrame.frameDesSysID = GduSocketConfig3.ONBOARD_SYSTEMS_ID;
        gduFrame.frameDesComID = GduSocketConfig3.ONBOARD_COMP_RC_ID;
        gduFrame.frameFlag = GduSocketConfig3.PROTOCOL_FLAG_REPORT;
        gduFrame.frameType = GduSocketConfig3.PROTOCOL_TYPE_MESSAGE;
        gduFrame.frameMsgID = (short) 0x0004;

        // 单片机串口发送给App的杆量 与发送给图传的不一致 需要调换一下
        if (GlobalVariable.isRCSEE || GlobalVariable.isCustomRC) {
            byte byte4 = gduFrame.frameContent[4];
            byte byte5 = gduFrame.frameContent[5];
            gduFrame.frameContent[4] = gduFrame.frameContent[6];
            gduFrame.frameContent[5] = gduFrame.frameContent[7];
            gduFrame.frameContent[6] = byte4;
            gduFrame.frameContent[7] = byte5;
            byte byte8 = gduFrame.frameContent[8];
            byte byte9 = gduFrame.frameContent[9];
            gduFrame.frameContent[8] = gduFrame.frameContent[10];
            gduFrame.frameContent[9] = gduFrame.frameContent[11];
            gduFrame.frameContent[10] = byte8;
            gduFrame.frameContent[11] = byte9;

            // bit7 是刹车 和bit6位置交换一下
            byte[] byte15 = DataUtil.getBit(gduFrame.frameContent[15]);
            byte bit6 = byte15[6];
            byte15[6] = byte15[7];
            byte15[7] = bit6;
            gduFrame.frameContent[15] = BitToByte(byte15);

            // 包序号 没变飞控不接受遥杆数据
            gduFrame.frameContent[18] = gduFrame.frameSerial;
        }
        byte[] data = GduFrameUtil3.gduFrame3ToByteArray(gduFrame);
        pushData(data);
    }


    public static byte BitToByte(byte[] bits) {
        byte temp = (byte) 0;
        for (int i = 0; i < bits.length; i++) {
            temp = (byte) (temp | bits[i] << i);
        }
        return temp ;
    }


    private void initHandler() {
        mHandler = new Handler(Looper.myLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == MQTT_CONNECT_SUCCEED) {
                    subscribeToTopic();
                }
            }
        };
    }


    /**
     * 初始化Mqtt  mClientId
     */
    public void initMqtt(MqttConnectCallback callback) {

        if (isConnected() && callback != null) {
            callback.onConnectSuccess();
            return;
        }
        String mClientId = AirlinkUtils.getSN();
        if (TextUtils.isEmpty(mClientId)) {
            AirlinkUtils.getUnique();
            if (callback != null) {
                callback.onConnectFail();
            }
            return;
        }

        changeService();

        if(mMqttAndroidClient==null) {
            int id = new Random().nextInt(100);
            mMqttAndroidClient = new MqttAndroidClient(GduAppEnv.application, mServerURI, mClientId + "_" + id);
            mMqttAndroidClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                }
            });
            mqttConnectOptions = new MqttConnectOptions();
            mqttConnectOptions.setKeepAliveInterval(5); // 心跳间隔5s 与飞机端保持同步
            mqttConnectOptions.setCleanSession(true);
            mqttConnectOptions.setConnectionTimeout(10);
            mqttConnectOptions.setUserName("admin");
            mqttConnectOptions.setPassword("admin@gdu".toCharArray());
        }

        if (isConnecting) {
            return;
        }
        // 更新SSL
//        mqttConnectOptions.setSocketFactory(XAEncryptApi.getSSLSocketFactory(mServerURI));

        try {
            isConnecting = true;
            mMqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {

                    if (mHandler != null) {
                        mHandler.sendEmptyMessage(MQTT_CONNECT_SUCCEED);
                    }

                    if (callback != null) {
                        callback.onConnectSuccess();
                    }
                    isConnecting = false;
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    if (callback != null) {
                        callback.onConnectFail();
                    }
                    isConnecting = false;

                    disconnect();
                }
            });
        } catch (Exception ex) {
            if (callback != null) {
                callback.onConnectFail();
            }
            isConnecting = false;
        }
    }

    private void changeService() {
        if (!GlobalVariable.BackAirLinkUrl.equals(mServerURI)) {
            mServerURI = GlobalVariable.BackAirLinkUrl;
            if (mMqttAndroidClient != null) {
                try {
                    mMqttAndroidClient.disconnect();
                    mMqttAndroidClient = null;
                } catch (Exception e) {

                }
            }
        }
    }

    /**
     * 判断mqtt是否连接
     * @return
     */
    public boolean isConnected() {
        return mMqttAndroidClient != null && mMqttAndroidClient.isConnected();
    }


    public void disconnect() {
        try {
            if (mMqttAndroidClient != null) {
                mMqttAndroidClient.disconnect();
                mMqttAndroidClient = null;
            }

        } catch (Exception e) {

        }
    }

    public void onDestroy() {
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        npsTimeoutRunning = false;
        disconnect();
        try {
            if (mReadThread != null) {
                mReadThread.interrupt();
                mReadThread = null;
            }
        } catch (Exception e) {

        }
    }

    /**
     * 订阅消息
     */
    public void subscribeToTopic() {
        MyLogUtils.i("subscribeToTopic()");
        try {
            if(mMqttAndroidClient == null){
                return;
            }
            String currentSN = AirlinkUtils.getSN();
            if (currentSN == null) {
                return;
            }
            //主题
            mMqttAndroidClient.subscribe(DRONE_INFO_TO_APP_TOPIC + currentSN, 0, new IMqttMessageListener() {
                @Override
                public void messageArrived(String topic, MqttMessage message)  {
                    if ( message != null) {
                        byte[] data = message.getPayload();
                        if (data != null) {
//                            data = XAEncryptApi.doVerifySign(data, true);
                            GlobalVariable.lastReceiveLteMsgTime = System.currentTimeMillis();

//                            checkNpsCallBack(data);
//                            boolean isSuccess = mDataQueue.offer(data);
//                            if (!isSuccess) {
//                                mDataQueue.clear();
//                            }
                        }
                    }
                }
            });
        } catch (Exception ex) {
        }
    }

    private void saveLog(byte[] data) {

        if (!UavStaticVar.isOpenTextEnvironment) {
            return;
        }
        short msgId = ByteUtilsLowBefore.byte2short(data, 8);
        if (data[6] == 0x01 && data[7] == 0x01 && msgId == 0x00D1) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < data.length; i++) {
                sb.append(DataUtil.byte2Hex(data[i]) + ",");
            }
            RonLog2FileLET.getSingle().saveData("LET receive D1  " + sb);
        }

    }


    public void pushData(byte[] data) {
        String currentSN = AirlinkUtils.getSN();
        if (StringUtils.isEmptyString(currentSN) || !isConnected()) {
            return;
        }
        try {
//            String topic = APP_CMD_TO_DRONE_TOPIC + currentSN;
//            mMqttAndroidClient.publish(topic, XAEncryptApi.doSignBytes(data, true), 0, false);
            pushNum++;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getPushNum() {
        return pushNum;
    }

    public int getReceiveNum() {
        return receiveNum;
    }

    private Runnable timeoutRunnable = new Runnable() {
        @Override
        public void run() {
            npsTimeoutRunning = false;
            if (socketCallBack3 != null) {
                socketCallBack3.callBack(GduConfig.TIME_OUT, null);
                socketCallBack3 = null;
            }
        }
    };

    //启动Nps超时定时器
    private void startNpsTimeout(long timeoutMillis) {
        if (mHandler == null) return;
        mHandler.removeCallbacks(timeoutRunnable);
        mHandler.postDelayed(timeoutRunnable, timeoutMillis);
        npsTimeoutRunning = true;
    }

    //取消Nps超时定时器
    private void cancelNpsTimeout() {
        npsTimeoutRunning = false;
        if (mHandler == null) return;
        mHandler.removeCallbacks(timeoutRunnable);
    }

    public void registerNpsForLte(SocketCallBack3 callBack3) {
        registerNpsForLte(callBack3, 5000);
    }

    public void registerNpsForLte(SocketCallBack3 callBack3, long timeoutMillis) {
        if (npsTimeoutRunning) return;
        this.socketCallBack3 = callBack3;
        byte[] data = new byte[14];
        data[0] = 4;
        data[1] = 1;
        data[2] = 0;
        data[3] = 0;
        data[4] = 0;
        data[5] = 0;
        data[6] = 2;
        data[7] = 0;
        data[8] = 0;
        byte[] smallV = ByteUtilsLowBefore.int2byte(6);
        data[9] = smallV[0];
        data[10] = smallV[1];
        data[11] = smallV[2];
        data[12] = smallV[3];
        data[13] = 0;

//        GduFrame3 frame3 = GduApplication.getSingleApp().gduCommunication.getGduSocket().createFrame3(
//                (short) 0x0A,
//                GduSocketConfig3.ONBOARD_SYSTEMS_ID,
//                GduSocketConfig3.ONBOARD_COMP_NPC,
//                GduSocketConfig3.PROTOCOL_TYPE_CONTROL,
//                GduSocketConfig3.PROTOCOL_FLAG_REQUEST,
//                data);
//        packageNum = frame3.frameSerial;
//        byte[] sendData = GduFrameUtil3.gduFrame3ToByteArray(frame3);
        sendRegisterTime = System.currentTimeMillis();
//        pushData(sendData);
        startNpsTimeout(timeoutMillis);
    }
}
