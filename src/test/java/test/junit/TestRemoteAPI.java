package test.junit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.*;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import sagex.phoenix.remote.Command;
import sagex.phoenix.remote.IOContext;
import sagex.phoenix.remote.MapFunction;
import sagex.phoenix.remote.ReferenceFunction;
import sagex.phoenix.remote.RemoteAPI;
import sagex.phoenix.remote.RemoteContext;
import sagex.phoenix.remote.StubIOContext;
import sagex.phoenix.vfs.VirtualMediaFile;
import sagex.phoenix.vfs.VirtualMediaFolder;
import sagex.remote.json.JSONObject;
import test.InitPhoenix;

public class TestRemoteAPI {
    @BeforeClass
    public static void init() throws IOException {
        InitPhoenix.init(true, true);
    }

    @Test
    public void testAPINoArgs() throws Exception {
        String ver = phoenix.system.GetVersion();
        Command cmd = new Command(new StubIOContext(), "phoenix.system.GetVersion()");
        RemoteAPI api = new RemoteAPI();
        Object reply = api.invokeAPI(cmd);
        assertNotNull(reply);
        assertEquals(ver, reply);
    }

    @Test
    public void testAPIWithArgs() throws Exception {
        Command cmd = new Command(new StubIOContext(), "phoenix.util.IsAtLeastVersion( 1.0.8, 1.0.7 )");
        RemoteAPI api = new RemoteAPI();
        Object reply = api.invokeAPI(cmd);
        System.out.println("reply: " + reply);
        assertNotNull(reply);
        assertTrue("Test Failed", (Boolean) reply);
    }

    @Test
    public void testReferences() throws Exception {
        StubIOContext io = new StubIOContext();
        RemoteContext.get().addReference("maxversion", "1.0.5", 0);
        Command cmd = new Command(io, "phoenix.util.IsAtLeastVersion(1.0.8, ref:maxversion)");
        cmd.setReferenceName("result");
        cmd.setReferenceExpiry(2000);
        RemoteAPI api = new RemoteAPI();
        Object reply = api.invokeAPI(cmd);
        System.out.println("reply: " + reply);
        assertNotNull(reply);
        assertTrue("Reference Lookup Test Failed", (Boolean) reply);

        assertTrue("Reference did not get created", RemoteContext.get().getReference("result") != null);
        Thread.currentThread().sleep(2400);
        assertTrue("Reference did not get removed", RemoteContext.get().getReference("result") == null);

        List l = new ArrayList();
        Collections.addAll(l, "Zero", "One", "Two", "Three");
        RemoteContext.get().addReference("collection", l, 0);
        ReferenceFunction f = new ReferenceFunction();
        Object o = f.apply("collection");
        assertTrue("Not a list", o == l);

        assertEquals("Zero", f.apply("collection[0]"));
        assertEquals("One", f.apply("collection[1]"));
        assertEquals("Two", f.apply("collection[2]"));
        assertEquals("Three", f.apply("collection[3]"));
        try {
            assertEquals("Three", f.apply("collection[4]"));
            fail("Index out of bounds should have been thrown");
        } catch (Throwable t) {
            // ok
        }
    }

    @Test
    public void testAPIWithArgsAndConversion() throws Exception {
        Command cmd = new Command(new StubIOContext(), "phoenix.util.GetRandomNumber(5)");
        RemoteAPI api = new RemoteAPI();
        Object reply = api.invokeAPI(cmd);
        System.out.println("reply: " + reply);
        assertNotNull(reply);
        assertTrue("Version API Failed", ((Integer) reply) < 5);
    }

    @Test
    public void testMapFunction() throws Exception {
        MapFunction f = new MapFunction();
        Map map = f.apply("{test:1, name:'sean'}");
        assertTrue("Map is null", map != null);

        assertEquals("1", map.get("test"));
        assertEquals("sean", map.get("name"));

        map = f.apply("{fields:['title','episode']}");
        assertTrue("Map is null", map != null);
        String[] fields = (String[]) map.get("fields");
        assertNotNull(fields);
        assertEquals(2, fields.length);
        assertEquals("title", fields[0]);
        assertEquals("episode", fields[1]);

    }

    @Test
    public void testAPIWithMapFunction() throws Exception {
        Command cmd = new Command(new StubIOContext(), "phoenix.umb.Search");
        cmd.getArgs().add("Bones");
        cmd.getArgs().add("Airing");
        cmd.getArgs().add("map:{fields:['title','episode']}");
        RemoteAPI api = new RemoteAPI();
        Map explain = api.explain(cmd);
        assertNotNull(explain);
        assertEquals(phoenix.umb.class, explain.get("class"));
        assertEquals("Search", ((Method) explain.get("method")).getName());
        Object args[] = (Object[]) explain.get("args");
        assertEquals(3, args.length);

        // test the args
        assertEquals("Bones", args[0]);
        assertEquals("Airing", args[1]);

        // test the map
        Map options = (Map) args[2];
        String[] fields = (String[]) options.get("fields");
        assertEquals("title", fields[0]);
        assertEquals("episode", fields[1]);
    }

