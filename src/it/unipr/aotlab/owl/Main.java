/*****************************************************************
 OWLBeans is a toolkit to manipulate ontologies.
 Its main purpose is to extract JavaBeans from OWL documents.
 Copyright (C) 2004 University of Parma.

 GNU Lesser General Public License

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation,
 version 2.1 of the License.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the
 Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 Boston, MA  02111-1307, USA.
 *****************************************************************/

package it.unipr.aotlab.owl;

import it.unipr.aotlab.owl.core.*;
import it.unipr.aotlab.owl.io.*;
import it.unipr.aotlab.owl.jena.OwlFileReader;
import it.unipr.aotlab.owl.velocity.VelocityFormatter;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The <code>Main</code> by default extracts JavaBeans from an OWL documents.
 * It can be executed from a shell, and can be customized to perform any one
 * of the coversions allowed by available readers and writers.
 *
 * @author <a href="mailto:tomamic@ce.unipr.it">Michele Tomaiuolo</a> -
 *         <a href="http://aot.ce.unipr.it">University of Parma</a>
 */
public class Main {
    public static final String LOGGER_NAME = "main";
    public static final String LOG_LEVEL = "main.log-level";

    static final String CONF_PREFIX = "main."; // not used

    // standard folder for conf (properties) files
    public static final String CONF_DIR = "conf/";
    // standard extension for conf (properties) files - not used
    public static final String CONF_EXT = ".conf";

    // keys to access properties
    public static final String OPTS2CONF = "main.opts2conf";
    public static final String READER = "main.reader-class";
    public static final String WRITER = "main.writer-class";

    // command line options
    public static final String INPUT = "-input";
    public static final String ONTOLOGY = "-ontology";
    public static final String IMPORTS = "-imports";
    public static final String OUTPUT = "-output";
    public static final String PACKAGE = "-package";

    // default configuration file
    static final String DEFAULT_CONF = "default.conf";

    // default conf
    Properties defaults = null;

    // conf (contains defaults, too)
    Properties conf = null;

    // to translate command line options into properties
    Map<String, String> opts2conf = null;

    public Main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: java " + getClass().getName() + " [-input <input-file>] [-output <output-dir>] [-package <pakage>] [-ontology <ontology-name>] [-imports (true|false)]\n");
        }

        // set default properties
        defaults = loadDefaultConf(DEFAULT_CONF, null);
        conf = new Properties();
        conf.putAll(defaults);

        // load user-defined properties
        conf = loadConf(DEFAULT_CONF, null);

        opts2conf = OwlHelper.createMap(conf.getProperty(OPTS2CONF));
        // read command line options
        for (int i = 0; i < args.length; i++) {
            String key = opts2conf.get(args[i]);
            if (key != null) {
                String val = (args.length > i + 1) ? args[i + 1] : "true";
                conf.put(key, val);
            }
        }

        Logger mainLogger = OwlHelper.getMainLogger();
        try {
            String level = conf.getProperty(LOG_LEVEL);
            if (level != null) mainLogger.setLevel(Level.parse(level));
        } catch (Exception e) {
            mainLogger.warning(e.getMessage());
        }

        Logger logger = OwlHelper.getLogger(LOGGER_NAME);
        logger.config("conf: " + conf.toString());
    }

    // load configuration properties from an xml file
    public static Properties loadDefaultConf(String confName, Properties conf) {
        if (conf == null) {
            conf = new Properties();
        }

        // load default properties (shipped in the jar)
        try {
            conf.loadFromXML(Main.class.getClassLoader().getResourceAsStream(CONF_DIR + confName));
        } catch (Exception e) {
            // TODO: write log here
            //e.printStackTrace();
        }
        return conf;
    }

    // load configuration properties from an xml file
    public static Properties loadConf(String confName, Properties conf) {
        if (conf == null) {
            conf = new Properties();
        }

        // overwrite default values with user-defined values
        try {
            conf.loadFromXML(new FileInputStream(confName));
        } catch (Exception e) {
            // TODO: write log here
            //e.printStackTrace();
        }
        return conf;
    }

    public Properties getDefaults() { return defaults; }
    public Properties getConf() { return conf; }

    public OwlOntology readOntology() throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        OwlReader reader = (OwlReader)Class.forName(conf.getProperty(READER)).newInstance();
        reader.init(conf);
        OwlOntology ont = reader.read();
        String key = opts2conf.get(ONTOLOGY);
        String name = conf.getProperty(key);
        if (ont.getName() == null || (name != null && ! name.equals(defaults.getProperty(key)))) {
            ont.setName(name);
        }
        return ont;
    }

    public void writeOntology(OwlOntology ontology, OutputStream stream) throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        // correct package name
        String key = opts2conf.get(PACKAGE);
        String pkg = conf.getProperty(key);
        if ((pkg == null || pkg.equals(defaults.getProperty(key))) && ontology.getName() != null) {
            pkg = ontology.getObjName();
        }
        conf.setProperty(key, pkg);
        // correct output path
        key = opts2conf.get(OUTPUT);
        String out = conf.getProperty(key);
        if (out != null) {
            if (! out.endsWith("/")) out += "/";
            conf.setProperty(key, out);
        }
        OwlWriter writer = (OwlWriter)Class.forName(conf.getProperty(WRITER)).newInstance();
        writer.init(conf, stream);
        writer.write(ontology);
    }

    public static void main(String[] args) {
        try {
            Main m = new Main(args);
            OwlOntology onto = m.readOntology();
            m.writeOntology(onto, null);
        } catch (Exception e) { e.printStackTrace(); }
    }
}
