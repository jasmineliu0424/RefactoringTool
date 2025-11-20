package cmu.detector.refactoring.semi;

import java.util.*;

/**
 * Group of similar extract method opportunities
 */
public class OpportunityGroup {
    private ExtractOpportunity primaryOpportunity;
    private List<ExtractOpportunity> alternatives;
    
    public OpportunityGroup(ExtractOpportunity primary) {
        this.primaryOpportunity = primary;
        this.alternatives = new ArrayList<>();
    }
    
    public void addAlternative(ExtractOpportunity alternative) {
        alternatives.add(alternative);
    }
    
    /**
     * Calculate benefits for all opportunities in group
     */
    public void calculateBenefits() {
        primaryOpportunity.calculateBenefit();
        for (ExtractOpportunity alt : alternatives) {
            alt.calculateBenefit();
        }
    }
    
    /**
     * Sort opportunities by benefit and determine primary
     */
    public void sortByBenefit(double significantDifferenceThreshold) {
        List<ExtractOpportunity> all = new ArrayList<>();
        all.add(primaryOpportunity);
        all.addAll(alternatives);
        
        // Sort by benefit (descending)
        all.sort((o1, o2) -> Double.compare(o2.getBenefit(), o1.getBenefit()));
        
        // If top two benefits are very close, use size as secondary criterion
        if (all.size() >= 2) {
            double topBenefit = all.get(0).getBenefit();
            double secondBenefit = all.get(1).getBenefit();
            
            double diff = Math.abs(topBenefit - secondBenefit) / Math.max(topBenefit, secondBenefit);
            
            if (diff < significantDifferenceThreshold) {
                // Use size as tiebreaker (larger is better)
                all.sort((o1, o2) -> {
                    int benefitCompare = Double.compare(o2.getBenefit(), o1.getBenefit());
                    if (Math.abs(o1.getBenefit() - o2.getBenefit()) / 
                        Math.max(o1.getBenefit(), o2.getBenefit()) < significantDifferenceThreshold) {
                        return Integer.compare(o2.getStatementCount(), o1.getStatementCount());
                    }
                    return benefitCompare;
                });
            }
        }
        
        // Set new primary and alternatives
        primaryOpportunity = all.get(0);
        alternatives = all.subList(1, all.size());
    }
    
    public ExtractOpportunity getPrimaryOpportunity() {
        return primaryOpportunity;
    }
    
    public List<ExtractOpportunity> getAlternatives() {
        return alternatives;
    }
}