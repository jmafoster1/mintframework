package mint.inference.gp.tree.terminals;

import java.util.HashSet;
import java.util.Set;

import mint.inference.evo.Chromosome;
import mint.inference.gp.tree.Node;
import mint.inference.gp.tree.Terminal;
import mint.tracedata.types.VariableAssignment;

/**
 * Created by neilwalkinshaw on 07/03/15.
 */
public abstract class VariableTerminal<T extends VariableAssignment<?>> extends Terminal<T> {

	protected final boolean LATENT;

	public VariableTerminal(boolean constant, boolean latent) {
		super(constant);
		this.LATENT = latent;
	}

	public String getName() {
		return terminal.getName();
	}

	@Override
	public String toString() {
		if (!terminal.isParameter() && isConstant())
			return "" + terminal.getValue();
		return terminal.getName();
	}

	public Object value() {
		return terminal.getValue();
	}

	@Override
	public void reset() {
		if (!isConstant()) {
			terminal.setValue(null);
		}
	}

	@Override
	public boolean sameSyntax(Chromosome c) {
//		System.out.print("Terminals: " + this + " and " + c + " ");
		if (this.getClass().equals(c.getClass())) {
			VariableTerminal<T> var = (VariableTerminal<T>) c;
			if (var.constant == this.constant) {
				if (this.constant) {
					return this.getName().equals(var.getName())
							&& this.getTerminal().getValue().equals(var.getTerminal().getValue());
				} else {
					return this.getName().equals(var.getName());
				}
			}
		}
//		System.out.print("false ");
		return false;
	}

	public boolean isLatent() {
		return LATENT;
	}

	@Override
	public Set<VariableTerminal<?>> varsInTree() {
		Set<VariableTerminal<?>> v = new HashSet<VariableTerminal<?>>();
		if (!this.isConstant())
			v.add((VariableTerminal<?>) this.copy());
		return v;
	}

	public String typeString() {
		return this.getTerminal().typeString();
	}

	@Override
	protected void addChild(Node<?> swap) {
		// No children to add to
	}
}
