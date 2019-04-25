package problems.qbf.solvers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import com.sun.xml.internal.ws.api.pipe.NextAction;

import metaheuristics.ga.AbstractGA.Chromosome;
import metaheuristics.ga.AbstractGA.Population;
import solutions.Solution;

public class GA_QBFPT extends GA_QBF {
	
    // GA strategies
    public static final int STANDARD = 1;
    public static final int STEADY_STATE  = 2;
    public static final int LATIN_HYPERCUBE = 3;
    private final int gaStrategie;
    private final int generationsLimit;
    private final int timeLimit;
    private final int valueLimit;


	/**
	 * List of prohibited triples.
	 */
	private ArrayList<ArrayList<Integer>> triples;

	public GA_QBFPT(Integer generationsLimit, int timeLimit, int valueLimit, Integer popSize, Double mutationRate, String filename, int gaStrategie) throws IOException {
		super(generationsLimit, popSize, mutationRate, filename);
		// TODO Auto-generated constructor stub

		this.triples = new ArrayList<ArrayList<Integer>>();
		this.gaStrategie = gaStrategie;
		this.generationsLimit = generationsLimit;
		this.timeLimit = timeLimit;
		this.valueLimit = valueLimit;

	}

	/**
	 * Linear congruent function l used to generate pseudo-random numbers.
	 */
	public int l(int pi1, int pi2, int u, int n) {
		return 1 + ((pi1 * u + pi2) % n);
	}

	/**
	 * Function g used to generate pseudo-random numbers
	 */
	public int g(int u, int n) {
		int pi1 = 131;
		int pi2 = 1031;
		int lU = l(pi1, pi2, u, n);

		if (lU != u) {
			return lU;
		} else {
			return 1 + (lU % n);
		}
	}

	/**
	 * Function h used to generate pseudo-random numbers
	 */
	public int h(int u, int n) {
		int pi1 = 193;
		int pi2 = 1093;
		int lU = l(pi1, pi2, u, n);
		int gU = g(u, n);

		if (lU != u && lU != gU) {
			return lU;
		} else if ((1 + (lU % n)) != u && (1 + (lU % n)) != gU) {
			return 1 + (lU % n);
		} else {
			return 1 + ((lU + 1) % n);
		}
	}

	public void generateTriples() {
		int n = ObjFunction.getDomainSize();
		for (int u = 1; u <= n; u++) {
			ArrayList<Integer> triple = new ArrayList<Integer>();
			triple.add(u - 1);
			triple.add(g(u-1, n) - 1);
			triple.add(h(u - 1, n) - 1);
			
			// Sorting new triple
			Collections.sort(triple);
			
			//Print triples
			//System.out.println(triple.get(0) + " " + triple.get(1) + " " + triple.get(2));
			
			// Adding to triples array
			this.triples.add(triple);
		}
	}
	
	public void fixChromosome(Chromosome chromosome) {
		Collections.shuffle(triples);
		for (ArrayList<Integer> triple : triples) {
			if (chromosome.get(triple.get(0)) == 1 && chromosome.get(triple.get(1)) == 1
					&& chromosome.get(triple.get(2)) == 1) {
				int randIndex = rng.nextInt(3);
				chromosome.set(triple.get(randIndex), 0);
			}
		}
	}
	
	protected Population initializePopulationLatinHypercube() {

		Population population = new Population();
		int qntGenes = ObjFunction.getDomainSize();
		int i = 0;
		
		ArrayList<Integer> column = new ArrayList<Integer>();
		while(i < popSize/2)
		{
			column.add(0);
			i++;
		}
		
		while(i < popSize)
		{
			column.add(1);
			i++;
		}
		
		i = 0;
		while (i < popSize) {
			population.add(new Chromosome());
			i++;
		}
		
		for(i = 0; i < qntGenes; i++)
		{
			Collections.shuffle(column);
			for(int j = 0; j < popSize; j++)
			{
				(population.get(j)).add(column.get(j));
			}
		}
		return population;
	}

