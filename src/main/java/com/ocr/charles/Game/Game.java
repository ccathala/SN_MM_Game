package com.ocr.charles.Game;

import com.ocr.charles.Exceptions.PlayerInputError;
import com.ocr.charles.Menu;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.Random;
import java.util.Scanner;

public abstract class Game {

    static final Logger logger = LogManager.getLogger(Menu.class);

    // Players list
    protected enum Player {
        Human,
        Ai;
    }

    private int[][] solutionReturn; /* int[2][x digit] / index 0 player solution  / index 1 AI solution  */
    protected int[][] answerReturn; /* int[2][x digit] / index 0 player answer  / index 1 AI answer  */
    private String[] result = new String[2]; /* Index 0 => player result/Index 1 Ai result */
    protected Player currentPlayer = Player.Human; /* Define current player*/
    protected int combinationDigitNumber; /*Set the number of digit for combinations, imported from config*/
    private boolean devMode = false; /* true if dev mode is enabled*/
    protected int devModeFromConfig; /* dev mode parameter for activation by importation of value from config / 1 if dev mode enabled*/
    protected int attemptSetting; /* Number of attempt parameter, imported from config*/
    private int allowedAttempts; /* = attemptSetting -1 to take in count 0*/
    protected int attemptNumber; /* Value of the current attempt*/
    private boolean[] gameOver = new boolean[3];/*index 0 : is game over / index 1 : does player win*/


    /**
     * Display game heading
     * Overrided method, display heading for both Game
     */
    public abstract void displayGameHeading();

    /**
     * Launch new game
     *
     * @param args if equal "dev" set on dev mode which display AI secret combination for both challenger mode
     */
    public void newGame(String args) {
        int mode = 0;
        // dev mode setting
        if (args.equals("dev")) {
            devMode = true;
        }
        // Display mode menu
        while (mode != 4) {
            mode = DisplayAndChooseGameMode();
            gameOver[2] = mode == 4;
            while (!gameOver[2]) {
                logger.info("--------------------Nouvelle partie----------------------");
                initGame(); /* init game parameters */
                boolean duelInitialized = false;
                boolean duelAttempt = false; /* used for duel mode, increment attempt only if true */
                while (!gameOver[0]) { /*index 0 => game is over: true/false*/
                    attemptNumber = attemptSetting - allowedAttempts; /*Set attempt number*/
                    // init duel mode, each player choose combination, display welcome sentences
                    if (mode == 3 && !duelInitialized) {
                        duelInitialized = initDuelMode();
                        logger.info("Mode choisi : Duel");
                    }
                    // Run mode sequence
                    int[][] returnMode = modeSequence(mode, currentPlayer, result[currentPlayer.ordinal()]);
                    int[] solution = returnMode[0];
                    int[] answer = returnMode[1];
                    logger.info("---------------------------------------------------------");
                    logger.info("Tour " + currentPlayer + " n°" + attemptNumber + " :");
                    logger.info("Solution : " + Arrays.toString(solution));
                    logger.info("Réponse : " + Arrays.toString(answer));
                    //Compare solution and answer
                    result[currentPlayer.ordinal()] = compareSolutionAndAttempt(solution, answer);
                    //Display indications
                    displayIndications(result[currentPlayer.ordinal()]);
                    //Analyse results
                    gameOver = analyseResults(isGameWon(result[currentPlayer.ordinal()]), mode, currentPlayer.toString());
                    // Display results if game is over
                    if (gameOver[0]) {
                        displayResults(mode, gameOver[1]);
                    }
                    //Increment attempt value for both mode
                    if (mode == 1 || mode == 2 || (mode == 3 && duelAttempt)) {
                        allowedAttempts = allowedAttempts - 1;
                        logger.info("Tentatives restantes : " + (allowedAttempts + 1));
                    }
                    //Switch player in duel mode
                    if (mode == 3) {
                        switchPlayer();
                        duelAttempt = !duelAttempt;
                        logger.info("Prochain joueur : " + currentPlayer);
                    }
                }
                // Display end game menu
                gameOver[2] = displayNewGameOrMainMenuAndRecordInputPlayerFor();
            }
        }
    }


    /**
     * Init new game parameters
     */
    public void initGame() {
        allowedAttempts = attemptSetting - 1;
        solutionReturn = new int[2][combinationDigitNumber];
        answerReturn = new int[2][combinationDigitNumber];
        gameOver[0] = false;
        gameOver[1] = false;
        if (devModeFromConfig == 1) {
            devMode = true;
        }
        if (devMode) {
            System.out.println("Mode développeur activé.");
            System.out.println();
        }
        logger.info("-----------------------Resume init-----------------------");
        logger.info("Développeur mode : " + devMode);
        logger.info("Nombre de digits : " + combinationDigitNumber);
        logger.info("Tentatives autorisées : " + attemptSetting);
        logger.info("Tentatives autorisées -1 : " + allowedAttempts);
        logger.info("Partie terminée : " + gameOver[0]);
        logger.info("Joueur gagne : " + gameOver[1]);
    }

