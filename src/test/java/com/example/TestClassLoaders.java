package com.example;

import com.example.Foo;
import com.example.Main;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Resources;
import org.pf4j.*;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

public class TestClassLoaders {
    private final PluginClassLoader pluginClassLoaderSelfFirst;

    private final PluginClassLoader pluginClassLoaderParentFirst;

    private final ClassLoader contextClassLoader;

    public TestClassLoaders() throws MalformedURLException {
        PluginManager pluginManager = new DefaultPluginManager();
        PluginDescriptor pluginDescriptor = new DefaultPluginDescriptor();
        contextClassLoader = Thread.currentThread().getContextClassLoader();
        pluginClassLoaderSelfFirst = new LoggablePluginClassLoader(pluginManager, pluginDescriptor, contextClassLoader);
        pluginClassLoaderParentFirst = new LoggablePluginClassLoader(pluginManager, pluginDescriptor, contextClassLoader, true);
        URL url = getClass().getResource("example.jar");
        pluginClassLoaderSelfFirst.addURL(url);
        pluginClassLoaderParentFirst.addURL(url);
    }

    @Test
    public void testClassInstantiationTakenFromDifferentClassLoader() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        Foo foo = (Foo) pluginClassLoaderSelfFirst.loadClass(Foo.class.getName()).newInstance();
        foo.print();
    }

    @Test
    public void testClassInstantiationTakenFromTheParentFirstClassLoader() throws ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        Class<?> fooPFirst = pluginClassLoaderParentFirst.loadClass(Foo.class.getName());
        Class<?> fooSFirst = pluginClassLoaderSelfFirst.loadClass(Foo.class.getName());

        assertEquals(Foo.class, fooPFirst);

        assertNotEquals(fooPFirst, fooSFirst);
        assertNotEquals(Foo.class, fooSFirst);

        assertFalse(fooPFirst.getClassLoader() instanceof PluginClassLoader);
        assertTrue(fooSFirst.getClassLoader() instanceof PluginClassLoader);
    }

    /**
     * --Class: com.example.Main was loaded in sun.misc.Launcher$AppClassLoader@2a139a55
     * --Class: com.example.Foo was loaded in sun.misc.Launcher$AppClassLoader@2a139a55
     * Main.foo executed
     * --Class: com.example.Main was loaded in sun.misc.Launcher$AppClassLoader@2a139a55
     * --Class: java.lang.Object was loaded in null
     * --Class: com.example.Foo was loaded in com.tibbo.aggregate.util.cl.TestClassLoaders$LoggablePluginClassLoader@2490d09
     */
    @Test
    public void testClassTypeMismatch() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
        Object foo = pluginClassLoaderParentFirst.loadClass(Foo.class.getName()).newInstance();
        pluginClassLoaderParentFirst.loadClass(Main.class.getName()).getMethod("run", Foo.class).invoke(null, foo);
        foo = pluginClassLoaderSelfFirst.loadClass(Foo.class.getName()).newInstance();
        pluginClassLoaderSelfFirst.loadClass(Main.class.getName()).getMethod("run", Foo.class).invoke(null, foo);
    }

    /**
     * --Class: java.lang.Object was loaded in null
     * --Class: com.example.Bar was loaded in com.tibbo.aggregate.util.cl.TestClassLoaders$LoggablePluginClassLoader@481564d0
     * --Class: com.example.Foo was loaded in sun.misc.Launcher$AppClassLoader@2a139a55
     * --Class: com.example.Main was loaded in sun.misc.Launcher$AppClassLoader@2a139a55
     * Main.foo executed
     * --Class: java.lang.Object was loaded in null
     * --Class: com.example.Bar was loaded in com.tibbo.aggregate.util.cl.TestClassLoaders$LoggablePluginClassLoader@b5c4493
     * --Class: com.example.Foo was loaded in com.tibbo.aggregate.util.cl.TestClassLoaders$LoggablePluginClassLoader@b5c4493
     * --Class: com.example.Main was loaded in sun.misc.Launcher$AppClassLoader@2a139a55
     */

    @Test
    public void testClassMethodLinkageError() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
        pluginClassLoaderParentFirst.loadClass("com.example.Bar").getMethod("execute").invoke(null);
        pluginClassLoaderSelfFirst.loadClass("com.example.Bar").getMethod("execute").invoke(null);
    }

    /**
     * --Class: java.lang.Object was loaded in null
     * --Class: com.example.Bar was loaded in com.tibbo.aggregate.util.cl.TestClassLoaders$LoggablePluginClassLoader@2490d09
     * --Class: com.example.Main was loaded in sun.misc.Launcher$AppClassLoader@2a139a55
     * --Class: com.example.Foo was loaded in com.tibbo.aggregate.util.cl.TestClassLoaders$LoggablePluginClassLoader@2490d09
     * Main.foo executed
     * --Class: com.example.Main was loaded in sun.misc.Launcher$AppClassLoader@2a139a55
     */
    @Test
    public void testClassMethodLinkageErrorDifferentType() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
        pluginClassLoaderSelfFirst.loadClass("com.example.Bar").getMethod("execute").invoke(null);
    }

    @Test
    public void testClassConstructorLinkageError() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
        pluginClassLoaderParentFirst.loadClass("com.example.Bar").getMethod("getInstance").invoke(null);
        pluginClassLoaderSelfFirst.loadClass("com.example.Bar").getMethod("getInstance").invoke(null);
    }

    private class LoggablePluginClassLoader extends PluginClassLoader {

        public LoggablePluginClassLoader(PluginManager pluginManager, PluginDescriptor pluginDescriptor, ClassLoader parent) {
            super(pluginManager, pluginDescriptor, parent);
        }

        public LoggablePluginClassLoader(PluginManager pluginManager, PluginDescriptor pluginDescriptor, ClassLoader parent, boolean parentFirst) {
            super(pluginManager, pluginDescriptor, parent, parentFirst);
        }

        @Override
        public Class<?> loadClass(String className) throws ClassNotFoundException {
            Class<?> aClass = super.loadClass(className);
            System.out.println("--Class: " + className + " was loaded in " + aClass.getClassLoader());
            return aClass;
        }
    }
}
