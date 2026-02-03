package com.yxh.fangs.ui.main.handler;

import android.content.Context;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.yxh.fangs.bean.ImageBean;
import com.yxh.fangs.bean.ImageCache;
import com.yxh.fangs.bean.Last24HoursBean;
import com.yxh.fangs.bean.NoticeType;
import com.yxh.fangs.ui.dialog.RedImageDialogFragment;
import com.yxh.fangs.ui.main.MainUiBinder;
import com.yxh.fangs.ui.main.MessageDispatcher;
import com.yxh.fangs.ui.main.MessageHandler;

public class ImageHandler implements MessageHandler {

    private final Context ctx;
    private final MainUiBinder ui;
    private final MessageDispatcher dispatcher;

    public ImageHandler(Context ctx, MainUiBinder ui, MessageDispatcher dispatcher) {
        this.ctx = ctx;
        this.ui = ui;
        this.dispatcher = dispatcher;
    }

    @Override
    public boolean canHandle(String type) {
        return NoticeType.NOTICE_NOTICE_IMAGE.equals(type);
    }

    @Override
    public void handle(Last24HoursBean.RowsBean msg, String level) {
        ImageBean imageBean = new Gson().fromJson(msg.getContent(), ImageBean.class);
        if (imageBean == null) {
            Toast.makeText(ctx, "图片信息异常！", Toast.LENGTH_SHORT).show();
            return;
        }
        if ("0".equals(level)) {
            ImageCache.base64 = imageBean.getBase64();
            RedImageDialogFragment imageDialogFragment = new RedImageDialogFragment(msg.getPublishTime());
            dispatcher.showDialog(imageDialogFragment, ((AppCompatActivity) ctx).getSupportFragmentManager(), "image");
        }

        ui.setScrollingText("您有一条图片消息");
        dispatcher.speak("您有一条图片消息");
    }
}
