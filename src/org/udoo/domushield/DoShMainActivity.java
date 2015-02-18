package org.udoo.domushield;

import org.udoo.adktoolkit.AdkManager;
import org.udoo.domushield.fragments.DoShMainFragment;

import android.app.Activity;
import android.content.Context;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class DoShMainActivity extends Activity {

	public static EkironjiDevice mDevice = null;    
	public static AdkManager mAdkManager;	
	
	private DoShMainFragment mFragment = null;
	private AdkReadTempTask mAdkReadTempTask;
	
	private boolean isAutoLightOn = false;
	private int autoLightTh = 10;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_do_sh_main);
		
		mDevice = new EkironjiDevice(null);
		mDevice.setActivity(this);
		
		mFragment = new DoShMainFragment(this, mDevice);
		
		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, mFragment).commit();
		}
		      
        mAdkManager = new AdkManager((UsbManager) getSystemService(Context.USB_SERVICE));        
        registerReceiver(mAdkManager.getUsbReceiver(), mAdkManager.getDetachedFilter());

	}

	
	@Override
	public void onResume() {
		super.onResume(); 
		mAdkManager.open();
		mAdkReadTempTask = new AdkReadTempTask();
		mAdkReadTempTask.execute();
	}
 
	@Override
	public void onPause() {
		super.onPause();
		mAdkManager.close();
		mAdkReadTempTask.pause();
	}
	
	@Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mAdkManager.getUsbReceiver());
    }
	
	public void sendMessage(int msg){
		//showToastMessage(intToBits(msg));		
		mAdkManager.writeByteArray(intToByteArray(msg));
	}
	
	public byte[] intToByteArray(int integer){
		byte[] array = new byte[4];
		
		array[0] = (byte)((integer >> 24) & 0x000000ff);
		array[1] = (byte)((integer >> 16) & 0x000000ff);
		array[2] = (byte)((integer >> 8)  & 0x000000ff);
		array[3] = (byte)((integer >> 0)  & 0x000000ff);
		
		return array;
	}
	
	/* 
	 * We put the readSerial() method in an AsyncTask to run the 
	 * continuous read task out of the UI main thread
	 */
	private class AdkReadTempTask extends AsyncTask<Void, String, Void> {

		private boolean running = true;
			
		public void pause(){
			running = false;
		}
		 
	    protected Void doInBackground(Void... params) {
	    	while(running) {
	    		publishProgress(mAdkManager.readSerial());
	     	}
	    	return null;
	    }

	    protected void onProgressUpdate(String... progress) {
	    	decodeInput(progress[0]);
	    }  
	}
	
	
	private class ToastTask extends AsyncTask<Void, String, Void> {

		private static final long TOAST_TIME = 30;
		private boolean running = true;
			
		public void pause(){
			running = false;
		}
		 
		
		@Override
		protected void onPreExecute() {
			mFragment.setRelay2Clickable(false);			
			mDevice.turnOnRelay(1);
		}
		
	    protected Void doInBackground(Void... params) {
	    	
	    	for(int i=0; i<TOAST_TIME; i++){
		    	try {
					Thread.sleep(1000);
					publishProgress(""+i);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}	
	    	}
	    	return null;
	    }

	    protected void onProgressUpdate(String... progress) {
	    	//decodeInput(progress[0]);
	    }  
	    
	    @Override
	    protected void onPostExecute(Void result) {
	    	mFragment.setRelay2Clickable(true);			
			mDevice.turnOffRelay(1);
	    }
	}
	
	
	
	
	public void decodeInput(String s){
		if(s.length() == 3){
			mFragment.setTemperatureValue((byte)s.charAt(0) + " C " + this.isAutoLightOn);
			mFragment.setHumidityValue((byte)s.charAt(1) + " %");
			mFragment.setLuminosityValue((byte)s.charAt(2) + " lux");
			
			if(this.isAutoLightOn){
				if((byte)s.charAt(2) < autoLightTh){
		            mDevice.turnOffRelay(1);
	            }
	            else{
	            	mDevice.turnOnRelay(1);
	            }
			}		
		}
		else{
			mFragment.setTemperatureValue("length: " + s.length());
			mFragment.setHumidityValue("na");
			mFragment.setLuminosityValue("na");
		}
	}
	
	public void setAutoLight(boolean state){
		this.isAutoLightOn = state;
		//mFragment.setRelay2Clickable(state);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.do_sh_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

    public void showToastMessage(String message){
    	Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
    
    public String intToBits(int integer){
    	String s = "";
    	
    	for(int i=31; i>=0; i--){
    		if(i % 8 == 7) s+= " ";
    		
    		if(((integer >>> i) & 0x1) == 0)
    			s+="0";
    		else
    			s+=1;
    	}
    	
    	return s;
    }
}
