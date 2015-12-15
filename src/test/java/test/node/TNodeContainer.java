package test.node;

import sagex.phoenix.node.IContainer;

public class TNodeContainer implements IContainer<TNodeContainer, TNode> {

    @Override
    public boolean hasChildren() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public int getChildCount() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public TNode getChild(int pos) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getId() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TNodeContainer getParent() {
        // TODO Auto-generated method stub
        return null;
    }

}
