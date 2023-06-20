package io.appmetrica.analytics.testutils;

import android.location.Location;

public class DummyLocationProvider {

    public static Location getLocation() {
        return getLocation("Test provider");
    }

    public static Location getLocation(String provider) {
        Location location = new Location(provider);
        location.setLatitude(123123D);
        location.setLongitude(343445D);
        location.setAccuracy(234234F);
        location.setAltitude(3345345D);
        location.setBearing(343F);
        location.setSpeed(453455F);
        location.setTime(1000L);
        return location;
    }

}
