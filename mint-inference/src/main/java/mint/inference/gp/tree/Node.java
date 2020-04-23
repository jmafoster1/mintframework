package mint.inference.gp.tree;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;

import mint.inference.evo.Chromosome;
import mint.inference.gp.Generator;
import mint.inference.gp.tree.nonterminals.strings.AssignmentOperator;
import mint.inference.gp.tree.terminals.VariableTerminal;
import mint.tracedata.types.VariableAssignment;

/**
 * Represents a node in a GP tree.
 *
 * If a GP tree is to be associated with a memory, the setMemory method must
 * only be called after the tree has been completed.
 *
 * Created by neilwalkinshaw on 03/03/15.
 */
public abstract class Node<T extends VariableAssignment<?>> implements Chromosome, Comparable<Node<?>> {

	private Double fitness = null;

	protected static int ids = 0;

	protected int id;

	protected AssignmentOperator def;

	protected Set<Object> vals = new HashSet<Object>();

	public Node() {
		id = ids++;
	}

	public AssignmentOperator getDef() {
		return def;
	}

	public void setDef(AssignmentOperator def) {
		this.def = def;
	}

	public void reset() {
		for (Node<?> child : getChildren()) {
			child.reset();
		}
	}

	public abstract boolean accept(NodeVisitor visitor) throws InterruptedException;

	public abstract List<Node<?>> getChildren();

	public abstract T evaluate() throws InterruptedException;

	@Override
	public abstract Node<T> copy();

	public abstract Node<?> mutate(Generator g, int depth);

	public Node<?> swap(Node<?> oldNode, Node<?> newNode) {
		if (oldNode == this) {
			return newNode;
		}

		Node<T> n = this.newInstance();
		for (Node<?> c : this.getChildren()) {
			if (c == oldNode)
				n.addChild(newNode);
			else
				n.addChild(c.swap(oldNode, newNode));
		}
//		if (parent == null)
//			System.out.println("swapping " + oldNode + " with " + newNode + " in " + this + " to make " + n);
		return n;
	}

	protected abstract void addChild(Node<?> swap);

	protected abstract Node<T> newInstance();

	public Datatype getReturnType() {
		return this.typeSignature()[this.typeSignature().length - 1];
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof Node))
			return false;

		Node<?> node = (Node<?>) o;

		if (id != node.id)
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		return id;
	}

	public abstract int size();

	protected void checkInterrupted() throws InterruptedException {
		if (Thread.interrupted()) {
			throw new InterruptedException();
		}

	}

	public abstract Set<VariableTerminal<?>> varsInTree();

	public Set<VariableTerminal<?>> latentVars() {
		return varsInTree().stream().filter(v -> v.isLatent()).collect(Collectors.toSet());
	}

	public abstract Expr toZ3(Context ctx);

	@Override
	@SuppressWarnings("unchecked")
	public Node<T> simp() {
		Context ctx = new Context();
		try {
			Expr z3Expr = this.toZ3(ctx).simplify();
			Node<T> retVal = (Node<T>) NodeSimplifier.fromZ3(z3Expr);
			return retVal;
		} catch (Exception e) {
			return this;
		} finally {
			ctx.close();
		}
	}

	@Override
	public Double getFitness() {
		return fitness;
	}

	public void setFitness(double f) {
		this.fitness = f;
	}

	@Override
	public int compareTo(Node<?> arg0) {
		Double fit = this.fitness;
		return fit.compareTo(arg0.fitness);
	}

	public abstract List<Node<?>> getAllNodesAsList();

	public abstract Datatype[] typeSignature();

	protected Node<?> getRandomNode(Generator g) {
		List<Node<?>> allNodesOfTree = this.getAllNodesAsList().stream()
				.filter(x -> x.getReturnType() == this.getReturnType()).collect(Collectors.toList());
		int allNodesOfTreeCount = allNodesOfTree.size();
		int indx = g.getRandom().nextInt(allNodesOfTreeCount);
		return allNodesOfTree.get(indx);
	}

	public int depth() {
		int max = 1;
		for (Node<?> c : getChildren()) {
			int cd = c.depth();
			if (cd > max) {
				max = cd;
			}
		}
		return max;
	}

	protected Node<?> mutateByGrowth(Generator g) {
		Node<?> mutationPoint = this.getRandomNode(g);
		Node<?> newTree = g.generateRandomExpression(this.depth(), mutationPoint.getReturnType());

		return swap(mutationPoint, newTree);
	}

	public Node<?> randomChild(Generator g) {
		return getChildren().get(g.getRandom().nextInt(getChildren().size()));
	}

}
