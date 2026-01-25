package com.yxh.fangs.ui.main.handler;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.google.gson.Gson;
import com.yxh.fangs.bean.ImageBean;
import com.yxh.fangs.bean.ImageCache;
import com.yxh.fangs.bean.Last24HoursBean;
import com.yxh.fangs.bean.NoticeType;
import com.yxh.fangs.ui.image.ImageDetailActivity;
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
    public void handle(Last24HoursBean.RowsBean msg, boolean isTop) {
        ImageBean imageBean = new Gson().fromJson(msg.getContent(), ImageBean.class);
        if (imageBean == null) {
            Toast.makeText(ctx, "图片信息异常！", Toast.LENGTH_SHORT).show();
            return;
        }
        ImageCache.base64 = imageBean.getBase64();

        Intent intent = new Intent(ctx, ImageDetailActivity.class);
        intent.putExtra("time", msg.getPublishTime());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(intent);

        if (isTop) {
            ui.setScrollingText("您有一条图片消息");
            dispatcher.speak("您有一条图片消息");
        }
    }
}
