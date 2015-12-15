package test;

import sagex.remote.json.JSONException;

import java.io.IOException;

public class TestMisc2 {
    public static void main(String args[]) throws IOException, JSONException {
        long bufferedSize = 1024 * 1024 * 7;
        long curSizeBytes = 5704144;
        int size = Math.min((int) (((double) curSizeBytes / (double) bufferedSize) * 100), 100);
        System.out.println("%: " + size + " [" + bufferedSize + "," + curSizeBytes + "]");
    }
}
