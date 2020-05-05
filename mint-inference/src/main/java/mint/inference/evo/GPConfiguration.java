package mint.inference.evo;

/**
 * Data class to store GP configuration data.
 */
public class GPConfiguration {

	private final int populationSize;
	public final int mu;
	public final double mutation;
	private final int depth;
	private final int tournamentSize;

	public GPConfiguration(int populationSize, int mu, double mutation, int depth, int tournamentSize) {
		if (tournamentSize > populationSize)
			throw new IllegalArgumentException("Population size must be greater than tournament size");
		this.populationSize = populationSize;
		this.mu = mu;
		this.mutation = mutation;
		this.depth = depth;
		this.tournamentSize = tournamentSize;
	}

	public GPConfiguration(int populationSize, int mu, double mutation, int depth) {
		tournamentSize = 2;
		if (tournamentSize > populationSize)
			throw new IllegalArgumentException("Population size must be greater than tournament size");
		this.populationSize = populationSize;
		this.mu = mu;
		this.mutation = mutation;
		this.depth = depth;
	}

	public int getPopulationSize() {
		return populationSize;
	}

	public int getDepth() {
		return depth;
	}

	public int getTournamentSize() {
		return tournamentSize;
	}
}
