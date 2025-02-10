package com.gdu.demo.flight.base;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.gdu.config.GlobalVariable;
import com.gdu.demo.R;

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
        switch (GlobalVariable.connStateEnum) {
            case Conn_None:
                toastLiveData.setValue(R.string.DeviceNoConn);
                return false;
            case Conn_MoreOne:
                toastLiveData.setValue(R.string.Label_ConnMore);
                return false;
            case Conn_Sucess:
                return true;
            default:
                break;
        }
        return false;
    }

    public MutableLiveData<ErrTipBean> getErrTipBeanLiveData() {
        return errTipBeanLiveData;
    }
}