    /**
     * Define throwing conditions for PlayerInputError exception when player have to choose a mode
     *
     * @param chosenMode mode menu choice made by user
     * @throws PlayerInputError Exception display message when user input error
     */
    private void playerChooseCorrectGameModeOption(int chosenMode) throws PlayerInputError {
        if (chosenMode < 1 || chosenMode > 4) throw new PlayerInputError();
    }

    /**
     * Display mode menu and record player input for chosen mode
     * return int to set variable "mode" of newGame method
     */
    private int DisplayAndChooseGameMode() {
        int mode = 0;
        Scanner sc = new Scanner(System.in);
        boolean correctInput;
        do {
            displayGameHeading(); /*Display game heading for both game*/
            System.out.println();
            System.out.println("Choisi ton mode de jeu");
            System.out.println("1 - Mode Challenger");
            System.out.println("2 - Mode Defender");
            System.out.println("3 - Mode Duel");
            System.out.println("4 - Retour au menu principal");
            try {
                mode = sc.nextInt(); /*Record user input for mode choice*/
                playerChooseCorrectGameModeOption(mode); /*Set throwing conditions of the exception*/
                correctInput = true;
            } catch (InputMismatchException e) {
                sc.next();
                System.out.println("Vous devez saisir un chiffre parmi les choix proposés.");
                System.out.println();
                logger.info("Erreur saisie utilisateur");
                correctInput = false;
            } catch (PlayerInputError e) {
                System.out.println("Vous n'avez pas choisi parmi les choix proposés.");
                System.out.println();
                logger.info("Erreur saisie utilisateur");
                correctInput = false;
            }
        } while (!correctInput); /*Repeat instructions until user input is correct*/
        if (mode == 4) {
            logger.info("Retour menu choix du jeu");
        }

        return mode;
    }

    /**
     * Import parameters from file config.properties
     */
    protected abstract void importParameterFromConfigProperties();

    /**
     * @param chosenMode user input
     * @param currentPlayer AI or HUMAN
     * @param result value from compareSolutionAndAttempt method for previous attempt
     * @return int[] solution and int[] answer
     */
    private int[][] modeSequence(int chosenMode, Player currentPlayer, String result) {
        if (chosenMode == 1 || (chosenMode == 3 && currentPlayer.ordinal() == 0)) {
            return challengerSequence(chosenMode);
        } else if (chosenMode == 2 || (chosenMode == 3 && currentPlayer.ordinal() == 1)) {
            return defenderSequence(result, chosenMode);
        }
        return new int[2][4];
    }

    /**
     * @param chosenMode user input
     * @return int[] solution and int[] answer
     */
    private int[][] challengerSequence(int chosenMode) {
        if (attemptNumber == 1 && chosenMode != 3) {
            currentPlayer = Player.Human;
            System.out.println("Bienvenue Challenger !");
            System.out.println();
            displayInstruction("challenger");
            System.out.println();
            System.out.println("Le décodeur dispose de " + attemptSetting + " tentatives pour trouver la combinaison cachée.");
            solutionReturn[1] = aiChooseRandomCombination();/*Record hidden combination*/
            logger.info("Mode choisi : Challenger");
        }
        System.out.println("----------------------------------");
        System.out.print("Tour joueur n°" + attemptNumber + " : ");
        int[][] challengerReturn = new int[2][combinationDigitNumber];
        answerReturn[0] = recordPlayerCombinationInput();/*Record player attempt*/
        challengerReturn[0] = solutionReturn[1];
        challengerReturn[1] = answerReturn[0];
        return challengerReturn;
    }

    protected abstract void displayInstruction(String gameMode);

    /**
     * AI choose a random combination composed by X digits between 0 and 9
     */
    private int[] aiChooseRandomCombination() {
        int[] aiHiddenCombination = new int[combinationDigitNumber];
        StringBuilder aiHiddenCombinationString = new StringBuilder();
        for (int i = 0; i < combinationDigitNumber; i++) {
            aiHiddenCombination[i] = randomNumber();
            aiHiddenCombinationString.append(aiHiddenCombination[i]);
        }
        if (devMode) {
            System.out.print("Combinaison secrète : " + aiHiddenCombinationString);
        }
        System.out.println();
        return aiHiddenCombination;
    }

