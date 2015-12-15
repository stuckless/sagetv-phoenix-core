package sagex.phoenix.metadata.proxy;

import org.apache.commons.lang.StringUtils;

import sagex.phoenix.metadata.CastMember;
import sagex.phoenix.metadata.ICastMember;

public class CastMemberPropertyListFactory extends StringPropertyListFactory {
    public CastMemberPropertyListFactory() {
        super();
    }

    @Override
    public Object decode(String item) {
        if (item == null)
            return null;
        String parts[] = item.split("\\s*--\\s*");
        if (parts == null || parts.length == 0)
            return null;
        CastMember cm = new CastMember();
        cm.setName(parts[0]);
        if (parts.length > 1 && !StringUtils.isEmpty(parts[1])) {
            cm.setRole(parts[1]);
        }
        return cm;
    }

    @Override
    public String encode(Object item) {
        if (item == null)
            return "";
        ICastMember cm = (ICastMember) item;
        if (StringUtils.isEmpty(cm.getRole())) {
            return cm.getName();
        } else {
            return cm.getName() + " -- " + cm.getRole();
        }
    }
}
