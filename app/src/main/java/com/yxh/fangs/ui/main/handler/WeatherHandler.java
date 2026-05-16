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
import com.yxh.fangs.ui.dialog.RedWeatherDialogFragment;
import com.yxh.fangs.ui.main.MainUiBinder;
import com.yxh.fangs.ui.main.MapController;
import com.yxh.fangs.ui.main.MessageDispatcher;
import com.yxh.fangs.ui.main.MessageHandler;
import com.yxh.fangs.ui.main.WeatherTextBuilder;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class WeatherHandler implements MessageHandler {

    private final Context ctx;
    private final MainUiBinder ui;
    private final MapController map;
    private final MessageDispatcher.DispatchState state;
    private final MessageDispatcher dispatcher;
    private final NoticeElementStore elementStore;
    private final Gson gson = new Gson();

    public WeatherHandler(Context ctx, MainUiBinder ui, MapController map,
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
            RedWeatherDialogFragment weatherDialogFragment = new RedWeatherDialogFragment(message, first.getWeatherPhenomenon());
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

        if (elementStore.contains(msgId)) return;

        List<Long> ids = drawWeatherShapes(msg);
        elementStore.put(msgId, ids);
    }

    public void syncCurrentWeatherIds(Set<String> currentWeatherMsgIds) {
        elementStore.sync(currentWeatherMsgIds);
    }

    public void clearAll() {
        elementStore.clearAll();
    }

    public void removeByMsgId(String msgId) {
        elementStore.remove(msgId);
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
