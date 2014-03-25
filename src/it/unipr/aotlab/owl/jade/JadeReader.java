package it.unipr.aotlab.owl.jade;

import it.unipr.aotlab.owl.core.*;
import it.unipr.aotlab.owl.io.*;

import jade.content.onto.*;
import jade.content.schema.*;
import jade.content.schema.facets.*;

import java.io.InputStream;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

/**
 * Reads an intermediate ontology from a JADE ontology.
 *
 * @author <a href="mailto:tomamic@ce.unipr.it">Michele Tomaiuolo</a> -
 *         <a href="http://aot.ce.unipr.it">University of Parma</a>
 */
public class JadeReader implements OwlReader {
    public static final String DATATYPES_NS = OwlResource.DATATYPES_NS;
    public static final OwlResource DEFAULT_RANGE = new OwlResource(DATATYPES_NS, "string");

    // keys to access properties
    public static final String INPUT = "jade-reader.input";
    public static final String ONTOLOGY = "jade-reader.ontology";
    public static final String IMPORTS = "jade-reader.imports";
    public static final String LANG = "jade-reader.lang";
    public static final String URI = "jade-reader.uri";

    static final String PREDICATE = "Predicate";
    static final String AGENT_ACTION = "AgentAction";
    static final String AID = "AID";
    static final String CONCEPT = "Concept";

    // default logger name
    public static final String LOGGER_NAME = "jade-reader";

    Logger logger = null;

    static final Map primitiveTypes = new HashMap();

    {
        primitiveTypes.put(BasicOntology.STRING, "string");
        primitiveTypes.put(BasicOntology.FLOAT, "float");
        primitiveTypes.put(BasicOntology.INTEGER, "integer");
        primitiveTypes.put(BasicOntology.BOOLEAN, "boolean");
        primitiveTypes.put(BasicOntology.DATE, "date");
        primitiveTypes.put(BasicOntology.BYTE_SEQUENCE, "string");
    }

    Ontology jadeOntology = null;
    String ns = null;

    public JadeReader(Ontology jadeOntology) {
        this.jadeOntology = jadeOntology;
    }

    public JadeReader() {
    }

    public void init(Properties conf) {
        init(conf, null);
    }

    public void init(Properties conf, InputStream input) {
        logger = OwlHelper.getLogger(LOGGER_NAME);

        if (jadeOntology != null) return;
        String jadeOntoCls = conf.getProperty(INPUT);
        if (jadeOntoCls != null) {
            try {
                System.out.println(jadeOntoCls);
                jadeOntology = (Ontology)Class.forName(jadeOntoCls).getMethod("getInstance").invoke(null);
            } catch (Exception e) { e.printStackTrace(); }
        }
        else {
            logger.warning("no input-stream set");
        }
    }

    public OwlResource convertRange(ObjectSchema range) {
        if (range == null) return DEFAULT_RANGE;

        String type = range.getTypeName();
        String converted = null;
        if (range instanceof PrimitiveSchema) converted = (String)primitiveTypes.get(type);

        if (converted != null) {
            return new OwlResource(DATATYPES_NS, converted);
        }
        else if (BasicOntology.AID.equals(type)) {
            return new OwlResource(ns, AID);
        }
        else if (range instanceof ConceptSchema) {
            return new OwlResource(ns, type);
        }
        else return DEFAULT_RANGE;
    }

    public static Ontology[] getBase(Object object) {
        return (Ontology[])spy(Ontology.class, "base", object);
    }

    public static Hashtable getElements(Object object) {
        return (Hashtable)spy(Ontology.class, "elements", object);
    }

    public static Vector getSlotNames(Object object) {
        return (Vector)spy("jade.content.schema.ObjectSchemaImpl", "slotNames", object);
    }

    public static Vector getSuperSchemas(Object object) {
        return (Vector)spy("jade.content.schema.ObjectSchemaImpl", "superSchemas", object);
    }

    public static int getMinCardinality(Object object) {
        return spyInt(CardinalityFacet.class, "cardMin", object);
    }

    public static int getMaxCardinality(Object object) {
        return spyInt(CardinalityFacet.class, "cardMax", object);
    }

