package test;

import java.io.File;

import sagex.phoenix.menu.Menu;
import sagex.phoenix.menu.MenuManager;

public class TestMenus {
    public static void main(String args[]) throws Exception {
        MenuManager mgr = new MenuManager(new File("src/main/menus"), new File("target/userdata/"));
        mgr.loadConfigurations();
        
        Menu m = mgr.getMenu("TestMenu");
        if (m==null) {
            throw new Exception("No Main Menu");
        }
        
        System.out.println(m);
    }
}
