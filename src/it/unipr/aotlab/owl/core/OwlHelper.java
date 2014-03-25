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

package it.unipr.aotlab.owl.core;

import java.io.*;
import java.text.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.FileHandler;

/**
 * Helps managing the intermediate ontology model.
 *
 * @author <a href="mailto:tomamic@ce.unipr.it">Michele Tomaiuolo</a> -
 *         <a href="http://aot.ce.unipr.it">University of Parma</a>
 */
public class OwlHelper {
    // indexes of styled names in the array returned by transformName
    public static final int SRC = 0;
    public static final int CLS = 1;
    public static final int OBJ = 2;
    public static final int STD = 3;
    public static final int CST = 4;

    // key to get from the properties a default parent for orphan classes - not used
    public static final String DEFAULT_PARENT = "helper.default-parent";

    // main logger name
    public static final String LOGGER_NAME = "default";
    // main logger name
    public static final Level LOG_LEVEL = Level.ALL;
    // standard log file-name
    public static final String LOG_FILE = "aot-owl";
    // standard log file-extension
    public static final String LOG_EXT = ".log.xml";

    // key to get from the properties a conversion map for xml types and owl classes - not used
    public static final String CONV = "helper.conv";

    // string to remove, eventually, from ontology name
    public static final String TRAILING_ONTOLOGY = "Ontology";

    // used to create different log files
    static final String LOG_DATE = null; //new SimpleDateFormat("yyyyMMdd'T'HHmmss").format(new Date());

    // main logger
    static final Logger MAIN_LOGGER = createMainLogger();

    // default parent for orphan classes
    OwlResource defaultParent = null;
    // list containing only the default parent
    List defaultParentList = new ArrayList();

    // conversion map for xml types and owl classes
    Map conv = null;

    public OwlHelper() {}

    public OwlHelper(Map conv, OwlResource defaultParent) {
        setConv(conv);
        setDefaultParent(defaultParent);
    }

    public static Logger getLogger(String name, String level) {
        Logger logger = getLogger(name);
        try {
            logger.setLevel(Level.parse(level));
        } catch (Exception e) {
            logger.setLevel(Level.ALL);
            logger.warning(e.getMessage());
        }
        return logger;
    }

    public static Logger getLogger(String name) {
        Logger logger = Logger.getLogger(name);
        if (logger != MAIN_LOGGER) logger.setParent(MAIN_LOGGER);
        return logger;
    }

    public static Logger getMainLogger() {
        return MAIN_LOGGER;
    }

    static Logger createMainLogger() {
        Logger logger = Logger.getLogger(LOGGER_NAME);
        if (logger.getUseParentHandlers()) {
            try {
                // a new logger has always use-parent-handlers = true
                String logFile = LOG_FILE + ((LOG_DATE != null) ? ("-" + LOG_DATE) : "") + LOG_EXT;
                logger.setUseParentHandlers(false);
                logger.addHandler(new FileHandler(logFile));
            } catch (Exception e) { e.printStackTrace(); }
            logger.setLevel(LOG_LEVEL);
        }
        return logger;
    }

    // set a default parent for orphan classes
    public void setDefaultParent(String parent) {
        setDefaultParent((parent == null || parent.length() == 0) ? null : new OwlResource(parent));
    }
    // set a default parent for orphan classes
    public void setDefaultParent(OwlResource parent) {
        defaultParent = parent;
        defaultParentList.clear();
        if (parent != null) defaultParentList.add(parent);
    }
    public OwlResource getDefaultParent() { return defaultParent; }

    // set a conversion map for xml types and owl classes
    public void setConv(String conv) { this.conv = createMap(conv); }
    public void setConv(Map conv) { this.conv = conv; }
    public Map getConv() { return conv; }

    // create a new owl resource
    public static OwlResource createResource(String name) {
        return new OwlResource(name);
    }
    // create a new map and load entries from a string
    public static Map<String, String> createMap(String entries) {
        return fillMap(new HashMap<String, String>(), entries);
    }

    // fill a map with key-value pairs parsed from a string
    public static Map fillMap(Map map, String entries) {
        if (entries == null) return map;
        return fillMap(map, parseKVPairs(entries));
    }

    // fill a map with a list of key-value pairs
    public static Map fillMap(Map map, String[][] kv) {
        for (int i = 0; i < kv.length; i++) {
            if (kv[i].length > 1) map.put(kv[i][0], kv[i][1]);
        }
        return map;
    }

    // split a string in a list of key-val pairs
    public static String[][] parseKVPairs(String kv) {
        // comma-separated list of entries
        String[] list = kv.split(",");
        String[][] result = new String[list.length][];
        for (int i = 0; i < list.length; i++) {
            // each entry is a colon-separated key-val pair
            result[i] = list[i].trim().split(":");
        }
        return result;
    }

    // list all properties of a class (including inherited properties)
    public static List listAllProperties(OwlClass c, OwlOntology o, List list) {
        list.addAll(c.getProperties().values());

        for (int i = 0; i < c.getParents().size(); i++) {
            String parentName = c.getParents().get(i).toString();
            OwlClass parent = (OwlClass)o.getClasses().get(parentName);
            listAllProperties(parent, o, list);
        }

        return list;
    }

