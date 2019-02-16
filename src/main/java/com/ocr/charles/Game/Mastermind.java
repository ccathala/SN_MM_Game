package com.ocr.charles.Game;


import com.ocr.charles.Exceptions.PlayerInputError;

import java.io.IOException;
import java.util.*;


public class Mastermind extends Game {


    private int indexTable = 0;
    private int[][] scoreTable;
    private LinkedList<int[]>propositionsList = new LinkedList<>();
    private LinkedList<Integer> scoresList = new LinkedList<>();
    // liste construite par la recursion
    private int[] liste = new int[combinationDigitNumber];
    private boolean initPropositionTable = false;
    private int[] optimizedProposition;


    @Override
    public void displayGameHeading() {
        System.out.println("----------[Mastermind]----------");
    }

    @Override
    public int[] generateAndDisplayAiAnswer(String result) {
        if (attemptNumber == 1) {
            generateCombinationList(0);
            return propositionsList.get(0);
        }
        else{
            for (int i=1;i<propositionsList.size();i++){
                String score = compareSolutionAndAttempt(propositionsList.get(0),propositionsList.get(i));
                if(!score.equals(result)){
                    propositionsList.remove(i);
                    i=i-1;
                }
            }
            propositionsList.remove(0);
            return propositionsList.get(0);
        }
    }

    @Override
    protected void playerCorrectCombinationInput(String[] playerInput) throws PlayerInputError {
        if (playerInput.length != combinationDigitNumber) throw new PlayerInputError();
        for (int i = 0; i < combinationDigitNumber; i++) {
            if (Integer.parseInt(playerInput[i]) > (mastermindAllowedNumber - 1)) throw new PlayerInputError();
        }
    }

    @Override
    public String compareSolutionAndAttempt(int[]solution, int[]answer) {
        int numberWellplaced = 0;
        int numberInCombination = 0;
        int min = 0;

        for (int i = 0; i < combinationDigitNumber; i++) {
            if (solution[i] == answer[i]) {
                numberWellplaced = numberWellplaced + 1;
            }
        }
        for (int i = 0; i < mastermindAllowedNumber; i++) {
            LinkedList<Integer> occurencies = new LinkedList<>();
            occurencies.add(0, 0);
            occurencies.add(1, 0);
            for (int j = 0; j < combinationDigitNumber; j++) {
                if (solution[j] == i) {
                    occurencies.set(0, occurencies.get(0) + 1);
                }
                if (answer[j] == i) {
                    occurencies.set(1, occurencies.get(1) + 1);
                }
            }
            min = min + Collections.min(occurencies);
        }
        numberInCombination = min - numberWellplaced;
        return String.valueOf((numberWellplaced * 10) + numberInCombination);
    }

    @Override
    public void displayIndications(String result){
        int numberWellplaced = Integer.parseInt(result)/10;
        int numberInCombination = Integer.parseInt(result) - numberWellplaced*10;
        System.out.print("->Réponse : ");
        if (numberWellplaced <= 1) {
            System.out.println(numberWellplaced + " chiffre bien placé.");
        } else {
            System.out.println(numberWellplaced + " chiffres bien placés.");
        }
        if (numberInCombination <= 1) {
            System.out.println("            " + numberInCombination + " chiffre présent, mal placé.");
        } else {
            System.out.println("            " + numberInCombination + " chiffres présents, mal placés.");
        }
        logger.info("Chiffres bien placés : " + numberWellplaced);
        logger.info("Chiffres présents, mal placés : " + numberInCombination);
    }

    @Override
    public boolean isGameWon(String result){
        if(Integer.parseInt(result)==combinationDigitNumber*10){
            return true;
        }else{
            return false;
        }
    }


