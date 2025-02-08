package com.gdu.demo.utils;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.gdu.demo.R;
import com.gdu.util.ViewUtils;

/**
 * Created by Administrator on 2016/11/4.
 */
public class AnimationUtils {



    /**
     * <p>创建view移动的animation</p>
     */
    public static void showMediaFragBottom(Context context, final boolean isShow, final View view) {
        TranslateAnimation translateAnimation;
        if (isShow) {
            translateAnimation = new TranslateAnimation(0, 0, ViewUtils.dip2px(context, 44), 0);
        } else {
            translateAnimation = new TranslateAnimation(0, 0, 0, ViewUtils.dip2px(context, 44));
        }

        translateAnimation.setDuration(500);
        translateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                if (isShow) {
                    view.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (!isShow) {
                    view.setVisibility(View.GONE);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        view.startAnimation(translateAnimation);
    }

    /**
     * 操作村杆渐变显示动画
     *
     * @param left_panel  渐变显示的view
     * @param right_panel 渐变显示的view
     */
    public static void panelAnima(View left_panel, View right_panel) {
        AlphaAnimation alphaAnimation = new AlphaAnimation(0.0f, 1.0f);
        alphaAnimation.setDuration(1000);
        alphaAnimation.setFillAfter(true);
        left_panel.startAnimation(alphaAnimation);
        right_panel.startAnimation(alphaAnimation);
    }

    /**
     * 右边菜单显示的动画
     */
    public static void menuShowAnima(final View rightPackBg, View rightMenuOut, View rightMenu, View rightMenuBack) {
        rightPackBg.setAlpha(1f);
        rightMenuOut.setAlpha(1f);
        rightPackBg.animate().alpha(0f).setDuration(800);
        rightMenuOut.animate().alpha(0f).setDuration(800);
        final boolean isShow = true;
        rightMenuOut.setVisibility(View.GONE);//让右菜单显示的按钮
        int[] location = (int[]) rightMenu.getTag();//拿到右菜单消失前的坐标
        int[] backLocation = (int[]) rightMenuBack.getTag();//拿到让右菜单消失按钮  在消失前的坐标
        rightMenu.animate().alpha(1.0f).x(location[0]).y(location[1]).setDuration(800);//右菜单渐变加移动的动画
        rightMenuBack.animate().alpha(1.0f).x(backLocation[0]).y(backLocation[1]).setDuration(800);//让右菜单消失的按钮
        rightMenu.setVisibility(View.VISIBLE);
        rightMenuBack.setVisibility(View.VISIBLE);
        rightPackBg.animate().setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (isShow) {
                    rightPackBg.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

    }

    /**
     * 右边菜单消失的动画
     */
    public static void menuDisappearAnima(final View rightMenu, final View rightMenuBack, final View rightPackBg, final View rightMenuOut) {
        /**
         * 获取rightMenu、rightMenuBack 控件当前坐标
         */
        int[] location = new int[2];
        int[] backLocation = new int[2];
        rightMenu.getLocationOnScreen(location);
        rightMenuBack.getLocationOnScreen(backLocation);
        rightMenu.setTag(location);
        rightMenuBack.setTag(backLocation);
        float x = (float) location[0];
        float y = (float) location[1];

        //初始化透明度
        rightMenu.setAlpha(0.5f);
        rightMenuBack.setAlpha(0.5f);

        //渐变消失动画
        rightMenu.animate().alpha(0f).x(x + 500).y(y + 500).setDuration(800);
        rightMenuBack.animate().alpha(0f).x(x + 500).y(y + 500).setDuration(800);
        rightMenu.animate().setListener(new Animator.AnimatorListener() {
            private boolean isOut = true;

            @Override
            public void onAnimationStart(Animator animation) {
                if (isOut) {
                    rightPackBg.setAlpha(0f);
                    rightMenuOut.setAlpha(0f);
                    rightPackBg.animate().alpha(1.0f).setDuration(800);
                    rightMenuOut.animate().alpha(1.0f).setDuration(800);
                    rightPackBg.setVisibility(View.VISIBLE);
                    rightMenuOut.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (isOut) {
                    rightMenu.setVisibility(View.GONE);
                    rightMenuBack.setVisibility(View.GONE);

                   /* rightMenuOut.setVisibility(View.VISIBLE);
                    rightPackBg.setVisibility(View.VISIBLE);*/
                    isOut = false;
                } else {
                    isOut = true;
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        rightPackBg.animate().setListener(null);

    }


    /**
     * 菜单从左下角移动消失动画
     *
     * @param leftMenu     要移动消失的View
     * @param leftMenuBack 要移动消失的View
     * @param leftMenuOut  view消失后，要显示的view
     */
    public static void menuDisappearAnima(final View leftMenu, final View leftMenuBack, final View leftMenuOut) {
        int[] location = new int[2];
        leftMenu.getLocationOnScreen(location);
        float x = (float) location[0];
        float y = (float) location[1];
        AnimationSet animationSet = new AnimationSet(true);
        AlphaAnimation menuAlpha = new AlphaAnimation(0.8f, 0.0f);
        menuAlpha.setDuration(500);
        menuAlpha.setFillAfter(true);

        TranslateAnimation menuTranslate = new TranslateAnimation(
                //X轴初始位置
                Animation.RELATIVE_TO_SELF, x,
                //X轴移动的结束位置
                Animation.RELATIVE_TO_SELF, -1.0f,
                //y轴开始位置
                Animation.RELATIVE_TO_SELF, y,
                //y轴移动后的结束位置
                Animation.RELATIVE_TO_SELF, 1.0f);
        menuTranslate.setDuration(500);
        menuTranslate.setFillAfter(true);

        animationSet.addAnimation(menuAlpha);
        animationSet.addAnimation(menuTranslate);

        leftMenu.startAnimation(animationSet);
        leftMenuBack.startAnimation(animationSet);

        menuAlpha.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                leftMenuOut.setVisibility(View.VISIBLE);
                leftMenu.setVisibility(View.GONE);
                leftMenuBack.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    public static void menuShowAnima(final View leftMenu, final View leftMenuBack, final View leftMenuOut) {
        leftMenuOut.setVisibility(View.GONE);
        int[] location = new int[2];
        leftMenu.getLocationOnScreen(location);
        float x = (float) location[0];
        float y = (float) location[1];
        AnimationSet animationSet = new AnimationSet(true);
        AlphaAnimation menuAlpha = new AlphaAnimation(0.0f, 0.8f);
        menuAlpha.setDuration(500);
        menuAlpha.setFillAfter(true);

        TranslateAnimation menuTranslate = new TranslateAnimation(
                //X轴初始位置
                Animation.RELATIVE_TO_SELF, -1.0f,
                //X轴移动的结束位置
                Animation.RELATIVE_TO_SELF, x,
                //y轴开始位置
                Animation.RELATIVE_TO_SELF, 1.0f,
                //y轴移动后的结束位置
                Animation.RELATIVE_TO_SELF, y);
        menuTranslate.setDuration(500);
        menuTranslate.setFillAfter(true);

        animationSet.addAnimation(menuAlpha);
        animationSet.addAnimation(menuTranslate);

        leftMenu.startAnimation(animationSet);
        leftMenuBack.startAnimation(animationSet);

        menuAlpha.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                leftMenu.setVisibility(View.VISIBLE);
                leftMenuBack.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    /**
     * 点击注册，登录，忘记密码 动画
     * setInterpolator表示设置旋转速率。LinearInterpolator为匀速效果，
     * Accelerateinterpolator为加速效果、DecelerateInterpolator为减速效果，
     * **duration必须写，要不动画不动、、、***
     */
    public Animation animation;
    private ImageView mIv_Loading;
    private TextView mTv_ConfirmButton;

//    public void StartLoadAnimation(Context context, ImageView Iv_Loading, TextView Tv_ConfirmButton) {
//        this.mIv_Loading = Iv_Loading;
//        this.mTv_ConfirmButton = Tv_ConfirmButton;
//        mIv_Loading.setVisibility(View.VISIBLE);
//        mTv_ConfirmButton.setVisibility(View.GONE);
//        animation = android.view.animation.AnimationUtils.loadAnimation(context, R.anim.button_login2register_loading);
//        LinearInterpolator lin = new LinearInterpolator();
//        animation.setInterpolator(lin);
//        Iv_Loading.startAnimation(animation);
//    }
//
//    private ImageView imageView;
//
//    /*
//    * 下载PDf的动画
//    * */
//    public void startDownLoadAnimation(Context context, ImageView imageView) {
//        this.imageView = imageView;
//        animation = android.view.animation.AnimationUtils.loadAnimation(context, R.anim.button_login2register_loading);
//        LinearInterpolator lin = new LinearInterpolator();
//        animation.setInterpolator(lin);
//        imageView.startAnimation(animation);
//    }

//    /*
//    * 取消下载的动画
//    * */
//    public void cancleDownLoadAnimation() {
//        if (animation != null && imageView.getAnimation() != null) {
//            imageView.clearAnimation();
//        }
//    }

    /**
     * <p>点击登录页面的Button的取消动画的dialog</p>
     */
    public void CancleLoadAnimation() {
        mIv_Loading.clearAnimation();
        mIv_Loading.setVisibility(View.GONE);
        mTv_ConfirmButton.setVisibility(View.VISIBLE);
    }


    /**
     * 飞行模式设置 伸展动画
     *
     * @param aniView
     * @param view
     */
    public void scaleSpreadAnimation(final View aniView, View view, boolean isSport, View sportView, View standerView) {
        view.setVisibility(View.GONE);
        aniView.setVisibility(View.VISIBLE);
        if (isSport) {
            sportView.setSelected(true);
            standerView.setSelected(false);
        } else {
            sportView.setSelected(false);
            standerView.setSelected(true);
        }
        final ScaleAnimation scaleAnimation = new ScaleAnimation(0.0f, 1.0f, 1.0f, 1.0f, Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 1.0f);
        scaleAnimation.setDuration(300);
        scaleAnimation.setFillAfter(true);
        aniView.setAnimation(scaleAnimation);
        scaleAnimation.startNow();
    }

    /**
     * 飞行模式设置 收缩动画
     *
     * @param aniView
     * @param view
     */
    public void scaleShrinkAnimation(final Activity activity, final View aniView, final TextView view, final boolean isSport) {
        aniView.setVisibility(View.GONE);
        ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f, 0.5f, 1.0f, 1.0f, Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 1.0f);
        scaleAnimation.setDuration(300);
        scaleAnimation.setFillAfter(true);
        aniView.setAnimation(scaleAnimation);
        scaleAnimation.startNow();
        scaleAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.setVisibility(View.VISIBLE);
                view.setSelected(true);
                aniView.clearAnimation();
                aniView.setVisibility(View.GONE);
                view.setText(activity.getString(isSport ? R.string.mode_sport : R.string.mode_standard));
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }


    /**
     * 飞行模式设置 伸展动画
     *
     * @param aniView
     * @param view
     */
    public void scaleSpreadYAnimation(final View aniView, View view) {
        view.setVisibility(View.INVISIBLE);
        aniView.setVisibility(View.VISIBLE);
        final ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f, 1.0f, 0.0f, 1.0f, Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setDuration(100);
        scaleAnimation.setFillAfter(true);
        aniView.setAnimation(scaleAnimation);
        scaleAnimation.startNow();
    }


    /**
     * 飞行模式设置 收缩动画
     *
     * @param aniView
     * @param view
     */
    public void scaleShrinkYAnimation(final Activity activity, final View aniView, final TextView view, final String[] C1C2_Data, final byte isDefine) {
        //20171110-add-c1c2 -便于扩展
//        final String[] C1C2_Data;
//        if (GlobalVariable.gimbalType == ByrdT_10X_Zoom || GlobalVariable.gimbalType == ByrdT_30X_Zoom) {  //TODO 目前暂时先隐藏，存在中途换云台的这种情况下个版本作逻辑处理  于浩
//            C1C2_Data = activity.getResources().getStringArray(R.array.PlanSet_C1C2_Parameter_10X_30X);
//        } else {
//            C1C2_Data = activity.getResources().getStringArray(R.array.PlanSet_C1C2_Parameter);
//        }
        aniView.setVisibility(View.GONE);
        ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f, 1.0f, 1.0f, 0.0f, Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setDuration(100);
        scaleAnimation.setFillAfter(true);
        aniView.setAnimation(scaleAnimation);
        scaleAnimation.startNow();
        scaleAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                view.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.setSelected(true);
                aniView.clearAnimation();
                aniView.setVisibility(View.GONE);
                view.setText(C1C2_Data[isDefine]);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    /**
     * 视频尺寸伸展动画
     *
     * @param aniView
     * @param showView
     */
    public void scale_StretchSize(View aniView, TextView showView, TextView[] textViews) {
        if (aniView.getVisibility() == View.GONE) {
            aniView.setVisibility(View.VISIBLE);
        }
        showView.setVisibility(View.GONE);
        final ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f, 1.0f, 0.0f, 1.0f, Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f);
        scaleAnimation.setDuration(100);
        scaleAnimation.setFillAfter(true);
        aniView.setAnimation(scaleAnimation);
        scaleAnimation.startNow();
        showView.setSelected(true);
        for (int i = 0; i < textViews.length; i++) {
            textViews[i].setSelected(showView.getText().toString().equals(textViews[i].getText().toString()));
        }
        aniView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }


    /**
     * 点击选中的收缩动画
     *
     * @param aniView
     * @param showView
     * @param selectView
     */
    public void scale_Size(final View aniView, TextView showView, TextView selectView) {
        if (aniView.getVisibility() == View.VISIBLE) {
            aniView.setVisibility(View.GONE);
            final ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f, 1.0f, 1.0f, 0.0f, Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f);
            scaleAnimation.setDuration(100);
            scaleAnimation.setFillAfter(true);
            aniView.setAnimation(scaleAnimation);
            scaleAnimation.startNow();
            showView.setVisibility(View.VISIBLE);
            if (selectView != null) {
                showView.setText(selectView.getText().toString());
                showView.setSelected(true);
            }
            scaleAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    aniView.clearAnimation();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        }
    }

    /**
     * 视觉避障,显示雷达向上移动动画
     *
     * @param view
     * @param distance
     */
    public void upAnimationVision(View view, float distance) {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(view, "translationY", 0.0f, -distance);
        objectAnimator.setDuration(200);
        objectAnimator.start();
    }

    /**
     * 视觉避障,显示雷达向下移动动画
     *
     * @param view
     * @param distance
     */
    public void downAnimationVision(View view, float distance) {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(view, "translationY", -distance, 0.0f);
        objectAnimator.setDuration(200);
        objectAnimator.start();
    }


    public static void animatorRightInOut(View view, boolean show) {
        if (view == null) {
            return;
        }
        if (show) {
            TranslateAnimation translateAniShow = new TranslateAnimation(
                    Animation.RELATIVE_TO_SELF, 1, Animation.RELATIVE_TO_SELF, 0,
                    Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0);
            translateAniShow.setDuration(300);
            view.startAnimation(translateAniShow);
            view.setVisibility(View.VISIBLE);
        } else {
            TranslateAnimation translateAniShow = new TranslateAnimation(
                    Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 1,
                    Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0);
            translateAniShow.setDuration(300);
            translateAniShow.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    view.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            view.startAnimation(translateAniShow);
        }
    }
}
