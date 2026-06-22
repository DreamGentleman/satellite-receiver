package com.yxh.fangs.map.layer;

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
        if (type == null || layerId <= 0) return;
        layerMap.get(type).add(layerId);
    }

    public void addLayer(LayerType type, List<Long> layerId) {
        if (type == null || layerId == null || layerId.isEmpty()) return;
        layerMap.get(type).addAll(layerId);
    }

    // 获取某类图层
    public List<Long> getLayers(LayerType type) {
        if (type == null) return new ArrayList<>();
        return layerMap.get(type);
    }

    // 清空某类图层
    public void clear(LayerType type) {
        if (type == null) return;
        layerMap.get(type).clear();
    }

    public void removeLayer(long layerId) {
        if (layerId <= 0) return;
        for (List<Long> ids : layerMap.values()) {
            ids.remove(layerId);
        }
    }

    // 获取全部图层
    public Map<LayerType, List<Long>> getAll() {
        return layerMap;
    }
}
