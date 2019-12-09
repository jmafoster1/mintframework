package mint.inference.gp.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import mint.inference.evo.Chromosome;
import mint.inference.gp.Generator;
import mint.inference.gp.tree.terminals.VariableTerminal;
import mint.tracedata.types.VariableAssignment;

/**
 * Created by neilwalkinshaw on 03/03/15.
 */
public abstract class NonTerminal<T extends VariableAssignment<?>> extends Node<T> {

	protected List<Node<?>> children;

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

	public void addChild(Node<?> child) {
		children.add(child);
		child.setParent(this);
	}

	@Override
	protected List<Node<?>> getAllNodesAsList() {
		List<Node<?>> nodes = new ArrayList<Node<?>>();
		nodes.add(this);

		for (Node<?> child : this.children) {
			nodes.add(child);
			nodes.addAll(child.getAllNodesAsList());
		}
		return nodes;
	}

	private Node<?> getRandomNode(Generator g) {
		List<Node<?>> allNodesOfTree = this.getAllNodesAsList();
		int allNodesOfTreeCount = allNodesOfTree.size();
		int indx = g.getRandom().nextInt(allNodesOfTreeCount);
		return allNodesOfTree.get(indx);
	}

	@Override
	public void mutate(Generator g, int depth) {
		int type = g.getRandom().nextInt(6);
		switch (type) {
		case 0:
			// HVL SUB
			mutateByRandomChangeOfFunction(g);
			break;
		case 1:
			mutateByDeletion(g);
			break;
		case 2:
			// HVL ADD
			mutateByGrowth(g);
			break;
		case 3:
			// Reverse children if they have the same return type, e.g. (x - y) -> (y - x)
			if (this.children.stream().map(child -> child.getReturnType()).distinct().limit(2).count() <= 1)
				Collections.reverse(this.children);
			break;
		case 4:
			// HVL DEL
			mutateByRandomChangeOfNodeToChild(g);
			break;
		case 5:
			// mutate by replacing the entire tree with a subtree
			swapWith(this.getRandomNode(g).copy());
			break;
		}
	}

	private void mutateByRandomChangeOfFunction(Generator g) {
		if (!g.nonTerms(this.getReturnType()).isEmpty()) {
			NonTerminal<?> newFun = (NonTerminal<?>) g.generateRandomNonTerminal(this.typeSignature());
			newFun.setChildren(this.children);
			this.swapWith(newFun);
		}
	}

	private void mutateByDeletion(Generator g) {
		if (!this.children.isEmpty()) {
			Node<?> child = this.getChild(g.getRandom().nextInt(this.children.size()));
			child.swapWith(g.generateRandomTerminal(child.getReturnType()));
		}
	}

	private void mutateByGrowth(Generator g) {
		if (!g.nonTerms(this.getReturnType()).isEmpty()) {
			Node<?> mutationPoint = this.getRandomNode(g);
			NonTerminal<?> newRoot = (NonTerminal<?>) g.generateRandomNonTerminal(mutationPoint.typeSignature());
			boolean thisAdded = false;
			for (Datatype type : newRoot.typeSignature()) {
				if (type == mutationPoint.getReturnType() && !thisAdded) {
					newRoot.addChild(mutationPoint.copy());
					thisAdded = true;
				} else
					newRoot.addChild(g.generateRandomTerminal(type));
			}
			mutationPoint.swapWith(newRoot);
		}
	}

	private void mutateByRandomChangeOfNodeToChild(Generator g) {
		Node<?> mutatingNode = this.getRandomNode(g);
		if (!mutatingNode.getChildren().isEmpty()) {
			int indx = g.getRandom().nextInt(mutatingNode.getChildren().size());
			mutatingNode.swapWith(mutatingNode.getChildren().get(indx));
		} else {
			this.mutateByRandomChangeOfFunction(g);
		}
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

	protected abstract NonTerminal<T> newInstance();

	@Override
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
