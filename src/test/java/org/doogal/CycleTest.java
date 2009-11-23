package org.doogal;

import java.io.IOException;
import java.util.Collection;

import jdepend.framework.JDepend;
import jdepend.framework.JavaPackage;
import junit.framework.TestCase;

public class CycleTest extends TestCase {
    @SuppressWarnings("unchecked")
    public final void test() throws IOException {

        final JDepend jdepend = new JDepend();

        jdepend.addDirectory("target/classes");
        jdepend.addPackage("org.doogal");

        jdepend.analyze();
        final Collection<JavaPackage> packages = jdepend.getPackages();

        assertFalse(0 < packages.size());
        for (final JavaPackage p : packages)
            assertEquals("Cycle exists: " + p.getName(), false, p.containsCycle());
    }
}
