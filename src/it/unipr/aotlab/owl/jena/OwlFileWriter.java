package it.unipr.aotlab.owl.jena;

import it.unipr.aotlab.owl.core.*;
import it.unipr.aotlab.owl.io.*;

import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFList;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.RDFWriter;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.Logger;

/**
 * Writes an intermediate ontology to an OWL document.
 *
 * @author <a href="mailto:tomamic@ce.unipr.it">Michele Tomaiuolo</a> -
 *         <a href="http://aot.ce.unipr.it">University of Parma</a>
 */
public class OwlFileWriter implements OwlWriter {
    public static final String DATATYPES_NS = OwlResource.DATATYPES_NS;
    public static final String NS_SEPARATOR = OwlResource.NS_SEPARATOR;

    public static final String DEFAULT_LEADING_CLASS_SEPARATOR = ".";
    static final String DAML_EXT = ".daml";

    // keys to access properties
    public static final String OUTPUT = "owl-file-writer.output";
    public static final String IMPORTS = "owl-file-writer.imports"; // TODO: not used
    public static final String ONTOLOGY = "owl-file-writer.ontology"; // TODO: not used
    public static final String LANG = "owl-file-writer.lang";
    public static final String URI = "owl-file-writer.uri";
    public static final String CLSSEP = "owl-file-writer.cls-sep";

    // TODO: delete
    //static final String DATATYPE_MASK = "boolean|float|decimal|double|int|positiveInteger|negativeInteger|nonPositiveInteger|nonNegativeInteger|long|integer|short|byte|unsignedLong|unsignedInteger|unsignedShort|unsignedByte|string|anyURI|normalizedString|token|language|date|dateTime|duration|time";
    //static final String DEFAULT_RANGE = "string";
    //static final String DEFAULT_RANGE_NS = DATATYPES_NAMESPACE;

    // default logger name
    public static final String LOGGER_NAME = "owl-file-writer";

    boolean isDAML = false;
    OutputStream output = null;
    String uri = null;
    String ns = null;
    String lang = null;
    Logger logger = null;

    // if not null, property names will be preceded by their class name, i.e. "theClass.theProperty"
    String leadingClassSeparator = null; // DEFAULT_LEADING_CLASS_SEPARATOR;

    public OwlFileWriter() {
    }

    public OwlFileWriter(Properties conf) throws IOException {
        init(conf, null);
    }

    public OwlFileWriter(Properties conf, OutputStream output) throws IOException {
        init(conf, output);
    }

    public void init(Properties conf) throws IOException {
        init(conf);
    }

    public void init(Properties conf, OutputStream output) throws IOException {
        logger = OwlHelper.getLogger(LOGGER_NAME);

        if (output != null) {
            this.output = output;
        }
        else {
            String file = conf.getProperty(OUTPUT);
            if (file != null) {
                isDAML = file != null && file.toLowerCase().endsWith(DAML_EXT);
                this.output = new FileOutputStream(file);
            }
            else logger.warning("no output-stream set");
        }
        lang = conf.getProperty(LANG);
        uri = conf.getProperty(URI);
        leadingClassSeparator = conf.getProperty(CLSSEP);
        if (leadingClassSeparator != null && leadingClassSeparator.length() == 0) {
            leadingClassSeparator = null;
        }
    }

    public String getLeadingClassSeparator() { return leadingClassSeparator; }
    public void setLeadingClassSeparator(String separator) { leadingClassSeparator = separator; }

    public void write(OwlOntology ontology) throws IOException {
        OntModel model = createOntModel(ontology);
        RDFWriter w = model.getWriter(lang);
        w.setProperty("xmlbase", uri);
        if (output != null) w.write(model, output, uri);
        else throw (new IOException("OwlFileWriter: no output-stream set"));
    }

