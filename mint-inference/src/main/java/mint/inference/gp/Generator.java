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
	protected List<VariableTerminal<?>> vars;
	protected List<VariableTerminal<?>> consts;

	protected AssignmentOperator aop;
	protected int listLength = 0;
	private final int TIMEOUT = 5;

	public void setListLength(int length) {
		listLength = length;
	}

	public Generator(Random r) {
		rand = r;
		functions = new ArrayList<NonTerminal<?>>();
		vars = new ArrayList<VariableTerminal<?>>();
		consts = new ArrayList<VariableTerminal<?>>();
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
		vars = terminals.stream().filter(x -> !x.isConstant()).collect(Collectors.toList());
		consts = terminals.stream().filter(x -> x.isConstant()).collect(Collectors.toList());
	}

	public void addTerminals(List<VariableTerminal<?>> terminals) {
		vars.addAll(terminals.stream().filter(x -> !x.isConstant()).collect(Collectors.toList()));
		consts.addAll(terminals.stream().filter(x -> x.isConstant()).collect(Collectors.toList()));
	}

	public void add(VariableTerminal<?> terminal) {
		if (terminal.isConstant())
			consts.add(terminal);
		else
			vars.add(terminal);
	}

	public List<NonTerminal<?>> getNonTerminals() {
		return this.functions;
	}

	public Node<?> generateRandomExpressionAux(int maxD, Datatype type) {
		List<NonTerminal<?>> nonTerms = nonTerminals(type);
		if (nonTerms.isEmpty() || maxD < 2) {
			return generateRandomTerminal(type);
		} else {
			if (rand.nextDouble() > 0.7)
				return generateRandomTerminal(type);

			NonTerminal<?> selected = generateRandomNonTerminal(type);

			Node<?> s = selected.createInstance(this, maxD - 1);

			return s;
		}
	}

	// We need this function to simplify the top-level expression. Doing it within
	// generateRandomExpressionAux simplifies the child expressions only
	public Node<?> generateRandomExpression(int maxD, Datatype type) {
		return generateRandomExpressionAux(maxD, type);
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
				int iteration = 0;
				while ((populationContains(population, instance) || populationContains(existing, instance))
						&& iteration < TIMEOUT) {
					iteration++;
					instance = generateRandomExpression(maxD + 1, type);
				}
			}
			population.add(instance);
		}
		return population;
	}

	public List<NonTerminal<?>> nonTerminals(Datatype s) {
		return this.functions.stream().filter(x -> x.getReturnType() == s).collect(Collectors.toList());
	}

	public List<VariableTerminal<?>> terminals(List<VariableTerminal<?>> terminals, Datatype type) {
		return terminals.stream().filter(x -> x.getReturnType() == type).collect(Collectors.toList());
	}

	public Node<?> generateRandomNonTerminal(NonTerminal<?> avoid, Datatype[] typeSignature) {
		List<NonTerminal<?>> suitable = functions.stream()
				.filter(f -> f.opString() != avoid.opString() && Datatype.typeChecks(f.typeSignature(), typeSignature))
				.collect(Collectors.toList());
		if (suitable.isEmpty())
			return null;

		return suitable.get(rand.nextInt(suitable.size())).newInstance();
	}

	public NonTerminal<?> generateRandomNonTerminal(Datatype type) {
		List<NonTerminal<?>> suitable = nonTerminals(type);
		if (suitable.isEmpty())
			throw new IllegalStateException("No suitable nonterminials for type signature " + type);

		return suitable.get(rand.nextInt(suitable.size())).newInstance();
	}

	public VariableTerminal<?> generateRandomTerminal(Datatype type) {
		List<VariableTerminal<?>> suitableVars = terminals(vars, type);
		List<VariableTerminal<?>> suitableConsts = terminals(consts, type);
		VariableTerminal<?> term;
		if (rand.nextBoolean() && !suitableVars.isEmpty())
			term = suitableVars.get(rand.nextInt(suitableVars.size()));
		else
			term = suitableConsts.get(rand.nextInt(suitableConsts.size()));
		return term;
	}
}
