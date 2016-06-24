package nl.hr.core;

import lombok.AllArgsConstructor;

import java.util.*;

@AllArgsConstructor
public class MainLoop {

    private double crossoverRate;
    private double mutationRate;
    private boolean elitsm;
    private int populationSize;
    private int numberOfIterations;

    private Value<Integer> rouletteWheelSelection(List<Value<Integer>> specimenPopulation) {
        int sumOfFitness = 0;
        for (Value<Integer> specimen : specimenPopulation) {
            double computedValue = calcFitness(specimen.getValue());
            sumOfFitness += computedValue;
        }
        int randNumber = new Random().nextInt(sumOfFitness);
        int partialSumOfFitness = 0;

        for (Value<Integer> aSpecimenPopulation : specimenPopulation) {
            partialSumOfFitness += calcFitness(aSpecimenPopulation.getValue());
            if (partialSumOfFitness >= randNumber) {
                return aSpecimenPopulation;
            }
        }
        return new Value<>(-1);
    }

    private double calcAverageFitness(List<Value<Integer>> population) {
        double result = 0;
        for (Value<Integer> v : population) {
            result += calcFitness(v.getValue());
        }
        return result / population.size();
    }

    private Map.Entry<Value<Integer>, Integer> getFittest(List<Value<Integer>> population) {
        Map<Value<Integer>, Integer> result = new HashMap<>();
        for (Value<Integer> e : population) {
            result.put(e, calcFitness(e.getValue()));
        }
        return Collections.max(result.entrySet(),
                (o1, o2) -> o1.getValue().compareTo(o2.getValue()));
    }

    private int calcFitness(int x) {
        int r = (int) (-(Math.pow(x, 2)) + (7 * x));
        return (r < 0) ? 0 : r;
    }

    private Value<Integer> singlePointXover(Value<Integer> p1, Value<Integer> p2) {
        String binp1 = convertToBin(p1);
        String binp2 = convertToBin(p2);
        int rand = new Random().nextInt(binp1.length() - 1);
        String p1Dna = binp1.substring(0, rand);
        String p2Dna = binp2.substring(rand, binp2.length());
        try {
            return new Value<Integer>(Integer.parseInt(p1Dna + p2Dna, 2));
        } catch (Exception ex) {
            return new Value<Integer>(-1);
        }
    }

    private String convertToBin(Value<Integer> convert) {
        String value = Integer.toBinaryString(convert.getValue());
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

    private Value<Integer> mutation(Value<Integer> specimen) {
        String specimenBinaryString = convertToBin(specimen);
        int r = new Random().nextInt(specimenBinaryString.length());
        char tempValue = specimenBinaryString.charAt(r);
        char[] tempChars;
        if (tempValue == '0') {
            tempChars = specimenBinaryString.toCharArray();
            tempChars[r] = '1';
            return new Value<Integer>(Integer.parseInt(String.valueOf(tempChars), 2));
        } else {
            tempChars = specimenBinaryString.toCharArray();
            tempChars[r] = '0';
            return new Value<Integer>(Integer.parseInt(String.valueOf(tempChars), 2));
        }
    }

    private List<Value<Integer>> populateSpecimens() {
        List<Value<Integer>> specimenPopulation = new ArrayList<>();
        for (int i = 0; i < populationSize; i++) {
            specimenPopulation.add(new Value<Integer>(new Random().nextInt(31)));
        }
        return specimenPopulation;
    }

    public void run() {
        Random random = new Random();
        Value<Integer> offspring;
        List<Value<Integer>> currentPopulation = populateSpecimens(); //populate randomly first time
        int index = -1;
        for (int i = 0; i < numberOfIterations; i++) {
            List<Value<Integer>> newPopulation = new ArrayList<>();
            System.out.println(String.format("Generation %s", i));
            if (elitsm) {
                newPopulation.add(getFittest(currentPopulation).getKey());
                index = 1;
            } else {
                index = 0;
            }
            for (int a = index; a < populationSize; a++) {
                //Create new specimen for the current population
                //Get parent1 && parent2
                Value<Integer> parent1 = rouletteWheelSelection(currentPopulation);
                Value<Integer> parent2 = rouletteWheelSelection(currentPopulation);
                //Perform Crossover
                if (random.nextDouble() < crossoverRate) {
                    offspring = singlePointXover(parent1, parent2);
                } else {
                    offspring = parent1;
                }
                //Perform Mutation
                if (random.nextDouble() < mutationRate) {
                    offspring = mutation(offspring);
                }
                newPopulation.add(offspring);
            }
            currentPopulation = newPopulation;

            System.out.println(String.format("==========================TEST GENERATION %s======================================================="
                    , i));
            //Print Average Fittest
            System.out.println(String.format("Average Fittest score: %s", calcAverageFitness(currentPopulation)));
            //Print Best Fittest Score
            System.out.println(String.format("Best Fittest Score: %s", getFittest(currentPopulation).getValue()));
            //Print Best Individual
            System.out.println(String.format("Best individual : x = %s", getFittest(currentPopulation).getKey().getValue()));
            System.out.println("=======================END TEST==========================================================");
        }
        //Print Average Fittest
        System.out.println(String.format("Average Fittest score: %s", calcAverageFitness(currentPopulation)));
        //Print Best Fittest Score
        System.out.println(String.format("Best Fittest Score: %s", getFittest(currentPopulation).getValue()));
        //Print Best Individual
        System.out.println(String.format("Best individual : x = %s", getFittest(currentPopulation).getKey().getValue()));
    }
}