package com.yxh.fangs.ui.main;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import com.google.gson.Gson;
import com.yxh.fangs.bean.DeviceLocationRecordRequest;
import com.yxh.fangs.bean.DeviceLocationRecordResponse;
import com.yxh.fangs.bean.Last24HoursBean;
import com.yxh.fangs.bean.NoticeType;
import com.yxh.fangs.bean.WeatherBean;
import com.yxh.fangs.core.tts.TTSManager;
import com.yxh.fangs.data.network.HttpUtils;
import com.yxh.fangs.data.network.api.UrlUtils;
import com.yxh.fangs.data.repository.WeatherRepository;
import com.yxh.fangs.map.layer.LayerType;
import com.yxh.fangs.ui.dialog.DialogStackManager;
import com.yxh.fangs.ui.dialog.WeatherDialog;
import com.yxh.fangs.ui.main.handler.BeidouHandler;
import com.yxh.fangs.ui.main.handler.ImageHandler;
import com.yxh.fangs.ui.main.handler.SmsHandler;
import com.yxh.fangs.ui.main.handler.TyphoonHandler;
import com.yxh.fangs.ui.main.handler.WarnHandler;
import com.yxh.fangs.ui.main.handler.WeatherHandler;
import com.yxh.fangs.util.LogUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MessageDispatcher {

    private final WeatherHandler weatherHandler;
    private final TyphoonHandler typhoonHandler;
    private final SmsHandler smsHandler;
    private final ImageHandler imageHandler;
    private final WarnHandler warnHandler;
    private final BeidouHandler beidouHandler;

    public interface DispatchState {
        void setSelectedWeather(WeatherBean bean);

        WeatherBean getSelectedWeather();

        void setSelectedLayer(LayerType type);

        LayerType getSelectedLayer();

        double getLon();

        double getLat();
    }

    private final Context ctx;
    private final MainUiBinder ui;
    private final MapController map;
    private final DispatchState state;
    private final DialogStackManager dialogManager;

    private final Gson gson = new Gson();

    private boolean turnOnNotice = true;
    private final Set<String> readIds = new HashSet<>();

    private final List<MessageHandler> handlers = new ArrayList<>();

    public MessageDispatcher(Activity ctx, MainUiBinder ui, MapController map, DispatchState state) {
        this.ctx = ctx;
        this.ui = ui;
        this.map = map;
        this.state = state;

        this.dialogManager = new DialogStackManager(ctx);

        // 注册处理器（顺序无所谓）
        beidouHandler = new BeidouHandler(ctx, ui, this);
        handlers.add(beidouHandler);
        warnHandler = new WarnHandler(ctx, ui, map, state, this);
        handlers.add(warnHandler);
        imageHandler = new ImageHandler(ctx, ui, this);
        handlers.add(imageHandler);
        smsHandler = new SmsHandler(ctx, ui, this);
        handlers.add(smsHandler);
        typhoonHandler = new TyphoonHandler(ctx, ui, map, state, this);
        handlers.add(typhoonHandler);
        weatherHandler = new WeatherHandler(ctx, ui, map, state, this);
        handlers.add(weatherHandler);
    }

    public void setTurnOnNotice(boolean on) {
        this.turnOnNotice = on;
    }

    public void setSelectedLayer(LayerType type) {
        state.setSelectedLayer(type);
    }

    public void fetchAndHandleMessages(boolean manualRefresh) {
        HttpUtils.get(UrlUtils.history(1, 1000), new HttpUtils.HttpCallback() {
            @Override
            public void onSuccess(String body) {
                ui.finishRefreshIfNeeded();
                LogUtils.json(body);
                Last24HoursBean bean = gson.fromJson(body, Last24HoursBean.class);
                if (bean == null || bean.getCode() != 200 || bean.getRows() == null) {
                    return;
                }

                List<Last24HoursBean.RowsBean> sortRow = bean.getSortRow();
                ui.updateNoticeList(sortRow);

                Set<String> currentWeatherIds = new HashSet<>();
                Set<String> currentTyphoonIds = new HashSet<>();
                Set<String> currentWarnIds = new HashSet<>();
                for (Last24HoursBean.RowsBean msg : sortRow) {
                    if (msg == null) continue;
                    if (NoticeType.NOTICE_WEATHER.equals(msg.getMessageType())) {
                        String id = msg.getId();
                        if (!TextUtils.isEmpty(id)) currentWeatherIds.add(id);
                        weatherHandler.drawOnly(msg);
                    } else if (NoticeType.NOTICE_TYPHOON.equals(msg.getMessageType())) {
                        String id = msg.getId();
                        if (!TextUtils.isEmpty(id)) currentTyphoonIds.add(id);
                        typhoonHandler.drawOnly(msg);
                    } else if (NoticeType.NOTICE_ALERT.equals(msg.getMessageType())) {
                        String id = msg.getId();
                        if (!TextUtils.isEmpty(id)) currentWarnIds.add(id);
                        warnHandler.drawOnly(msg);
                    }
                }
                warnHandler.syncCurrentWarnIds(currentWarnIds);
                weatherHandler.syncCurrentWeatherIds(currentWeatherIds);
                typhoonHandler.syncCurrentTyphoonIds(currentTyphoonIds);

                // 逐条处理（遇到第一条新消息就 break，保持你原来的行为）
                for (int i = 0; i < sortRow.size(); i++) {
                    Last24HoursBean.RowsBean msg = sortRow.get(i);
                    if (msg == null) continue;
                    String id = msg.getId();
                    if (!TextUtils.isEmpty(id) && readIds.contains(id)) continue;
                    if (!TextUtils.isEmpty(id)) readIds.add(id);

                    boolean handled = dispatchOne(msg, i == 0);
                    if (handled) break;
                }
            }

            @Override
            public void onError(String msg) {
                ui.finishRefreshIfNeeded();
            }
        });
    }

    private boolean dispatchOne(Last24HoursBean.RowsBean msg, boolean isTop) {
        for (MessageHandler handler : handlers) {
            if (handler.canHandle(msg.getMessageType())) {
                handler.handle(msg, isTop);
                return true;
            }
        }
        return false;
    }

    public void speak(String text) {
        if (!turnOnNotice || TextUtils.isEmpty(text)) return;
        TTSManager.getInstance().speak(text);
    }

    // ===== 外部调用：显示天气详情 =====
    public void showWeatherDetail(WeatherBean bean) {
        String message = WeatherTextBuilder.buildForecastText(bean);
        speak(message);
        WeatherDialog dialog = WeatherDialog.newInstance(ctx, message, WeatherRepository.getWeatherIcon(bean.getWeatherPhenomenon()));
        dialog.show();
    }

    // ===== LayoutActivity 回调应用图层展示 =====
    public void applyDrawContent(String drawContent) {
        // drawContent 为空：全部隐藏
        if (TextUtils.isEmpty(drawContent)) {
            map.hideAllBaseLayers();
            map.hideLayer(LayerType.TYPHOON);
            map.hideLayer(LayerType.RAINSTORM);
            return;
        }

        boolean showFishing = drawContent.contains("渔场");
        boolean showNoFishLine = drawContent.contains("机轮拖网渔业禁渔线");
        boolean showCoast = drawContent.contains("领海基线");
        boolean showCKFA = drawContent.contains("中韩渔业协定水域");
        boolean showCJFA = drawContent.contains("中日渔业协定水域");
        boolean showTyphoon = drawContent.contains("台风预警");
        boolean showRain = drawContent.contains("气象信息");

        map.hideAllBaseLayers();
        map.hideLayer(LayerType.TYPHOON);
        map.hideLayer(LayerType.RAINSTORM);

        if (showFishing) map.drawBaseLine(LayerType.FISHING_GROUND);
        if (showNoFishLine) map.drawBaseLine(LayerType.NO_FISHING_LINE);
        if (showCoast) map.drawBaseLine(LayerType.COAST_LINE);
        if (showCKFA) map.drawBaseLine(LayerType.CKFA);
        if (showCJFA) map.drawBaseLine(LayerType.CJFA);

        if (showTyphoon) map.showLayer(LayerType.TYPHOON);
        if (showRain) map.showLayer(LayerType.RAINSTORM);
    }

    // ===== 上传定位（你原来的逻辑） =====
    public void uploadDeviceLocation(String deviceSn, double lon, double lat) {
        DeviceLocationRecordRequest req = new DeviceLocationRecordRequest();
        req.setDeviceSn(deviceSn);
        req.setLongitude(String.valueOf(lon));
        req.setLatitude(String.valueOf(lat));
        String json = new Gson().toJson(req);

        HttpUtils.postJson(UrlUtils.getDeviceLocationAddUrl(), json, new HttpUtils.HttpCallback() {
            @Override
            public void onSuccess(String body) {
                DeviceLocationRecordResponse resp = new Gson().fromJson(body, DeviceLocationRecordResponse.class);
                if (resp != null && resp.getCode() == 200) {
                    // 不再 Toast 干扰
                }
            }

            @Override
            public void onError(String msg) {
                // ignore
            }
        });
    }

    public void toast(String text) {
        Toast.makeText(ctx, text, Toast.LENGTH_SHORT).show();
    }

    public void showDialog(AlertDialog dialog) {
        dialogManager.show(dialog);
    }

    public void release() {
        dialogManager.release();
    }
}
