/**
 * @description Helper class to perform Location updates using play service LocationService API.
 * @author Rahul Asati
 * @date 19/09/2015
 */

package com.olx.techathon;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationServices;

public class LocationHelper implements ConnectionCallbacks,
		OnConnectionFailedListener {
	private static final String TAG = "Rahul";//LocationHelper.class.getSimpleName();

	private GoogleApiClient mGoogleApiClient;
	private OnLocationChangeListener mLocationListener;
	private static LocationHelper mInstance;
	private static Object lock = new Object();
	private Context mContext;
	private Location mLastLocation;

	public static LocationHelper getInstance() {
		if (mInstance == null) {
			synchronized (lock) {
				if (mInstance == null) {
					mInstance = new LocationHelper();
				}
			}
		}
		return mInstance;
	}

	protected synchronized void buildGoogleApiClient(Context context) {
		mContext = context;
		mGoogleApiClient = new GoogleApiClient.Builder(context)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.addApi(LocationServices.API).build();
		mGoogleApiClient.connect();
	}

	public String getAddressByLocation(Context context, Location location) {
		String addressStr = "";
		try {
			Geocoder geoCoder = new Geocoder(context, Locale.getDefault());
			List<Address> addresses = geoCoder.getFromLocation(
					location.getLatitude(), location.getLongitude(), 1);
			// Handle case where no address was found.
			if (addresses == null || addresses.size() == 0) {
				Toast.makeText(context, "Address is Null", Toast.LENGTH_SHORT)
						.show();
			} else {
				Address address = addresses.get(0);
				ArrayList<String> addressFragments = new ArrayList<String>();
				// Fetch the address lines using getAddressLine,
				// join them, and send them to the thread.
				for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
					addressFragments.add(address.getAddressLine(i));
				}
				addressStr = TextUtils.join(
						System.getProperty("line.separator"), addressFragments);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return addressStr;
	}

	public String getLocality(Context context, Location location) {
		String addressStr = "";
		try {
			Geocoder geoCoder = new Geocoder(context, Locale.getDefault());
			List<Address> addresses = geoCoder.getFromLocation(
					location.getLatitude(), location.getLongitude(), 1);
			// Handle case where no address was found.
			if (addresses != null || addresses.size() > 0) {
				Address address = addresses.get(0);
				if (address == null) {
					return addressStr;
				}
				String throughfare = address.getThoroughfare();
				String locality = address.getLocality();
				if (throughfare != null) {
					addressStr += throughfare + ", ";
				}

				if (locality != null) {
					addressStr += locality;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return addressStr;
	}

	public Location getCurrentLocation() {
		return mLastLocation;
	}

	public void setCurrentLocation(Location location) {
		mLastLocation = location;
	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		Toast.makeText(mContext, "Location Connection Failed",
				Toast.LENGTH_SHORT).show();
		if(mLocationListener != null) {
			mLocationListener.onLocationFailed(connectionResult);
		}
	}

	@Override
	public void onConnected(Bundle bundle) {
		Log.v(TAG, "LocationService onConnected");
		mLastLocation = LocationServices.FusedLocationApi
				.getLastLocation(mGoogleApiClient);
		Log.v(TAG, "LocationService Location : "+mLastLocation);

		if(mLocationListener != null) {
			mLocationListener.onLocationFound(mLastLocation);
		}
	}

	@Override
	public void onConnectionSuspended(int arg0) {
		Toast.makeText(mContext, "Location Connection Suspended",
				Toast.LENGTH_SHORT).show();
	}

	public static boolean isGPSEnabled(Context context) {
		LocationManager lm = (LocationManager) context
				.getSystemService(Context.LOCATION_SERVICE);
		boolean gpsEnabled = false;
		boolean networkEnabled = false;

		try {
			gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
			networkEnabled = lm
					.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		} catch (Exception ex) {
			Log.v(TAG, "" + ex);
		}

		if (gpsEnabled || networkEnabled) {
			return true;
		}

		return false;
	}

	public void setLocationListener(OnLocationChangeListener loctionListener) {
		this.mLocationListener = loctionListener;
	}

}
