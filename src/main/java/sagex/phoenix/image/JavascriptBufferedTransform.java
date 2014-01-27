package sagex.phoenix.image;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.apache.log4j.Logger;

import sagex.remote.json.JSONObject;

public class JavascriptBufferedTransform implements IBufferedTransform {
    private static final Logger log = Logger.getLogger(JavascriptBufferedTransform.class);
    
    public static interface JSBufferedTransform {
        public BufferedImage transform(BufferedImage image, JSONObject args);
    }
    
    private File jsFile = null;
    private long lastModified = 0;
    
    private ScriptEngineManager manager = new ScriptEngineManager();
    private ScriptEngine engine = manager.getEngineByName("JavaScript");
    private JSONObject args = null;
    
    public JavascriptBufferedTransform(File jsFile, JSONObject args) {
        this.jsFile = jsFile;
        this.args=args;
    }
    
    public String getId() {
        return jsFile.getName().substring(0, jsFile.getName().indexOf("."));
    }
    
    public BufferedImage transform(BufferedImage image) {
        if (jsFile.lastModified()>lastModified) {
            lastModified = jsFile.lastModified();
            Reader r = null;
            try {
                log.debug("Loading Javascript Transform: " + jsFile.getAbsolutePath());
                r = new FileReader(jsFile);
                Bindings b = engine.getBindings(ScriptContext.ENGINE_SCOPE);
                b.put("util", this);
                engine.eval(r);
            } catch (Exception e) {
                log.error("Failed to load the javascript transform! " + jsFile.getAbsolutePath(), e);
            } finally {
                if (r!=null) {
                    try {
                        r.close();
                    } catch (IOException e) {
                    }
                }
            }
        }
        
        Invocable inv = (Invocable) engine;
        JSBufferedTransform bt = inv.getInterface(JSBufferedTransform.class);
        try {
            return bt.transform(image, args);
        } catch (Exception e) {
            log.error("Failed while invoking javascript transform!", e);
            throw new RuntimeException(e);
        }
    }
    
    public BufferedImage newImage(int width, int height) {
        return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    }

    public BufferedImage loadImage(String img) throws IOException {
        return ImageUtil.readImage(new File(img));
    }
}
