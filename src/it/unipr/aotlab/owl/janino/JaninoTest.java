package it.unipr.aotlab.owl.janino;

import it.unipr.aotlab.owl.Main;
import it.unipr.aotlab.owl.core.*;
import it.unipr.aotlab.owl.velocity.VelocityFormatter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;

import net.janino.Java;
import net.janino.Parser;
import net.janino.Scanner;
import net.janino.ScriptEvaluator;
import net.janino.SimpleCompiler;

/**
 * Just a first test for the integration with Janino.
 *
 * @author <a href="mailto:tomamic@ce.unipr.it">Michele Tomaiuolo</a> -
 *         <a href="http://aot.ce.unipr.it">University of Parma</a>
 */
public class JaninoTest extends Main {

	public JaninoTest(String[] args) {
		super(args);
		try {
			// getConf().remove(Main.OUTPUT);
			getConf().setProperty(VelocityFormatter.PATTERNS, "conf/jade-janino.vm");

			OwlOntology onto = readOntology();
			OutputStream stream = new ByteArrayOutputStream();
			writeOntology(onto, stream);
			String source = stream.toString();

			// 1st step: obtain class loader for ontology classes
			ClassLoader cl = getClassLoader(source, null);

			// 2nd step: manipulate ontology concepts in a script
			ScriptEvaluator eval = new ScriptEvaluator(
				getScanner("test.Concetto1 c1 = new test.Concetto1Impl(); c1.setFp(param); return c1.getFp().toUpperCase();"),
				String.class, // Result type
				new String[] {"param"}, // Parameter names
				new Class[] {String.class}, // Parameter types
				new Class[] {}, // Exception types
				cl);
			System.out.println(eval.evaluate(new Object[] {":-p"}));
		} catch (Exception e) { e.printStackTrace(); }
	}

	public static Scanner getScanner(String source) throws Scanner.ScanException, IOException {
		return new Scanner(null, new StringReader(source));
	}

	public static ClassLoader getClassLoader(String source, ClassLoader cl) throws Scanner.ScanException, Parser.ParseException, Java.CompileException, IOException {
		SimpleCompiler sc = new SimpleCompiler(getScanner(source), cl);
		return sc.getClassLoader();
	}

	public static void main(String[] args) {
		JaninoTest t = new JaninoTest(args);
	}
}