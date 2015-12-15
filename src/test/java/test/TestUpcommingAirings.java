package test;

import sagex.SageAPI;
import sagex.api.AiringAPI;
import sagex.api.ChannelAPI;
import sagex.api.Database;
import sagex.api.ShowAPI;
import sagex.remote.rmi.RMISageAPI;

public class TestUpcommingAirings {
    public static void main(String args[]) {
        SageAPI.setProvider(new RMISageAPI("mediaserver"));
        System.out.println("Searching...");
        Object airings[] = Database.GetAiringsOnViewableChannelsAtTime(System.currentTimeMillis(), System.currentTimeMillis()
                + (10 * 60 * 60 * 1000), true);
        System.out.println("Checking...");
        for (Object a : airings) {
            if (AiringAPI.IsAiringHDTV(a)) {
                if ("Movie".equals(ShowAPI.GetShowCategory(a))) {
                    System.out.println("Channel: " + ChannelAPI.GetChannelName(a) + " ; Title: " + AiringAPI.GetAiringTitle(a));
                }
            }
        }
        System.out.println("Done");
    }
}
