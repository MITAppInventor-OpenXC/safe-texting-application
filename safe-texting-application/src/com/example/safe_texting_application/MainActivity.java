package com.example.safe_texting_application;

import java.util.Locale;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.view.Menu;
import android.widget.Toast;

public class MainActivity extends Activity implements OnInitListener {
	
    private static MainActivity m_instance = null;
    private TextToSpeech myTTS;
    private int MY_DATA_CHECK_CODE = 0;
    
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.activity_main);

    	Intent checkTTSIntent = new Intent();
    	checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
    	startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	// Inflate the menu; this adds items to the action bar if it is present.
    	getMenuInflater().inflate(R.menu.main, menu);
    	return true;
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
    		Toast.makeText(this, "Sorry! Text To Speech failed...", Toast.LENGTH_LONG).show();
    	}
    }

    public void speakWords(String speech) {
    	myTTS.speak(speech, TextToSpeech.QUEUE_ADD, null);
    }

}
