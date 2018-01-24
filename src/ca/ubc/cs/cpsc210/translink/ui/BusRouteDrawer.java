package ca.ubc.cs.cpsc210.translink.ui;

import android.content.Context;
import ca.ubc.cs.cpsc210.translink.BusesAreUs;
import ca.ubc.cs.cpsc210.translink.model.*;
import ca.ubc.cs.cpsc210.translink.util.Geometry;
import ca.ubc.cs.cpsc210.translink.util.LatLon;
import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.util.*;

// A bus route drawer
public class BusRouteDrawer extends MapViewOverlay {
    /** overlay used to display bus route legend text on a layer above the map */
    private BusRouteLegendOverlay busRouteLegendOverlay;
    /** overlays used to plot bus routes */
    private List<Polyline> busRouteOverlays;

    /**
     * Constructor
     * @param context   the application context
     * @param mapView   the map view
     */
    public BusRouteDrawer(Context context, MapView mapView) {
        super(context, mapView);
        busRouteLegendOverlay = createBusRouteLegendOverlay();
        busRouteOverlays = new ArrayList<>();
    }

    /**
     * Plot each visible segment of each route pattern of each route going through the selected stop.
     */
    public void plotRoutes(int zoomLevel) {
        updateVisibleArea();
        busRouteOverlays.clear();
        busRouteLegendOverlay.clear();
        Stop selectedStop = StopManager.getInstance().getSelected();

        if (selectedStop != null){
            for (Route route: selectedStop.getRoutes()){
                int color = busRouteLegendOverlay.add(route.getNumber());

                for (RoutePattern routePattern: route.getPatterns()){
                    List<LatLon> path = routePattern.getPath();

                    for (int i=0; i< path.size()-1; i++){
                        Polyline polyline = new Polyline(mapView.getContext());
                        LatLon start = path.get(i);
                        LatLon end = path.get(i+1);

                        polyline.setWidth(getLineWidth(zoomLevel));
                        polyline.setColor(color);
                        createPolyline(start, end, polyline);
                        busRouteOverlays.add(polyline);
                    }

                }
            }
        }
    }
    /**
     * Create 1 polyline from 2 points
     * @param start  the first point
     * @param end    the second point
     * @param polyline the polyline object to add points to
     */
    public void createPolyline(LatLon start, LatLon end, Polyline polyline){
        if (isPolylineVisible(start, end)){
            List<GeoPoint> geopointPair = new ArrayList<>();
            geopointPair.add(Geometry.gpFromLL(start));
            geopointPair.add(Geometry.gpFromLL(end));
            polyline.setPoints(geopointPair);
        }
    }

    /**
     * Determine if line made from 2 points is visible in mapview
     * @param start  the first point
     * @param end    the second point
     */
    public boolean isPolylineVisible(LatLon start, LatLon end){
        return Geometry.rectangleIntersectsLine(northWest, southEast, start, end);
    }


    public List<Polyline> getBusRouteOverlays() {
        return Collections.unmodifiableList(busRouteOverlays);
    }

    public BusRouteLegendOverlay getBusRouteLegendOverlay() {
        return busRouteLegendOverlay;
    }


    /**
     * Create text overlay to display bus route colours
     */
    private BusRouteLegendOverlay createBusRouteLegendOverlay() {
        ResourceProxy rp = new DefaultResourceProxyImpl(context);
        return new BusRouteLegendOverlay(rp, BusesAreUs.dpiFactor());
    }

    /**
     * Get width of line used to plot bus route based on zoom level
     * @param zoomLevel   the zoom level of the map
     * @return            width of line used to plot bus route
     */
    private float getLineWidth(int zoomLevel) {
        if(zoomLevel > 14)
            return 7.0f * BusesAreUs.dpiFactor();
        else if(zoomLevel > 10)
            return 5.0f * BusesAreUs.dpiFactor();
        else
            return 2.0f * BusesAreUs.dpiFactor();
    }
}
