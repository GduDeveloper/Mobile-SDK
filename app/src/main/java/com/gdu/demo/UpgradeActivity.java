package com.gdu.demo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.gdu.api.GduFirmwareManager;
import com.gdu.api.upgrade.OnFirmwareUpgradeListener;
import com.gdu.drone.FirmwareInfo;
import com.gdu.drone.FirmwareType;
import com.gdu.drone.PlanType;

/**
 *
 * @author zhangzhilai
 * @date 2018/7/9
 */

public class UpgradeActivity extends Activity {

    private GduFirmwareManager mGduFirmwareManager;
    private TextView mNewVersionTextView;
    private TextView mCurrentVersionTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upgrade);
        mNewVersionTextView = findViewById(R.id.new_version);
        mCurrentVersionTextView = findViewById(R.id.current_version);
        mGduFirmwareManager = new GduFirmwareManager(this);
        initListener();
    }

    private void initListener() {
    }

    private void showText(final TextView version, final String content){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                version.setText(content);
            }
        });
    }

    public void startUpgrade(View view) {
        mGduFirmwareManager.startUpgradeFirmware(FirmwareType.COMPUTE_STICK_FIRMWARE, new OnFirmwareUpgradeListener() {
            @Override
            public void onDownloadStart(float totalSize) {
                System.out.println("test load onDownloadStart " + totalSize);
            }

            @Override
            public void onDownloading(int progress, float size) {
                System.out.println("test load onDownloading " + progress + " size " + size);
            }


            @Override
            public void onDownloaded() {
                System.out.println("test load onDownloaded ");
            }


            @Override
            public void onUploadStart() {
                System.out.println("test onUploadStart ");
            }

            @Override
            public void onUploading(int progress, float size) {
                System.out.println("test onUploading progress " + progress + "  " + size);
            }


            @Override
            public void onUploaded() {
                System.out.println("test onUploaded ");
            }

            @Override
            public void onUpgradeFailed(int error) {
                System.out.println("test onUploadFailed " + error);
            }
        });
    }

    public void getFirmware(View view) {
        mGduFirmwareManager.getFirmwareInfo(new GduFirmwareManager.OnGetFirmwareListener() {
            @Override
            public void onCurrentFirmwareGot(FirmwareInfo firmwareInfo) {
                StringBuilder sb = new StringBuilder();
                sb.append(firmwareInfo.getBatteryVersion() + " ");
                sb.append(firmwareInfo.getAp12Version() + " ");
                sb.append(firmwareInfo.getFcVersion() + " ");
                sb.append(firmwareInfo.getGimbalVersion() + " ");
                sb.append(firmwareInfo.getOtaVersion() + " ");
                sb.append(firmwareInfo.getComputerStickVersion() + " ");
                System.out.println("test onCurrentFirmwareGot " + sb.toString());
                showText(mCurrentVersionTextView, sb.toString());
            }

            @Override
            public void onNewFirmwareGot(FirmwareInfo firmwareInfo) {
                StringBuilder sb = new StringBuilder();
                sb.append(firmwareInfo.getBatteryVersion() + " ");
                sb.append(firmwareInfo.getAp12Version() + " ");
                sb.append(firmwareInfo.getFcVersion() + " ");
                sb.append(firmwareInfo.getGimbalVersion() + " ");
                sb.append(firmwareInfo.getOtaVersion() + " ");
                sb.append(firmwareInfo.getComputerStickVersion() + " ");
                System.out.println("test onNewFirmwareGot " + sb.toString());
                showText(mNewVersionTextView, sb.toString());
            }


            @Override
            public void onFirmwareGotFailed(int i) {

            }


        }, PlanType.O2Plan_SagaPro);
    }

    /**
     * 开始升级
     */
    public void startUpgrade(){

    }

    public void getNetFirmware(View view) {
//        mGduFirmwareManager.getUpgradeFirmwareInfo(new GduFirmwareManager.OnGetUpgradeFirmwareListener() {
//            @Override
//            public void onFirmwareUpgradeVersionGot(List<FirmwareVersionType> list) {
//                if (list != null) {
//                    System.out.println("test size " + list.size());
//                }
//            }
//
//            @Override
//            public void onFirmwareUpgradeGetFailed(int error) {
//
//            }
//        });
    }
}
