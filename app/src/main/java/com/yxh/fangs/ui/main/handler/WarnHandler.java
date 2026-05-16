package com.yxh.fangs.ui.main.handler;

import android.content.Context;
import android.text.TextUtils;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.yxh.fangs.R;
import com.yxh.fangs.bean.Last24HoursBean;
import com.yxh.fangs.bean.NoticeType;
import com.yxh.fangs.bean.WarnBean;
import com.yxh.fangs.map.layer.LayerType;
import com.yxh.fangs.ui.dialog.RedMessageDialogFragment;
import com.yxh.fangs.ui.main.MainUiBinder;
import com.yxh.fangs.ui.main.MapController;
import com.yxh.fangs.ui.main.MessageDispatcher;
import com.yxh.fangs.ui.main.MessageHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class WarnHandler implements MessageHandler {

    private final Context ctx;
    private final MainUiBinder ui;
    private final MapController map;
    private final MessageDispatcher.DispatchState state;
    private final MessageDispatcher dispatcher;
    private final Gson gson = new Gson();

    private final NoticeElementStore elementStore;

    public WarnHandler(Context ctx, MainUiBinder ui, MapController map,
                       MessageDispatcher.DispatchState state, MessageDispatcher dispatcher) {
        this.ctx = ctx;
        this.ui = ui;
        this.map = map;
        this.state = state;
        this.dispatcher = dispatcher;
        this.elementStore = new NoticeElementStore(map);
    }

    @Override
    public boolean canHandle(String type) {
        return NoticeType.NOTICE_ALERT.equals(type);
    }

    @Override
    public void handle(Last24HoursBean.RowsBean msg, String level) {
        if (msg == null) return;

        drawOnly(msg);

        WarnBean warnBean = gson.fromJson(msg.getContent(), WarnBean.class);
        if (warnBean == null) return;

        String content = warnBean.getWarningLevel();
        if ("0".equals(level)) {
            RedMessageDialogFragment messageDialogFragment = new RedMessageDialogFragment(msg.getTitle(), content, msg.getPublishTime());
            dispatcher.showDialog(messageDialogFragment, ((AppCompatActivity) ctx).getSupportFragmentManager(), "alert");
        }

        ui.setScrollingText(content);
        dispatcher.speak("您有一条预警信息，" + content);
    }

    public void drawOnly(Last24HoursBean.RowsBean msg) {
        if (msg == null) return;
        String msgId = msg.getId();
        if (TextUtils.isEmpty(msgId)) return;

        if (elementStore.contains(msgId)) return;

        List<Long> ids = drawWarnElements(msg);
        elementStore.put(msgId, ids);
    }

    public void syncCurrentWarnIds(Set<String> currentWarnMsgIds) {
        elementStore.sync(currentWarnMsgIds);
    }

    public void clearAll() {
        elementStore.clearAll();
    }


    public void removeByMsgId(String msgId) {
        elementStore.remove(msgId);
    }

    /**
     * 实际绘制预警图元，并返回 elementId 列表
     */
    private List<Long> drawWarnElements(Last24HoursBean.RowsBean msg) {
        state.setSelectedLayer(LayerType.RAINSTORM);

        WarnBean warnBean = gson.fromJson(msg.getContent(), WarnBean.class);
        if (warnBean == null) return null;

        if (warnBean.getWarningArea() == null || warnBean.getWarningArea().getCoordinates() == null) {
            return null;
        }

        WarnBean.WarningAreaBean area = warnBean.getWarningArea();
        WarnBean.WarningAreaBean.CoordinatesBean c = area.getCoordinates();

        List<Long> ids = new ArrayList<>();

        switch (area.getType()) {

            case "polygon": {
                if (c.getPoints() == null || c.getPoints().size() < 3) return null;

                // 画边界线（你现在是 polyline，保留你的表现）
                List<com.bigemap.bmcore.entity.GeoPoint> pts = new ArrayList<>();
                for (WarnBean.WarningAreaBean.CoordinatesBean.PointsBean p : c.getPoints()) {
                    pts.add(new com.bigemap.bmcore.entity.GeoPoint(p.getLng(), p.getLat(), 0));
                }

                long lineId = map.drawPolyline(
                        LayerType.RAINSTORM,
                        pts,
                        c.getColor(),
                        "6",
                        warnBean.getWarningLevel()
                );
                if (lineId > 0) ids.add(lineId);
                break;
            }

            case "circle": {
                if (c.getCenter() == null) return null;

                // 你原来用 area 算半径，我保留
                double radius = c.getArea() > 0 ? Math.sqrt(c.getArea() / Math.PI) : 100_000;

                long circleId = map.drawCircle(
                        LayerType.RAINSTORM,
                        c.getCenter().getLng(),
                        c.getCenter().getLat(),
                        c.getColor(),
                        radius,
                        warnBean.getWarningLevel()
                );
                if (circleId > 0) ids.add(circleId);
                break;
            }

            case "rectangle": {
                if (c.getBounds() == null) return null;

                long rectId = map.drawRectangle(
                        LayerType.RAINSTORM,
                        c.getBounds().getSw().getLng(), c.getBounds().getSw().getLat(),
                        c.getBounds().getNe().getLng(), c.getBounds().getNe().getLat(),
                        c.getColor(),
                        warnBean.getWarningLevel()
                );
                if (rectId > 0) ids.add(rectId);
                break;
            }
            case "marker": {
                if (c.getCenter() == null) return null;
                int icon = R.mipmap.ic_sos_1;
                long pointId = map.drawPoint(
                        LayerType.RAINSTORM,
                        c.getCenter().getLng(),
                        c.getCenter().getLat(),
                        icon,
                        1.0f,
                        0
                );
                if (pointId > 0) ids.add(pointId);
                break;
            }
        }
        return ids;
    }
}
