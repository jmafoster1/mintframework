package mint.inference.gp.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;

import mint.inference.evo.Chromosome;
import mint.inference.gp.Generator;
import mint.inference.gp.tree.terminals.VariableTerminal;
import mint.tracedata.types.VariableAssignment;

/**
 * Created by neilwalkinshaw on 03/03/15.
 */
public abstract class NonTerminal<T extends VariableAssignment<?>> extends Node<T> {

	protected List<Node<?>> children;

	private final int MAX_MUTATIONS = 3;

	public void setChildren(List<Node<?>> newChildren) {
		this.children = newChildren;
	}

	public NonTerminal() {
		super();
		children = new ArrayList<Node<?>>();
	}

	protected void visitChildren(NodeVisitor visitor) throws InterruptedException {
		Stack<Node<?>> childrenStack = new Stack<Node<?>>();
		for (Node<?> child : children) {
			childrenStack.push(child);

		}
		while (!childrenStack.isEmpty()) {
			childrenStack.pop().accept(visitor);
		}
	}

	/**
	 * Get the first value from vals (there must be one) and return as a terminal.
	 * Used only for simplification
	 * 
	 * @return
	 */
	protected abstract Terminal<T> getTermFromVals();

	@Override
	public List<Node<?>> getChildren() {
		return children;
	}

	@Override
	public void addChild(Node<?> child) {
		children.add(child);
	}

	@Override
	public List<Node<?>> getAllNodesAsList() {
		List<Node<?>> nodes = new ArrayList<Node<?>>();
		nodes.add(this);

		for (Node<?> child : this.children) {
			nodes.add(child);
			nodes.addAll(child.getAllNodesAsList());
		}
		return nodes;
	}

	@Override
	public Node<?> mutate(Generator g, int depth) {
		// System.out.println("============================================");
		// System.out.println("Mutating " + this);
		int mutations = 0;
		Node<?> newNode = this;
		boolean mutate = true;
		while (mutate && mutations < MAX_MUTATIONS) {
			mutate = g.getRandom().nextBoolean();
			// System.out.println("Iteration: " + mutations);
			mutations++;
			int choices = 6;
			switch (g.getRandom().nextInt(choices)) {
			case 0:
				// HVL SUB
				// System.out.println(" mutateByRandomChangeOfFunction " + this + " ");
				newNode = mutateByRandomChangeOfFunction(g);
				break;
			case 1:
				// HLV DEL
				// System.out.println(" mutateByDeletion " + this + " ");
				newNode = mutateByDeletion(g);
				break;
			case 2:
				// HVL INS
				// System.out.println(" mutateByGrowth " + this + " ");
				newNode = mutateByGrowth(g);
				break;
			case 3:
				// Reverse children if they have the same return type, e.g. (x - y) -> (y - x)
				// System.out.println(" reverseChildren " + this + " ");
				if (this.children.stream().map(child -> child.getReturnType()).distinct().limit(2).count() <= 1)
					Collections.reverse(this.children);
				break;
			case 4:
				// mutate by replacing the entire tree with a subtree
				// System.out.println(" Subtree " + this + " ");
				Node<?> subtree = this.getRandomNode(g);
				newNode = swap(this, subtree);
				break;
			case 5:
				// fuzz a terminal
				List<Node<?>> terms = this.getAllNodesAsList().stream().filter(x -> x instanceof VariableTerminal)
						.collect(Collectors.toList());
				VariableTerminal<?> term = (VariableTerminal<?>) terms.get(g.getRandom().nextInt(terms.size()));
				// System.out.println(" fuzzTerminal " + term + " in " + this + " ");
				term.getTerminal().fuzz();
				break;
			}
			// System.out.println(" " + newNode);
		}
		// System.out.println("============================================");
		return newNode;
	}

	private Node<?> mutateByRandomChangeOfFunction(Generator g) {
		if (!g.nonTerminals(this.getReturnType()).isEmpty()) {
			NonTerminal<?> newFun = (NonTerminal<?>) g.generateRandomNonTerminal(this, this.typeSignature());
			if (newFun == null)
				return this;
			newFun.setChildren(this.children);
			return newFun;
		} else {
			// System.out.println(" same");
			return this;
		}
	}

	private Node<?> mutateByDeletion(Generator g) {
		List<Node<?>> nodes = this.getAllNodesAsList().stream().filter(x -> x.getReturnType() == this.getReturnType())
				.collect(Collectors.toList());
		if (nodes.isEmpty())
			return this;

		Node<?> child = nodes.get(g.getRandom().nextInt(nodes.size()));
		return swap(this, child);
	}

	public abstract NonTerminal<T> createInstance(Generator g, int depth);

	/**
	 * String that returns a summary of the node and its children.
	 * 
	 * @return
	 */
	protected abstract String nodeString();

	protected String childrenString() {
		String retString = "";
		for (int i = 0; i < children.size(); i++) {
			if (i > 0)
				retString += " ";
			retString += children.get(i).toString();
		}
		return retString;
	}

	@Override
	public int size() {
		return 1 + childrenSizes();
	}

	private int childrenSizes() {
		int sizes = 0;
		for (Node<?> n : children) {
			sizes += n.size();
		}
		return sizes;
	}

	public Node<?> getChild(int x) {
		return children.get(x);
	}

	@Override
	public int numVarsInTree() {
		int vars = 0;
		for (Node<?> n : children) {
			vars += n.numVarsInTree();
		}
		return vars;
	}

	@Override
	public Set<VariableTerminal<?>> varsInTree() {
		Set<VariableTerminal<?>> vars = new HashSet<VariableTerminal<?>>();
		for (Node<?> child : this.getChildren()) {
			for (VariableTerminal<?> var : child.varsInTree()) {
				vars.add(var);
			}
		}
		return vars;
	}

	@Override
//	public String toString() {
//		return nodeString();
//	}

	public String toString() {
		if (opString() == "")
			return "(" + opString() + childrenString() + ")";
		else
			return "(" + opString() + " " + childrenString() + ")";
	}

	public abstract String opString();

	public void clearChildren() {
		children.clear();
	}

	@Override
	public Node<T> copy() {
		NonTerminal<T> copy = this.newInstance();
		for (Node<?> child : children) {
			if (child == this)
				throw new IllegalStateException("Child == this");
			copy.addChild(child.copy());
		}
		return copy;
	}

	@Override
	public abstract NonTerminal<T> newInstance();

	@Override
	@SuppressWarnings("unchecked")
	public boolean sameSyntax(Chromosome c) {
		if (this.getClass().equals(c.getClass())) {
			if (this.getChildren().size() == ((NonTerminal<T>) c).getChildren().size()) {
				for (int i = 0; i < this.getChildren().size(); i++) {
					if (!(this.getChild(i).sameSyntax(((NonTerminal<T>) c).getChild(i))))
						return false;
				}
				return true;
			}
		}
		return false;
	}

}