    public static Object spy(String cls, String fieldName, Object object) {
        try {
            return spy(Class.forName(cls), fieldName, object);
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    public static Object spy(Class cls, String fieldName, Object object) {
        try {
            java.lang.reflect.Field field = cls.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(object);
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    public static int spyInt(Class cls, String fieldName, Object object) {
        try {
            java.lang.reflect.Field field = cls.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.getInt(object);
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }

    public static List listAllSchemas(Ontology jadeOntology, List list) {
        Ontology[] base = getBase(jadeOntology);
        if (base != null) for (int i = 0; i < base.length; i++) {
            if (! base[i].getClass().getName().startsWith("jade.content.onto."))
                list = listAllSchemas(base[i], list);
        }

        Hashtable elements = getElements(jadeOntology);
        if (elements != null) list.addAll(elements.values());
        return list;
    }

    public static OwlOntology addFipaClasses(OwlOntology o) {
        if (o.getClasses() == null) o.setClasses(new HashMap());

        OwlClass conceptClass = new OwlClass(CONCEPT);
        conceptClass.setParents(new ArrayList());
        conceptClass.setProperties(new HashMap());
        o.getClasses().put(conceptClass.getName(), conceptClass);

        OwlClass agentActionClass = new OwlClass(AGENT_ACTION);
        agentActionClass.setParents(new ArrayList());
        agentActionClass.setProperties(new HashMap());
        agentActionClass.getParents().add(new OwlResource(CONCEPT));
        o.getClasses().put(agentActionClass.getName(), agentActionClass);

        OwlClass aidClass = new OwlClass(AID);
        aidClass.setParents(new ArrayList());
        aidClass.setProperties(new HashMap());
        aidClass.getParents().add(new OwlResource(CONCEPT));
        o.getClasses().put(aidClass.getName(), aidClass);

        OwlClass predicateClass = new OwlClass(PREDICATE);
        predicateClass.setParents(new ArrayList());
        predicateClass.setProperties(new HashMap());
        o.getClasses().put(predicateClass.getName(), predicateClass);

        return o;
    }

    public OwlOntology read() {
        OwlOntology o = addFipaClasses(new OwlOntology(jadeOntology.getName()));

        List schemas = listAllSchemas(jadeOntology, new ArrayList());
        for (int i = 0; i < schemas.size(); i++) {
            ObjectSchema jadeSchema = (ObjectSchema)schemas.get(i);

            OwlClass c = new OwlClass(jadeSchema.getTypeName());
            c.setParents(new ArrayList());
            c.setProperties(new HashMap());

            Vector superSchemas = getSuperSchemas(jadeSchema);
            if (superSchemas == null || superSchemas.size() == 0) {
                if (jadeSchema instanceof PredicateSchema) c.getParents().add(new OwlResource(PREDICATE));
                else if (jadeSchema instanceof AgentActionSchema) c.getParents().add(new OwlResource(AGENT_ACTION));
                else if (jadeSchema instanceof ConceptSchema) c.getParents().add(new OwlResource(CONCEPT));
            }
            else for (int j = 0; j < superSchemas.size(); j++) {
                ObjectSchema superSchema = (ObjectSchema)superSchemas.get(j);
                String parent = superSchema.getTypeName();
                c.getParents().add(new OwlResource(parent));
            }

            Vector slotNames = getSlotNames(jadeSchema);
            if (slotNames != null) for (int j = 0; j < slotNames.size(); j++) {
                String slotName = slotNames.get(j).toString();

                OwlProperty p = new OwlProperty(slotName);
                p.setDomain(c);
                ObjectSchema range = null;
                try {
                    range = jadeSchema.getSchema(slotName);
                } catch (OntologyException e) { e.printStackTrace(); }
                p.setRange(convertRange(range));

                p.setMinCardinality(0);
                p.setMaxCardinality(1);
                try { if (jadeSchema.isMandatory(slotName)) p.setMinCardinality(1); } catch (OntologyException e) { e.printStackTrace(); }
                if (range instanceof AggregateSchema) p.setMaxCardinality(OwlProperty.UNLIMITED);
                //p.setFunctional(! "Aggregate".equals(p.getRange()));

                Facet[] facets = jadeSchema.getFacets(slotName);
                if (facets != null) for (int k = 0; k < facets.length; k++) {
                    if (facets[k] instanceof TypedAggregateFacet) {
                        TypedAggregateFacet facet = (TypedAggregateFacet)facets[k];
                        p.setRange(convertRange(facet.getType()));
                    }
                    if (facets[k] instanceof CardinalityFacet) {
                        CardinalityFacet facet = (CardinalityFacet)facets[k];
                        int minCard = getMinCardinality(facet);
                        int maxCard = getMaxCardinality(facet);
                        p.setMinCardinality(minCard);
                        p.setMaxCardinality(maxCard);
                    }
                }
                c.getProperties().put(p.getName(), p);
            }

            o.getClasses().put(c.getName(), c);
        }

        return o;
    }

    public static void main(String[] args) {
        try {
            Ontology jadeOntology = (Ontology)Class.forName(args[0]).getMethod("getInstance").invoke(null);
            OwlOntology owlOntology = new JadeReader(jadeOntology).read();
            System.out.println(OwlHelper.toString(owlOntology));

            java.io.FileOutputStream out = new java.io.FileOutputStream(args[1]);
            new it.unipr.aotlab.owl.jena.OwlFileWriter(it.unipr.aotlab.owl.Main.loadConf("default.conf", null), out).write(owlOntology);
            out.flush();
            out.close();
        } catch (Exception e) { e.printStackTrace(); }
    }
}
