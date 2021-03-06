package mint.inference.evo;

/**
 * Created by neilwalkinshaw on 19/05/2016.
 */
public interface Chromosome {

	public Double getFitness();

	public Chromosome copy();

	public Chromosome simp();

	public boolean sameSyntax(Chromosome c);

}
