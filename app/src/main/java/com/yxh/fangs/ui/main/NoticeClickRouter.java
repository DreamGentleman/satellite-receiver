package com.yxh.fangs.ui.main;

import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yxh.fangs.bean.BeidouBean;
import com.yxh.fangs.bean.ImageBean;
import com.yxh.fangs.bean.ImageCache;
import com.yxh.fangs.bean.Last24HoursBean;
import com.yxh.fangs.bean.NoticeType;
import com.yxh.fangs.bean.TyphoonBean2;
import com.yxh.fangs.bean.WarnBean;
import com.yxh.fangs.bean.WeatherBean;
import com.yxh.fangs.ui.dialog.ImageDialogFragment;
import com.yxh.fangs.ui.dialog.MessageDialogFragment;
import com.yxh.fangs.ui.dialog.WeatherDialogFragment;

import java.lang.reflect.Type;
import java.util.List;

public class NoticeClickRouter {

    private final AppCompatActivity activity;
    private final MessageDispatcher dispatcher;
    private final Gson gson = new Gson();

    public NoticeClickRouter(AppCompatActivity activity, MessageDispatcher dispatcher) {
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

                MessageDialogFragment messageDialogFragment = new MessageDialogFragment(rowsBean.getTitle(), content, rowsBean.getPublishTime());
                dispatcher.showDialog(messageDialogFragment, activity.getSupportFragmentManager(), "beidou");
                break;
            }

            case NoticeType.NOTICE_ALERT: {
                WarnBean warnBean = gson.fromJson(rowsBean.getContent(), WarnBean.class);
                if (warnBean == null) return;

                String content = warnBean.getWarningLevel();
                dispatcher.speak("您有一条预警信息，" + content);

                MessageDialogFragment messageDialogFragment = new MessageDialogFragment(rowsBean.getTitle(), content, rowsBean.getPublishTime());
                dispatcher.showDialog(messageDialogFragment, activity.getSupportFragmentManager(), "alert");
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

                ImageDialogFragment imageDialogFragment = new ImageDialogFragment(rowsBean.getPublishTime());
                dispatcher.showDialog(imageDialogFragment, activity.getSupportFragmentManager(), "image");
                break;
            }

            case NoticeType.NOTICE_SMS: {
                dispatcher.speak("您有一条短消息，" + rowsBean.getTitle());
                MessageDialogFragment messageDialogFragment = new MessageDialogFragment(rowsBean.getTitle(), rowsBean.getContent(), rowsBean.getPublishTime());
                dispatcher.showDialog(messageDialogFragment, activity.getSupportFragmentManager(), "alert");
                break;
            }

            case NoticeType.NOTICE_TYPHOON: {
                TyphoonBean2 typhoonBean = gson.fromJson(rowsBean.getContent(), TyphoonBean2.class);
                if (typhoonBean == null) return;

                dispatcher.speak("您有一条台风" + typhoonBean.getTyphoonName() + "的消息");
                MessageDialogFragment messageDialogFragment = new MessageDialogFragment(rowsBean.getTitle(), typhoonBean.getMovingDirection(), rowsBean.getPublishTime());
                dispatcher.showDialog(messageDialogFragment, activity.getSupportFragmentManager(), "typhoon");
                break;
            }

            case NoticeType.NOTICE_WEATHER: {
                Type type = new TypeToken<List<WeatherBean>>() {
                }.getType();
                List<WeatherBean> list = gson.fromJson(rowsBean.getContent(), type);

                if (list != null && !list.isEmpty()) {
                    WeatherBean weatherBean = list.get(0);
//                    dispatcher.showWeatherDetail(weatherBean);

                    String message = WeatherTextBuilder.buildForecastText(weatherBean);
                    WeatherDialogFragment weatherDialogFragment = new WeatherDialogFragment(message, weatherBean.getWeatherPhenomenon());
                    dispatcher.showDialog(weatherDialogFragment, activity.getSupportFragmentManager(), "alert");
                }
                break;
            }
        }
    }
}
