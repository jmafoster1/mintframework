package mint.inference.gp;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import mint.inference.evo.Chromosome;
import mint.inference.gp.tree.Datatype;
import mint.inference.gp.tree.Node;
import mint.inference.gp.tree.NonTerminal;
import mint.inference.gp.tree.nonterminals.strings.AssignmentOperator;
import mint.inference.gp.tree.terminals.VariableTerminal;
import mint.tracedata.types.VariableAssignment;

/**
 *
 * A random expression generator - generating random tree-shaped expressions for
 * evaluation in a GP context.
 *
 * Created by neilwalkinshaw on 04/03/15.
 */
public class Generator {

	protected Random rand;
	protected List<NonTerminal<?>> dFunctions;
	protected List<VariableTerminal<?>> dTerminals;
	protected List<NonTerminal<?>> iFunctions;
	protected List<VariableTerminal<?>> iTerminals;
	protected List<NonTerminal<?>> sFunctions;
	protected List<VariableTerminal<?>> sTerminals;
	protected List<NonTerminal<?>> bFunctions;
	protected List<VariableTerminal<?>> bTerminals;
	protected AssignmentOperator aop;
	protected int listLength = 0;

	public void setListLength(int length) {
		listLength = length;
	}

	public Generator(Random r) {
		rand = r;
		dFunctions = new ArrayList<NonTerminal<?>>();
		iFunctions = new ArrayList<NonTerminal<?>>();
		dTerminals = new ArrayList<VariableTerminal<?>>();
		iTerminals = new ArrayList<VariableTerminal<?>>();
		sTerminals = new ArrayList<VariableTerminal<?>>();
		sFunctions = new ArrayList<NonTerminal<?>>();
		bTerminals = new ArrayList<VariableTerminal<?>>();
		bFunctions = new ArrayList<NonTerminal<?>>();
		aop = new AssignmentOperator();
	}

	public Random getRandom() {
		return rand;
	}

	public void setDoubleFunctions(List<NonTerminal<?>> doubleFunctions) {
		dFunctions = doubleFunctions;
	}

	public void setIntegerFunctions(List<NonTerminal<?>> intFunctions) {
		iFunctions = intFunctions;
	}

	public void setDoubleTerminals(List<VariableTerminal<?>> doubleTerms) {
		dTerminals = doubleTerms;
	}

	public void setIntegerTerminals(List<VariableTerminal<?>> intFunctions) {
		iTerminals = intFunctions;
	}

	public void setStringTerminals(List<VariableTerminal<?>> sTerms) {
		sTerminals = sTerms;
	}

	public void setStringFunctions(List<NonTerminal<?>> sFunctions) {
		this.sFunctions = sFunctions;
	}

	public void setBooleanTerminals(List<VariableTerminal<?>> bTerms) {
		bTerminals = bTerms;
	}

	public void setBooleanFunctions(List<NonTerminal<?>> bFunctions) {
		this.bFunctions = bFunctions;
	}

	/*
	 * public Chromosome generateRandomExpression(int maxD, List<NonTerminal<?>>
	 * nonTerms, List<VariableTerminal<?>> terms){ if(nonTerms.isEmpty()){ return
	 * selectRandomTerminal(terms); } if((maxD < 2 || rand.nextDouble() <
	 * threshold())&&!terms.isEmpty()){ return selectRandomTerminal(terms); } else
	 * return selectRandomNonTerminal(nonTerms, maxD); }
	 */

	public Node<?> generateRandomExpressionAux(int maxD, Datatype type) {
		List<NonTerminal<?>> nonTerms = nonTerms(type);
		List<VariableTerminal<?>> terms = terms(type);
		if (nonTerms.isEmpty() || maxD < 2) {
			return selectRandomTerminal(terms);
		} else {
			if (rand.nextDouble() > 0.7)
				return selectRandomTerminal(terms);

			NonTerminal<?> selected = nonTerms.get(rand.nextInt(nonTerms.size()));
			return selected.createInstance(this, maxD - 1);
		}
	}

	// We need this function to simplify the top-level expression. Doing it within
	// generateRandomExpressionAux simplifies the child expressions only
	public Node<?> generateRandomExpression(int maxD, Datatype type) {
		return generateRandomExpressionAux(maxD, type).simp();
	}

