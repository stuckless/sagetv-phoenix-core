package test.junit.cm;

import sagex.phoenix.configuration.ConfigScope;
import sagex.phoenix.configuration.proxy.AField;
import sagex.phoenix.configuration.proxy.AGroup;
import sagex.phoenix.configuration.proxy.FieldProxy;
import sagex.phoenix.configuration.proxy.GroupProxy;

@AGroup(label = "My Proxy Group", description = "", path = "mygroup")
public class MyProxyGroup extends GroupProxy {
    @AField(label = "Name")
    public FieldProxy<String> name = new FieldProxy<String>("Sean");

    @AField(label = "Name2", fullKey = "test/name2")
    public FieldProxy<String> name2 = new FieldProxy<String>("Stuckless");

    @AField(label = "testint")
    public FieldProxy<Integer> testint = new FieldProxy<Integer>(1);

    @AField(label = "testboolean")
    public FieldProxy<Boolean> testboolean = new FieldProxy<Boolean>(true);

    @AField(label = "testuserscope", scope = ConfigScope.USER)
    public FieldProxy<String> testuserscope = new FieldProxy<String>("UserScoped1");

    @AField(label = "testoptions", listSeparator = ";", list = "1:One,2:Two,3:Three")
    public FieldProxy<String> testlist = new FieldProxy<String>("3");

    @AField(label = "testpasswordhint", hints = "CONFIG_PASSWORD")
    public FieldProxy<String> testpasswordhint = new FieldProxy<String>("secret");

    public MyProxyGroup() {
        super();
        init();
    }
}
