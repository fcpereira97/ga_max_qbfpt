package problems.qbf.solvers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;

import solutions.Solution;
import triple.Triple;
import triple.TripleElement;

public class GA_QBFPT extends GA_QBF {
	
	/**
     * List of element objects used in prohibited triples. These objects
     * represents the variables of the model.
     */
    private TripleElement[] tripleElements;

    /**
     * List of prohibited triples.
     */
    private Triple[] triples; 

	public GA_QBFPT(Integer generations, Integer popSize, Double mutationRate, String filename) throws IOException {
		super(generations, popSize, mutationRate, filename);
		// TODO Auto-generated constructor stub
		
		generateTripleElements();
        generateTriples();
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
	
	/**
    * Method that generates a list of n prohibited triples using l g and h
    * functions
    */
   private void generateTriples() {
       int n = ObjFunction.getDomainSize();
       this.triples = new Triple[ObjFunction.getDomainSize()];

       for (int u = 1; u <= n; u++) {
           TripleElement te1, te2, te3;
           Triple newTriple;

           te1 = tripleElements[u - 1];
           te2 = tripleElements[g(u - 1, n) - 1];
           te3 = tripleElements[h(u - 1, n) - 1];
           newTriple = new Triple(te1, te2, te3);
           
           //Sorting new triple
           Arrays.sort(newTriple.getElements(), Comparator.comparing(TripleElement::getIndex));

           //newTriple.printTriple();
           this.triples[u-1] = newTriple;
       }
   }
   

   /**
    * That method generates a list of objects (Triple Elements) that represents
    * each binary variable that could be inserted into a prohibited triple
    */
	private void generateTripleElements() {
       int n = ObjFunction.getDomainSize();
       this.tripleElements = new TripleElement[n];

       for (int i = 0; i < n; i++) {
           tripleElements[i] = new TripleElement(i);
       }
   }
	
	public void fixChromosome(Chromosome chromosome)
	{
		ArrayList<Triple> triplesCopy = new ArrayList<Triple>(Arrays.asList(triples));
		Collections.shuffle(triplesCopy);
		
		for(Triple triple : triplesCopy)
		{
			ArrayList<Integer> candToFix = new ArrayList<Integer>();
			
			candToFix.add((triple.getElements()[0]).getIndex());
			candToFix.add((triple.getElements()[1]).getIndex());
			candToFix.add((triple.getElements()[2]).getIndex());

			if(chromosome.get(candToFix.get(0)) == 1 && chromosome.get(candToFix.get(1)) == 1 && chromosome.get(candToFix.get(2)) == 1)
			{
				Collections.shuffle(candToFix);
			}
			chromosome.set(candToFix.get(0), 0);
		}
	}
	
	/**
	 * The GA mainframe. It starts by initializing a population of chromosomes.
	 * It then enters a generational loop, in which each generation goes the
	 * following steps: parent selection, crossover, mutation, population update
	 * and best solution update.
	 * 
	 * @return The best feasible solution obtained throughout all iterations.
	 */
	@Override
	public Solution<Integer> solve() {

		/* starts the initial population */
		Population population = initializePopulation();

		bestChromosome = getBestChromosome(population);
		bestSol = decode(bestChromosome);
		System.out.println("(Gen. " + 0 + ") BestSol = " + bestSol);

		/*
		 * enters the main loop and repeats until a given number of generations
		 */
		for (int g = 1; g <= generations; g++) {

			Population parents = selectParents(population);

			Population offsprings = crossover(parents);
			for(Chromosome chromosome : offsprings)
				fixChromosome(chromosome);
				
			Population mutants = mutate(offsprings);
			for(Chromosome chromosome : mutants)
				fixChromosome(chromosome);

			Population newpopulation = selectPopulation(mutants);

			population = newpopulation;

			bestChromosome = getBestChromosome(population);

			if (fitness(bestChromosome) > bestSol.cost) {
				bestSol = decode(bestChromosome);
				if (verbose)
					System.out.println("(Gen. " + g + ") BestSol = " + bestSol);
			}

		}

		return bestSol;
	}
}