	public boolean populationContains(List<Chromosome> population, Chromosome c1) {
		return population.stream().anyMatch(c2 -> c1.sameSyntax(c2));
	}

	public List<Chromosome> generatePopulation(int size, int maxD, Datatype type) {
		List<Chromosome> population = new ArrayList<Chromosome>();
		for (int i = 0; i < size; i++) {
			Chromosome instance = generateRandomExpression(maxD + 1, type);
			// We want to make sure the initial population is filled with unique individuals
			// if we can. If there are no nonterminals then we can't do this.
			if (!nonTerms(type).isEmpty()) {
				while (populationContains(population, instance)) {
					instance = generateRandomExpression(maxD + 1, type);
				}
			}
			population.add(instance);
		}
		return population;
	}

	public List<Chromosome> generatePopulation(int size, int maxD, Datatype type, List<Chromosome> existing) {
		List<Chromosome> population = new ArrayList<Chromosome>();
		for (int i = 0; i < size; i++) {
			Chromosome instance = generateRandomExpression(maxD + 1, type);
			// We want to make sure the initial population is filled with unique individuals
			// if we can. If there are no nonterminals then we can't do this.
			if (!nonTerms(type).isEmpty()) {
				while (populationContains(population, instance) || populationContains(existing, instance)) {
					instance = generateRandomExpression(maxD + 1, type);
				}
			}
			population.add(instance);
		}
		return population;
	}

	public Node<? extends VariableAssignment<?>> selectRandomTerminal(List<VariableTerminal<?>> nodes) {
		int index = rand.nextInt(nodes.size());
		VariableTerminal<?> selected = nodes.get(index);

		return selected.copy();
	}

	public List<NonTerminal<?>> nonTerms(Datatype s) {
		switch (s) {
		case INTEGER:
			return this.iFunctions;
		case DOUBLE:
			return this.dFunctions;
		case BOOLEAN:
			return this.bFunctions;
		case STRING:
			return this.sFunctions;
		default:
			break;
		}
		throw new IllegalArgumentException("Invaild type " + s);
	}

	public List<VariableTerminal<?>> terms(Datatype s) {
		switch (s) {
		case INTEGER:
			return this.iTerminals;
		case DOUBLE:
			return this.dTerminals;
		case BOOLEAN:
			return this.bTerminals;
		case STRING:
			return this.sTerminals;
		default:
			break;
		}
		throw new IllegalArgumentException("Invaild type " + s);
	}

	public Node<?> generateRandomTerminal(Datatype type) {
		switch (type) {
		case BOOLEAN:
			return selectRandomTerminal(bTerminals);
		case STRING:
			return selectRandomTerminal(sTerminals);
		case INTEGER:
			return selectRandomTerminal(iTerminals);
		case DOUBLE:
			return selectRandomTerminal(dTerminals);
		default:
			break;
		}

		throw new IllegalArgumentException("Datatype must be one of BOOLEAN, STRING, INTEGER, or DOUBLE");
	}

	public Node<?> generateRandomNonTerminal(Datatype[] typeSignature) {
		Datatype returnType = typeSignature[typeSignature.length - 1];
		List<NonTerminal<?>> suitable;

		switch (returnType) {
		case BOOLEAN:
			suitable = bFunctions.stream().filter(f -> Datatype.typeChecks(f.typeSignature(), typeSignature))
					.collect(Collectors.toList());
			return suitable.get(rand.nextInt(suitable.size()));
		case STRING:
			suitable = sFunctions.stream().filter(f -> Datatype.typeChecks(f.typeSignature(), typeSignature))
					.collect(Collectors.toList());
			return suitable.get(rand.nextInt(suitable.size()));
		case INTEGER:
			suitable = iFunctions.stream().filter(f -> Datatype.typeChecks(f.typeSignature(), typeSignature))
					.collect(Collectors.toList());
			return suitable.get(rand.nextInt(suitable.size()));
		case DOUBLE:
			suitable = dFunctions.stream().filter(f -> Datatype.typeChecks(f.typeSignature(), typeSignature))
					.collect(Collectors.toList());
			return suitable.get(rand.nextInt(suitable.size()));
		default:
			break;
		}
		throw new IllegalArgumentException("Datatype must be one of BOOLEAN, STRING, INTEGER, or DOUBLE");
	}

}
