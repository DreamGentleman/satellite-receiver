package com.yxh.fangs.ui.main.handler;

import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.yxh.fangs.bean.BeidouBean;
import com.yxh.fangs.bean.Last24HoursBean;
import com.yxh.fangs.bean.NoticeType;
import com.yxh.fangs.ui.dialog.MessageDialogFragment;
import com.yxh.fangs.ui.main.MainUiBinder;
import com.yxh.fangs.ui.main.MessageDispatcher;
import com.yxh.fangs.ui.main.MessageHandler;

public class BeidouHandler implements MessageHandler {

    private final Context ctx;
    private final MainUiBinder ui;
    private final MessageDispatcher dispatcher;

    public BeidouHandler(Context ctx, MainUiBinder ui, MessageDispatcher dispatcher) {
        this.ctx = ctx;
        this.ui = ui;
        this.dispatcher = dispatcher;
    }

    @Override
    public boolean canHandle(String type) {
        return NoticeType.NOTICE_BEIDOU.equals(type);
    }

    @Override
    public void handle(Last24HoursBean.RowsBean msg, String level) {
        BeidouBean beidouBean = new Gson().fromJson(msg.getContent(), BeidouBean.class);
        String content = "北斗通道号为：" + beidouBean.getBeidouChannel()
                + "，卫星编号为：" + beidouBean.getSatelliteId()
                + "，信号强度：" + beidouBean.getSignalStrength();
        if ("0".equals(level)) {
            MessageDialogFragment messageDialogFragment = new MessageDialogFragment(msg.getTitle(), content, msg.getPublishTime());
            dispatcher.showDialog(messageDialogFragment, ((AppCompatActivity) ctx).getSupportFragmentManager(), "alert");
        }

        ui.setScrollingText(content);
        dispatcher.speak("您有一条北斗消息，" + content);
    }
}
