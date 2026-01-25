package com.yxh.fangs.ui.main.handler;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.yxh.fangs.R;
import com.yxh.fangs.bean.Last24HoursBean;
import com.yxh.fangs.bean.NoticeType;
import com.yxh.fangs.bean.WarnBean;
import com.yxh.fangs.map.layer.LayerType;
import com.yxh.fangs.ui.dialog.MessageDialog;
import com.yxh.fangs.ui.main.MainUiBinder;
import com.yxh.fangs.ui.main.MapController;
import com.yxh.fangs.ui.main.MessageDispatcher;
import com.yxh.fangs.ui.main.MessageHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class WarnHandler implements MessageHandler {

    private final Context ctx;
    private final MainUiBinder ui;
    private final MapController map;
    private final MessageDispatcher.DispatchState state;
    private final MessageDispatcher dispatcher;
    private final Gson gson = new Gson();

    // msgId -> elementIds（该预警绘制出来的所有图元id）
    private final Map<String, List<Long>> drawnIdsByMsgId = new HashMap<>();

    public WarnHandler(Context ctx, MainUiBinder ui, MapController map,
                       MessageDispatcher.DispatchState state, MessageDispatcher dispatcher) {
        this.ctx = ctx;
        this.ui = ui;
        this.map = map;
        this.state = state;
        this.dispatcher = dispatcher;
    }

    @Override
    public boolean canHandle(String type) {
        return NoticeType.NOTICE_ALERT.equals(type);
    }

    @Override
    public void handle(Last24HoursBean.RowsBean msg, boolean isTop) {
        if (msg == null) return;

        drawOnly(msg);

        WarnBean warnBean = gson.fromJson(msg.getContent(), WarnBean.class);
        if (warnBean == null) return;

        String content = warnBean.getWarningLevel();

        MessageDialog dialog = MessageDialog.newInstance(ctx, msg.getTitle(), content, msg.getPublishTime());
        dispatcher.showDialog(dialog);

        if (isTop) {
            ui.setScrollingText(content);
            dispatcher.speak("您有一条预警信息，" + content);
        }
    }

    public void drawOnly(Last24HoursBean.RowsBean msg) {
        if (msg == null) return;
        String msgId = msg.getId();
        if (TextUtils.isEmpty(msgId)) return;

        // 去重：已画过则跳过
        if (drawnIdsByMsgId.containsKey(msgId)) return;

        List<Long> ids = drawWarnElements(msg);
        if (ids != null && !ids.isEmpty()) {
            drawnIdsByMsgId.put(msgId, ids);
        }
    }

    /**
     * 同步：本轮仍存在的预警 msgId 保留；消失的精准删除
     */
    public void syncCurrentWarnIds(Set<String> currentWarnMsgIds) {
        if (currentWarnMsgIds == null) currentWarnMsgIds = Collections.emptySet();

        Iterator<Map.Entry<String, List<Long>>> it = drawnIdsByMsgId.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, List<Long>> e = it.next();
            String oldMsgId = e.getKey();

            if (!currentWarnMsgIds.contains(oldMsgId)) {
                removeByMsgId(oldMsgId);
                it.remove();
            }
        }
    }

    /**
     * 精准删除某个 msgId 的所有图元
     */
    public void removeByMsgId(String msgId) {
        if (TextUtils.isEmpty(msgId)) return;

        List<Long> ids = drawnIdsByMsgId.get(msgId);
        if (ids == null || ids.isEmpty()) return;

        for (Long id : ids) {
            if (id == null) continue;
            map.removeElement(id);
        }
        drawnIdsByMsgId.remove(msgId);
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
