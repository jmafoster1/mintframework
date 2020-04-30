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
		this.children = new ArrayList<Node<?>>();
	}

	@Override
	public void reset() {
		for (Node<?> child : getChildren()) {
			child.reset();
		}
	}

	protected void visitChildren(NodeVisitor visitor) throws InterruptedException {
		Stack<Node<?>> childrenStack = new Stack<Node<?>>();
		for (Node<?> child : this.children) {
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
		return this.children;
	}

	@Override
	public void addChild(Node<?> child) {
		this.children.add(child);
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
		// System.out.println("Mutating " + node);
		int mutations = 0;
		Node<?> newNode = this;
		boolean mutate = true;
		while (mutate && mutations < MAX_MUTATIONS) {
			mutate = g.getRandom().nextBoolean();
			// System.out.println("Iteration: " + mutations);
			mutations++;
			int choices = 6;
			int op = g.getRandom().nextInt(choices);
			switch (op) {
			case 0:
				// HVL SUB
				// System.out.println(" mutateByRandomChangeOfFunction " + node + " ");
				newNode = mutateByRandomChangeOfFunction(newNode, g);
				break;
			case 1:
				// HLV DEL
				// System.out.println(" mutateByDeletion " + node + " ");
				newNode = mutateByDeletion(newNode, g);
				break;
			case 2:
				// HVL INS
				// System.out.println(" mutateByGrowth " + node + " ");
				newNode = mutateByGrowth(newNode, g);
				break;
			case 3:
				// Reverse this.children if they have the same return type, e.g. (x - y) -> (y -
				// x)
				// System.out.println(" reverseChildren " + node + " ");
				if (newNode.getChildren().stream().map(child -> child.getReturnType()).distinct().limit(2).count() <= 1)
					Collections.reverse(newNode.getChildren());
				break;
			case 4:
				// mutate by replacing a random node with a terminal
				Node<?> node = newNode.getRandomNode(g);
				newNode = swap(node, g.generateRandomTerminal(node.getReturnType()));
				break;
			case 5:
				// fuzz a terminal
				List<Node<?>> terms = newNode.getAllNodesAsList().stream().filter(x -> x instanceof VariableTerminal)
						.collect(Collectors.toList());
				VariableTerminal<?> term = (VariableTerminal<?>) terms.get(g.getRandom().nextInt(terms.size()));
				// System.out.println(" fuzzTerminal " + term + " in " + node + " ");
				term.getTerminal().fuzz();
				break;
			}
		}
		// System.out.println("============================================");
		return newNode;
	}

	private Node<?> mutateByRandomChangeOfFunction(Node<?> node, Generator g) {
		List<Node<?>> mutationPoints = node.getAllNodesAsList().stream().filter(x -> x instanceof NonTerminal<?>)
				.collect(Collectors.toList());

		if (!g.nonTerminals(node.getReturnType()).isEmpty() && !mutationPoints.isEmpty()) {
			NonTerminal<?> mutationPoint = (NonTerminal<?>) mutationPoints
					.get(g.getRandom().nextInt(mutationPoints.size()));

			NonTerminal<?> newFun = (NonTerminal<?>) g.generateRandomNonTerminal(mutationPoint,
					mutationPoint.typeSignature());
			if (newFun == null)
				return node;
			newFun.setChildren(mutationPoint.getChildren());

			return newFun;
		} else {
			return node;
		}
	}

	private Node<?> mutateByDeletion(Node<?> node, Generator g) {
		List<Node<?>> nodes = node.getAllNodesAsList().stream().filter(x -> x.getReturnType() == node.getReturnType())
				.collect(Collectors.toList());
		if (nodes.isEmpty())
			return node;

		Node<?> child = nodes.get(g.getRandom().nextInt(nodes.size()));

		return child;
	}

	public abstract NonTerminal<T> createInstance(Generator g, int depth);

	/**
	 * String that returns a summary of the node and its this.children.
	 * 
	 * @return
	 */
	protected abstract String nodeString();

	protected String childrenString() {
		String retString = "";
		for (int i = 0; i < this.children.size(); i++) {
			if (i > 0)
				retString += " ";
			retString += this.children.get(i).toString();
		}
		return retString;
	}

	@Override
	public int size() {
		return 1 + childrenSizes();
	}

	private int childrenSizes() {
		int sizes = 0;
		for (Node<?> n : this.children) {
			sizes += n.size();
		}
		return sizes;
	}

	public Node<?> getChild(int x) {
		return this.children.get(x);
	}

	@Override
	public Set<VariableTerminal<?>> varsInTree() {
		Set<VariableTerminal<?>> vars = new HashSet<VariableTerminal<?>>();
		for (Node<?> child : this.children) {
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
			return "(" + opString() + this.childrenString() + ")";
		else
			return "(" + opString() + " " + this.childrenString() + ")";
	}

	public abstract String opString();

	public void clearChildren() {
		this.children.clear();
	}

	@Override
	public Node<T> copy() {
		NonTerminal<T> copy = newInstance();
		for (Node<?> child : this.children) {
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
		if (getClass().equals(c.getClass())) {
			if (getChildren().size() == ((NonTerminal<T>) c).getChildren().size()) {
				for (int i = 0; i < getChildren().size(); i++) {
					if (!(getChild(i).sameSyntax(((NonTerminal<T>) c).getChild(i))))
						return false;
				}
				return true;
			}
		}
		return false;
	}

}
