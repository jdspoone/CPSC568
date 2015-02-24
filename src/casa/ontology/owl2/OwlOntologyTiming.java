/**
 * <p>Title: CASA Agent Infrastructure</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary. 
 * Permission to use, copy, modify, distribute and sell this software and its
 * documentation for any purpose is hereby granted without fee, provided that
 * the above copyright notice appear in all copies and that both that copyright
 * notice and this permission notice appear in supporting documentation.  
 * The  Knowledge Science Group makes no representations about the suitability
 * of  this software for any purpose.  It is provided "as is" without express
 * or implied warranty.</p>
 * <p>Company: Knowledge Science Group, University of Calgary</p>
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 * @version 0.9
 */
package casa.ontology.owl2;

import casa.TransientAgent;
import casa.jade.CasaKB;
import casa.ui.BufferedAgentUI;

import java.text.ParseException;
import java.util.Vector;

/**
 * @author <a href="http://pages.cpsc.ucalgary.ca/~kremer/">Rob Kremer</a>
 *
 */
public class OwlOntologyTiming {
	
	static TransientAgent agent;
	static CasaKB KB ;
	static enum predEnum {
		hasskin("hasskin", -1),
		hasfur("hasfur", 0),
		hashair("hashair", 1),
		hastailhairs("hastailhairs", 2);
		private String name;
		private int code;
		private predEnum(String name, int code) {this.name = name; this.code = code;}
		@Override public String toString() {return name;}
		public int getCode() {return code;}
	}
	private predEnum preds;
	static enum typeEnum {
		animal("animal", -1),
		mammal("mammal", 0),
		dog("dog", 1),
		goldenretriver("goldenretriver", 2);
		private String name;
		private int code;
		private typeEnum(String name, int code) {this.name = name; this.code = code;}
		@Override public String toString() {return name;}
		public int getCode() {return code;}
	}
	private typeEnum types;
	
	static final String separator = "\t";
	static final String quote = "\"";
	static final int n = 10;
	
	static class TableRow {
		String predicate;
		String code;
		boolean result;
		long[] time;
		TableRow(String pred, String code, boolean result, long... time) {
			predicate = pred;
			this.code = code;
			this.result = result;
			this.time = time;
		}
		@Override
		public String toString() {
			StringBuilder b = new StringBuilder("="+quote+predicate+quote+separator+"="+quote+code+quote+separator+result);
			for (long t: time) {
				b.append(separator).append(t);
			}
			return b.toString();
		}
	}
	private static Vector<TableRow> table = new Vector<TableRow>() {
		@Override
		public String toString() {
			StringBuilder b = new StringBuilder();
			for (TableRow line: table) {
				b.append(line.toString()).append("\n");
			}
			return b.toString();
		}
	};


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		BufferedAgentUI ui = new BufferedAgentUI();
		agent =  casa.CASA.startAgent(ui);
		while(!agent.isInitialized())
		{
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		KB=agent.getKnowledgeBase();
		OWLOntology ontology1= (OWLOntology) ((OWLOntology)agent.getOntology()).getOntology("Myontology");
		agent.setOntology(ontology1);

		try {
			agent.assert_("(hasfur)");
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			agent.assert_("(hasfur mammal)");
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			agent.assert_("(hasfur mammal mammal)");
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for (predEnum i: predEnum.values()) {
			String exp = "("+i+")";
			String code = ""+i.getCode();
			record(exp,code);
		}
		for (predEnum i: predEnum.values()) {
			for (typeEnum j: typeEnum.values()) {
				String exp = "("+i+" "+j+")";
				String code = ""+i.getCode()+j.getCode();
				record(exp,code);
			}
		}
		for (predEnum i: predEnum.values()) {
			for (typeEnum j: typeEnum.values()) {
				for (typeEnum k: typeEnum.values()) {
					String exp = "("+i+" "+j+" "+k+")";
					String code = ""+i.getCode()+j.getCode()+k.getCode();
					record(exp,code);
				}
			}
		}
		System.out.println(table);
		
		agent.exit();
	}
	
	protected static void record(String exp, String code) {
		boolean result;
		try {
			result = agent.querywithFilter(exp, agent); //dry run
			long[] time = new long[n];
			for (int i=0; i<n; i++) {
				long start = System.nanoTime();
				agent.querywithFilter(exp, agent);
				long done = System.nanoTime();
				time[i] = (done-start)/1000;
			}
			table.add(new TableRow(exp, code, result, time));
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	

}
