package nl.hr.core;

import java.util.*;

public class MainLoop {
    private double crossoverRate;
    private double mutationRate;
    private boolean elitsm;
    private int populationSize;
    private int numberOfIterations;
    private Random random;

    public MainLoop(double crossoverRate, double mutationRate, boolean elitsm, int populationSize, int numberOfIterations) {
        this.crossoverRate = crossoverRate;
        this.mutationRate = mutationRate;
        this.elitsm = elitsm;
        this.populationSize = populationSize;
        this.numberOfIterations = numberOfIterations;
        this.random = new Random();
    }

    public List<Specimen> populateSpecimens() {
        List<Specimen> specimenPopulation = new ArrayList<Specimen>();

        for (int i = 0; i < populationSize; i++) {
            specimenPopulation.add(new Specimen(random.nextInt(31)));
        }
        return specimenPopulation;
    }

    public Specimen rouletteWheelSelection(List<Specimen> specimenPopulation) {
        //tips found at http://www.obitko.com/tutorials/genetic-algorithms/selection.ph
        int sumOfFitness = 0;
        for (Specimen specimen : specimenPopulation) {
            double computedValue = assignmentFormula(specimen.getValue());
            sumOfFitness += computedValue;
        }
        int randNumber = new Random().nextInt(sumOfFitness);
        int partialSumOfFitness = 0;
        for (int i = 0; i < specimenPopulation.size(); i++) {
            partialSumOfFitness += assignmentFormula(specimenPopulation.get(i).getValue());
            if (partialSumOfFitness >= randNumber) {
                return specimenPopulation.get(i);
            }
        }
        return new Specimen(-1);
    }

    private Specimen singlePointCrossOver(Specimen parent1, Specimen parent2) {
        String binaryStringParent1 = convertToBin(parent1);
        String binaryStringParent2 = convertToBin(parent2);
        //Random point for the binaryString crossOver.
        int rand = new Random().nextInt(binaryStringParent1.length() - 1);//size of parent1 & 2 are the same
        String subStringParent1 = binaryStringParent1.substring(0, rand);
        String subStringParent2 = binaryStringParent2.substring(rand, binaryStringParent2.length());
        //Try-Catch in case conversion goes wrong return a specimen with negative value;
        try {
            return new Specimen(Integer.parseInt(subStringParent1 + subStringParent2, 2));
        } catch (Exception ex) {
            return new Specimen(-1);
        }
    }
    private String convertToBin(Specimen spec) {
        String value = Integer.toBinaryString(spec.getValue());
        StringBuilder sb = new StringBuilder();
        if(value.length() < 5) {
            int missingZeroes = 5 - value.length();
            for (int i = 0; i < missingZeroes; i++) {
                sb.append('0');
            }
            return sb.toString()+value;
        } else {
            return value;
        }
    }

    private Specimen mutation(Specimen specimen) {
        String specimenBinaryString = convertToBin(specimen);
        int rand = new Random().nextInt(specimenBinaryString.length()); //
        char spec = specimenBinaryString.charAt(rand);
        char[] chars;
        if (spec == '0') {
            chars = specimenBinaryString.toCharArray();
            chars[rand] = '1';
            return new Specimen(Integer.parseInt(String.valueOf(chars), 2));
        } else {
            chars = specimenBinaryString.toCharArray();
            chars[rand] = '0';
            return new Specimen(Integer.parseInt(String.valueOf(chars), 2));
        }

    }

    public double averageFitnessFromPopulation(List<Specimen> specimenPopulation) {
        double result = 0;
        for (Specimen specimen : specimenPopulation) {
            result += assignmentFormula(specimen.getValue());
        }
        return result / specimenPopulation.size();
    }

    public Map.Entry<Specimen, Integer> getFittestSpecimen(List<Specimen> specimenPopulation) {
        HashMap<Specimen, Integer> fittestSpecimenMap = new HashMap<Specimen, Integer>();
        //compute and return a sorted list with the highest fit level being at the
        for (Specimen specimen : specimenPopulation) {
            fittestSpecimenMap.put(specimen, assignmentFormula(specimen.getValue()));
        }
        return Collections.max(fittestSpecimenMap.entrySet(),
                (o1, o2) -> o1.getValue().compareTo(o2.getValue()));
    }

    private int assignmentFormula(int x) {
        //compute fitness
        //f(x) = −x 2 + 7x see assignment description
        int r = (int) (-(Math.pow(x, 2)) + 7 * x);
        return (r < 0) ? 0 : r;
    }

    public void run() {
        Random random = new Random();
        Specimen offspring;
        List<Specimen> currentPopulation = populateSpecimens(); //populate randomly first time
        int startIndex;
        for (int i = 0; i < numberOfIterations; i++) {
            List<Specimen> newPopulation = new ArrayList<>();
            System.out.println(String.format("Generation %s", i));
            if (elitsm) {
                List<Specimen> fittest = new ArrayList<>();
                newPopulation.add(getFittestSpecimen(currentPopulation).getKey());
                startIndex = 1;
            } else {
                startIndex = 0;
            }
            for (int j = startIndex; j < populationSize; j++) {
                //Create new specimen for the current population
                //Get parent1 && parent2
                Specimen parent1 = rouletteWheelSelection(currentPopulation);
                Specimen parent2 = rouletteWheelSelection(currentPopulation);
                //Perform Crossover
                if (random.nextDouble() < crossoverRate) {
                    offspring = singlePointCrossOver(parent1, parent2);
                } else {
                    offspring = parent1;
                }
                //Perform Mutation
                if (random.nextDouble() < mutationRate) {
                    offspring = mutation(offspring);
                }
                newPopulation.add(offspring);
            }
            //The new population becomes the current for the next iteration.
            currentPopulation = newPopulation;

            System.out.println(String.format("==========================TEST GENERATION %s======================================================="
                    , i));
            //Print Average Fittest
            System.out.println(String.format("Average Fittest score: %s", averageFitnessFromPopulation(currentPopulation)));
            //Print Best Fittest Score
            System.out.println(String.format("Best Fittest Score: %s", getFittestSpecimen(currentPopulation).getValue()));
            //Print Best Individual
            System.out.println(String.format("Best individual : x = %s", getFittestSpecimen(currentPopulation).getKey().getValue()));
            System.out.println("=======================END TEST==========================================================");
        }
        //Print Average Fittest
        System.out.println(String.format("Average Fittest score: %s", averageFitnessFromPopulation(currentPopulation)));
        //Print Best Fittest Score
        System.out.println(String.format("Best Fittest Score: %s", getFittestSpecimen(currentPopulation).getValue()));
        //Print Best Individual
        System.out.println(String.format("Best individual : x = %s", getFittestSpecimen(currentPopulation).getKey().getValue()));
    }
}