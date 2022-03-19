/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim;

import edu.kit.kastel.mcse.ardoco.core.text.IWord;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

/**
 * A ComparisonContext contains all information that can be used for comparing similarity between objects that occur
 * within ArDoCo.
 */
public record ComparisonContext(@Nonnull String firstString, @Nonnull String secondString, @Nullable IWord firstWord, @Nullable IWord secondWord,
        boolean lemmatize) {

    public ComparisonContext(@Nonnull String firstString, @Nonnull String secondString) {
        this(firstString, secondString, null, null, false);
    }

    public ComparisonContext(@Nonnull String firstString, @Nonnull String secondString, boolean lemmatize) {
        this(firstString, secondString, null, null, lemmatize);
    }

    public ComparisonContext(IWord firstWord, IWord secondWord, boolean lemmatize) {
        this(firstWord.getText(), secondWord.getText(), firstWord, secondWord, lemmatize);
    }

    /**
     * Finds the most appropriate string representation by the first object in this comparison object. This method can
     * be used as a shorthand to avoid going through all variables that could possibly represent the first object.
     *
     * @return the most appropriate string presentation of the first object in this comparison
     */
    @Nonnull
    public String firstTerm() {
        return findAppropriateTerm(firstString, firstWord);
    }

    /**
     * Finds the most appropriate string representation by the second object in this comparison object. This method can
     * be used as a shorthand to avoid going through all variables that could possibly represent the second object.
     *
     * @return the most appropriate string presentation of the second object in this comparison
     */
    @Nonnull
    public String secondTerm() {
        return findAppropriateTerm(secondString, secondWord);
    }

    /**
     * TODO
     * @return
     */
    public List<String> firstTerms() {
        return SubWordUtils.getSubWords(firstTerm());
    }

    /**
     * TODO
     * @return
     */
    public List<String> secondTerms() {
        return SubWordUtils.getSubWords(secondTerm());
    }

    private String findAppropriateTerm(@Nonnull String string, @Nullable IWord word) {
        Objects.requireNonNull(string);

        if (word != null) {
            return lemmatize ? word.getLemma() : word.getText();
        } else {
            return string;
        }
    }

}
