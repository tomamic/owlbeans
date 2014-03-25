package it.unipr.aotlab.owl.jena;

import it.unipr.aotlab.owl.core.*;
import it.unipr.aotlab.owl.io.*;

import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.Logger;

/**
 * Reads an intermediate ontology from an OWL document.
 *
 * @author <a href="mailto:tomamic@ce.unipr.it">Michele Tomaiuolo</a>,
 *         <a href="mailto:bernini@ce.unipr.it">Bernini Nicola</a>,
 *         <a href="mailto:bombini@ce.unipr.it">Bombini Luca</a> -
 *         <a href="http://aot.ce.unipr.it">University of Parma</a>
 */
public class OwlFileReader implements OwlReader {
    public static final String DATATYPES_NS = OwlResource.DATATYPES_NS;
    public static final OwlResource DEFAULT_RANGE = new OwlResource(DATATYPES_NS, "string");

    public static final String DEFAULT_LEADING_CLASS_SEPARATOR = ".";
    public static final String DAML_EXT = ".daml";

    // keys to access properties
    public static final String INPUT = "owl-file-reader.input";
    public static final String ONTOLOGY = "owl-file-reader.ontology";
    public static final String IMPORTS = "owl-file-reader.imports";
    public static final String LANG = "owl-file-reader.lang";
    public static final String URI = "owl-file-reader.uri";

    // default logger name
    public static final String LOGGER_NAME = "owl-file-reader";

    OntModel model = null;
    InputStream input = null;
    boolean isDAML = false;
    boolean processImports = false;
    String leadingClassSeparator = DEFAULT_LEADING_CLASS_SEPARATOR;
    String name = null;
    String lang = null;
    String uri = null;
    String ns = null;
    Logger logger = null;

    public OwlFileReader() throws IOException {
    }

    public OwlFileReader(Properties conf) throws IOException {
        init(conf, null);
    }

    public OwlFileReader(Properties conf, InputStream input) throws IOException {
        init(conf, input);
    }

    public OwlFileReader(OntModel model) {
        try {
            init(new Properties(), null);
        } catch (IOException e) { e.printStackTrace(); /*never*/ }
        setModel(model);
    }

    public void init(Properties conf) throws IOException {
        init(conf, null);
    }

    public void init(Properties conf, InputStream input) throws IOException {
        logger = OwlHelper.getLogger(LOGGER_NAME);

        if (input != null) {
            this.input = input;
        }
        else {
            // open file-stream, name comes from properties
            String file = conf.getProperty(INPUT);
            if (file != null) {
                isDAML = file != null && file.toLowerCase().endsWith(DAML_EXT);
                this.input = new FileInputStream(file);
            }
            else logger.warning("no input-stream set");
        }

        name = conf.getProperty(ONTOLOGY);
        lang = conf.getProperty(LANG);
        uri = conf.getProperty(URI);
    }

    public void setModel(OntModel model) { this.model = model; }
    public OntModel getModel() { return model; }

    public OntModel readModel() throws IOException {
        OntModelSpec spec = isDAML ? OntModelSpec.DAML_MEM : OntModelSpec.OWL_MEM_RULE_INF;
        OntModel model = ModelFactory.createOntologyModel(spec, null);
        // model.getDocumentManager().setProcessImports(processImports);
        model.getDocumentManager().setProcessImports(false);
        if (input != null) model.read(input, uri, lang);
        else throw (new IOException("OwlFileReader: no input-stream set"));
        return model;
    }

    public String getLeadingClassSeparator() { return leadingClassSeparator; }
    public void setLeadingClassSeparator(String separator) { leadingClassSeparator = separator; }

