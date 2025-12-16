package com.yxh.fangs.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LayerManager {

    private final Map<LayerType, List<Long>> layerMap = new HashMap<>();

    public LayerManager() {
        for (LayerType type : LayerType.values()) {
            layerMap.put(type, new ArrayList<>());
        }
    }

    // 添加图层id
    public void addLayer(LayerType type, long layerId) {
        if (type == null) return;
        layerMap.get(type).add(layerId);
    }

    // 获取某类图层
    public List<Long> getLayers(LayerType type) {
        if (type == null) new ArrayList<>();
        return layerMap.get(type);
    }

    // 清空某类图层
    public void clear(LayerType type) {
        layerMap.get(type).clear();
    }

    // 获取全部图层
    public Map<LayerType, List<Long>> getAll() {
        return layerMap;
    }
}
