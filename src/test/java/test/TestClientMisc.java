package test;

import phoenix.impl.ClientAPI;
import phoenix.impl.UtilAPI;
import sagex.UIContext;
import sagex.api.Global;
import sagex.phoenix.metadata.MetadataException;

import java.io.IOException;

public class TestClientMisc {
    public static void main(String args[]) throws MetadataException, IOException {
        InitPhoenix.init(true, true);
        UtilAPI api = new UtilAPI();
        ClientAPI client = new ClientAPI();

        System.out.println("Context: " + Global.GetUIContextName());
        System.out.println("ContextName: " + client.GetName(Global.GetUIContextName()));
        System.out.println("IsServer: " + api.IsServerLocal(null));
        System.out.println("IsClient: " + api.IsPlaceshifter(null));
        System.out.println("IsExtender: " + api.IsExtender(null));

        for (String s : Global.GetUIContextNames()) {
            System.out.println("------------------------------------");
            System.out.println("Context         : " + s);
            System.out.println("ContextName: " + client.GetName(s));
            UIContext ctx = new UIContext(s);
            System.out.println("IsServerLocal: " + api.IsServerLocal(ctx));
            System.out.println("IsPlaceshifter: " + api.IsPlaceshifter(ctx));
            System.out.println("IsExtender: " + api.IsExtender(ctx));
            // System.out.println("IsClient        : " + Global.IsClient(ctx));
            // System.out.println("IsDesktopUI     : " +
            // Global.IsDesktopUI(ctx));
            // System.out.println("IsEmbeddedSystem: " +
            // Global.IsEmbeddedSystem(ctx));
            // System.out.println("IsRemoteUI      : " +
            // Global.IsRemoteUI(ctx));
            // System.out.println("IsServerUI      : " +
            // Global.IsServerUI(ctx));
        }

        System.out.println("======================");
        for (String s : Global.GetConnectedClients()) {
            System.out.println("------------------------------------");
            System.out.println("Context         : " + s);
            System.out.println("ContextName: " + client.GetName(s));
            UIContext ctx = new UIContext(s);
            System.out.println("IsServerLocal: " + api.IsServerLocal(ctx));
            System.out.println("IsPlaceshifter: " + api.IsPlaceshifter(ctx));
            System.out.println("IsExtender: " + api.IsExtender(ctx));
        }

        for (String s : client.GetConnectedClients()) {
            System.out.println("++++++++++++++++++++++++");
            System.out.println("Client: " + s);
            System.out.println("Client Name: " + client.GetName(s));
            System.out.println("Connected: " + client.IsConnected(s));

        }
    }

}
