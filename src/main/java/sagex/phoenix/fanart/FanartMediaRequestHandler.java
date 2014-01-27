package sagex.phoenix.fanart;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.BooleanUtils;

import phoenix.impl.ImageAPI;
import sagex.api.MediaFileAPI;
import sagex.api.Utility;
import sagex.phoenix.image.ImageUtil;
import sagex.remote.media.MediaHandler;
import sagex.remote.media.SageMediaRequestHandler;
import sagex.util.MetaImageUtil;

/**
 * Plugs into the sagex.api MediaHandler providing fanart support to the remote apis
 * 
 * @see MediaHandler
 * @see SageMediaRequestHandler
 * 
 * This Handler can accept a "transform" url parameter and accept and apply a transformation can be consumed by {@link ImageAPI}.
 * The result is the transformation is applied to the image and the resulting image is sent back.  Transformations can be used
 * for image scaling, ie, /sagex/media/12323223?transform={name:scale,height:100}
 * <br/>
 * To force an overwrite of an image, you can use user &overwrite=true.  The default is that once an image transform has been performed
 * then it will be cached.  So, overwrite=true, is used to force a rebuild of the transformation.
 * 
 * 
 * @author seans
 *
 */
public class FanartMediaRequestHandler implements SageMediaRequestHandler {
    private String fanart = null;
    
    public FanartMediaRequestHandler(String fanart) {
        this.fanart = fanart;
    }

    public void processRequest(HttpServletRequest req, HttpServletResponse resp, Object sageMediaFile) throws Exception {
        String path = null;
        if ("banner".equals(fanart)) {
            path = phoenix.fanart.GetFanartBanner(sageMediaFile);
        } else if ("background".equals(fanart)) {
            path = phoenix.fanart.GetFanartBackground(sageMediaFile);
        } else {
            path = phoenix.fanart.GetFanartPoster(sageMediaFile);
            if (path==null) {
                // no poster, so use the default thumbnail
            	File f = MetaImageUtil.getThumbnailImageFile(sageMediaFile, 100, 2000);
            	path=f.getAbsolutePath();
            }
        }
        if (path == null) throw new FileNotFoundException(MediaFileAPI.GetMediaTitle(sageMediaFile));
        File f = new File(path);
        if (!f.exists()) throw new FileNotFoundException(f.getAbsolutePath());

        boolean overwrite = BooleanUtils.toBoolean(req.getParameter("overwrite"));
        String transform = req.getParameter("transform");
        
        //Object image = sagex.api.Utility.LoadImage(f);
        Object image = f;
        if (transform!=null) {
        	try {
        		image = phoenix.image.CreateImage(path + "_" + transform, "webfanart", image, transform, overwrite);
        	} catch (Throwable t) {
        		t.printStackTrace();
        	}
        }
        
        if (image==null) {
        	image=f;
        }
        
        writeImage(image, resp);
        Utility.UnloadImage(f.getAbsolutePath());
    }
    
    private void writeImage(Object sageImage, HttpServletResponse resp) throws IOException {
        BufferedImage img = null;
        if (sageImage instanceof BufferedImage) {
        	img = (BufferedImage) sageImage;
        } else if (sageImage instanceof File) {
        	img = ImageUtil.readImage((File) sageImage);
        } else if (sageImage instanceof String) {
        	img = ImageUtil.readImage(new File((String) sageImage));
        } else {
        	img = Utility.GetImageAsBufferedImage(sageImage);
        }
        resp.setContentType(ImageUtil.DEFAULT_IMAGE_MIME_TYPE);
        OutputStream os = resp.getOutputStream();
        ImageUtil.writeImage(img, ImageUtil.DEFAULT_IMAGE_FORMAT, os);
    }
}
