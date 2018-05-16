package mcmc_decoder;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class CompLingFinal {

    double[][] bigramDist = new double[27][27];
    List<String> alphaList = Arrays.asList("a", "b", "c", "d", "e", "f", "g", "h", "i", "j",
            "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z");

    public CompLingFinal() {
        super();
    }

    //Processes given plaintext file, makes lowercase and removes punctuation. Saves as ASCII file. 
    public void processText(String plainText) {
        try (BufferedReader reader = new BufferedReader(new FileReader(plainText))) {
            String line;
            PrintWriter writer = new PrintWriter("src/references/ReferenceProcessed.txt", "ASCII");
            while ((line = reader.readLine()) != null) {
                String processed = (line.replaceAll("[^a-zA-Z ]", "").toLowerCase());
                writer.println(processed);
            }
            reader.close();
            writer.close();
        } catch (Exception e) {
            System.err.format("Exception occurred trying to read '%s'.", plainText);
        }
    }

    //Generates a transition frequency matrix from reference file. 
    //String text is the file name of the processed reference
    public void bigramDist(String text) {
        try (BufferedReader reference = new BufferedReader(new FileReader(text))) {
            for (String line = reference.readLine(); line != null; line = reference.readLine()) {
                if (!line.isEmpty()) {
                    for (int i = 0; i < line.length() - 1; i++) {
                        char ch1 = line.charAt(i);
                        char ch2 = line.charAt(i + 1);
                        try {
                            if ((int) ch1 != 32 && (int) ch2 != 32) {
                                int ascii1 = (int) ch1 - 97;
                                int ascii2 = (int) ch2 - 97;
                                bigramDist[ascii1][ascii2]++;
                            } else {
                                int ascii1 = (int) ch1 - 97;
                                int ascii2 = (int) ch2 - 97;
                                if ((int) ch1 == 32 && (int) ch2 == 32) {
                                    bigramDist[26][26]++;
                                } else if ((int) ch1 == 32) {
                                    bigramDist[26][ascii2]++;
                                } else if ((int) ch2 == 32) {

                                    bigramDist[ascii1][26]++;
                                }
                            }

                        } catch (Exception e) {
                            System.err.format("Something went wrong: %s", e);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.format("Exception occurred trying to read '%s': %s", text, e);
        }

        for (int row = 0; row < bigramDist.length; row++) {
            int totalRow = 0;

            //Laplace (Add-1) smoothing here...
            for (int col = 0; col < bigramDist[row].length; col++) {
                bigramDist[row][col]++;
                totalRow += bigramDist[row][col];
            }

            //Normalize here...
            for (int c = 0; c < bigramDist[row].length; c++) {
                bigramDist[row][c] = (double) (bigramDist[row][c] / totalRow);
            }
        }
    }

    //Calculate score for given decryption
    public double logLikelihood(String document) {
        double score = 0.0;

        for (int c = 0; c < document.length() - 1; c++) {
            if ((int) document.charAt(c) != 32 && (int) document.charAt(c + 1) != 32) {
                int index1 = alphaList.indexOf(Character.toString(document.charAt(c)));
                int index2 = alphaList.indexOf(Character.toString(document.charAt(c + 1)));
                if (index1 > -1 && index2 > -1) {
                    score += Math.log(bigramDist[index1][index2]);
                }
            } else {
                if ((int) document.charAt(c) == 32 && (int) document.charAt(c + 1) == 32) {
                    score += Math.log(bigramDist[26][26]);
                } else if ((int) document.charAt(c) == 32) {
                    int index2 = alphaList.indexOf(Character.toString(document.charAt(c + 1)));
                    score += Math.log(bigramDist[26][index2]);
                } else if ((int) document.charAt(c + 1) == 32) {
                    int index1 = alphaList.indexOf(Character.toString(document.charAt(c)));

                    score += Math.log(bigramDist[index1][26]);
                }
            }
        }
        return score;
    }

    //Decrypts a cipher when given a key
    public String decode(List<String> key, String cipher) {
        String newText = "";

        for (int c = 0; c < cipher.length(); c++) {
            if ((int) cipher.charAt(c) != 32) {
                int trans = key.indexOf(Character.toString(cipher.charAt(c)));
                newText += alphaList.get(trans);
            } else {
                newText += " ";
            }
        }
        return newText;
    }

    //Encrypts a plaintext without punction or uppercase letters
    public String encode(String document) {
        List<String> randomList = new ArrayList<>(alphaList);
        Collections.shuffle(randomList);
        String cipherText = "";

        for (int c = 0; c < document.length(); c++) {
            if ((int) document.charAt(c) != 32) {
                int trans = alphaList.indexOf(Character.toString(document.charAt(c)));
                cipherText += randomList.get(trans);
            } else {
                cipherText += " ";
            }
        }

        return cipherText;
    }

    //Swaps two letters in a key
    public List<String> swap(List<String> key) {

        List<String> swapList = new ArrayList<>(key);

        Random r = new Random();
        int p1 = r.nextInt((25 - 0) + 1) + 0;
        int p2 = r.nextInt((25 - 0) + 1) + 0;
        String getChar1 = key.get(p1);
        String getChar2 = key.get(p2);
        swapList.set(p1, getChar2);
        swapList.set(p2, getChar1);

        return swapList;
    }

    //Acceptance calculations
    public boolean accept(double proposed, double current, double scaleP) {
        Random r = new Random();
        double uniform = r.nextDouble();

        //return (uniform < Math.exp(proposed - current));
        return (uniform < Math.exp((proposed * scaleP) - (current * scaleP)));

    }

    public ArrayList substitutionMetropolisHastings(String cipher) {

        int iterations = 1000;
        int accepted = 0;
        double inf = Double.POSITIVE_INFINITY;

        //Step 1: Initialize...
        List<String> currentList = new ArrayList<>(alphaList);
        Collections.shuffle(currentList);

        ArrayList bestKey = new ArrayList();
        bestKey.add(0, cipher);
        bestKey.add(1, (double) (inf * -1));

        for (int i = 0; accepted < iterations; i++) {

            double scaleP = Math.pow((i + 1), 3.0 / 4.0);

            //Step 2: Swap two letters...
            List<String> newList = new ArrayList<>(currentList);
            List<String> proposedList = swap(newList);

            String proposed_doc = decode(proposedList, cipher);
            String current_doc = decode(currentList, cipher);

            //Step 3: Calculate Scores...
            double proposed_log = logLikelihood(proposed_doc);
            double current_log = logLikelihood(current_doc);

            if (proposed_log > (double) bestKey.get(1)) {
                bestKey.set(0, proposed_doc);
                bestKey.set(1, proposed_log);
            }

            //Step 4: Accept or reject...
            if (accept(proposed_log, current_log, scaleP)) {
                currentList = proposedList;
                accepted++;
            }
            System.out.println(i + ":" + accepted + ": " + bestKey);
        }
        return bestKey;
    }

    public void perform() {
        //processText("src/mcmc_decoder/war-and-peace.txt.txt");
        String originalText = "insert text to be made into a cipher here";
        String cipher = encode(originalText);

        //Run Program
        bigramDist("src/references/ReferenceProcessed.txt");
        ArrayList result = substitutionMetropolisHastings(cipher);

        //Calculates accuracy.
        float accuracy = 0;
        String bestDoc = (String) result.get(0);

        for (int r = 0; r < cipher.length(); r++) {
            if (bestDoc.charAt(r) == originalText.charAt(r)) {
                accuracy++;
            }
        }
        accuracy = (accuracy / cipher.length()) * 100;
        System.out.println("Accuracy: " + accuracy);
    }

    public static void main(String[] args) {
        CompLingFinal decode = new CompLingFinal();
        decode.perform();
        System.exit(0);
    }

}
