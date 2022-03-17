/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.fastText;

import edu.kit.kastel.mcse.ardoco.core.common.util.CommonTextToolsConfig;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.ComparisonContext;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.WordSimMeasure;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.vector.VectorUtils;

/**
 * A word similarity measure based on the fastText neural network.
 * It grabs the vectors for each word and compares them using cosine similarity.
 */
public class FastTextMeasure implements WordSimMeasure {

    private final DL4JFastTextDataSource dataSource;
    private final double similarityThreshold;

    /**
     * Constructs a new {@link FastTextMeasure} using the settings provided by
     * {@link edu.kit.kastel.mcse.ardoco.core.common.util.CommonTextToolsConfig}.
     *
     * @param dataSource the data source to use for word similarity computation
     */
    public FastTextMeasure(DL4JFastTextDataSource dataSource) {
        this(dataSource, CommonTextToolsConfig.FASTTEXT_SIMILARITY_THRESHOLD);
    }

    /**
     * Constructs a new {@link FastTextMeasure} instance.
     *
     * @param dataSource          the data source to use for word similarity computation
     * @param similarityThreshold the threshold above which words are considered similar
     */
    public FastTextMeasure(DL4JFastTextDataSource dataSource, double similarityThreshold) {
        this.dataSource = dataSource;
        this.similarityThreshold = similarityThreshold;
    }

    @Override public boolean areWordsSimilar(ComparisonContext ctx) {
        var firstVec = this.dataSource.getWordVector(ctx.firstTerm()).orElse(null);
        if (firstVec == null) {
            return false;
        }

        var secondVec = this.dataSource.getWordVector(ctx.secondTerm()).orElse(null);
        if (secondVec == null) {
            return false;
        }

        double similarity = VectorUtils.cosineSimilarity(firstVec, secondVec);

        return similarity >= this.similarityThreshold;
    }

}
