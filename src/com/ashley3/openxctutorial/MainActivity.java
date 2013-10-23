package com.ashley3.openxctutorial;

import com.openxc.VehicleManager;
import com.openxc.measurements.Measurement;
import com.openxc.measurements.UnrecognizedMeasurementTypeException;
import com.openxc.measurements.VehicleSpeed;
import com.openxc.remote.VehicleServiceException;

import android.os.Bundle;
import android.os.IBinder;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;

public class MainActivity extends Activity {
	private VehicleManager mVehicleManager;
	private boolean mBound = false;
	
	private TextView mVehicleSpeedView;
	
	private VehicleSpeed.Listener mSpeedListener = new VehicleSpeed.Listener() {
		public void receive(Measurement measurement) {
			final VehicleSpeed speed = (VehicleSpeed) measurement;
			MainActivity.this.runOnUiThread(new Runnable() {
				public void run() {
					mVehicleSpeedView.setText("Vehicle speed (km/h): "+speed.getValue().doubleValue());
				}
			});
		}
	};
	
	private ServiceConnection mConnection = new ServiceConnection() {
		// Called when the connection with the service is established
		public void onServiceConnected(ComponentName className, IBinder service) {
			Log.i("openxc","Bound to VehicleManager");
			mVehicleManager = ((VehicleManager.VehicleBinder)service).getService();
			try {
				mVehicleManager.addListener(VehicleSpeed.class, mSpeedListener);
			} catch (VehicleServiceException e) {
				e.printStackTrace();
			} catch (UnrecognizedMeasurementTypeException e) {
				e.printStackTrace();
			}
		}
		
		// Called when the connection with the service disconnects unexpectedly
		public void onServiceDisconnected(ComponentName className) {
			Log.w("openxc","VehicleService disconnected unexpectedly");
			mVehicleManager = null;
			mBound = false;
		}
	};

	@Override
	protected void onStart() {
		super.onStart();
		Intent intent = new Intent(this, VehicleManager.class);
		if (!mBound)
			mBound = bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	public void onPause() {
		super.onPause();
		Log.i("openxc","Unbinding from vehicle service");
		unbindService(mConnection);
		mBound = false;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		if (!mBound) {
			Intent intent = new Intent(this, VehicleManager.class);
			mBound = bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
			if (mBound)
				Log.i("openxc","Binding to Vehicle Manager");
			else
				Log.e("openxc","Failed to bind to Vehicle Manager");
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mVehicleSpeedView = (TextView) findViewById(R.id.vehicle_speed);
	}

}