    // list parents of a class (including default-parent if class is orphan)
    public List listParents(OwlClass c) {
        List parents = c.getParents();
        return (parents != null && parents.size() > 0) ? parents : defaultParentList;
    }

    // true iff r.name is in conv.keys
    public boolean isBasicClass(OwlResource r) {
        return isBasicClass(r, conv);
    }

    // true iff r.name is in conv.keys
    public static boolean isBasicClass(OwlResource r, Map conv) {
        return conv != null && conv.containsKey(r.getName());
    }

    // return the first ancestor class which has its name in conv.keys
    public OwlResource getBasicClass(OwlClass c, OwlOntology o) {
        // 1. c itself is a basic-class
        if (isBasicClass(c)) return c;

        // otherwise search for a basic class among ancestors
        for (Iterator i = listParents(c).iterator(); i.hasNext();) {
            String parentName = i.next().toString();
            OwlClass parent = (OwlClass)o.getClasses().get(parentName);
            // recursive call: cycles in hierarchy will crash the app
            if (parent != null) {
                OwlResource basic = getBasicClass(parent, o);
                // 2. if a basic class is found in a branch, return it
                if (basic != null) return basic;
            }
        }
        // 3. no basic classes among ancestors: return null;
        return null;
    }

    // return the value mapped in conv for name, if one is found, or name itself otherwise
    public String convert(String name) {
        return convert(name, name, conv);
    }

    // return the value mapped in conv for name, if one is found, or default otherwise
    public String convert(String name, String defaultValue) {
        return convert(name, defaultValue, conv);
    }

    // return the value mapped in conv for name, if one is found, or name itself otherwise
    public static String convert(String name, Map conv) {
        return convert(name, name, conv);
    }

    // return the value mapped in conv for name, if one is found, or default otherwise
    public static String convert(String name, String defaultValue, Map conv) {
        Object converted = (conv != null) ? conv.get(name) : null;
        return (converted != null) ? converted.toString() : defaultValue;
    }

    // return the value mapped in conv for res.name, if one is found, or res.clsName otherwise
    public String convertClsName(OwlResource res) {
        return convert(res.getName(), res.getClsName());
    }

    // return the value mapped in conv for res.name, if one is found, or res.objName otherwise
    public String convertObjName(OwlResource res) {
        return convert(res.getName(), res.getObjName());
    }

    // convert a package name to a path
    public static String packageToPath(String packageName) {
        return packageName.toString().replace('.', '/') + '/';
    }

    // transform to name of a resource according to various styles (source, class, object, standard, constant)
    public static String[] transformName(String name) {
        if (name == null || name.length() == 0) return new String[] { name, name, name, name, name };

        StringBuilder cls = new StringBuilder();
        StringBuilder obj = new StringBuilder();
        StringBuilder std = new StringBuilder();
        StringBuilder cst = new StringBuilder();

        // last char was not a punctuation char
        boolean blank = true;
        // the first letter has not yet gone
        boolean first = true;
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (Character.isLetter(c) || (! first && Character.isDigit(c))) {
                // a new word starts when there's a letter after punctuations, or when there's an uppercase letter
                boolean newWord = blank || Character.isUpperCase(c);

                // "class" style: capitalized words
                cls.append(newWord ? Character.toUpperCase(c) : c);

                // "object" style: first word lowercase, next ones capitalized
                obj.append(first ? Character.toLowerCase(c) : (newWord ? Character.toUpperCase(c) : c));

                // "standard" style: dash-separated lowercase words
                if (newWord && ! first) std.append('-');
                std.append(Character.toLowerCase(c));

                // "constant" style: uppercase without punctuations
                cst.append(Character.toUpperCase(c));

                // last char was not a punctuation char
                first = false;
                // the first letter has gone
                blank = false;
            }
            else {
                // last char was a punctuation char
                blank = true;
            }
        }

        String clsName = cls.toString();
        String objName = obj.toString();
        String stdName = std.toString();
        String cstName = cst.toString();
        return new String[] { name, clsName, objName, stdName, cstName };
    }

    public static String toString(OwlOntology ontology) {
        StringBuilder dump = new StringBuilder();
        for (Iterator i = ontology.getClasses().values().iterator(); i.hasNext();) {
            OwlClass c = (OwlClass)i.next();
            dump.append("class: ").append(c.getNamespace()).append(" ").append(c.getName()).append("\n");
            // add super-classes
            for (Iterator j = c.getParents().iterator(); j.hasNext();) {
                OwlResource p = (OwlResource)j.next();
                dump.append("parent: ").append(p.getNamespace()).append(" ").append(p.getName()).append("\n");
            }
            for (Iterator j = c.getProperties().values().iterator(); j.hasNext();) {
                OwlProperty p = (OwlProperty)j.next();
                dump.append("property: ").append(p).append(" ");
                dump.append("range: ").append(p.getRange().getNamespace()).append(" ").append(p.getRange().getName()).append(" ");
                dump.append("card: ").append(p.getMinCardinality()).append(" ").append(p.getMaxCardinality()).append("\n");
            }
        }
        return dump.toString();
    }
}
