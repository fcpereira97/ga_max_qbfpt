package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import problems.qbf.solvers.GA_QBFPT;
import solutions.Solution;

public class Main {

    public static int timeLimit = 30; // Max time in minutes
    public static int generationsLimit = 50000; // Max generations (use MAX_INT to disable)
    public static int valueLimit; // Value limit to stop iterations
    public static String outputCsv; // Output name file
    
    // Instances
    public static final String[] FILES_LIST = new String[]{
        //"instances/qbf020",
    	//"instances/qbf040",
    	"instances/qbf060",
        "instances/qbf080",
        "instances/qbf100",
        "instances/qbf200",
        "instances/qbf400"
    };
    
	/**
	 * A main method used for testing the GA metaheuristic.
	 * 
	 */
	public static void main(String[] args) throws IOException {

		outputCsv = "fileName,config,valueSol,tempExec, sol\n";
        
        // Configurations
		/*
         executeGA(100, 1.0 / 100.0, GA_QBFPT.STANDARD, "P");
         saveOutput("outputP2.csv", outputCsv);
         
         executeGA(200, 1.0 / 100.0, GA_QBFPT.STANDARD, "A");
         saveOutput("outputA2.csv", outputCsv);
         executeGA(100, 2.0 / 100.0, GA_QBFPT.STANDARD, "B");
         saveOutput("outputB2.csv", outputCsv);
         executeGA(100, 1.0 / 100.0, GA_QBFPT.STEADY_STATE, "C");
         saveOutput("outputC2.csv", outputCsv);
         */
		executeGA(100, 1.0 / 100.0, GA_QBFPT.LATIN_HYPERCUBE, "D");
        saveOutput("outputD3.csv", outputCsv);
         
         
          // Setting the name of the output file
         
	}
	
    private static void executeGA(int popSize, double mutationRate, int gaStrategie, String configuration) throws IOException
    {    	
    	// Iterating over files
        for (String file : FILES_LIST) {
        	if(file.equals("instances/qbf020")) {
        		valueLimit = 125;
        	} else if(file.equals("instances/qbf040")) {
        		valueLimit = 366;
        	} else if(file.equals("instances/qbf060")) {
        		valueLimit = 576;
        	} else if(file.equals("instances/qbf080")) {
        		valueLimit = 1000;
    		} else if(file.equals("instances/qbf100")) {
        		valueLimit = 1539;
    		} else if(file.equals("instances/qbf200")) {
        		valueLimit = 5826;
    		}else if(file.equals("instances/qbf400")) {
        		valueLimit = 16625;
    		}

            //Print configurations of the execution
            System.out.println("Executing GA for file: " + file);
            System.out.println(" Configuration = " + configuration);
            printGaStrategie (gaStrategie);
            System.out.println("Population = " + popSize + "\n Mutation rate = " + mutationRate);
            printStopCriterion();

            // Executing GA
            System.out.println("Execution:");

            long beginInstanceTime = System.currentTimeMillis();
            
            // Setting configurations parameters
            // tenure is defined by the ternurePercent * size
            GA_QBFPT ga = new GA_QBFPT(generationsLimit, timeLimit, valueLimit, popSize, mutationRate, file, gaStrategie);
            ga.generateTriples();
            Solution<Integer> bestSolution = ga.solve(); // Starting solve model
            
            System.out.println(" maxVal = " + bestSolution); // Print best solution
            
            // Print other data
            long endInstanceTime = System.currentTimeMillis();
            long totalInstanceTime = endInstanceTime - beginInstanceTime;
            System.out.println("Time = " + (double) totalInstanceTime / (double) 1000 + " seg");
            System.out.println("\n");
            
            // Add info to output csv file
            outputCsv += file + "," + configuration + ","
                     + bestSolution.cost + "," + (double)totalInstanceTime / 1000 + ", \"" + bestSolution + "\"\n";

        }

        System.out.println("----------------------------------------------------- \n \n");
    }
	
	// Print GA strategy
    private static void printGaStrategie(int gaStrategie) {
        String resp = " GA strategie = ";

        if (gaStrategie == GA_QBFPT.STANDARD) {
            resp += "Standard";
        }
        if (gaStrategie == GA_QBFPT.STEADY_STATE) {
            resp += "Steady State";
        }
        if (gaStrategie == GA_QBFPT.LATIN_HYPERCUBE) {
            resp += "Latin Hypercube";
        }

        System.out.println(resp);
    }
	
	// Print stop criterion
    private static void printStopCriterion() {
    	
        String resp = " Stop Criterion = ";
        
        if(generationsLimit <= 0)
        	resp += generationsLimit + " generations";
        
        else if (timeLimit > 0) {
            resp +=  timeLimit + " minutes";
        }

        System.out.println(resp);
    }
	
	// Save input file
    public static void saveOutput(String fileName, String content) {
        File dir;
        PrintWriter out;

        dir = new File("output");

        if (!dir.exists()) {
            dir.mkdirs();
        }

        try {
            out = new PrintWriter(new File(dir, fileName));
            out.print(content);
            out.close();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
    }

}
