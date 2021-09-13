package spell;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class SpellCorrector implements ISpellCorrector {
    private ITrie trie;

    public SpellCorrector() {
        trie = new Trie();
    }

    @Override
    public void useDictionary(String dictionaryFileName) {
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
        getByEditDistance(suggestions, inputWord, 1);

        if (suggestions.size() == 0) {
            ArrayList<String> suggestionsDistance2 = new ArrayList<>();
            getByEditDistance(suggestionsDistance2, inputWord, 2);
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

    private void getByEditDistance(
        ArrayList<String> suggestions,
        String inputWord,
        int editDistance
    ) {
        HashSet<String> generatedStrings = new HashSet<>();
        getDeletionDistanceWords(inputWord, generatedStrings);
        getTranspositionDistanceWords(inputWord, generatedStrings);
        getAlterationDistanceWords(inputWord, generatedStrings);
        getInsertionDistanceWords(inputWord, generatedStrings);

        for (String generatedString : generatedStrings) {
            if (trie.find(generatedString) != null) {
                suggestions.add(generatedString);
            }
        }

        if (editDistance == 2) {
            HashSet<String> generatedStringsDistance1 = new HashSet<>(generatedStrings);
            for (String generatedStringDistance1 : generatedStringsDistance1) {
                getDeletionDistanceWords(generatedStringDistance1, generatedStrings);
                getTranspositionDistanceWords(generatedStringDistance1, generatedStrings);
                getAlterationDistanceWords(generatedStringDistance1, generatedStrings);
                getInsertionDistanceWords(generatedStringDistance1, generatedStrings);
            }
            for (String generatedString : generatedStrings) {
                if (trie.find(generatedString) != null) {
                    suggestions.add(generatedString);
                }
            }
        }
    }

    private void getDeletionDistanceWords(String wordToGenerateFrom, HashSet<String> generatedStrings) {
        if (wordToGenerateFrom.length() <= 1) {
            return;
        }
        for (int i = 0; i < wordToGenerateFrom.length(); i++) {
            String suggestion = wordToGenerateFrom.substring(0, i) + wordToGenerateFrom.substring(i + 1);
            generatedStrings.add(suggestion);
        }
    }

    private void getTranspositionDistanceWords(String wordToGenerateFrom, HashSet<String> generatedStrings) {
        if (wordToGenerateFrom.length() <= 1) {
            return;
        }
        for (int i = 0; i < wordToGenerateFrom.length() - 1; i++) {
            String swappedChars = wordToGenerateFrom.substring(i+1, i+2) + wordToGenerateFrom.charAt(i);
            String suggestion = wordToGenerateFrom.substring(0, i) + swappedChars + wordToGenerateFrom.substring(i + 2);
            generatedStrings.add(suggestion);
        }
    }

    private void getAlterationDistanceWords(String wordToGenerateFrom, HashSet<String> generatedStrings) {
        for (int w = 0; w < wordToGenerateFrom.length(); w++) {
            for (int c = 0; c < 26; c++) {
                char replacementChar = (char)('a' + c);
                if (wordToGenerateFrom.codePointAt(w) == replacementChar) {
                    continue;
                }
                String suggestion = wordToGenerateFrom.substring(0, w) + replacementChar + wordToGenerateFrom.substring(w + 1);
                generatedStrings.add(suggestion);
            }
        }
    }

    private void getInsertionDistanceWords(String wordToGenerateFrom, HashSet<String> generatedStrings) {
        for (int w = 0; w < wordToGenerateFrom.length() + 1; w++) {
            for (int c = 0; c < 26; c++) {
                char insertionChar = (char)('a' + c);
                String suggestion = wordToGenerateFrom.substring(0, w) + insertionChar + wordToGenerateFrom.substring(w);
                generatedStrings.add(suggestion);
            }
        }
    }
}
