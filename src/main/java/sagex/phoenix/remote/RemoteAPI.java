package sagex.phoenix.remote;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import sagex.UIContext;
import sagex.api.Utility;
import sagex.phoenix.factory.BaseConfigurable;
import sagex.phoenix.image.ImageUtil;
import sagex.phoenix.util.Function;
import sagex.phoenix.util.Loggers;
import sagex.phoenix.vfs.IMediaResource;
import sagex.util.TypesUtil;

/**
 * Supported conversion functions in args include... uicontext, ref, int,
 * mediafile
 *
 * @author sls
 */
public class RemoteAPI {
    private static Map<String, Function<String, ?>> functions = new HashMap<String, Function<String, ?>>();

    static {
        functions.put("uicontext", new Function<String, UIContext>() {
            @Override
            public UIContext apply(String in) {
                if (StringUtils.isEmpty(in))
                    return UIContext.getCurrentContext();
                return new UIContext(in);
            }
        });

        functions.put("ref", new ReferenceFunction());
        functions.put("int", new IntFunction());
        functions.put("long", new IntFunction());
        functions.put("double", new DoubleFunction());
        functions.put("boolean", new BooleanFunction());
        functions.put("mediafile", new MediaFileFunction());
        functions.put("airing", new AiringFunction());
        functions.put("map", new MapFunction());

        // just return null
        functions.put("null", new Function<String, String>() {
            @Override
            public String apply(String in) {
                return null;
            }
        });
    }

    public RemoteAPI() {
    }

    public void callAPI(Command cmd) throws Exception {
        encode(cmd, invokeAPI(cmd));
    }

    public void encode(Command cmd, Object in) throws Exception {
        if (cmd.getEncoder() == Command.Encoder.IMAGE) {
            try {
                if (in instanceof File) {
                    writeFile((File) in, cmd);
                } else if (in instanceof String) {
                    writeFile(new File((String) in), cmd);
                } else if (Utility.IsMetaImage(in)) {
                    File f = Utility.GetMetaImageSourceFile(in);
                    if (f != null && f.exists()) {
                        writeFile(f, cmd);
                    } else {
                        writeSageImageFile(in, cmd);
                    }
                } else {
                    cmd.getIOContext().sendError(404, "Unknown Image");
                }
            } catch (Exception e) {
                cmd.getIOContext().sendError(404, e.getMessage());
            }
        } else {
            // default is json encoded

            Map<String, Object> reply = new HashMap<String, Object>();
            reply.put("reply", in);

            cmd.getIOContext().setEncoding("UTF-8");
            cmd.getIOContext().writeHeader("Content-Type", "application/json; charset=UTF-8");

            PrintWriter pw = cmd.getIOContext().getWriter();

            String jsonSer = cmd.getIOContext().getParameter("_jsonser");

            // TODO: jsonSer to JsonSerializer and invoke serializer on the
            // reply

            createGson(BooleanUtils.toBoolean(cmd.getIOContext().getParameter("_prettyprint"))).toJson(reply, pw);
            pw.flush();
        }
    }

    public static Gson createGson(boolean prettyPrint) {
        GsonBuilder b = new GsonBuilder().registerTypeHierarchyAdapter(IMediaResource.class, new MediaResourceSerializer())
                .registerTypeHierarchyAdapter(BaseConfigurable.class, new BaseConfigurableSerializer())
                .setExclusionStrategies(new ExclusionStrategy() {
                    @Override
                    public boolean shouldSkipField(FieldAttributes f) {
                        return (f.getName().equals("parent") || f.getName().equals("repository") || f.getName().equals("log"));
                    }

                    @Override
                    public boolean shouldSkipClass(Class<?> arg0) {
                        return false;
                    }
                });

        if (prettyPrint) {
            b.setPrettyPrinting();
        }

        return b.create();
    }

    private void writeFile(File in, Command cmd) throws IOException {
        if (in == null || !in.exists()) {
            throw new IOException("File Not Found: " + in);
        }

        String ext = FilenameUtils.getExtension(in.getName());
        if (ext == null)
            ext = "jpg";
        cmd.getIOContext().writeHeader("Content-Type", "image/" + ext);
        OutputStream os = cmd.getIOContext().getOutputStream();
        InputStream is = new FileInputStream(in);
        IOUtils.copyLarge(is, os);
        is.close();
        os.flush();
    }

    public static void writeSageImageFile(Object sageImage, Command cmd) throws IOException {
        if (sageImage == null) {
            throw new FileNotFoundException("No Image");
        }
        // SEAN: Should block until the image is loaded
        Utility.LoadImage(sageImage);

        BufferedImage img = Utility.GetImageAsBufferedImage(sageImage);
        if (img == null)
            throw new FileNotFoundException("Unable to get BufferedImage: " + sageImage);
        cmd.getIOContext().writeHeader("Content-Type", ImageUtil.DEFAULT_IMAGE_MIME_TYPE);
        OutputStream os = cmd.getIOContext().getOutputStream();
        ImageUtil.writeImage(img, ImageUtil.DEFAULT_IMAGE_FORMAT, os);
        os.flush();
    }

    public Map explain(Command cmd) throws Exception {
        Class cl = Class.forName(cmd.getClassName());

        Method method = getMethod(cl, cmd);
        Object args[] = new Object[method.getParameterTypes().length];

        for (int i = 0; i < args.length; i++) {
            args[i] = resolveArg(method.getParameterTypes()[i], cmd.getArgs().get(i));
        }

        Map exp = new HashMap();
        exp.put("class", cl);
        exp.put("method", method);
        exp.put("args", args);
        return exp;
    }

    public Object invokeAPI(Command cmd) throws Exception {
        Class cl = Class.forName(cmd.getClassName());

        Method method = getMethod(cl, cmd);
        Object args[] = new Object[method.getParameterTypes().length];

        for (int i = 0; i < args.length; i++) {
            args[i] = resolveArg(method.getParameterTypes()[i], cmd.getArgs().get(i));
        }

        // set the UI Context if passed
        if (cmd.getContext() != null) {
            UIContext.setCurrentContext(cmd.getContext());
        }

        // now call the api
        Object val = method.invoke(null, args);
        if (val != null) {
            // the caller has asked use to hold a reference for this value
            if (cmd.getReferenceName() != null) {
                RemoteContext.get().addReference(cmd.getReferenceName(), val, cmd.getReferenceExpiry());
            }
        }
        return val;
    }

    private Object resolveArg(Class<?> argType, String argValue) {
        int pos = argValue.indexOf(":");
        if (pos != -1) {
            String func = argValue.substring(0, pos);
            if (pos + 1 < argValue.length()) {
                String other = argValue.substring(pos + 1);
                Function<String, ?> f = functions.get(func);
                if (f != null) {
                    return f.apply(other);
                } else {
                    Loggers.LOG.warn("No Function for [" + func + "]");
                }
            }
        }

        return TypesUtil.fromString(argValue, argType);
    }

    private Method getMethod(Class cl, Command cmd) throws Exception {
        Method reply = null;

        if (cmd.getSignature() != null) {
            return cl.getMethod(cmd.getMethodName(), cmd.getSignature());
        }

        for (Method m : cl.getMethods()) {
            if (m.getName().equals(cmd.getMethodName())) {
                if (m.getParameterTypes().length == cmd.getArgs().size()) {
                    if (reply != null)
                        throw new Exception("Ambiguous API Reference.  Don't know which one to call for: " + cmd);
                    reply = m;
                }
            }
        }
        if (reply == null) {
            throw new Exception("Unknown API Method " + cmd.getMethodName() + " for API " + cmd.getClassName());
        }
        return reply;
    }
}
