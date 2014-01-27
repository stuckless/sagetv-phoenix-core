package test;

import sagex.api.CaptureDeviceAPI;
import sagex.api.CaptureDeviceInputAPI;
import sagex.api.ChannelAPI;



public class TestChannelLineup {
    public static void main(String args[]) throws Throwable {
        InitPhoenix.init(true,true);

        String lineup = null;
        String tuners[] = CaptureDeviceAPI.GetActiveCaptureDevices();
        for (String t: tuners) {
        	System.out.println("Tuner: " + t);
        	String inputs[] = CaptureDeviceAPI.GetCaptureDeviceInputs(t);
        	for (String i: inputs) {
        		System.out.println("Input: " + i);
        		if (CaptureDeviceInputAPI.GetLineupForCaptureDeviceInput(i)!=null) {
        			lineup = CaptureDeviceInputAPI.GetLineupForCaptureDeviceInput(i);
        		}
        	}
        }
        
    	if (lineup!=null) {
            Object channels[] = ChannelAPI.GetAllChannels();
            for (Object ch: channels) {
            	System.out.printf("%s: %s: %s\n", ChannelAPI.GetChannelNumber(ch) , ChannelAPI.GetChannelName(ch), ChannelAPI.IsChannelViewableOnLineup(ch, lineup));
            }
    	}
    	
    	//ChannelAPI.I
    }
}
