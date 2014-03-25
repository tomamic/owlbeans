package it.unipr.aotlab.owl.velocity;

import it.unipr.aotlab.owl.core.*;
import it.unipr.aotlab.owl.io.*;

import java.io.*;
import java.util.*;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.texen.Generator;
/**
 * Writes some java code from an intermediate ontology.
 *
 * @author <a href="mailto:tomamic@ce.unipr.it">Michele Tomaiuolo</a> -
 *         <a href="http://aot.ce.unipr.it">University of Parma</a>
 */
public class VelocityWriter implements OwlWriter {
    // velocity conf file
    public static final String VELOCITY_CONF = "velocity.conf";

    // keys to access properties
    public static final String TEMPLATE = "velocity-formatter.template";
    public static final String PATTERNS = "velocity-formatter.patterns";
    public static final String PACKAGE = "velocity-formatter.package";
    public static final String OUTPUT = "velocity-formatter.output";

    // patterns with a trailing ":cls" must be applied to each class
    public static final String CLS_PATTERN = "cls";

    // in-param names for templates
    public static final String ONT = "ont";
    public static final String CLS = "cls";
    public static final String PKG = "pkg";
    public static final String OUT = "out";
    public static final String HLP = "hlp";
    public static final String GEN = "gen";

    // out-param names for templates
    public static final String PATH = "path";

    String[][] patterns = null;
    String out = null;
    String pkg = null;
    OwlHelper hlp = null;
    Writer writer = null;
    String template = null;

    VelocityEngine velocity = null;

    public VelocityWriter() {
        hlp = new OwlHelper();

        Properties vc = it.unipr.aotlab.owl.Main.loadDefaultConf(VELOCITY_CONF, null);
        velocity = new VelocityEngine();
        try {
            velocity.init(vc);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public VelocityWriter(Properties conf, Writer w) {
        this();
        init(conf, w);
    }

    public void init(Properties conf, Writer writer) {
        if (writer != null) {
            this.writer = writer;
        }
        init(conf);
    }

    public void init(Properties conf, OutputStream output) {
        if (output != null) {
            this.writer = new OutputStreamWriter(output);
        }
        init(conf);
    }

    public void init(Properties conf) {
        if (conf != null) {
            out = conf.getProperty(OUTPUT);
            pkg = conf.getProperty(PACKAGE);
            patterns = OwlHelper.parseKVPairs(conf.getProperty(PATTERNS));
        }
    }

    public void write(OwlOntology ontology) throws IOException {
        VelocityContext params = new VelocityContext();
        params.put(ONT, ontology);
        params.put(PKG, pkg);
        params.put(OUT, out);
        params.put(HLP, hlp);

        Generator g = Generator.getInstance();
        g.setOutputPath(out);
        try {
            String output = g.parse(template, params);
            if (writer != null) writer.write(output);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
