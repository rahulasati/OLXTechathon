package com.olx.techathon;

import com.google.android.gms.common.ConnectionResult;

import android.location.Location;

public interface OnLocationChangeListener {
	void onLocationFound(Location location);
	void onLocationFailed(ConnectionResult connectionResult);
}