    public OntModel createOntModel(OwlOntology ontology) {
        OntModelSpec spec = isDAML ? OntModelSpec.DAML_MEM : OntModelSpec.OWL_MEM;
        OntModel model = ModelFactory.createOntologyModel(spec, null);

        String ontoNs = ontology.getNamespace();

        logger.fine("uri (conf): " + uri);
        logger.fine("uri (ontology): " + ontoNs);

        // set model uri from the uri of the ontology
        if (ontoNs != null) uri = ontoNs;
        if (uri == null) uri = "";
        if (uri.endsWith(NS_SEPARATOR)) uri = uri.substring(0, uri.length() - 1);
        logger.info("uri: " + uri);

        ns = uri + NS_SEPARATOR;

        for (Iterator i = model.getNsPrefixMap().keySet().iterator(); i.hasNext();) {
            String prefix = (String)i.next();
            if (! prefix.matches("rdf|rdfs|owl")) model.removeNsPrefix(prefix);
        }
        model.setNsPrefix("", ns);

        Ontology oo = model.createOntology(uri);
        oo.setLabel(ontology.getName(), null);

        for (Iterator i = ontology.getClasses().values().iterator(); i.hasNext();) {
            OwlClass c = (OwlClass)i.next();
            OntClass oc = model.createClass(ns + c.getName());
        }

        for (Iterator i = ontology.getClasses().values().iterator(); i.hasNext();) {
            OwlClass c = (OwlClass)i.next();
            OntClass oc = model.getOntClass(ns + c.getName());

            // add super-classes
            for (Iterator j = c.getParents().iterator(); j.hasNext();) {
                String p = j.next().toString();
                oc.addSuperClass(model.getResource(ns + p));
            }

            // add properties
            for (Iterator j = c.getProperties().values().iterator(); j.hasNext();) {
                OwlProperty p = (OwlProperty)j.next();
                //System.out.println("adding to owl: " + c.getName() + "." + p.getName() + ":" + p.getRange() + ";");
                String pName = p.getName();
                if (leadingClassSeparator != null) pName = c.getName() + leadingClassSeparator + pName;
                OwlResource range = p.getRange();
                boolean isDatatype = DATATYPES_NS.equals(range.getNamespace());

                OntProperty op = model.getOntProperty(ns + pName);
                if (op == null) {
                    // create property
                    op = isDatatype ?
                        model.createDatatypeProperty(ns + pName) :
                        model.createObjectProperty(ns + pName);

                    // set range
                    try {
                        String rangeNs = range.getNamespace(); // isDatatype ? DATATYPE_NAMESPACE : ns;
                        if (rangeNs == null) rangeNs = ns;
                        op.setRange(model.getResource(rangeNs + range));
                    } catch (Exception e) { e.printStackTrace(); }

                    // set domain
                    op.setDomain(oc);
                }
                else {
                    // add class to property domain
                    OntResource domainResource = op.getDomain();
                    if (domainResource == null) op.setDomain(oc);
                    else try {
                        OntClass domainClass = domainResource.asClass();
                        if (domainClass.isUnionClass()) {
                            UnionClass domainUnion = domainClass.asUnionClass();
                            domainUnion.addOperand(oc);
                        }
                        else {
                            RDFNode[] domainNodes = new RDFNode[] {domainClass, oc};
                            RDFList domainList = model.createList(domainNodes);
                            UnionClass domainUnion = model.createUnionClass(null, domainList);
                            op.setDomain(domainUnion);
                        }
                    } catch (ConversionException ce) { ce.printStackTrace(); }
                }
                //System.out.println("added to owl: " + c.getName() + "." + p.getName() + ";");

                // add restrictions
                if (p.getMaxCardinality() == 1) {
                    op = op.convertToFunctionalProperty();
                }
                if (p.getMinCardinality() > 0) {
                    OntClass restr = model.createMinCardinalityRestriction(null, op, p.getMinCardinality());
                    oc.addSuperClass(restr);
                }
                if (p.getMaxCardinality() != OwlProperty.UNLIMITED) {
                    OntClass restr = model.createMaxCardinalityRestriction(null, op, p.getMaxCardinality());
                    oc.addSuperClass(restr);
                }
            }
        }
        return model;
    }

}
