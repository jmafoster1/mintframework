import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import mint.inference.evo.GPConfiguration;
import mint.inference.gp.Generator;
import mint.inference.gp.LatentVariableGP;
import mint.inference.gp.tree.Node;
import mint.inference.gp.tree.nonterminals.integers.AddIntegersOperator;
import mint.inference.gp.tree.nonterminals.integers.SubtractIntegersOperator;
import mint.inference.gp.tree.terminals.IntegerVariableAssignmentTerminal;
import mint.tracedata.types.IntegerVariableAssignment;
import mint.tracedata.types.VariableAssignment;

public class SRPlayground {

	public static MultiValuedMap<List<VariableAssignment<?>>, VariableAssignment<?>> generateTrainingSet() {
		MultiValuedMap<List<VariableAssignment<?>>, VariableAssignment<?>> trainingSet = new HashSetValuedHashMap<List<VariableAssignment<?>>, VariableAssignment<?>>();

		List<VariableAssignment<?>> i10 = new ArrayList<VariableAssignment<?>>();
		i10.add(new IntegerVariableAssignment("i0", 10l));

		List<VariableAssignment<?>> i20 = new ArrayList<VariableAssignment<?>>();
		i20.add(new IntegerVariableAssignment("i0", 20l));

		List<VariableAssignment<?>> i50 = new ArrayList<VariableAssignment<?>>();
		i50.add(new IntegerVariableAssignment("i0", 50l));

		List<VariableAssignment<?>> i100 = new ArrayList<VariableAssignment<?>>();
		i100.add(new IntegerVariableAssignment("i0", 100l));

		trainingSet.put(i50, new IntegerVariableAssignment("o1", 50l));
		trainingSet.put(i50, new IntegerVariableAssignment("o1", 100l));
		trainingSet.put(i100, new IntegerVariableAssignment("o1", 100l));

		trainingSet.put(i10, new IntegerVariableAssignment("o1", 10l));
		trainingSet.put(i20, new IntegerVariableAssignment("o1", 30l));
		trainingSet.put(i50, new IntegerVariableAssignment("o1", 70l));
		trainingSet.put(i100, new IntegerVariableAssignment("o1", 100l));

		return trainingSet;
	}

	public static boolean run(long seed) {
		BasicConfigurator.resetConfiguration();
		BasicConfigurator.configure();
		Logger.getRootLogger().setLevel(Level.DEBUG);

		Generator gpGenerator = new Generator(new Random(seed));

		gpGenerator.add(new AddIntegersOperator());
		gpGenerator.add(new SubtractIntegersOperator());

		gpGenerator.add(new IntegerVariableAssignmentTerminal("i0", false));
		gpGenerator.add(new IntegerVariableAssignmentTerminal("r1", true));

		for (int i : new int[] { 0, 1, 2, 960, 1060, 720, 1070, 1220, 950, 870, 890, 60, 790, 730, 770, 920, 970, 990,
				1090, 1190, 590, 660, 90, 430, 40, 750, 850, 1240, 1260, 1310, 1360, 1410, 1460, 280, 330, 450, 980,
				1100, 1120, 1170, 1270, 300, 780, 830, 810, 880, 900, 1000, 1020, 70, 290, 510, 610, 130, 80, 230, 250,
				350, 650, 680, 800, 820, 270, 640, 740, 840, 940, 1040, 1140, 1160, 1210, 1230, 1250, 1300, 1400, 1420,
				1470, 1490, 440, 380, 400, 620, 180, 310, 220, 320, 370, 420, 470, 520, 570, 670, 690, 710, 760, 860,
				910, 930, 1030, 1010, 1080, 120, 140, 240, 340, 490, 540, 560, 580, 630, 500, 600, 700, 100, 200, 50,
				150, 20, 170, 190, 110, 210, 160, 260, 360, 410, 390, 460, 480, 530, 550 })
			gpGenerator.add(new IntegerVariableAssignmentTerminal(i));

		MultiValuedMap<List<VariableAssignment<?>>, VariableAssignment<?>> trainingSet = generateTrainingSet();

		System.out.println("Training set: " + trainingSet);
		System.out.println("Int values: " + IntegerVariableAssignment.values());

		LatentVariableGP gp = new LatentVariableGP(gpGenerator, trainingSet,
				new GPConfiguration(20, 0.9f, 0.01f, 5, 2));

//		AddIntegersOperator seed = new AddIntegersOperator(new IntegerVariableAssignmentTerminal("i0", false),
//				new IntegerVariableAssignmentTerminal("r1", true));
//		gp.addSeed(seed);

		Node<?> best = (Node<?>) gp.evolve(100);
		System.out.println(best + ": " + best.getFitness());
		System.out.println(best.simp());
		System.out.println("correct? " + gp.isCorrect(best));

		return best.toString().equals(best.simp().toString());
	}

	public static void main(String[] args) {
		run(5);

	}
}