	/**
	 * The GA mainframe. It starts by initializing a population of chromosomes. It
	 * then enters a generational loop, in which each generation goes the following
	 * steps: parent selection, crossover, mutation, population update and best
	 * solution update.
	 * 
	 * @return The best feasible solution obtained throughout all iterations.
	 */
	@Override
	public Solution<Integer> solve() {
		long beginTime = System.currentTimeMillis();
		double partialTime =  ((double)(System.currentTimeMillis() - beginTime) / 1000) / 60;
		int g = 1;
		Population population;

		/* starts the initial population */
		if(gaStrategie == GA_QBFPT.LATIN_HYPERCUBE)
			population = initializePopulationLatinHypercube(); 
		else
			population = initializePopulation();

		bestChromosome = getBestChromosome(population);
		bestSol = decode(bestChromosome);
		System.out.println("(Gen. " + 0 + ") BestSol = " + bestSol);
		
		
		/*
		 * enters the main loop and repeats until a given number of generations
		 */
		while(g < this.generationsLimit && partialTime < this.timeLimit && bestSol.cost < this.valueLimit) {

			Population parents = selectParents(population);
			//System.out.println("Parents: "+parents.size());
			Population offsprings = crossover(parents);
			for (Chromosome chromosome : offsprings) {
				fixChromosome(chromosome);
			}
			Population mutants = mutate(offsprings);
			for (Chromosome chromosome : mutants) {
				fixChromosome(chromosome);
			}
			Population newpopulation = selectPopulation(mutants);
			
			if(gaStrategie == GA_QBFPT.STEADY_STATE) {
				newpopulation = selectParentsSteadyState(newpopulation);
			}

			population = newpopulation;
			
			bestChromosome = getBestChromosome(population);

			if (fitness(bestChromosome) > bestSol.cost) {
				bestSol = decode(bestChromosome);
				if (verbose)
					System.out.println("(Gen. " + g + ") BestSol = " + bestSol);
			}
			
			partialTime =  ((double)(System.currentTimeMillis() - beginTime) / 1000) / 60;
			g++;
		}

		return bestSol;
	}
	
	public Population selectParentsSteadyState(Population population) {

		/*
		 * Selecionando dois pais distintos aleatoriamente
		 */
		int index1 = rng.nextInt(popSize);
		Chromosome parent1 = population.get(index1);
		int index2 = rng.nextInt(popSize);
		while(index1 == index2) {
			index2 = rng.nextInt(popSize);
		}
		Chromosome parent2 = population.get(index2);
		
		/*
		 * Gerando os dois filhos
		 */
		int crosspoint1 = rng.nextInt(chromosomeSize + 1);
		int crosspoint2 = crosspoint1 + rng.nextInt((chromosomeSize + 1) - crosspoint1);

		Chromosome offspring1 = new Chromosome();
		Chromosome offspring2 = new Chromosome();

		for (int j = 0; j < chromosomeSize; j++) {
			if (j >= crosspoint1 && j < crosspoint2) {
				offspring1.add(parent2.get(j));
				offspring2.add(parent1.get(j));
			} else {
				offspring1.add(parent1.get(j));
				offspring2.add(parent2.get(j));
			}
		}

		/*
		 * Corrigindo os filhos
		 */
		fixChromosome(offspring1);
		fixChromosome(offspring2);
		/*
		 * Escolhendo o melhor filho
		 */
		Chromosome bestoffspring = new Chromosome();
		if(fitness(offspring1) > fitness(offspring2)) {
			bestoffspring = offspring1;
		}else {
			bestoffspring = offspring2;
		}
		/*
		 * Analisando se deve entrar na solução
		 */
		Chromosome worse = getWorseChromosome(population);
		if (fitness(worse) < fitness(bestoffspring)) {
			population.remove(worse);
			population.add(bestChromosome);
		}
		return population;
		
	}
}
