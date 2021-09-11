package spell;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class SpellCorrector implements ISpellCorrector {
    private ITrie trie;

    public SpellCorrector() {
        trie = new Trie();
    }

    @Override
    public void useDictionary(String dictionaryFileName) throws IOException {
        try {
            File file = new File(dictionaryFileName);
            Scanner reader = new Scanner(file);
            ArrayList<String> words = new ArrayList<>();
            while (reader.hasNextLine()) {
                String[] data = reader.nextLine().split("\\s+");
                words.addAll(Arrays.asList(data));
            }
            for (String word : words) {
                trie.add(word);
            }
            reader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    @Override
    public String suggestSimilarWord(String inputWord) {
        if (trie.find(inputWord) != null) {
            return inputWord.toLowerCase();
        } else {
            return findSimilarWord(inputWord);
        }
    }

    private String findSimilarWord(String inputWord) {
        ArrayList<String> suggestions = new ArrayList<>();
        String[] dictionaryWords = trie.toString().split("\\s+");
        checkEditDistance(suggestions, dictionaryWords, inputWord, 1);

        if (suggestions.size() == 0) {
            // Optimization
            ArrayList<String> dictionaryWordsList = new ArrayList<>();
            int minLength = inputWord.length() - 2;
            int maxLength = inputWord.length() + 2;
            int test = 0;
            for (String dictionaryWord : dictionaryWords) {
                if (dictionaryWord.length() <= maxLength || dictionaryWord.length() >= minLength) {
                    dictionaryWordsList.add(dictionaryWord);
                }
                test++;
                if (test == 10000) {
                    break;
                }
            }
            dictionaryWords = dictionaryWordsList.toArray(new String[dictionaryWordsList.size()]);
            System.out.println(dictionaryWords.length);
            ArrayList<String> suggestionsDistance2 = new ArrayList<>();
            checkEditDistance(suggestionsDistance2, dictionaryWords, inputWord, 2);
            if (suggestionsDistance2.size() == 0) {
                return null;
            } else if (suggestionsDistance2.size() == 1) {
                return suggestionsDistance2.get(0);
            } else {
                String wordToSuggest = findHighestFrequencyWord(suggestionsDistance2);
                if (wordToSuggest == null) {
                    wordToSuggest = findFirstInAlphabet(suggestionsDistance2);
                }

                return wordToSuggest;
            }
        } else if (suggestions.size() == 1) {
            return suggestions.get(0);
        } else {
            String wordToSuggest = findHighestFrequencyWord(suggestions);
            if (wordToSuggest == null) {
                wordToSuggest = findFirstInAlphabet(suggestions);
            }

            return wordToSuggest;
        }
    }

    private String findHighestFrequencyWord(ArrayList<String> suggestions) {
        int highestFrequency = 0;
        String wordToSuggest = null;

        for (String suggestion : suggestions) {
            int frequency = trie.find(suggestion).getValue();
            if (frequency > highestFrequency) {
                highestFrequency = frequency;
                wordToSuggest = suggestion;
            } else if (frequency == highestFrequency && !suggestion.equals(wordToSuggest)) {
                wordToSuggest = null;
            }
        }

        return wordToSuggest;
    }

    private String findFirstInAlphabet(ArrayList<String> suggestions) {
        String wordToSuggest = suggestions.get(0);
        for (int i = 1; i < suggestions.size(); i++) {
            Trie trie = (Trie)this.trie;
            INode node1 = trie.find(wordToSuggest);
            INode node2 = trie.find(suggestions.get(i));
            if (trie.compareNodes(node1, node2) == node2){
                wordToSuggest = suggestions.get(i);
            }
        }

        return wordToSuggest;
    }

    private void checkEditDistance(
        ArrayList<String> suggestions,
        String[] dictionaryWords,
        String inputWord,
        int deletionDistance
    ) {
        for (String dictionaryWord : dictionaryWords) {
            String[] deletionDistanceWords = getDeletionDistanceWords(dictionaryWord);
            String[] transpositionDistanceWords = getTranspositionDistanceWords(dictionaryWord);
            String[] alterationDistanceWords = getAlterationDistanceWords(dictionaryWord);
            String[] insertionDistanceWords = getInsertionDistanceWords(dictionaryWord);

            if (deletionDistance == 1) {
                if (findMatch(deletionDistanceWords, inputWord)) {
                    suggestions.add(dictionaryWord);
                }
                if (findMatch(transpositionDistanceWords, inputWord)) {
                    suggestions.add(dictionaryWord);
                }
                if (findMatch(alterationDistanceWords, inputWord)) {
                    suggestions.add(dictionaryWord);
                }
                if (findMatch(insertionDistanceWords, inputWord)) {
                    suggestions.add(dictionaryWord);
                }
            } else {
                checkEditDistance2Words(suggestions, dictionaryWord, deletionDistanceWords, inputWord);
                checkEditDistance2Words(suggestions, dictionaryWord, transpositionDistanceWords, inputWord);
                checkEditDistance2Words(suggestions, dictionaryWord, alterationDistanceWords, inputWord);
                checkEditDistance2Words(suggestions, dictionaryWord, insertionDistanceWords, inputWord);
            }
        }
    }

    private void checkEditDistance2Words(
        ArrayList<String> suggestions,
        String dictionaryWord,
        String[] wordsToCheck,
        String inputWord
    ) {
        if (wordsToCheck == null) {
            return;
        }
        for (String wordToCheck : wordsToCheck) {
            String[] deletionDistance2Words = getDeletionDistanceWords(wordToCheck);
            if (findMatch(deletionDistance2Words, inputWord)) {
                suggestions.add(dictionaryWord);
            }
            String[] transpositionDistance2Words = getTranspositionDistanceWords(wordToCheck);
            if (findMatch(transpositionDistance2Words, inputWord)) {
                suggestions.add(dictionaryWord);
            }
            String[] alterationDistance2Words = getAlterationDistanceWords(wordToCheck);
            if (findMatch(alterationDistance2Words, inputWord)) {
                suggestions.add(dictionaryWord);
            }
            String[] insertionDistance2Words = getInsertionDistanceWords(wordToCheck);
            if (findMatch(insertionDistance2Words, inputWord)) {
                suggestions.add(dictionaryWord);
            }
        }
    }

    private boolean findMatch(String[] generatedStrings, String inputString) {
        if (generatedStrings == null) {
            return false;
        }
        for (String generatedString : generatedStrings) {
            if (Objects.equals(generatedString, inputString)) {
                return true;
            }
        }

        return false;
    }

    private String[] getDeletionDistanceWords(String dictionaryWord) {
        if (dictionaryWord.length() <= 1) {
            return null;
        }
        String[] suggestions = new String[dictionaryWord.length()];
        for (int i = 0; i < dictionaryWord.length(); i++) {
            String suggestion = dictionaryWord.substring(0, i) + dictionaryWord.substring(i + 1);
            suggestions[i] = suggestion;
        }

        return suggestions;
    }

    private String[] getTranspositionDistanceWords(String dictionaryWord) {
        if (dictionaryWord.length() <= 1) {
            return null;
        }
        String[] suggestions = new String[dictionaryWord.length() - 1];
        for (int i = 0; i < dictionaryWord.length() - 1; i++) {
            String swappedChars = dictionaryWord.substring(i+1, i+2) + dictionaryWord.charAt(i);
            String suggestion = dictionaryWord.substring(0, i) + swappedChars + dictionaryWord.substring(i + 2);
            suggestions[i] = suggestion;
        }

        return suggestions;
    }

    private String[] getAlterationDistanceWords(String dictionaryWord) {
        String[] suggestions = new String[dictionaryWord.length() * 25];
        int numSuggestions = 0;
        for (int w = 0; w < dictionaryWord.length(); w++) {
            for (int c = 0; c < 26; c++) {
                char replacementChar = (char)('a' + c);
                if (dictionaryWord.codePointAt(w) == replacementChar) {
                    continue;
                }
                String suggestion = dictionaryWord.substring(0, w) + replacementChar + dictionaryWord.substring(w + 1);
                suggestions[numSuggestions] = suggestion;
                numSuggestions++;
            }
        }

        return suggestions;
    }

    private String[] getInsertionDistanceWords(String dictionaryWord) {
        String[] suggestions = new String[(dictionaryWord.length() + 1) * 26];
        int numSuggestions = 0;
        for (int w = 0; w < dictionaryWord.length() + 1; w++) {
            for (int c = 0; c < 26; c++) {
                char insertionChar = (char)('a' + c);
                String suggestion = dictionaryWord.substring(0, w) + insertionChar + dictionaryWord.substring(w);
                suggestions[numSuggestions] = suggestion;
                numSuggestions++;
            }
        }

        return suggestions;
    }
}
