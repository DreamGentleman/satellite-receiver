package com.yxh.fangs.ui.main.handler;

import android.content.Context;
import android.text.TextUtils;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yxh.fangs.bean.Last24HoursBean;
import com.yxh.fangs.bean.MapShape;
import com.yxh.fangs.bean.NoticeType;
import com.yxh.fangs.bean.WeatherBean;
import com.yxh.fangs.data.repository.WeatherRepository;
import com.yxh.fangs.map.layer.LayerType;
import com.yxh.fangs.ui.dialog.WeatherDialogFragment;
import com.yxh.fangs.ui.main.MainUiBinder;
import com.yxh.fangs.ui.main.MapController;
import com.yxh.fangs.ui.main.MessageDispatcher;
import com.yxh.fangs.ui.main.MessageHandler;
import com.yxh.fangs.ui.main.WeatherTextBuilder;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class WeatherHandler implements MessageHandler {

    private final Context ctx;
    private final MainUiBinder ui;
    private final MapController map;
    private final MessageDispatcher.DispatchState state;
    private final MessageDispatcher dispatcher;
    private final Map<String, List<Long>> drawnIdsByMsgId = new HashMap<>();
    private final Gson gson = new Gson();

    public WeatherHandler(Context ctx, MainUiBinder ui, MapController map,
                          MessageDispatcher.DispatchState state, MessageDispatcher dispatcher) {
        this.ctx = ctx;
        this.ui = ui;
        this.map = map;
        this.state = state;
        this.dispatcher = dispatcher;
    }

    @Override
    public boolean canHandle(String type) {
        return NoticeType.NOTICE_WEATHER.equals(type);
    }

    @Override
    public void handle(Last24HoursBean.RowsBean msg, String level) {
        state.setSelectedLayer(LayerType.RAINSTORM);

        Type t = new TypeToken<List<WeatherBean>>() {
        }.getType();
        List<WeatherBean> list = gson.fromJson(msg.getContent(), t);
        if (list == null || list.isEmpty()) return;

        WeatherBean first = list.get(0);
        state.setSelectedWeather(first);

        String message = WeatherTextBuilder.buildForecastText(first);

        if ("0".equals(level)) {
            WeatherDialogFragment weatherDialogFragment = new WeatherDialogFragment(message, first.getWeatherPhenomenon());
            dispatcher.showDialog(weatherDialogFragment, ((AppCompatActivity) ctx).getSupportFragmentManager(), "weather");
        }

        ui.setScrollingText(message);
        dispatcher.speak(message);
    }

    /**
     * 轮询绘制：只绘制，不播报
     * - 同一 msgId 只画一次（避免叠图）
     */
    public void drawOnly(Last24HoursBean.RowsBean msg) {
        if (msg == null) return;
        String msgId = msg.getId();
        if (TextUtils.isEmpty(msgId)) return;

        // 去重：已画过则跳过
        if (drawnIdsByMsgId.containsKey(msgId)) return;

        List<Long> ids = drawWeatherShapes(msg);
        if (ids != null && !ids.isEmpty()) {
            drawnIdsByMsgId.put(msgId, ids);
        }
    }

    /**
     * 同步：本轮仍存在的天气 msgId 保留；消失的精准删除
     */
    public void syncCurrentWeatherIds(Set<String> currentWeatherMsgIds) {
        if (currentWeatherMsgIds == null) currentWeatherMsgIds = Collections.emptySet();

        Iterator<Map.Entry<String, List<Long>>> it = drawnIdsByMsgId.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, List<Long>> e = it.next();
            String oldMsgId = e.getKey();
            if (!currentWeatherMsgIds.contains(oldMsgId)) {
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
            map.removeElement(id);
        }

        drawnIdsByMsgId.remove(msgId);
    }

    private List<Long> drawWeatherShapes(Last24HoursBean.RowsBean msg) {
        state.setSelectedLayer(LayerType.RAINSTORM);

        Type t = new TypeToken<List<WeatherBean>>() {
        }.getType();
        List<WeatherBean> list = gson.fromJson(msg.getContent(), t);
        if (list == null || list.isEmpty()) return null;

        List<Long> ids = new ArrayList<>();

        for (WeatherBean weatherBean : list) {
            if (weatherBean == null) continue;

            MapShape shape = gson.fromJson(weatherBean.getMapJson(), MapShape.class);
            if (shape == null || shape.coordinates == null) continue;

            int iconResId = WeatherRepository.getWeatherIcon(weatherBean.getWeatherPhenomenon());

            // ===== circle =====
            if ("circle".equals(shape.type)
                    && shape.coordinates.center != null) {

                long circleId = map.drawCircle(
                        LayerType.RAINSTORM,
                        shape.coordinates.center.lng,
                        shape.coordinates.center.lat,
                        shape.coordinates.color,
                        shape.coordinates.radius,
                        ""
                );
                if (circleId > 0) ids.add(circleId);

                long pointId = map.drawPoint(
                        LayerType.RAINSTORM,
                        shape.coordinates.center.lng,
                        shape.coordinates.center.lat,
                        iconResId,
                        2f,
                        0
                );
                if (pointId > 0) ids.add(pointId);

            }
            // ===== rectangle =====
            else if ("rectangle".equals(shape.type)
                    && shape.coordinates.bounds != null
                    && shape.coordinates.center != null) {

                long rectId = map.drawRectangle(
                        LayerType.RAINSTORM,
                        shape.coordinates.bounds.sw.lng,
                        shape.coordinates.bounds.sw.lat,
                        shape.coordinates.bounds.ne.lng,
                        shape.coordinates.bounds.ne.lat,
                        shape.coordinates.color,
                        ""
                );
                if (rectId > 0) ids.add(rectId);

                long pointId = map.drawPoint(
                        LayerType.RAINSTORM,
                        shape.coordinates.center.lng,
                        shape.coordinates.center.lat,
                        iconResId,
                        2f,
                        0
                );
                if (pointId > 0) ids.add(pointId);
            }
        }

        return ids;
    }
}
