package com.gdu.demo.flight.base;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.gdu.demo.R;
import com.gdu.demo.SdkDemoApplication;

/**
 * @author wuqb
 * @date 2025/2/8
 * @description TODO
 */
public class BaseViewModel extends ViewModel {
    protected final MutableLiveData<Integer> toastLiveData;
    protected final MutableLiveData<ErrTipBean> errTipBeanLiveData;

    public BaseViewModel(){
        toastLiveData = new MutableLiveData<>();
        errTipBeanLiveData = new MutableLiveData<>();
    }

    public MutableLiveData<Integer> getToastLiveData() {
        return toastLiveData;
    }


    public boolean connStateToast() {
        if (!SdkDemoApplication.getAircraftInstance().isConnected()) {
            toastLiveData.setValue(R.string.DeviceNoConn);
            return false;
        }
        return true;
    }

    public MutableLiveData<ErrTipBean> getErrTipBeanLiveData() {
        return errTipBeanLiveData;
    }
}