    /**
     * @return random int from 0 to 9
     */
    protected abstract int randomNumber();

    /**
     * @param result value from compareSolutionAndAttempt method for previous attempt
     * @param chosenMode user input
     * @return int[] solution and int[] answer
     */
    private int[][] defenderSequence(String result, int chosenMode) {
        if (attemptNumber == 1 && chosenMode != 3) {
            currentPlayer = Player.Ai;
            System.out.println("Bienvenue Défenseur !");
            System.out.println();
            displayInstruction("defender");
            solutionReturn[0] = recordPlayerCombinationInput();/*Record hidden combination*/
            System.out.println();
            System.out.println("Le décodeur dispose de " + attemptSetting + " tentatives pour trouver la combinaison cachée.");
            logger.info("Mode choisi : Defender");
        }
        System.out.println("----------------------------------");
        System.out.print("Tour ordinateur n°" + attemptNumber + " : ");
        int[][] defenderReturn = new int[2][combinationDigitNumber];
        answerReturn[1] = generateAndDisplayAiAnswer(result);/*Record AI attempt*/
        defenderReturn[0] = solutionReturn[0];
        defenderReturn[1] = answerReturn[1];
        return defenderReturn;
    }

    /**
     * @param result value from compareSolutionAndAttempt method for previous attempt
     * @return int[] answer
     */
    protected abstract int[] generateAndDisplayAiAnswer(String result);

    /**
     * @param playerInput Player input splitted in String[]
     * @throws PlayerInputError Exception display message when incorrect input
     */
    protected abstract void playerCorrectCombinationInput(String[] playerInput) throws PlayerInputError;

    protected abstract void errorSentence();

    /**
     * Record player input combination composed by X digits between 0 and 9
     * Increment by 1 the value of attempNumberPlayer
     */
    private int[] recordPlayerCombinationInput() {
        boolean correctInput;
        int[] playerCombinationInput = new int[combinationDigitNumber];
        do {
            try {
                String[] playerCombinationInputString;
                Scanner sc = new Scanner(System.in);
                playerCombinationInputString = sc.nextLine().split("");
                playerCorrectCombinationInput(playerCombinationInputString);
                for (int i = 0; i < combinationDigitNumber; i++) {
                    playerCombinationInput[i] = Integer.parseInt(playerCombinationInputString[i]);
                }
                correctInput = true;
            } catch (PlayerInputError|NumberFormatException e) {
                errorSentence();
                if(currentPlayer.ordinal()==0) {
                    System.out.println("----------------------------------");
                    System.out.print("Tour joueur n°" + attemptNumber + " : ");
                }
                correctInput = false;
                }
        } while (!correctInput);
        return playerCombinationInput;
    }

    /**
     * Initialize duel mode by display welcome sentence, instructions and recording hidden combination for bot players
     */
    private boolean initDuelMode() {
        currentPlayer=Player.Ai;
        System.out.println("C'est l'heure du Duel! Trouvez la combinaison secrète en " + attemptSetting + " tentatives.");
        System.out.println();
        System.out.println("Joueur, choisi une combinaison cachée à " + combinationDigitNumber + " chiffres.");
        solutionReturn[0] = recordPlayerCombinationInput();/*Record player hidden combination*/
        solutionReturn[1] = aiChooseRandomCombination();/*Record AI hidden combination*/
        currentPlayer = Player.valueOf(toss()); /* Choose randomly who begins Player or AI, display who begins*/
        return true;
    }

    /**
     * Do a toss to define the first player
     */
    private String toss() {
        Random random = new Random();
        int toss = random.nextInt(2);
        String currentPlayer;
        String playerWhoBegins;
        if (toss == 0) {
            playerWhoBegins = "Joueur commence !";
            currentPlayer = "Human";
        } else {
            playerWhoBegins = "Ordinateur commence !";
            currentPlayer = "Ai";
        }
        System.out.println(playerWhoBegins);
        return currentPlayer;

    }

    /**
     * Switch player turn for duel mode
     */
    private void switchPlayer() {
        if (currentPlayer.toString().equals("Human")) {
            currentPlayer = Player.Ai;
        } else {
            currentPlayer = Player.Human;
        }
    }

    /**
     * @param solution current solution formatted in int[combinationDigitNumber]
     * @param answer generated answer formatted in int[combinationDigitNumber]
     * @return comparaison result format: "++-==-" or score format ex: "40"
     */
    protected abstract String compareSolutionAndAttempt(int[] solution, int[] answer);

    /**
     * @param result from compareSolutionAndAttempt
     */
    protected abstract void displayIndications(String result);

    /**
     * @param result from compareSolutionAndAttempt
     * @return true if game is won whoever the winner
     */
    protected abstract boolean isGameWon(String result);

