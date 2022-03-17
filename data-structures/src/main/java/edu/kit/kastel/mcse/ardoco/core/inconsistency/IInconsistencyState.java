/* Licensed under MIT 2021. */
package edu.kit.kastel.mcse.ardoco.core.inconsistency;

import java.util.List;

import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.mcse.ardoco.core.common.IState;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.IRecommendedInstance;

/**
 * @author Jan Keim
 *
 */
public interface IInconsistencyState extends IState<IInconsistencyState> {

    /**
     * Returns a list of inconsistencies held by this state
     *
     * @return list of inconsistencies
     */
    ImmutableList<IInconsistency> getInconsistencies();

    /**
     * Add an Inconsistency to this state
     *
     * @param inconsistency the inconsistency to add
     * @return true if added successfully
     */
    boolean addInconsistency(IInconsistency inconsistency);

    default boolean addRecommendedInstances(List<IRecommendedInstance> recommendedInstances) {
        var success = true;
        for (var recommendedInstance : recommendedInstances) {
            success &= addRecommendedInstance(recommendedInstance);
        }
        return success;
    }

    boolean addRecommendedInstance(IRecommendedInstance recommendedInstance);

    default boolean removeRecommendedInstances(List<IRecommendedInstance> recommendedInstances) {
        var success = true;
        for (var recommendedInstance : recommendedInstances) {
            success &= removeRecommendedInstance(recommendedInstance);
        }
        return success;
    }

    boolean removeRecommendedInstance(IRecommendedInstance recommendedInstance);

    /**
     * @param recommendedInstances the recommendedInstances to set
     */
    void setRecommendedInstances(List<IRecommendedInstance> recommendedInstances);

    /**
     * @return the recommendedInstances
     */
    MutableList<IRecommendedInstance> getRecommendedInstances();

}
