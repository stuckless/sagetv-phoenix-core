package sagex.phoenix.image;

import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class CompositeTransform implements IBufferedTransform, Iterable<IBufferedTransform> {
    private List<IBufferedTransform> transforms = new LinkedList<IBufferedTransform>();
    
    public CompositeTransform() {
    }
    
    public CompositeTransform(IBufferedTransform transform) {
        addTransform(transform);
    }
    
    public void addTransform(IBufferedTransform transform) {
        transforms.add(transform);
    }

    public BufferedImage transform(BufferedImage image) {
        BufferedImage reply = image;
        for (IBufferedTransform bt : transforms) {
            reply = bt.transform(reply);
        }
        return reply;
    }
    
    public int size() {
        return transforms.size();
    }

    public Iterator<IBufferedTransform> iterator() {
        return transforms.iterator();
    }
}
