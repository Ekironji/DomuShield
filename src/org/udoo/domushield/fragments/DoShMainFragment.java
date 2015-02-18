package org.udoo.domushield.fragments;

import org.udoo.domushield.DoShMainActivity;
import org.udoo.domushield.EkironjiDevice;
import org.udoo.domushield.HSVColorPickerDialog;
import org.udoo.domushield.HSVColorPickerDialog.OnColorSelectedListener;
import org.udoo.domushield.R;

import android.app.Fragment;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.VideoView;

/**
 * A placeholder fragment containing a simple view.
 */
public class DoShMainFragment extends Fragment {
	
	private DoShMainActivity mActivity = null;
	private EkironjiDevice mDevice = null;
	
	private TextView mTemperatureValue = null;
	private TextView mHumidityValue    = null;
	private TextView mLightValue       = null;
	
	private ToggleButton mRelayOneButton  = null;
	private ToggleButton mRelayTwoButton  = null;
	private ToggleButton mAutoLightButton = null;
	
	private Button mRgbButton = null;
	
	private VideoView mVideoView = null;
	private Uri videoUri = null;
	
	public DoShMainFragment(DoShMainActivity mActivity, EkironjiDevice mDevice) {
		this.mActivity = mActivity;
		this.mDevice = mDevice;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View rootView = inflater.inflate(R.layout.fragment_do_sh_main, container, false);
		
		mTemperatureValue = (TextView) rootView.findViewById(R.id.temperatureValue);
		mHumidityValue    = (TextView) rootView.findViewById(R.id.humidityValue);
		mLightValue       = (TextView) rootView.findViewById(R.id.lightValue);
		
		mRelayOneButton   = (ToggleButton) rootView.findViewById(R.id.relayOneSwitch);
		mRelayTwoButton   = (ToggleButton) rootView.findViewById(R.id.relayTwoSwitch);
		mAutoLightButton  = (ToggleButton) rootView.findViewById(R.id.autoLightButton);

		mRgbButton = (Button) rootView.findViewById(R.id.rgbButton);
		mVideoView = (VideoView) rootView.findViewById(R.id.videoView);		
			
		videoUri = Uri.parse("android.resource://" + mActivity.getPackageName() + "/"+ R.raw.ruggero);
		
		mRelayOneButton.setChecked(true);
		mRelayTwoButton.setChecked(true);
		mAutoLightButton.setChecked(true);
		
		// BOTTONE REELAY UNO
		mRelayOneButton.setOnClickListener(new OnClickListener() {
	        @Override
	        public void onClick(View arg0) {

	            if(mRelayOneButton.isChecked()){
	            	mDevice.turnOffRelay(0);
	            }
	            else{
	            	mDevice.turnOnRelay(0);
	            }
	        }                   
	    });
		
		// BOTTONE RELAY DUE
		mRelayTwoButton.setOnClickListener(new OnClickListener() {
	        @Override
	        public void onClick(View arg0) {
	            if(mRelayTwoButton.isChecked()){
	            	mDevice.turnOffRelay(1);
	            }
	            else{
	            	mDevice.turnOnRelay(1);
	            }
	        }                   
	    });
	
		
		// BOTTONE LED
		mRgbButton.setOnClickListener(new OnClickListener() {
	        @Override
	        public void onClick(View arg0) {
	        	try {
					showColorPicker(0);
				} catch (Exception e) {
					e.printStackTrace();
				}
	        }                   
	    });
		
		mAutoLightButton.setOnClickListener(new OnClickListener() {
	        @Override
	        public void onClick(View arg0) {
	            if(mAutoLightButton.isChecked()){

		        	Log.i("","onClick era checcked");
	            	mActivity.setAutoLight(false);
	            	setRelay2Clickable(false);
	            }
	            else{

		        	Log.i("","onClick NON era checcked");
	            	mActivity.setAutoLight(true);
	            	setRelay2Clickable(true);
	            }
	        }                   
	    });
		
		return rootView;
	}
	
	
	public void setRelay2Clickable(boolean state){
		mRelayTwoButton.setEnabled(state);			
	}
	
	
	
	
	@Override
	public void onResume() {
		super.onResume();
		
		mVideoView.setVideoURI(videoUri);
		mVideoView.setOnPreparedListener(new OnPreparedListener() {
	        @Override
	        public void onPrepared(MediaPlayer mp) {
	            mp.setLooping(true);
	        }
	    });
		mVideoView.start();
	}
		
	
	public void setTemperatureValue(String s){
		mTemperatureValue.setText(s);
		Log.v("DoSHFragment","setTemperatureValue");
	}
	
	public void setHumidityValue(String s){
		mHumidityValue.setText(s);
		Log.v("DoSHFragment","setHumidityValue");
	}
	
	public void setLuminosityValue(String s){
		mLightValue.setText(s);
		Log.v("DoSHFragment","setLuminosityValue");
	}
	
	
	private int lastColorSelected = Color.GREEN;
	private HSVColorPickerDialog cpd;
	private int pos;
	
	private void showColorPicker(int position){
		pos = position;
		Log.i("showColorPicker", "showColorPicker(int position)");
		cpd = new HSVColorPickerDialog(mActivity, lastColorSelected, 
				new OnColorSelectedListener() {
			
		    @Override
		    public void colorSelected(Integer color) {
		    	lastColorSelected = color;
				Log.i("showColorPicker", "rgb " + lastColorSelected);
				// send color command
				mDevice.sendSimpleColor(0, lastColorSelected);
		    }
		});
		cpd.setTitle( "Pick a color" );
		cpd.show();
	}


}