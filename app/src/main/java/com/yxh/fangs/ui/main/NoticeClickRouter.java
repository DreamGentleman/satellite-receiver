package com.yxh.fangs.ui.main;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yxh.fangs.bean.BeidouBean;
import com.yxh.fangs.bean.ImageBean;
import com.yxh.fangs.bean.ImageCache;
import com.yxh.fangs.bean.Last24HoursBean;
import com.yxh.fangs.bean.NoticeType;
import com.yxh.fangs.bean.TyphoonBean;
import com.yxh.fangs.bean.WarnBean;
import com.yxh.fangs.bean.WeatherBean;
import com.yxh.fangs.ui.dialog.MessageDialog;
import com.yxh.fangs.ui.image.ImageDetailActivity;

import java.lang.reflect.Type;
import java.util.List;

public class NoticeClickRouter {

    private final Activity activity;
    private final MessageDispatcher dispatcher;
    private final Gson gson = new Gson();

    public NoticeClickRouter(Activity activity, MessageDispatcher dispatcher) {
        this.activity = activity;
        this.dispatcher = dispatcher;
    }

    public void onNoticeClicked(Last24HoursBean.RowsBean rowsBean) {
        if (rowsBean == null) return;

        switch (rowsBean.getMessageType()) {

            case NoticeType.NOTICE_BEIDOU: {
                BeidouBean beidouBean = gson.fromJson(rowsBean.getContent(), BeidouBean.class);
                if (beidouBean == null) return;

                String content = "北斗通道号为：" + beidouBean.getBeidouChannel()
                        + "，卫星编号为：" + beidouBean.getSatelliteId()
                        + "，信号强度：" + beidouBean.getSignalStrength();

                dispatcher.speak("您有一条北斗消息，" + content);
                MessageDialog dialog = MessageDialog.newInstance(activity, rowsBean.getTitle(), content, rowsBean.getPublishTime());
                dispatcher.showDialog(dialog);
                break;
            }

            case NoticeType.NOTICE_ALERT: {
                WarnBean warnBean = gson.fromJson(rowsBean.getContent(), WarnBean.class);
                if (warnBean == null) return;

                String content = warnBean.getWarningLevel();
                dispatcher.speak("您有一条预警信息，" + content);

                MessageDialog dialog = MessageDialog.newInstance(activity, rowsBean.getTitle(), content, rowsBean.getPublishTime());
                dispatcher.showDialog(dialog);
                break;
            }

            case NoticeType.NOTICE_NOTICE_IMAGE: {
                ImageBean imageBean = gson.fromJson(rowsBean.getContent(), ImageBean.class);
                if (imageBean == null || TextUtils.isEmpty(imageBean.getBase64())) {
                    Toast.makeText(activity, "图片信息异常！", Toast.LENGTH_SHORT).show();
                    return;
                }
                ImageCache.base64 = imageBean.getBase64();
                dispatcher.speak("您有一条图片消息");

                Intent intent = new Intent(activity, ImageDetailActivity.class);
                intent.putExtra("time", rowsBean.getPublishTime());
                activity.startActivity(intent);
                break;
            }

            case NoticeType.NOTICE_SMS: {
                dispatcher.speak("您有一条短消息，" + rowsBean.getTitle());
                MessageDialog dialog = MessageDialog.newInstance(activity, rowsBean.getTitle(), rowsBean.getContent(), rowsBean.getPublishTime());
                dispatcher.showDialog(dialog);
                break;
            }

            case NoticeType.NOTICE_TYPHOON: {
                TyphoonBean typhoonBean = gson.fromJson(rowsBean.getContent(), TyphoonBean.class);
                if (typhoonBean == null) return;

                dispatcher.speak("您有一条台风" + typhoonBean.getTyphoonName() + "的消息");
                MessageDialog dialog = MessageDialog.newInstance(activity, rowsBean.getTitle(), typhoonBean.getMovingDirection(), rowsBean.getTitle());
                dispatcher.showDialog(dialog);
                break;
            }

            case NoticeType.NOTICE_WEATHER: {
                Type type = new TypeToken<List<WeatherBean>>() {}.getType();
                List<WeatherBean> list = gson.fromJson(rowsBean.getContent(), type);

                if (list != null && !list.isEmpty()) {
                    WeatherBean weatherBean = list.get(0);

                    // 直接复用 dispatcher 的方法
                    dispatcher.showWeatherDetail(weatherBean);
                }
                break;
            }
        }
    }
}
