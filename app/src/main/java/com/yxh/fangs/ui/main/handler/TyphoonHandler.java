package com.yxh.fangs.ui.main.handler;

import android.content.Context;
import android.text.TextUtils;

import androidx.appcompat.app.AppCompatActivity;

import com.bigemap.bmcore.entity.GeoPoint;
import com.google.gson.Gson;
import com.yxh.fangs.bean.Last24HoursBean;
import com.yxh.fangs.bean.NoticeType;
import com.yxh.fangs.bean.TyphoonBean2;
import com.yxh.fangs.map.layer.LayerType;
import com.yxh.fangs.ui.dialog.MessageDialogFragment;
import com.yxh.fangs.ui.main.MainUiBinder;
import com.yxh.fangs.ui.main.MapController;
import com.yxh.fangs.ui.main.MessageDispatcher;
import com.yxh.fangs.ui.main.MessageHandler;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TyphoonHandler implements MessageHandler {

    private final Context ctx;
    private final MainUiBinder ui;
    private final MapController map;
    private final MessageDispatcher.DispatchState state;
    private final MessageDispatcher dispatcher;
    private final Gson gson = new Gson();
    private long lastTyphoonCircleId = -1;
    private int drawHour = -1;
    private final Map<String, List<Long>> drawnIdsByMsgId = new HashMap<>();

    public TyphoonHandler(Context ctx, MainUiBinder ui, MapController map,
                          MessageDispatcher.DispatchState state, MessageDispatcher dispatcher) {
        this.ctx = ctx;
        this.ui = ui;
        this.map = map;
        this.state = state;
        this.dispatcher = dispatcher;
    }

    @Override
    public boolean canHandle(String type) {
        return NoticeType.NOTICE_TYPHOON.equals(type);
    }

    @Override
    public void handle(Last24HoursBean.RowsBean msg, String level) {
        if (msg == null) return;
        state.setSelectedLayer(LayerType.TYPHOON);

        drawOnly(msg);
        TyphoonBean2 typhoonBean = gson.fromJson(msg.getContent(), TyphoonBean2.class);
        if (typhoonBean == null) return;
        if ("0".equals(level)) {
            MessageDialogFragment messageDialogFragment = new MessageDialogFragment(msg.getTitle(), typhoonBean.getMovingDirection(), msg.getPublishTime());
            dispatcher.showDialog(messageDialogFragment, ((AppCompatActivity) ctx).getSupportFragmentManager(), "typhoon");
        }

        dispatcher.speak("您有一条台风消息");
        ui.setScrollingText("您有一条台风消息");
    }

    /**
     * 轮询绘制：只绘制，不播报
     * - 同一 msgId 只画一次（避免叠图）
     */
    public void drawOnly(Last24HoursBean.RowsBean msg) {
        if (msg == null) return;
        String msgId = msg.getId();

        if (TextUtils.isEmpty(msgId)) return;
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        // 去重：已画过则跳过
        if (drawnIdsByMsgId.containsKey(msgId)) {
            if (hour == drawHour) {
                return;
            }
            removeByMsgId(msgId);
        }
        List<Long> ids = drawTyphoonElements(msg);
        drawHour = hour;
        if (ids != null && !ids.isEmpty()) {
            drawnIdsByMsgId.put(msgId, ids);
        }
    }

    /**
     * 同步：本轮仍存在的台风 msgId 保留；消失的精准删除
     */
    public void syncCurrentTyphoonIds(Set<String> currentTyphoonMsgIds) {
        if (currentTyphoonMsgIds == null) currentTyphoonMsgIds = Collections.emptySet();

        Iterator<Map.Entry<String, List<Long>>> it = drawnIdsByMsgId.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, List<Long>> e = it.next();
            String oldMsgId = e.getKey();
            if (!currentTyphoonMsgIds.contains(oldMsgId)) {
                removeByMsgId(oldMsgId);
                it.remove();
            }
        }
    }

    public void clearAll() {
        // 1) 逐条 removeElement：保证地图引擎真的删掉
        for (Map.Entry<String, List<Long>> e : drawnIdsByMsgId.entrySet()) {
            List<Long> ids = e.getValue();
            if (ids == null) continue;
            for (Long id : ids) {
                if (id == null) continue;
                map.removeElement(id);
            }
        }

        // 2) 清缓存
        drawnIdsByMsgId.clear();

        // 3) 可选：如果你希望“当前图层选择”也复位（不影响清除）
        // state.setSelectedLayer(null);
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
            map.removeElement(id); // 关键：按 elementId 删除
        }

        drawnIdsByMsgId.remove(msgId);
    }

    /**
     * 绘制台风图元并返回 elementId 列表：
     * - 轨迹线（polyline）
     * - 风圈（circle）
     * <p>
     * 注意：你原来的 TyphoonDrawManager2.draw(typhoonBean) 内部也可能会画东西，
     * 但它如果不返回 elementId，你无法精准移除它画的那些元素。
     * <p>
     * 所以建议：要么以后改造 TyphoonDrawManager2，让它返回 ids；
     * 要么这里先不调用它，只用你自己可控的 drawPolyline/drawCircle。
     */
    private List<Long> drawTyphoonElements(Last24HoursBean.RowsBean msg) {
        state.setSelectedLayer(LayerType.TYPHOON);

        TyphoonBean2 typhoonBean = gson.fromJson(msg.getContent(), TyphoonBean2.class);
        if (typhoonBean == null || typhoonBean.getTyphoonInfo() == null) return null;

        List<Long> ids = new ArrayList<>();

        // ===== 轨迹点 =====
        List<com.bigemap.bmcore.entity.GeoPoint> track = new ArrayList<>();
        List<TyphoonBean2.TyphoonInfoBean.MapDataBean.CoordinatesBean.PointsBean> pts =
                typhoonBean.getTyphoonInfo().getMapData().getCoordinates().getPoints();

        if (pts == null || pts.isEmpty()) return null;

        for (TyphoonBean2.TyphoonInfoBean.MapDataBean.CoordinatesBean.PointsBean p : pts) {
            track.add(new com.bigemap.bmcore.entity.GeoPoint(p.getLng(), p.getLat()));
        }

        // ===== 画轨迹线（可控可删）=====
        long lineId = map.drawPolyline(
                LayerType.TYPHOON,
                track,
                "#FF00BFFF",
                "5",
                "台风路径"
        );
        if (lineId > 0) ids.add(lineId);

        // ===== 找当前小时的风圈 =====
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
//        TyphoonBean2.WindCirclesBean current =
//                TyphoonTimeUtils.findNearestWindCircle(typhoonBean.getWindCircles(), hour);

//        if (current != null) {
//            int idx = current.getPathPointIndex();
//            if (idx < 0) idx = 0;
//            if (idx >= track.size()) idx = track.size() - 1;
//
//            GeoPoint center = track.get(idx);
//
//            long circleId = map.drawCircle(
//                    LayerType.TYPHOON,
//                    center.lon,
//                    center.lat,
//                    current.getColor(),
//                    current.getRadius() * 1000,
//                    current.getDescription()
//            );
//            if (circleId > 0) ids.add(circleId);
//        }

        List<TyphoonBean2.WindCirclesBean> currents =
                TyphoonTimeUtils.findNearestWindCircles(typhoonBean.getWindCircles(), hour);
        for (TyphoonBean2.WindCirclesBean current : currents) {
            if (current == null) continue;

            int idx = current.getPathPointIndex();
            if (idx < 0) idx = 0;
            if (idx >= track.size()) idx = track.size() - 1;

            GeoPoint center = track.get(idx);

            long circleId = map.drawCircle(
                    LayerType.TYPHOON,
                    center.lon,
                    center.lat,
                    current.getColor(),
                    current.getRadius() * 1000,
                    current.getDescription()
            );
            if (circleId > 0) {
                ids.add(circleId);
            }
        }

        return ids;
    }
}
