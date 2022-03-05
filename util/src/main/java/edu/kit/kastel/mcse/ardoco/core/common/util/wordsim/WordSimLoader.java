package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim;

import edu.kit.kastel.mcse.ardoco.core.common.util.CommonTextToolsConfig;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.equality.EqualityMeasure;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.jarowinkler.JaroWinklerMeasure;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.levenshtein.LevenshteinMeasure;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.ngram.NgramMeasure;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.sewordsim.SEWordSimMeasure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Responsible for loading the word similarity measures that should be enabled according to the {@link CommonTextToolsConfig}.
 */
public class WordSimLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(WordSimLoader.class);

    /**
     * Loads and returns the word similarity measures that should be enabled according to {@link CommonTextToolsConfig}.
     *
     * @return a list of word similarity measures
     */
    public static List<WordSimMeasure> loadUsingProperties() {
        try {
            var list = new ArrayList<WordSimMeasure>();

            list.add(new EqualityMeasure());

            if (CommonTextToolsConfig.JAROWINKLER_ENABLED) {
                list.add(new LevenshteinMeasure());
            }

            if (CommonTextToolsConfig.JAROWINKLER_ENABLED) {
                list.add(new JaroWinklerMeasure());
            }

            if (CommonTextToolsConfig.NGRAM_ENABLED) {
                list.add(new NgramMeasure());
            }

            if (CommonTextToolsConfig.SEWORDSIM_ENABLED) {
                list.add(new SEWordSimMeasure());
            }

            return list;
        } catch (Exception e) {
            LOGGER.error("Failed to load word similarity measures", e);
            return Collections.emptyList();
        }
    }

}
