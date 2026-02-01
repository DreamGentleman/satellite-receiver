package com.yxh.fangs.ui.main.handler;

import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;

import com.yxh.fangs.bean.Last24HoursBean;
import com.yxh.fangs.bean.NoticeType;
import com.yxh.fangs.ui.dialog.MessageDialogFragment;
import com.yxh.fangs.ui.main.MainUiBinder;
import com.yxh.fangs.ui.main.MessageDispatcher;
import com.yxh.fangs.ui.main.MessageHandler;

public class SmsHandler implements MessageHandler {

    private final Context ctx;
    private final MainUiBinder ui;
    private final MessageDispatcher dispatcher;

    public SmsHandler(Context ctx, MainUiBinder ui, MessageDispatcher dispatcher) {
        this.ctx = ctx;
        this.ui = ui;
        this.dispatcher = dispatcher;
    }

    @Override
    public boolean canHandle(String type) {
        return NoticeType.NOTICE_SMS.equals(type);
    }

    @Override
    public void handle(Last24HoursBean.RowsBean msg, String level) {
        if ("0".equals(level)) {
            MessageDialogFragment messageDialogFragment = new MessageDialogFragment(msg.getTitle(), msg.getContent(), msg.getPublishTime());
            dispatcher.showDialog(messageDialogFragment, ((AppCompatActivity) ctx).getSupportFragmentManager(), "sos");
        }

        ui.setScrollingText(msg.getTitle());
        dispatcher.speak("您有一条短消息，" + msg.getTitle());
    }
}