    @Test
    public void testAPIWithArgsAndConversionForJSON() throws Exception {
        StubIOContext io = new StubIOContext();
        Command cmd = new Command(io, "phoenix.util.GetRandomNumber(5)");
        RemoteAPI api = new RemoteAPI();

        api.callAPI(cmd);
        String reply = io.getBuffer();
        System.out.println("reply: " + reply);
        assertNotNull(reply);

        JsonParser jsonp = new JsonParser();
        JsonObject el = (JsonObject) jsonp.parse(reply);
        System.out.println("json decoded reply: " + el.get("reply").getAsInt());
        assertTrue("reply is not a number", el.get("reply").getAsInt() < 5);
    }

    @Test
    public void testVIEWS() throws Exception {
        VirtualMediaFolder f = new VirtualMediaFolder(null, "virtfolder", "folder", "Root Folder");
        VirtualMediaFile file = new VirtualMediaFile("File 1");
        f.addMediaResource(file);

        VirtualMediaFolder f2 = new VirtualMediaFolder("Sub Folder");
        VirtualMediaFile file2 = new VirtualMediaFile("File 2");
        f2.addMediaResource(file2);

        f.addMediaResource(f2);

        // should serialize everything
        StubIOContext io = new StubIOContext();
        Command cmd = new Command(io, "phoenix.test");
        RemoteAPI api = new RemoteAPI();
        StringWriter sw = new StringWriter();
        api.encode(cmd, f);
        System.out.println(io.getBuffer());

        // should serialize only children and not the sub folders
        io = new StubIOContext();
        cmd = new Command(io, "phoenix.test");
        RemoteContext.get().setSerializeDepth(1);
        sw = new StringWriter();
        api.encode(cmd, f);
        System.out.println(io.getBuffer());
    }

    @Test
    public void testVIEWSWithRange() throws Exception {
        VirtualMediaFolder f = new VirtualMediaFolder(null, "virtfolder", "folder", "Root Folder");

        for (int i = 0; i < 10; i++) {
            VirtualMediaFile file = new VirtualMediaFile("File " + (i + 1));
            f.addMediaResource(file);
        }

        assertEquals(10, f.getChildren().size());
        RemoteContext.get().reset();
        RemoteContext.get().setData("start", 1);
        RemoteContext.get().setData("end", 5);
        if (((Integer) RemoteContext.get().getData("end")) > 0) {
            RemoteContext.get().setData("useranges", true);
        }

        JSONObject jo = new JSONObject(RemoteAPI.createGson(true).toJson(f));
        System.out.println(jo.toString(3));
        assertEquals(jo.optJSONArray("children").length(), 4);
        assertEquals(jo.optJSONArray("children").optJSONObject(0).optString("title"), "File 2");
        assertEquals(jo.optJSONArray("children").optJSONObject(3).optString("title"), "File 5");
    }

    @Test
    public void testFunctions() {
        MapFunction mf = new MapFunction();
        Map reply = mf.apply("{name: 'sean', age: 15, 'arr': [1,2,3]}");
        assertEquals(3, reply.size());
        assertEquals("sean", reply.get("name"));
        assertEquals("15", reply.get("age"));
        String[] arr = (String[]) reply.get("arr");
        assertNotNull(arr);
        assertEquals("3", arr[2]);
    }

    @Test
    public void testUTF8Characters() throws Exception {
        String title = "“ ” ‘ ’ – — … ‐ ‒ ° © ® ™ • ½ ¼ ¾ ⅓ ⅔ † ‡ µ ¢ £ € « » ♠ ♣ ♥ ♦ ¿ �";
        VirtualMediaFile vmf = new VirtualMediaFile(title);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(output);
        Command cmd = mock(Command.class);
        IOContext ctx = mock(IOContext.class);
        when(cmd.getIOContext()).thenReturn(ctx);
        when(ctx.getOutputStream()).thenReturn(output);
        when(ctx.getWriter()).thenReturn(pw);

        RemoteAPI api = new RemoteAPI();
        api.encode(cmd, vmf);

        InputStream input = new ByteArrayInputStream(output.toByteArray());

        // NOTE: Applications consuming the stream MUST utf-8 decode it as well
        JSONObject jo = new JSONObject(IOUtils.toString(input, "UTF-8"));
        assertEquals(title, jo.getJSONObject("reply").getString("title"));
    }
}
