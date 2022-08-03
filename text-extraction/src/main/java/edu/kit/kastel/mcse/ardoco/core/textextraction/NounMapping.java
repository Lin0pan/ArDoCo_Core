/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.textextraction;

import static edu.kit.kastel.informalin.framework.common.AggregationFunctions.AVERAGE;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.set.ImmutableSet;
import org.eclipse.collections.api.set.MutableSet;

import edu.kit.kastel.informalin.framework.common.AggregationFunctions;
import edu.kit.kastel.informalin.framework.common.JavaUtils;
import edu.kit.kastel.mcse.ardoco.core.api.agent.IClaimant;
import edu.kit.kastel.mcse.ardoco.core.api.data.Confidence;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.IPhrase;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.IWord;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.INounMapping;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.MappingKind;
import edu.kit.kastel.mcse.ardoco.core.common.util.CommonUtilities;
import edu.kit.kastel.mcse.ardoco.core.common.util.SimilarityUtils;

/**
 * The Class NounMapping is a basic realization of {@link INounMapping}.
 *
 * @author Sophie Schulz
 * @author Jan Keim
 */
public class NounMapping implements INounMapping {

    /* Main reference */
    private final ImmutableList<IWord> referenceWords;

    /* Words are the references within the text */
    private final MutableList<IWord> words;

    private final MutableList<IWord> coreferences = Lists.mutable.empty();

    /* the different surface forms */
    private final MutableList<String> surfaceForms;

    private Map<MappingKind, Confidence> distribution;

    private static final AggregationFunctions DEFAULT_AGGREGATOR = AVERAGE;

    /**
     * Instantiates a new noun mapping.
     */
    public NounMapping(ImmutableList<IWord> words, Map<MappingKind, Confidence> distribution, ImmutableList<IWord> referenceWords,
            ImmutableList<String> surfaceForms) {
        this.words = Lists.mutable.withAll(words);
        initializeDistribution(distribution);
        this.referenceWords = Lists.immutable.withAll(referenceWords);
        this.surfaceForms = Lists.mutable.withAll(surfaceForms);
    }

    /**
     * Instantiates a new noun mapping.
     */
    public NounMapping(ImmutableList<IWord> words, MappingKind kind, IClaimant claimant, double probability, ImmutableList<IWord> referenceWords,
            ImmutableList<String> occurrences) {
        Objects.requireNonNull(claimant);

        distribution = new EnumMap<>(MappingKind.class);
        distribution.put(kind, new Confidence(claimant, probability, DEFAULT_AGGREGATOR));

        this.words = Lists.mutable.withAll(words);
        initializeDistribution(distribution);
        this.referenceWords = Lists.immutable.withAll(referenceWords);
        surfaceForms = Lists.mutable.withAll(occurrences);
    }

    private void initializeDistribution(Map<MappingKind, Confidence> distribution) {
        this.distribution = new EnumMap<>(distribution);
        this.distribution.putIfAbsent(MappingKind.NAME, new Confidence(DEFAULT_AGGREGATOR));
        this.distribution.putIfAbsent(MappingKind.TYPE, new Confidence(DEFAULT_AGGREGATOR));
    }

    /**
     * Returns the surface forms (previously called occurrences) of this mapping.
     *
     * @return all appearances of the mapping
     */
    @Override
    public final ImmutableList<String> getSurfaceForms() {
        return Lists.immutable.withAll(surfaceForms);
    }

    /**
     * Returns all words that are contained by the mapping. This should include coreferences.
     *
     * @return all words that are referenced with this mapping
     */
    @Override
    public final ImmutableList<IWord> getWords() {
        return Lists.immutable.withAll(words);
    }

    /**
     * Adds nodes to the mapping, if they are not already contained.
     *
     * @param words graph nodes to add to the mapping
     */
    @Override
    public final void addWords(ImmutableList<IWord> words) {
        for (var word : words) {
            addWord(word);
        }
    }

    /**
     * Adds a node to the mapping, it its not already contained.
     *
     * @param word graph node to add.
     */
    @Override
    public final void addWord(IWord word) {
        if (!words.contains(word)) {
            words.add(word);
        }
    }

    /**
     * Returns the reference, the comparable and naming attribute of this mapping.
     *
     * @return the reference
     */
    @Override
    public final String getReference() {
        if (referenceWords.size() == 1) {
            return referenceWords.get(0).getText();
        }
        return CommonUtilities.createReferenceForPhrase(referenceWords);
    }

    /**
     * Returns the reference words
     *
     * @return the reference words
     */
    @Override
    public final ImmutableList<IWord> getReferenceWords() {
        return referenceWords;
    }

    /**
     * Returns the sentence numbers of occurrences, sorted.
     *
     * @return sentence numbers of the occurrences of this mapping.
     */
    @Override
    public final ImmutableList<Integer> getMappingSentenceNo() {
        MutableList<Integer> positions = Lists.mutable.empty();
        for (IWord word : words) {
            positions.add(word.getSentenceNo() + 1);
        }
        return positions.toSortedList().toImmutable();
    }

    @Override
    public ImmutableSet<IPhrase> getPhrases() {

        MutableSet<IPhrase> phrases = Sets.mutable.empty();
        for (IWord word : this.words) {
            phrases.add(word.getPhrase());
        }
        return phrases.toImmutable();
    }

    @Override
    public INounMapping splitByPhrase(IPhrase phrase) {

        MutableList<IWord> wordsToRemove = Lists.mutable.empty();

        for (IWord word : words) {
            if (word.getPhrase().equals(phrase)) {
                wordsToRemove.add(word);
            }
        }

        words.removeAll(wordsToRemove);

        // TODO: Recalculate confidence

        // return noun mapping out of removed words
        return new NounMapping(wordsToRemove.toImmutable(), getDistribution(), getReferenceWords(), getSurfaceForms());

    }