    @Override
    protected int[] importParameterFromConfigProperties() {
        Properties properties = new Properties();
        int[] importedValues = new int[4];
        try {
            properties.load(Game.class.getClassLoader().getResourceAsStream("config.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        importedValues[0] = Integer.parseInt(properties.getProperty("mastermindCombinationDigitNumber"));
        importedValues[1] = Integer.parseInt(properties.getProperty("mastermindAllowedAttempts"));
        importedValues[3] = Integer.parseInt(properties.getProperty("mastermindAllowedNumber"));
        importedValues[2] = Integer.parseInt(properties.getProperty("devMode"));
        return importedValues;
    }

    @Override
    protected int randomNumber() {
        Random random = new Random();
        return random.nextInt(mastermindAllowedNumber);
    }

    /* IA MASTERMIND------------------------------------------------------------------------------------*/

    // construction recursive des listes possibles
    public void generateCombinationList(int index) {
        if (index >= combinationDigitNumber) {
            // la liste est construite -> FIN
            int[] propositionTemplate = new int[combinationDigitNumber];
            for (int i = 0; i < combinationDigitNumber; i++) {
                propositionTemplate[i] = (liste[i]);
            }
            propositionsList.add(propositionTemplate);
            /*System.out.println(propositionsTable[indexTable][0]);*/
            indexTable = indexTable + 1;
            /*System.out.println(test);*/
            return;
        }

        // ajoute un nouvel element candidat dans la liste
        // - sans ordre -> candidat: tous les elements
        // - avec ordre -> candidat: seulement les elements supérieurs au précédant

        int start = 0;
        for (int i = start; i < mastermindAllowedNumber; i++) {
            liste[index] = i;
            generateCombinationList(index + 1);
        }
    }

    public void generateScoresList() {
        for (int i = 0; i <= combinationDigitNumber; i++) {
            int dizaine = 10 * i;
            if (i <= combinationDigitNumber - 2) {
                for (int j = 0; j <= combinationDigitNumber; j++) {
                    if (j < i) {
                        j = i;
                    }
                    scoresList.add(dizaine);
                    dizaine = dizaine + 1;
                }
            } else {
                scoresList.add(dizaine);
            }
        }
        /*System.out.println(scoresList);
        System.out.println(scoresList.size());*/
        scoreTable = new int[(int) Math.pow((double) mastermindAllowedNumber, combinationDigitNumber)][scoresList.size() + 1];
    }


   /* public int scoreCalculation() {
        int[][] solutionAndAttempt = new int[2][combinationDigitNumber];
        int score = 0;
        int minWeight = 0;
        // choix de la proposition de référence
        for (int i = 0; i < propositionsList.size(); i++) {
            solutionAndAttempt[0] = propositionsList[i];
            // choix de la proposition candidate
            for (int j = 0; j < propositionsList.length; j++) {
                if (i == j) {
                    j++;
                }
                solutionAndAttempt[1] = propositionsTable[j];
                // Calcul du score
                score = Integer.parseInt(compareAttemptAndSolution(solutionAndAttempt)[1]);
                // Ajout du score dans le tableau de score
                int k = 0;
                while (score != scoresList.get(k)) {
                    k++;
                }
                scoreTable[i][k]++;
            }
            // Recherche du poids Max de la proposition de référence
            int weight = 0;
            for (int j = 0; j <= scoresList.size(); j++) {
                if (weight < scoreTable[i][j]) {
                    weight = scoreTable[i][j];
                }
            }
            // Ajout du poids max dans le tableau de score
            scoreTable[i][scoresList.size() + 1] = weight;
            // Recherche du poids min parmi toutes les propositions candidates
            if (minWeight == 0) {
                minWeight = scoreTable[i][scoresList.size() + 1];
            } else if (minWeight > scoreTable[i][scoresList.size() + 1]) {
                minWeight = scoreTable[i][scoresList.size() + 1];
            }
        }
        return minWeight;
    }*/

    /*public int[] optimizedPropositionChoice(int minWeight) {
        int i = 0;
        while (scoreTable[i][scoresList.size() + 1] != minWeight) {
            i++;
        }
        return propositionsTable[i];
    }*/


}

