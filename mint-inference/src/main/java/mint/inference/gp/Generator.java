package mint.inference.gp;

import java.util.ArrayList;
import java.util.Arrays;
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
	protected List<NonTerminal<?>> functions;
	protected List<VariableTerminal<?>> terminals;
	protected AssignmentOperator aop;
	protected int listLength = 0;
	private final int TIMEOUT = 5;

	public void setListLength(int length) {
		listLength = length;
	}

	public Generator(Random r) {
		rand = r;
		functions = new ArrayList<NonTerminal<?>>();
		terminals = new ArrayList<VariableTerminal<?>>();
		aop = new AssignmentOperator();
	}

	public Random getRandom() {
		return rand;
	}

	public void setFunctions(List<NonTerminal<?>> functions) {
		this.functions = functions;
	}

	public void addFunctions(List<NonTerminal<?>> functions) {
		this.functions.addAll(functions);
	}

	public void add(NonTerminal<?> functions) {
		this.functions.add(functions);
	}

	public void setTerminals(List<VariableTerminal<?>> terminals) {
		this.terminals = terminals;
	}

	public void addTerminals(List<VariableTerminal<?>> terminals) {
		this.terminals.addAll(terminals);
	}

	public void add(VariableTerminal<?> terminals) {
		this.terminals.add(terminals);
	}

	public List<VariableTerminal<?>> getTerminals() {
		return this.terminals;
	}

	public List<NonTerminal<?>> getNonTerminals() {
		return this.functions;
	}

	public Node<?> generateRandomExpressionAux(int maxD, Datatype type) {
		List<NonTerminal<?>> nonTerms = nonTerminals(type);
		List<VariableTerminal<?>> terms = terminals(type);
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
			if (!nonTerminals(type).isEmpty()) {
				int iteration = 0;
				while (populationContains(population, instance) && iteration < TIMEOUT) {
					iteration++;
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
			if (!nonTerminals(type).isEmpty()) {
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

	public List<NonTerminal<?>> nonTerminals(Datatype s) {
		return this.functions.stream().filter(x -> x.getReturnType() == s).collect(Collectors.toList());
	}

	public List<VariableTerminal<?>> terminals(Datatype s) {
		return this.terminals.stream().filter(x -> x.getReturnType() == s).collect(Collectors.toList());
	}

	public Node<?> generateRandomTerminal(Datatype type) {
		return selectRandomTerminal(terminals(type));
	}

	public Node<?> generateRandomNonTerminal(Datatype[] typeSignature) {
		List<NonTerminal<?>> suitable = functions.stream()
				.filter(f -> Datatype.typeChecks(f.typeSignature(), typeSignature)).collect(Collectors.toList());
		if (suitable.isEmpty())
			throw new IllegalStateException(
					"No suitable nonterminials for type signature " + Arrays.toString(typeSignature));

		return suitable.get(rand.nextInt(suitable.size()));
	}

	public Node<?> generateRandomNonTerminal(Datatype typeSignature) {
		List<NonTerminal<?>> suitable = functions.stream().filter(f -> f.getReturnType() == typeSignature)
				.collect(Collectors.toList());
		if (suitable.isEmpty())
			throw new IllegalStateException("No suitable nonterminials for type signature " + typeSignature);

		return suitable.get(rand.nextInt(suitable.size()));
	}
}
