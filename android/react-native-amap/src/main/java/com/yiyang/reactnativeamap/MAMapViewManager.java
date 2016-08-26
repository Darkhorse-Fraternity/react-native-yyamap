package com.yiyang.reactnativeamap;

import android.content.Context;
import android.graphics.Color;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.util.Log;

import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.CameraPosition;
import com.amap.api.maps2d.model.CircleOptions;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.AMap.OnCameraChangeListener;

import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yiyang on 16/3/1.
 */
public class MAMapViewManager extends SimpleViewManager<ReactMapView> implements OnCameraChangeListener {
    public static final String RCT_CLASS = "RCTAMap";

    private ReactMapView mMapView;

    private Context mContext;


    @Override
    public String getName() {
        return RCT_CLASS;
    }

    @Override
    protected ReactMapView createViewInstance(ThemedReactContext themedReactContext) {
        mMapView = new ReactMapView(themedReactContext);
        this.mContext = themedReactContext;
        mMapView.onCreate(null);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        return mMapView;
    }

    public ReactMapView ReactMapView () {
        return mMapView;
    }

    @ReactProp(name="showsUserLocation", defaultBoolean = false)
    public void showsUserLocation(MapView mapView, Boolean show) {
        mapView.getMap().setMyLocationEnabled(show);
    }

    @ReactProp(name="showCenterMarker", defaultBoolean = false)
    public void showCenterMarker(MapView mapView, Boolean show) {
        mapView.getMap().setOnCameraChangeListener(this);
        LatLng latLng = new LatLng(39.906901,116.397972);
        final Marker marker = mapView.getMap().addMarker(new MarkerOptions().
                position(latLng).
                title("北京").
                snippet("DefaultMarker"));
        marker.setPositionByPixels(mapView.getWidth() / 2,
                mapView.getHeight() / 2);
    }

    @ReactProp(name="showsCompass", defaultBoolean = false)
    public void showsCompass(MapView mapView, Boolean show) {
        mapView.getMap().getUiSettings().setCompassEnabled(show);
    }

    @ReactProp(name="zoomEnabled", defaultBoolean = true)
    public void setZoomEnabled(MapView mapView, Boolean enable) {
        mapView.getMap().getUiSettings().setZoomGesturesEnabled(enable);
    }

    @ReactProp(name="rotateEnabled", defaultBoolean = true)
    public void setRotateEnabled(MapView mapView, Boolean enable) {
//        mapView.getMap().getUiSettings().setRotateGesturesEnabled(enable);
    }

    @ReactProp(name="pitchEnabled", defaultBoolean = false)
    public void setTiltGestureEnabled(MapView mapView, Boolean enable) {
//        mapView.getMap().getUiSettings().setTiltGesturesEnabled(enable);
    }

    @ReactProp(name="scrollEnabled", defaultBoolean = false)
    public void setScrollEnabled(MapView mapView, Boolean enable) {
        mapView.getMap().getUiSettings().setScrollGesturesEnabled(enable);
    }

    @ReactProp(name = "mapType", defaultInt = AMap.MAP_TYPE_NORMAL)
    public void setMapType(MapView mapView, int mapType) {
        mapView.getMap().setMapType(mapType);
    }

    @ReactProp(name = "annotations")
    public void setAnnotations(ReactMapView mapView, @Nullable ReadableArray value) throws Exception{
        AMap map = mapView.getMap();
        if (value == null || value.size() == 0) {
            Log.e(RCT_CLASS, "Error: no annotation");
            return;
        }

        List<ReactMapMarker> markers = new ArrayList<ReactMapMarker>();
        int size = value.size();
        for (int i = 0; i < size; i++) {
            ReadableMap annotation = value.getMap(i);
            ReactMapMarker marker = new ReactMapMarker(this.mContext);
            marker.buildMarker(annotation);
            markers.add(marker);

        }

        mapView.setMarker(markers);

    }

    @ReactProp(name = "overlays")
    public void setOverlays(ReactMapView mapView, @Nullable ReadableArray value) throws Exception{
        if (value == null || value.size() == 0) {
            return;
        }

        List<ReactMapOverlay> overlays = new ArrayList<ReactMapOverlay>();
        int size = value.size();
        for(int i = 0; i < size; i++) {
            ReadableMap overlay = value.getMap(i);
            ReactMapOverlay polyline = new ReactMapOverlay(overlay);
            overlays.add(polyline);
        }

        mapView.setOverlays(overlays);
    }

    @ReactProp(name = "circle")
    public void setCircle(ReactMapView mapView, @Nullable ReadableMap circle) {
        if (circle != null) {
            double latitude = circle.getDouble("latitude");
            double longitude = circle.getDouble("longitude");
            int radius = circle.getInt("radius");
            int strokeWidth = circle.getInt("strokeWidth");
            mapView.getMap().addCircle(new CircleOptions().center(new LatLng(latitude, longitude))
                    .radius(radius).strokeColor(0xFFFF0000).fillColor(0x4400FF00)
                    .strokeWidth(strokeWidth));
        }
    }

    @ReactProp(name = "region")
    public void setRegion(ReactMapView mapView, @Nullable ReadableMap center) {
        if (center != null) {
            double latitude = center.getDouble("latitude");
            double longitude = center.getDouble("longitude");
            int zoomLevel = center.getInt("zoomLevel");
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(latitude, longitude)).zoom(zoomLevel)
                    .build();
            mapView.getMap().moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
    }

    private void sendEvent(String eventName,
                           @javax.annotation.Nullable WritableMap params) {
        if (mContext != null) {
            ReactContext reactContext = (ReactContext)mContext;
            reactContext
                    .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                    .emit(eventName, params);
        }
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        LatLng target = cameraPosition.target;
        if (cameraPosition != null) {
            sendEvent("onCameraChangedAmap", amapLocationToObject(cameraPosition));
        }
    }

    private WritableMap amapLocationToObject(CameraPosition cameraPosition) {
        WritableMap map = Arguments.createMap();
        LatLng pos = cameraPosition.target;
        map.putDouble("centerLantitude", pos.latitude);
        map.putDouble("centerLongitude", pos.longitude);
        return map;
    }

    @Override
    public void onCameraChangeFinish(CameraPosition cameraPosition) {

    }
}
