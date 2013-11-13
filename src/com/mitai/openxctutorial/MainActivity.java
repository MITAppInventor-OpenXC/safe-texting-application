package com.mitai.openxctutorial;

import java.util.Locale;

import com.ashley3.openxctutorial.R;
import com.openxc.NoValueException;
import com.openxc.VehicleManager;
import com.openxc.measurements.IgnitionStatus;
import com.openxc.measurements.Measurement;
import com.openxc.measurements.TransmissionGearPosition;
import com.openxc.measurements.UnrecognizedMeasurementTypeException;
import com.openxc.remote.VehicleServiceException;

import android.os.Bundle;
import android.os.IBinder;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnInitListener{

	public static final String TAG = MainActivity.class.getCanonicalName();

	private static MainActivity m_instance = null;
	private TextToSpeech myTTS;
	private int MY_DATA_CHECK_CODE = 0;

	private VehicleManager mVehicleManager;
	private boolean mBound = false;

	private TextView mShouldReadTextOutLoudView;

	private TextView mIgnitionStatusView;
	private IgnitionStatus.IgnitionPosition mIgnitionStatus;

	private TextView mTransmissionGearPositionView;
	private TransmissionGearPosition.GearPosition mGearPosition;

	private IgnitionStatus.Listener mIgnitionStatusListener = new IgnitionStatus.Listener() {
		@Override
		public void receive(Measurement measurement) {
			final IgnitionStatus status = (IgnitionStatus) measurement;
			setStatus(status);	
		}
	};

	private TransmissionGearPosition.Listener mTransmissionGearListener = new TransmissionGearPosition.Listener() {
		@Override
		public void receive(Measurement measurement) {
			final TransmissionGearPosition status = (TransmissionGearPosition) measurement;
			setPosition(status);
		}
	};
	
	private boolean shouldReadTextOutLoud() {
		if(mIgnitionStatus==null || mGearPosition==null) return false;
		return(!mIgnitionStatus.equals(IgnitionStatus.IgnitionPosition.OFF) && !mGearPosition.equals(TransmissionGearPosition.GearPosition.NEUTRAL));
	}

	private ServiceConnection mConnection = new ServiceConnection() {
		// Called when the connection with the service is established
		public void onServiceConnected(ComponentName className, IBinder service) {
			Log.i("openxc","Bound to VehicleManager"); 
			mVehicleManager = ((VehicleManager.VehicleBinder)service).getService();
			try {
				mVehicleManager.addListener(IgnitionStatus.class, mIgnitionStatusListener);
				mVehicleManager.addListener(TransmissionGearPosition.class, mTransmissionGearListener);
			} catch (VehicleServiceException e) {
				e.printStackTrace();
			} catch (UnrecognizedMeasurementTypeException e) {
				e.printStackTrace();
			}
			try {
				setStatus((IgnitionStatus) mVehicleManager.get(IgnitionStatus.class));
				setPosition((TransmissionGearPosition) mVehicleManager.get(TransmissionGearPosition.class));
			} catch(NoValueException e) {
				Log.w(TAG, "The vehicle may not have made the measurement yet");
			} catch(UnrecognizedMeasurementTypeException e) {
				Log.w(TAG, "The measurement type was not recognized");
			}
		}

		// Called when the connection with the service disconnects unexpectedly
		public void onServiceDisconnected(ComponentName className) {
			Log.w("openxc","VehicleService disconnected unexpectedly");
			mVehicleManager = null;
			mBound = false;
		}
	};

	/**
	 * Get the singleton instance of this class
	 * @return MainActivity
	 */
	public static MainActivity getInstance() {
		if (m_instance == null) {
			m_instance = new MainActivity();
		}
		return m_instance;
	}

	public MainActivity() {
		m_instance = this;

	}

	private void setStatus(IgnitionStatus status){
		mIgnitionStatus = status.getValue().enumValue();
		MainActivity.this.runOnUiThread(new Runnable() {
			public void run() {
				mIgnitionStatusView.setText("Vehicle ignition status (enum): "+mIgnitionStatus);
			}
		});
		setShouldReadOutLoud();
	}

	private void setPosition(TransmissionGearPosition status){
		mGearPosition = status.getValue().enumValue();
		MainActivity.this.runOnUiThread(new Runnable() {
			public void run() {
				mTransmissionGearPositionView.setText("Transmission gear position (enum): "+mGearPosition);
			}
		});
		setShouldReadOutLoud();
	}

	private void setShouldReadOutLoud(){
		MainActivity.this.runOnUiThread(new Runnable() {
			public void run() {
				mShouldReadTextOutLoudView.setText("Should read texts out loud? "+shouldReadTextOutLoud());
			}
		});
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		Intent intent = new Intent(this, VehicleManager.class);
		if (!mBound)
			mBound = bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
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
		mIgnitionStatusView = (TextView) findViewById(R.id.vehicle_ignition_status);
		mIgnitionStatusView.setText("Vehicle ignition status: waiting for input");
		mTransmissionGearPositionView = (TextView) findViewById(R.id.vehicle_transmission_gear_position);
		mTransmissionGearPositionView.setText("Transmission gear position: waiting for input");
		mShouldReadTextOutLoudView = (TextView) findViewById(R.id.read_texts_out_loud_boolean);
		Intent checkTTSIntent = new Intent();
		checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == MY_DATA_CHECK_CODE) {
			if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
				myTTS = new TextToSpeech(this, (OnInitListener) this);
			}
			else {
				Intent installTTSIntent = new Intent();
				installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
				startActivity(installTTSIntent);
			}
		}
	}

	@Override
	public void onInit(int initStatus) {
		if (initStatus == TextToSpeech.SUCCESS && myTTS.isLanguageAvailable(Locale.US) == TextToSpeech.LANG_AVAILABLE) {
			myTTS.setLanguage(Locale.US);
		} else if (initStatus == TextToSpeech.ERROR) {
			Log.e(TAG,"error with TTS");
			Toast.makeText(this, "Sorry! Text To Speech failed...", Toast.LENGTH_LONG).show();
		}
	}

	public void speakWords(String speech) {
		if(shouldReadTextOutLoud()){
			myTTS.speak(speech, TextToSpeech.QUEUE_ADD, null);
		}
	}
}
