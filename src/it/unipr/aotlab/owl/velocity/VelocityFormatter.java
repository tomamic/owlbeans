package it.unipr.aotlab.owl.velocity;

import it.unipr.aotlab.owl.core.*;
import it.unipr.aotlab.owl.io.*;

import java.io.*;
import java.util.*;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.runtime.RuntimeInstance;

/**
 * Writes some java code from an intermediate ontology.
 *
 * @author <a href="mailto:tomamic@ce.unipr.it">Michele Tomaiuolo</a> -
 *         <a href="http://aot.ce.unipr.it">University of Parma</a>
 */
public class VelocityFormatter implements OwlWriter {
    // velocity conf file
    public static final String VELOCITY_CONF = "velocity.conf";

    // keys to access properties
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

    // out-param names for templates
    public static final String PATH = "path";

    String[][] patterns = null;
    String out = null;
    String pkg = null;
    OwlHelper hlp = null;
    Writer writer = null;

    RuntimeInstance velocity = null;

    public VelocityFormatter() {
        hlp = new OwlHelper();

        Properties vc = it.unipr.aotlab.owl.Main.loadDefaultConf(VELOCITY_CONF, null);
        velocity = new RuntimeInstance();
        try {
            velocity.init(vc);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public VelocityFormatter(Properties conf, Writer w) {
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

    String format(String patternName, Map params) {
        StringWriter sw = new StringWriter();
        try {
            // context will hold a reference to params (not a copy)
            VelocityContext context = new VelocityContext(params);
            Template t = velocity.getTemplate(patternName);
            t.merge(context, sw);
            // for a pre-loaded pattern: Velocity.evaluate(context, sw, "vlog", new StringReader(pattern));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sw.toString();
    }

    void write(String patternName, Map params) throws IOException {
        // make a copy to avoid params to go from a context to the following one
        Map outParams = new HashMap(params);
        // merge parameters with template
        String result = format(patternName, outParams);

        // compose file-path to write output to
        String path = (String)outParams.get(PATH);
        if (path != null && path.length() > 0) {
            if (out != null) {
                // create non-existing directories contained in the file-path
                new File(path).getParentFile().mkdirs();
                // write file - ontology or class
                FileWriter fw = new FileWriter(path);
                fw.write(result);
                fw.flush();
                fw.close();
            }
            if (writer != null) writer.write(result);
        }
    }

    public void write(OwlOntology ontology) throws IOException {
        Map params = new HashMap();
        params.put(ONT, ontology);
        params.put(PKG, pkg);
        params.put(OUT, out);
        params.put(HLP, hlp);

        for (int i = 0; i < patterns.length; i++) {
            if (patterns[i].length > 1 && CLS_PATTERN.equals(patterns[i][1])) {
                // pattern has be applyed to each class
                for (Iterator j = ontology.getClasses().values().iterator(); j.hasNext();) {
                    params.put(CLS, (OwlClass)j.next());
                    write(patterns[i][0], params);
                }
            }
            else {
                // pattern has be applyed to the whole ontology
                write(patterns[i][0], params);
            }
        }
    }
}