    void handleRestriction(Restriction r, OwlProperty p) {
        int old, card;
        try {
            if (r.isMinCardinalityRestriction()) {
                card = r.asMinCardinalityRestriction().getMinCardinality();
                old = p.getMinCardinality();
                p.setMinCardinality(old >= 0 ? Math.max(old, card) : card);
            }

            if (r.isMaxCardinalityRestriction()) {
                card = r.asMaxCardinalityRestriction().getMaxCardinality();
                old = p.getMaxCardinality();
                p.setMaxCardinality(old >= 0 ? Math.min(old, card) : card);
            }

            if (r.isCardinalityRestriction()) {
                card = r.asCardinalityRestriction().getCardinality();
                old = p.getMinCardinality();
                p.setMinCardinality(old >= 0 ? Math.max(old, card) : card);
                old = p.getMaxCardinality();
                p.setMaxCardinality(old >= 0 ? Math.min(old, card) : card);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void readClasses(OntModel model, OwlOntology owlOntology) {
        owlOntology.setClasses(new HashMap());

        for (Iterator classes = model.listNamedClasses(); classes.hasNext();) {
            try {
                OntClass c = (OntClass) classes.next();
                // check removed: only named classes here
                // if (!c.isRestriction()) {
                if (c.getNameSpace().equals(ns)) {
                    OwlClass owlClass = new OwlClass(c.getNameSpace(), c.getLocalName());
                    owlClass.setParents(new ArrayList());
                    owlClass.setProperties(new HashMap());

                    owlOntology.getClasses().put(owlClass.getName(), owlClass);
                    System.out.println("found class: " + owlClass.getName());
                }
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    void readSuperClasses(OntModel model, OwlOntology owlOntology) {

        for (Iterator classes = model.listNamedClasses(); classes.hasNext();) {
            try {
                OntClass ontClass = (OntClass) classes.next();
                // check removed: only named classes here
                // if (!c.isRestriction()) {
                if (ontClass.getNameSpace().equals(ns)) {
                    OwlClass owlClass = (OwlClass) owlOntology.getClasses().get(ontClass.getLocalName());
                    if (owlClass != null) {

                        // Only first level parents
                        for (Iterator parents = ontClass.listSuperClasses(true); parents.hasNext();) {
                            try {
                                OntClass parent = (OntClass) parents.next();
                                if (! parent.isAnon() && parent.getNameSpace().equals(ns)) {

                                    OwlResource owlParent = new OwlResource(parent.getNameSpace(), parent.getLocalName());
                                    owlClass.getParents().add(owlParent);
                                    System.out.println("found parent: " + owlClass.getName()+ "<" + owlParent.getName());

                                } else if (parent.isRestriction()) {

                                    Restriction restr = parent.asRestriction();
                                    OntProperty property = (OntProperty) restr.getOnProperty();
                                    if (property != null) {
                                        OwlProperty owlProperty = (OwlProperty) owlClass.getProperties().get(property.getLocalName());
                                        if (owlProperty != null) {
                                            handleRestriction(restr, owlProperty);
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    void readProperties(OntModel model, OwlOntology owlOntology) {

        for (Iterator properties = model.listOntProperties(); properties.hasNext();) {
            OntProperty p = (OntProperty) properties.next();
            if (p.getNameSpace().equals(ns)) {
                OntResource domain = p.getDomain();
                if (domain != null && domain.canAs(OntClass.class)) {
                    try {
                        OntClass domainClass = domain.asClass();

                        if (domainClass.canAs(UnionClass.class)) {
                            UnionClass union = domainClass.asUnionClass();
                            for (Iterator classes = union.listOperands(); classes.hasNext();) {
                                handleClassProperty(p, (OntClass) classes.next(), owlOntology);
                            }
                        } else {
                            handleClassProperty(p, domainClass, owlOntology);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void handleClassProperty(OntProperty ontProperty, OntClass ontClass, OwlOntology owlOntology) {

        if (ontProperty.getNameSpace().equals(ns) && ontClass.getNameSpace().equals(ns)) {

            OwlClass owlClass = (OwlClass) owlOntology.getClasses().get(ontClass.getLocalName());

            String propertyLocalName = ontProperty.getLocalName();
            String propertyNameSpace = ontProperty.getNameSpace();
            if (leadingClassSeparator != null) {
                String toRemove = owlClass.getName() + leadingClassSeparator;
                if (propertyLocalName.startsWith(toRemove)) {
                    propertyLocalName = propertyLocalName.substring(toRemove.length());
                }
            }

            OwlProperty owlProperty = new OwlProperty(propertyNameSpace, propertyLocalName);
            owlProperty.setDomain(owlClass); // Domain

            OntResource range = (OntResource) ontProperty.getRange(); // Range
            if (range != null && range.getLocalName() != null) {
                owlProperty.setRange(new OwlResource(range.getNameSpace(), range.getLocalName()));
            }
            else {
                owlProperty.setRange(DEFAULT_RANGE);
            }

            // owlProperty.setDatatype(p.isDatatypeProperty());

            owlProperty.setMinCardinality(OwlProperty.DEFAULT_MINCARD);
            if (ontProperty.isFunctionalProperty()) {
                //owlProperty.setFunctional(true);
                owlProperty.setMaxCardinality(1);
            }
            else {
                //owlProperty.setFunctional(false);
                owlProperty.setMaxCardinality(OwlProperty.DEFAULT_MAXCARD);
            }

            owlClass.getProperties().put(owlProperty.getName(), owlProperty);
            System.out.println("found property: " + owlClass.getName()+ "." + owlProperty.getName());
        }
    }

    public OwlOntology read() throws IOException {
        if (model == null) model = readModel();

        Iterator ontologies = model.listOntologies();
        Ontology ontology = ontologies.hasNext() ? (Ontology)ontologies.next() : null;

        // guess ontology name from the label of the ontology owl-resource
        String labelName = (ontology != null) ? ontology.getLabel(null) : null;

        logger.fine("name (conf): " + name);
        logger.fine("name (label): " + labelName);

        if (labelName != null) name = labelName;
        logger.info("name: " + name);

        // guess ontology uri from the ontology owl-resource
        String ontoURI = (ontology != null) ? ontology.getURI() : null;

        // guess ontology uri from the default namespace
        String prefixURI = model.getNsPrefixURI("");

        logger.fine("uri (conf): " + uri);
        logger.fine("uri (ontology): " + ontoURI);
        logger.fine("uri (prefix): " + prefixURI);

        // set ontology uri from the uri of the ontology owl-resource
        if (ontoURI != null) uri = ontoURI;
        else if (prefixURI != null) uri = prefixURI;
        if (uri == null) uri = "";
        if (uri.endsWith("#")) uri = uri.substring(0, uri.length() - 1);
        logger.info("uri: " + uri);

        ns = uri + "#";

        OwlOntology result = new OwlOntology(ns, name);
        readClasses(model, result);
        readProperties(model, result);
        readSuperClasses(model, result);
        logger.info("onto:\n" + OwlHelper.toString(result));
        return result;
    }
}
