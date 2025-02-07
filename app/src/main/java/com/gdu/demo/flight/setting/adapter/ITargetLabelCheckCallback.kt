package com.gdu.demo.flight.setting.adapter

import com.gdu.demo.flight.setting.bean.TargetDetectModel


interface ITargetLabelCheckCallback {
    fun onCheckChange(data: TargetDetectModel?)
}