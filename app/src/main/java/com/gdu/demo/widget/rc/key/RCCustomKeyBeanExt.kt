package com.gdu.demo.widget.rc.key

import com.gdu.msdk.key.value.rc.RCCustomKeyBean

fun RCCustomKeyBean.getKeyUpEvents(prevKey: RCCustomKeyBean?): List<Int> {
    val list = mutableListOf<Int>()

    do {
        if (prevKey?.c1 == true && !this.c1) {
            break
        }
        if (prevKey?.c2 == true && !this.c2) {
            break
        }
        if (prevKey?.l1 == true && !this.l1) {
            break
        }
        if (prevKey?.l2 == true && !this.l2) {
            break
        }
        if (prevKey?.r1 == true && !this.r1) {
            break
        }
        if (prevKey?.r2 == true && !this.r2) {
            break
        }
        if (prevKey?.fl == true && !this.fl) {
            break
        }
        if (prevKey?.ft == true && !this.ft) {
            break
        }
        if (prevKey?.fr == true && !this.fr) {
            break
        }
        if (prevKey?.fb == true && !this.fb) {
            break
        }
        if (prevKey?.fc == true && !this.fc) {
            break
        }
        return list
    } while (false)

    if (prevKey?.c1 == true) {
        list.add(IActionViewKey.VIEW_C1)
    }
    if (prevKey?.c2 == true) {
        list.add(IActionViewKey.VIEW_C2)
    }
    if (prevKey?.l1 == true) {
        list.add(IActionViewKey.VIEW_L1)
    }
    if (prevKey?.l2 == true) {
        list.add(IActionViewKey.VIEW_L2)
    }
    if (prevKey?.r1 == true) {
        list.add(IActionViewKey.VIEW_R1)
    }
    if (prevKey?.r2 == true) {
        list.add(IActionViewKey.VIEW_R2)
    }
    if (prevKey?.fl == true) {
        list.add(IActionViewKey.VIEW_FIVE_LEFT)
    }
    if (prevKey?.ft == true) {
        list.add(IActionViewKey.VIEW_FIVE_UP)
    }
    if (prevKey?.fr == true) {
        list.add(IActionViewKey.VIEW_FIVE_RIGHT)
    }
    if (prevKey?.fb == true) {
        list.add(IActionViewKey.VIEW_FIVE_BOTTOM)
    }
    if (prevKey?.fc == true) {
        list.add(IActionViewKey.VIEW_FIVE_CENTER)
    }
    return list
}