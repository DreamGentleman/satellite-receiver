package com.yxh.fangs.ui.main;

import com.bigemap.bmcore.entity.GeoPoint;
import com.bigemap.bmcore.entity.MapConfig;
import com.bigemap.bmcore.entity.VectorElement;
import com.bigemap.bmcore.listener.OperationCallback;

final class EarthReadyCallback implements OperationCallback {

    interface Listener {
        void onEarthReady();

        void onScreenCenterChanged(GeoPoint center, double altitude);
    }

    private final Listener listener;

    EarthReadyCallback(Listener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreateEarthComplete() {
        listener.onEarthReady();
    }

    @Override
    public void onCreateEarthFail(int i) {
    }

    @Override
    public void onScroll() {
    }

    @Override
    public void callbackEarthOrientation(float v) {
    }

    @Override
    public void callbackScreenCenterPoint(GeoPoint center, double altitude, long l, int i) {
        listener.onScreenCenterChanged(center, altitude);
    }

    @Override
    public void onSingleTapConfirmed(android.view.MotionEvent motionEvent, GeoPoint geoPoint) {
    }

    @Override
    public void onLongPress(android.view.MotionEvent motionEvent, GeoPoint geoPoint) {
    }

    @Override
    public void onCallbackSiWeiHistoryData(String[] strings) {
    }

    @Override
    public void onCallbackDrawElementStepEditing(VectorElement vectorElement) {
    }

    @Override
    public void onCallbackDrawElementStepCreated(VectorElement vectorElement) {
    }

    @Override
    public void onClickedElement(VectorElement vectorElement) {
    }

    @Override
    public void onLongClickedElement(VectorElement vectorElement) {
    }

    @Override
    public void onChangeMapSourceComplete(MapConfig mapConfig) {
    }

    @Override
    public void onChangeMapTypeGroupComplete(MapConfig mapConfig) {
    }

    @Override
    public void onCallbackHistoricalImagery(int[] ints) {
    }

    @Override
    public void onCallbackHistoricalImagery(String[] strings) {
    }

    @Override
    public void onCallbackAddedTrackPoint(GeoPoint geoPoint) {
    }

    @Override
    public void onLoadVectorFileStart(int i) {
    }

    @Override
    public void onLoadVectorFileDoing() {
    }

    @Override
    public void onLoadVectorFileComplete(boolean b, long l) {
    }

    @Override
    public void onLoadVectorFileComplete(VectorElement vectorElement) {
    }

    @Override
    public byte[] onFormatStringToPicture(String s) {
        return new byte[0];
    }

    @Override
    public byte[] webPToPng(byte[] bytes) {
        return new byte[0];
    }

    @Override
    public boolean onUpdateOfflineCallback(int i, int i1) {
        return false;
    }
}
