package sagex.phoenix.image;

import org.apache.log4j.Logger;
import sagex.phoenix.cache.ICache;
import sagex.phoenix.cache.MapCache;
import sagex.remote.json.JSONArray;
import sagex.remote.json.JSONException;
import sagex.remote.json.JSONObject;

import java.awt.image.BufferedImage;
import java.io.File;

public class TransformFactory {
    private static final Logger log = Logger.getLogger(TransformFactory.class);

    private ICache<IBufferedTransform> transforms = new MapCache<IBufferedTransform>();
    private File jsDir = null;

    public TransformFactory(File jsTransformDir) {
        this.jsDir = jsTransformDir;
    }

    public IBufferedTransform createTransform(String jsonCmd) throws Exception {
        try {
            if (jsonCmd == null)
                throw new Exception("createTransform(): was passed null transformat command.");
            if (jsonCmd.startsWith("[")) {
                IBufferedTransform bt = getTransform(jsonCmd);
                if (bt != null) {
                    return bt;
                }

                JSONArray jarr = new JSONArray(jsonCmd);
                CompositeTransform ct = new CompositeTransform();
                for (int i = 0; i < jarr.length(); i++) {
                    ct.addTransform(createTransform(jarr.getJSONObject(i)));
                }
                registerTransform(jsonCmd, ct);
                return ct;
            } else {
                if (!jsonCmd.startsWith("{")) {
                    jsonCmd = "{" + jsonCmd + "}";
                }
                IBufferedTransform bt = getTransform(jsonCmd);
                if (bt != null)
                    return bt;
                JSONObject jo = new JSONObject(jsonCmd);
                bt = createTransform(jo);
                registerTransform(jsonCmd, bt);
                return bt;
            }
        } catch (JSONException e) {
            throw new Exception(e);
        }
    }

    public void registerTransform(String transformId, String jsonTransform) throws Exception {
        transforms.put(transformId, createTransform(jsonTransform));
    }

    public void registerTransform(String transformId, IBufferedTransform transform) {
        transforms.put(transformId, transform);
    }

    public IBufferedTransform getTransform(String id) {
        return transforms.get(id);
    }

    public BufferedImage applyTransform(BufferedImage bi, String transform) throws Exception {
        IBufferedTransform bt = createTransform(transform);
        return bt.transform(bi);
    }

    private IBufferedTransform createTransform(JSONObject jo) throws Exception {
        IBufferedTransform bt = null;
        String id = null;
        if (jo.has("id")) {
            id = jo.getString("id");
            bt = getTransform(id);
            if (bt != null)
                return bt;
        }

        String name = jo.getString("name");
        if ("scale".equals(name)) {
            bt = new ScaledImageTransform(getInt(jo, "width", -1), getInt(jo, "height", -1));
        } else if ("dummy".equals(name)) {
            bt = new DummyImageTransform();
        } else if ("rotate".equals(name)) {
            bt = new RotateImageTransform(jo.getDouble("theta"));
        } else if ("reflection".equals(name)) {
            if (jo.has("alphaStart")) {
                bt = new AlphaReflectionImageTransform((float) jo.getDouble("alphaStart"), (float) jo.getDouble("alphaEnd"));
            } else {
                bt = new ReflectionImageTransform();
            }
        } else if ("just_reflection".equals(name)) {
            bt = new JustReflectionImageTransform((float) jo.getDouble("alphaStart"), (float) jo.getDouble("alphaEnd"));
        } else if ("perspective".equals(name)) {
            bt = new PerspectiveImageTransform(jo.getDouble("scalex"), jo.getDouble("shifty"));
        } else if ("gradient".equals(name)) {
            bt = new GradientImageTransform(jo.getInt("width"), jo.getInt("height"), (float) jo.getDouble("opacityStart"),
                    (float) jo.getDouble("opacityEnd"));
        } else if ("shadow".equals(name)) {
            bt = new ShadowTransform(getInt(jo, "size", 5), getFloat(jo, "opacity", 1.0f), getInt(jo, "color", 0x000000));
        } else if ("opacity".equals(name)) {
            bt = new OpacityTransform((float) jo.getDouble("opacity"));
        } else if ("overlay".equals(name)) {
            bt = new OverlayTransform(jo.getString("image"), getFloat(jo, "opacity", 1.0f), getInt(jo, "x", 0), getInt(jo, "y", 0));
        } else if ("rounded".equals(name)) {
            bt = new RoundedCornersTransform(jo.getInt("arcSize"));
        } else {
            File f = new File(jsDir, name + ".js");
            if (f.exists()) {
                bt = new JavascriptBufferedTransform(f, jo);
            } else {
                throw new Exception("Unknown Transform: " + name);
            }
        }

        if (id != null) {
            registerTransform(id, bt);
        }
        return bt;
    }

    private float getFloat(JSONObject jo, String key, float defValue) {
        try {
            return (float) jo.getDouble(key);
        } catch (Exception e) {
            return defValue;
        }
    }

    private int getInt(JSONObject jo, String key, int defValue) {
        try {
            return jo.getInt(key);
        } catch (Exception e) {
            return defValue;
        }
    }
}