    /**
     * Adds occurrences to the mapping
     *
     * @param newOccurances occurrences to add
     */
    @Override
    public final void addOccurrence(ImmutableList<String> newOccurances) {
        for (String o : newOccurances) {
            if (!surfaceForms.contains(o)) {
                surfaceForms.add(o);
            }
        }
    }

    /**
     * Adds the kind with probability.
     *
     * @param kind        the kind
     * @param probability the probability
     */
    @Override
    public void addKindWithProbability(MappingKind kind, IClaimant claimant, double probability) {
        var currentProbability = distribution.get(kind);
        currentProbability.addAgentConfidence(claimant, probability);
    }

    @Override
    public INounMapping createCopy() {
        return new NounMapping(words.toImmutable(), JavaUtils.copyMap(this.distribution, Confidence::createCopy), Lists.immutable.withAll(referenceWords),
                surfaceForms.toImmutable());
    }

    @Override
    public Map<MappingKind, Confidence> getDistribution() {
        return new EnumMap<>(distribution);
    }

    /**
     * Splits all occurrences with a whitespace in it at their spaces and returns all parts that are similar to the
     * reference. If it contains a separator or similar to the reference it is added to the comparables as a whole.
     *
     * @return all parts of occurrences (split at their spaces) that are similar to the reference.
     */
    @Override
    public ImmutableList<String> getRepresentativeComparables() {
        MutableList<String> comparables = Lists.mutable.empty();
        for (String occ : surfaceForms) {
            if (CommonUtilities.containsSeparator(occ)) {
                var parts = CommonUtilities.splitAtSeparators(occ);
                for (String part : parts) {
                    if (SimilarityUtils.areWordsSimilar(getReference(), part)) {
                        comparables.add(part);
                    }
                }
                comparables.add(occ);
            } else if (SimilarityUtils.areWordsSimilar(getReference(), occ)) {
                comparables.add(occ);
            }
        }
        return comparables.toImmutable();
    }

    /**
     * Returns the probability of being a mapping of its kind.
     *
     * @return probability of being a mapping of its kind.
     */
    @Override
    public double getProbability() {
        return distribution.get(getKind()).getConfidence();
    }

    /**
     * Returns the kind: name, type.
     *
     * @return the kind
     */
    @Override
    public MappingKind getKind() {
        var probName = distribution.get(MappingKind.NAME).getConfidence();
        var probType = distribution.get(MappingKind.TYPE).getConfidence();
        if (probName >= probType) {
            return MappingKind.NAME;
        }
        return MappingKind.TYPE;
    }

    /**
     * @return the coreferences
     */
    @Override
    public ImmutableList<IWord> getCoreferences() {
        return coreferences.toImmutable();
    }

    @Override
    public AggregationFunctions getAggregationFunction() {
        return DEFAULT_AGGREGATOR;
    }

    /**
     * @param coreferences the coreferences to add
     */
    @Override
    public void addCoreferences(Collection<IWord> coreferences) {
        this.coreferences.addAll(coreferences);
    }

    /**
     * @param coreference the coreference to add
     */
    @Override
    public void addCoreference(IWord coreference) {
        coreferences.add(coreference);
    }

    @Override
    public INounMapping merge(INounMapping other) {
        Map<MappingKind, Confidence> otherDistribution = other.getDistribution();

        otherDistribution.keySet()
                .forEach(kind -> Confidence.merge(distribution.get(kind), otherDistribution.get(kind), DEFAULT_AGGREGATOR, DEFAULT_AGGREGATOR));

        this.addOccurrence(other.getSurfaceForms());
        other.getWords().forEach(this::addWord);

        return this;
    }

    @Override
    public INounMapping split(ImmutableList<IWord> words) {
        var sharedWords = this.words.select(words::contains);
        this.words.removeAll(sharedWords.toList());
        return new NounMapping(sharedWords.toImmutable(), distribution, referenceWords, surfaceForms.toImmutable());
    }

    @Override
    public String toString() {
        return "NounMapping [" + "distribution="
                + distribution.entrySet().stream().map(entry -> entry.getKey() + ":" + entry.getValue()).collect(Collectors.joining(",")) + //
                ", reference=" + getReference() + //
                ", node=" + String.join(", ", surfaceForms) + //
                ", position=" + String.join(", ", getWords().collect(word -> String.valueOf(word.getPosition()))) + //
                ", probability=" + getProbability() + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(getReference());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        var other = (INounMapping) obj;
        return Objects.equals(getReference(), other.getReference());
    }

    @Override
    public boolean isTheSameAs(INounMapping other) {
        return Objects.equals(getReference(), other.getReference()) && Objects.equals(getWords(), other.getWords())
                && Objects.equals(getKind(), other.getKind()) && Objects.equals(getPhrases(), other.getPhrases());
    }

    @Override
    public boolean containsSameWordsAs(INounMapping nounMapping) {
        // getWords().anySatisfy(w -> w.getPosition() == nounMapping.getWords().get(0).getPosition()
        // && w.getSentenceNo() == nounMapping.getWords().get(0).getSentenceNo())
        return words.size() == nounMapping.getWords().size() && this.words.containsAllIterable(nounMapping.getWords());
    }

    @Override
    public boolean sharesTextualWordRepresentation(INounMapping nounMapping) {

        return nounMapping.getWords().allSatisfy(w -> this.getWords().anySatisfy(thisW -> thisW.getText().equals(w.getText())));
    }

    @Override
    public double getProbabilityForKind(MappingKind mappingKind) {
        return distribution.get(mappingKind).getConfidence();
    }

}