    /**
     * Analyze results and end the game if win condition is true
     *
     * @param chosenMode    user input for mode
     * @param currentPlayer AI or human
     * @return boolean gameOver = true if game is ended
     */
    private boolean[] analyseResults(boolean isGameWon, int chosenMode, String currentPlayer) {
        if (isGameWon) {
            gameOver[0] = true;
            if (chosenMode == 1) {
                gameOver[1] = true;
            } else if (chosenMode == 2) {
                gameOver[1] = false;
            } else if (chosenMode == 3) {
                gameOver[1] = currentPlayer.equals("Human");
            }
        } else if (allowedAttempts == 0) {
            gameOver[0] = true;
            if (chosenMode == 1) {
                gameOver[1] = false;
            } else if (chosenMode == 2) {
                gameOver[1] = true;
            } else if (chosenMode == 3) {
                gameOver[1] = !currentPlayer.equals("Human");
            }
        }
        logger.info("La partie est terminée : " + gameOver[0]);
        logger.info("Le joueur remporte la partie : " + gameOver[1]);
        return gameOver;
    }

    /**
     * Display the win or lose sentence according to results
     *
     * @param chosenMode user input for mode
     * @param playerWin  Exception display message when user input error
     */
    private void displayResults(int chosenMode, boolean playerWin) {
        StringBuilder answer = new StringBuilder();
        if (playerWin) {

            System.out.print("Bravo ");
            if (chosenMode == 1) {
                System.out.print("tu as trouvé la combinaison, ");
            } else if (chosenMode == 2) {
                System.out.print("l'ordinateur n'a pas trouvé la combinaison, ");
            } else if (chosenMode == 3) {
                System.out.print("tu as trouvé la combinaison avant l'ordinateur, ");
            }

            System.out.println("tu remportes la partie !");

        } else {
            for (int i = 0; i < combinationDigitNumber; i++) {
                answer.append(solutionReturn[1][i]);
            }
            if (chosenMode == 1) {
                System.out.println("Tu as utilisé toutes tes tentatives, l'ordinateur remporte la partie !");
                System.out.println();
                System.out.println("La solution était " + answer + ".");

            } else if (chosenMode == 2) {
                System.out.println("L'ordinateur a trouvé la combinaison, il remporte la partie !");
            } else if (chosenMode == 3) {
                System.out.println("L'ordinateur a trouvé la combinaison avant toi, il remporte la partie !");
                System.out.println();
                System.out.println("La solution était " + answer + ".");
            }
        }
        System.out.println();
    }

    /**
     * Define the throwing conditions for PlayerInputError exception when player have to choose new game or quit game
     *
     * @param choice play a new game with same parameters/return mode menu/quit application
     * @throws PlayerInputError Exception display message when user input error
     */
    private void playerChooseCorrectNewGameOption(int choice) throws PlayerInputError {
        if (choice < 1 || choice > 3) throw new PlayerInputError();
    }

    /**
     * Dispaly new game menu and record input player to choose new game or quit game option
     *
     * @return true if player quit game / false if player play another game
     */
    private boolean displayNewGameOrMainMenuAndRecordInputPlayerFor() {
        Scanner sc = new Scanner(System.in);
        boolean playerQuitGame = false;
        boolean correctInput;
        int endGameChoice = 0;
        System.out.println("Partie Terminée !");
        System.out.println();
        do {

            System.out.println("1 - Rejouer une partie");
            System.out.println("2 - Retour au menu mode de jeu");
            System.out.println("3 - Quitter l'application");
            try {
                endGameChoice = sc.nextInt();
                playerChooseCorrectNewGameOption(endGameChoice);
                correctInput = true;
            } catch (InputMismatchException e) {
                sc.next();
                System.out.println("Vous devez saisir un chiffre parmi les choix proposés.");
                System.out.println();
                logger.info("Erreur saisie utilisateur");
                correctInput = false;
            } catch (PlayerInputError e) {
                System.out.println("Choisissez parmi les choix proposés.");
                System.out.println();
                logger.info("Erreur saisie utilisateur");
                correctInput = false;
            }
        } while (!correctInput);
        if (endGameChoice == 1) {
            logger.info("---------------------------------------------------------");
            logger.info("Joueur rejoue une partie.");
        } else if (endGameChoice == 2) {
            playerQuitGame = true;
            logger.info("---------------------------------------------------------");
            logger.info("Retour menu choix du mode.");
        } else if (endGameChoice == 3) {
            logger.info("--------------FERMETURE DE L'APPLICATION-----------------");
            logger.info("---------------------------------------------------------");
            System.exit(1);
        }
        return playerQuitGame;
    }
}
