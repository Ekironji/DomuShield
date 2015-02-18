/**
*  Copyright (C) 2014 Ekironji <ekironjisolutions@gmail.com>
*
*  This file is part of UdooLights
*
*  UdooLights is free software: you can redistribute it and/or modify
*  it under the terms of the GNU General Public License as published by
*  the Free Software Foundation, either version 3 of the License, or
*  (at your option) any later version.
*
*  UdooLights is distributed in the hope that it will be useful,
*  but WITHOUT ANY WARRANTY; without even the implied warranty of
*  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*  GNU General Public License for more details.
*
*  You should have received a copy of the GNU General Public License
*  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*
*/
package org.udoo.domushield;

import android.app.Activity;
import android.graphics.Color;

public class EkironjiDevice {

	/**
	 * These values are use to encode and decode an easy custom protocol
	 * 
	 * |__-__+__ __|__ __ __ __|__ __ __ __|__ __ __ __| (4 bytes)
	 *  31       23          15           8           0      
	 */	
	// masks
	public final static int OP_CODE_MASK       = 0xf0000000;
	public final static int ID_CODE_MASK       = 0x0f000000;	
	public final static int MAIN_OP_CODE_MASK  = 0xc0000000;
	public final static int SUB_OP_CODE_MASK   = 0x30000000;
	
	public final static int CMD_MASK    = 0xff000000;
	public final static int RED_MASK    = 0x00ff0000;
	public final static int GREEN_MASK  = 0x0000ff00;
	public final static int BLUE_MASK   = 0x000000ff;
	
	public final static int PAYLOAD_MASK   = 0x00ffffff;
	
	// offsets
	public final static int MAIN_OP_OFFSET= 30;
	public final static int SUB_OP_OFFSET = 28;
	public final static int OP_OFFSET     = 28;
	public final static int ID_OFFSET     = 24;
	public final static int CMD_OFFSET    = 24;
	public final static int RED_OFFSET    = 16;
	public final static int GREEN_OFFSET  = 8;
	public final static int BLUE_OFFSET   = 0;	
	
	// MAIN op_codes
	public final static int REQUEST_MSG   = 0x0;
	public final static int RELAY_MSG     = 0x1;
	public final static int STRIP_MSG     = 0x2;
	public final static int VIDEO_MSG     = 0x3;
	
	// SUB op_codes
	public final static int REQUEST_IP_DISCOVERY_MSG   = 0x0;
	public final static int REQUEST_SERVICE_LIST_MSG   = 0x1;
	public final static int REQUEST_GENERIC_MSG        = 0x2;	
	
	public final static int STRIP_DIRECT_MSG   = 0x0;
	public final static int STRIP_FADE_MSG     = 0x1;
	public final static int STRIP_BLINK_MSG    = 0x2;
	public final static int STRIP_RAINBOW_MSG   = 0x3; 
	
	public final static int RELAY_OFF_MSG    = 0x0;
	public final static int RELAY_ON_MSG     = 0x1;
	public final static int RELAY_CHANGE_MSG = 0x2;
	
	public final static int VIDEO_GET_LIST_MSG   = 0x0;
	public final static int VIDEO_PLAY_MSG       = 0x1;
	public final static int VIDEO_PLAY_LOOP_MSG  = 0x2;
	public final static int VIDEO_PLAY_EXTRA_MSG   = 0x3; // si usano i bit dell id per ulteriori comandi
		
	private DoShMainActivity mActivity = null;
	
	private String ipAddress = null;
//	private String id;
//	private String name;
//	private String SSID;
	
	
	public EkironjiDevice(String ipAddress) {
		this.ipAddress = ipAddress;
	}
	
	public void setIpAddress(String ip){
		this.ipAddress = ip;
	}
	
	public String getAddress(){
		return this.ipAddress;
	}
	
	// It checks if a Udoo was found on the wifi
	public boolean isUdooPresent(){
		if(this.ipAddress == null)
			return false;
		else
			return true;
	}
	
	public void setActivity(DoShMainActivity mActivity){
		this.mActivity = mActivity;
	}
	
	
	// These methods can be use to send speciic methods
	public void sendSimpleColor(int strip, int color){		
		int msg = pack(STRIP_MSG, STRIP_DIRECT_MSG, strip, Color.red(color), Color.green(color), Color.blue(color) );	
		sendMessage(msg);
	}
	
	public void sendFadeColor(int strip, int color){		
		int msg = pack(STRIP_MSG, STRIP_FADE_MSG, strip, Color.red(color), Color.green(color), Color.blue(color) );		
		sendMessage(msg);
	}
	
	public void sendBlinkColor(int strip, int color){		
		int msg = pack(STRIP_MSG, STRIP_BLINK_MSG, strip, Color.red(color), Color.green(color), Color.blue(color) );		
		sendMessage(msg);
	}
	
	public void sendRainbowColor(int strip){		
		int msg = pack(STRIP_MSG, STRIP_RAINBOW_MSG, strip, null, null, null );		
		sendMessage(msg);
	}
	
	// relay methods
	public void turnOffRelay(int strip){		
		int msg = pack(RELAY_MSG, RELAY_OFF_MSG, strip, null, null, 0 );		
		sendMessage(msg);
	}
	
	public void turnOnRelay(int strip){		
		int msg = pack(RELAY_MSG, RELAY_ON_MSG, strip, null, null, 1 );		
		sendMessage(msg);
	}
	
	public void switchRelay(int strip){		
		int msg = pack(RELAY_MSG, RELAY_CHANGE_MSG, strip, null, null, 2 );		
		sendMessage(msg);
	}
	
	// video
	public void playVideo(int videoId){
		int msg = pack(VIDEO_MSG, VIDEO_PLAY_MSG, null, null, null, null );
		msg |= (videoId & PAYLOAD_MASK);
		sendMessage(msg);
	}
	
	public void playLoopingVideo(int videoId){
		int msg = pack(VIDEO_MSG, VIDEO_PLAY_LOOP_MSG, null, null, null, null );
		msg |= (videoId & PAYLOAD_MASK);
		sendMessage(msg);
	}
	
	
	// If app discovered an UDOO server over Wifi it sends a message 
	private void sendMessage(int msg){
		mActivity.sendMessage(msg);
	}

	
	// This method pack the bits into an integer before send it over udp
	private int pack(Integer mainOpCode, Integer subOpCode, Integer idCode, 
			Integer r, Integer g, Integer b){		
		int msg = 0;
		
		msg |= (mainOpCode << MAIN_OP_OFFSET);
		msg |= (subOpCode  << SUB_OP_OFFSET);
		
		if(idCode != null)
			msg |= (idCode << ID_OFFSET);
		
		if(r != null)
			msg |= (r << RED_OFFSET);
		
		if(g != null)
			msg |= (g << GREEN_OFFSET);
		
		if(b != null)
			msg |= (b << BLUE_OFFSET);
		
		return msg;
	}
	
	// It returns a string with bit representation of an integer
	public String getBitString(int number){
		String s = "";
		
		for(int i=31; i>=0; i--){
			if((number >> i & 0x1) == 1)
				s += "1";
			else
				s += "0";
			
			if(i % 8 == 0)
				s += " ";
		}
		return s;
	}
	
	
}
