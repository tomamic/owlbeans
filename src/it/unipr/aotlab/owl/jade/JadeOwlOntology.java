package it.unipr.aotlab.owl.jade;

import it.unipr.aotlab.owl.core.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import jade.content.onto.BasicOntology;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.schema.ObjectSchema;
import jade.content.schema.TermSchema;
import jade.content.schema.PredicateSchema;
import jade.content.schema.ConceptSchema;
import jade.content.schema.AgentActionSchema;

/**
 * A JADE ontology which can be instantiated and populated with classes
 * and properties described in an intermediate ontology.
 *
 * @author <a href="mailto:tomamic@ce.unipr.it">Michele Tomaiuolo</a> -
 *         <a href="http://aot.ce.unipr.it">University of Parma</a>
 */
// TODO: Test
public class JadeOwlOntology extends Ontology {

	// TODO: handle BasicOntology.BYTE_SEQUENCE
	// from BasicOntology
	/*
	public static final String         STRING = "BO_String";
  public static final String         FLOAT = "BO_Float";
  public static final String         INTEGER = "BO_Integer";
  public static final String         BOOLEAN = "BO_Boolean";
  public static final String         DATE = "BO_Date";
  public static final String         BYTE_SEQUENCE = "BO_Byte-sequence";
  */

	// default parent for orphan classes
	public static final String DEFAULT_PARENT = "Concept";

	// map to convert primitive types and basic jade classes
	static final Map conv = new HashMap();

	{
		conv.put("boolean", BasicOntology.BOOLEAN);
		conv.put("float", BasicOntology.FLOAT);
		conv.put("decimal", BasicOntology.FLOAT);
		conv.put("double", BasicOntology.FLOAT);
		conv.put("int", BasicOntology.INTEGER);
		conv.put("positiveInteger", BasicOntology.INTEGER);
		conv.put("negativeInteger", BasicOntology.INTEGER);
		conv.put("nonPositiveInteger", BasicOntology.INTEGER);
		conv.put("nonNegativeInteger", BasicOntology.INTEGER);
		conv.put("long", BasicOntology.INTEGER);
		conv.put("integer", BasicOntology.INTEGER);
		conv.put("short", BasicOntology.INTEGER);
		conv.put("byte", BasicOntology.INTEGER);
		conv.put("unsignedLong", BasicOntology.INTEGER);
		conv.put("unsignedInteger", BasicOntology.INTEGER);
		conv.put("unsignedShort", BasicOntology.INTEGER);
		conv.put("unsignedByte", BasicOntology.INTEGER);
		conv.put("string", BasicOntology.STRING);
		conv.put("anyURI", BasicOntology.STRING);
		conv.put("normalizedString", BasicOntology.STRING);
		conv.put("token", BasicOntology.STRING);
		conv.put("language", BasicOntology.STRING);
		conv.put("date", BasicOntology.DATE);
		conv.put("dateTime", BasicOntology.DATE);
		conv.put("duration", BasicOntology.DATE);
		conv.put("time", BasicOntology.DATE);

		conv.put("AID", BasicOntology.AID);
		conv.put("AgentIdentifier", BasicOntology.AID);
		conv.put("AgentAction", "");
		conv.put("Concept", "");
		conv.put("Predicate", "");
	}

 	public JadeOwlOntology(OwlOntology ontology) {
		super(ontology.getName(), BasicOntology.getInstance());

		OwlHelper helper = new OwlHelper(conv, new OwlResource(DEFAULT_PARENT));

		for (Iterator i = ontology.getClasses().values().iterator(); i.hasNext();) {
			OwlClass cls = (OwlClass)i.next();
			addSchema(cls, ontology, helper);
		}
	}

	public void addSlot(ConceptSchema schema, String slot, TermSchema type, int minCard, int maxCard) {
		int optionality = (minCard > 0) ? ObjectSchema.MANDATORY : ObjectSchema.OPTIONAL;
		if (maxCard == 1) schema.add(slot, type, optionality);
		else schema.add(slot, type, minCard, maxCard);
	}

	public void addSlot(PredicateSchema schema, String slot, TermSchema type, int minCard, int maxCard) {
		int optionality = (minCard > 0) ? ObjectSchema.MANDATORY : ObjectSchema.OPTIONAL;
		if (maxCard == 1) schema.add(slot, type, optionality);
		else schema.add(slot, type, minCard, maxCard);
	}

	public void addSlot(ObjectSchema schema, OwlProperty prop, OwlHelper h) throws OntologyException {
		String name = prop.getName();
		int minCard = prop.getMinCardinality();
		int maxCard = prop.getMaxCardinality();
		String rangeName = h.convert(prop.getRange().getName());
		TermSchema range = (TermSchema)getSchema(rangeName);
		if (schema instanceof PredicateSchema) {
			addSlot((PredicateSchema)schema, name, range, minCard, maxCard);
		}
		else {
			addSlot((ConceptSchema)schema, name, range, minCard, maxCard);
		}
	}

	public void addSuperSchema(ObjectSchema schema, OwlResource parent, OwlHelper h) throws OntologyException {
		if (! h.isBasicClass(parent)) {
			if (schema instanceof PredicateSchema) {
				((PredicateSchema)schema).addSuperSchema((PredicateSchema)getSchema(parent.getName()));
			}
			else {
				((ConceptSchema)schema).addSuperSchema((ConceptSchema)getSchema(parent.getName()));
			}
		}
	}

	void addSchema(OwlClass c, OwlOntology o, OwlHelper h) {
		try {
			if (h.isBasicClass(c)) return;

			String type = h.getBasicClass(c, o).getClsName();
			ObjectSchema schema = null;
			if ("Predicate".equals(type)) schema = new PredicateSchema(c.getName());
			else if ("AgentAction".equals(type)) schema = new AgentActionSchema(c.getName());
			else schema = new ConceptSchema(c.getName());
			add(schema);

			for (Iterator j = c.getProperties().values().iterator(); j.hasNext();) {
				OwlProperty prop = (OwlProperty)j.next();
				addSlot(schema, prop, h);
			}

			for (Iterator j = c.getParents().iterator(); j.hasNext();) {
				OwlResource parent = (OwlResource)j.next();
				addSuperSchema(schema, parent, h);
			}
		} catch (OntologyException e) { e.printStackTrace(); }
	}
}
