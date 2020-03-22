package network.palace.library;

import java.net.URL;
import java.net.URLClassLoader;

public class DashboardClassLoader extends URLClassLoader {

    public DashboardClassLoader() throws Exception {
        super(new URL[0], null);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        Class<?> clazz;
        try {
            clazz = super.findClass(name);
        } catch (Exception e) {
            clazz = null;
        }
        if (clazz == null) {
            return getSystemClassLoader().loadClass(name);
        }
        return clazz;
    }

    @Override
    protected void addURL(URL url) {
        super.addURL(url);
    }
}
