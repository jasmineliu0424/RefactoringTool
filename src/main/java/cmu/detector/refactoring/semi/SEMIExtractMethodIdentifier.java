package cmu.detector.refactoring.semi;

import org.eclipse.jdt.core.dom.*;
import java.util.*;

/**
 * SRP-based Extract Method Identification (SEMI)
 * Identifies extract method opportunities based on functional relevance
 */
public class SEMIExtractMethodIdentifier {
    
    private MethodDeclaration method;
    private CompilationUnit compilationUnit;
    private List<Statement> statements;
    private CohesionMatrix cohesionMatrix;
    
    // Parameters for grouping
    private double maxSizeDifference = 0.2;
    private double minOverlap = 0.1;
    private double significantDifferenceThreshold = 0.01;
    
    public SEMIExtractMethodIdentifier(MethodDeclaration method) {
        this.method = method;
        this.compilationUnit = getCompilationUnit(method);
        this.statements = extractStatements(method);
        this.cohesionMatrix = new CohesionMatrix(statements, compilationUnit);
    }
    
    /**
     * Main entry point - find all extract method opportunities
     */
    public List<ExtractOpportunity> findExtractOpportunities() {
        System.out.println("=== SEMI Extract Method Identification Started ===");
        System.out.println("Method: " + method.getName());
        System.out.println("Total statements: " + statements.size());
        for (int i = 0; i < statements.size(); i++) {
            System.out.println(String.format("  %d. %s", i, statements.get(i).toString().trim()));
        }

        // Step 1: Build cohesion matrix
        System.out.println("\n--- Step 1: Building Cohesion Matrix ---");
        cohesionMatrix.build();
        
        // Step 2: Identify candidate fragments
        System.out.println("\n--- Step 2: Identifying Candidates ---");
        List<ExtractOpportunity> candidates = identifyCandidates();
        for (int i = 0; i < candidates.size(); i++) {
            ExtractOpportunity opp = candidates.get(i);
            System.out.println(String.format("  %d. Lines %d-%d (statements: %d)",
                    i + 1, opp.getStartLine(), opp.getEndLine(), opp.getStatementCount()
            ));
        }
        
        // Step 3: Group and rank opportunities
        System.out.println("\n--- Step 3: Grouping and Ranking ---");
        List<OpportunityGroup> groups = groupOpportunities(candidates);
        rankGroups(groups);
        for (int i = 0; i < groups.size(); i++) {
            OpportunityGroup group = groups.get(i);
            System.out.println(String.format("  Group %d: primary opportunity: Lines %d-%d (statements: %d)",
                    i + 1, group.getPrimaryOpportunity().getStartLine(), group.getPrimaryOpportunity().getEndLine(), group.getPrimaryOpportunity().getStatementCount()
            ));
        }
        
        // Return sorted primary opportunities
        return extractPrimaryOpportunities(groups);
    }
    
    /**
     * Step 2: Identify candidate extract method opportunities
     */
    private List<ExtractOpportunity> identifyCandidates() {
        List<ExtractOpportunity> candidates = new ArrayList<>();
        int maxStep = statements.size();
        
        // Iterative search with increasing step size
        for (int step = 1; step < maxStep; step++) {
            System.out.println("  Finding clusters with step size: " + step);
            List<StatementCluster> stepClusters = findClustersWithStep(step);

            // Merge overlapping clusters
            List<StatementCluster> mergedClusters = mergeClusters(stepClusters);

            if (step == 1 || step == 2){
                // Print all step clusters
                System.out.println("    Step clusters (" + stepClusters.size() + "):");
                for (int k = 0; k < stepClusters.size(); k++) {
                    StatementCluster sc = stepClusters.get(k);
                    System.out.println(String.format("      %d. start=%d end=%d size=%d",
                            k + 1, sc.getStart(), sc.getEnd(), sc.size()));
                }

                // Print merged clusters
                System.out.println("    Merged clusters (" + mergedClusters.size() + "):");
                for (int k = 0; k < mergedClusters.size(); k++) {
                    StatementCluster mc = mergedClusters.get(k);
                    System.out.println(String.format("      %d. start=%d end=%d size=%d",
                            k + 1, mc.getStart(), mc.getEnd(), mc.size()));
                }
            }

            // Create a list of opportunities both original clusters and merged ones
            addClustersToCandidates(stepClusters, candidates);
            addClustersToCandidates(mergedClusters, candidates);
        }
        
        // Remove duplicates
        return removeDuplicates(candidates);
    }
    
    /**
     * Find cohesive statement clusters with given step size
     */
    private List<StatementCluster> findClustersWithStep(int step) {
        List<StatementCluster> clusters = new ArrayList<>();
        
        for (int i = 0; i < statements.size(); i++) {
            for (int j = i + 1; j < statements.size() && j - i <= step; j++) {
                if (cohesionMatrix.areCohesive(i, j)) {
                    // Find all statements between i and j that are cohesive
                    StatementCluster cluster = buildCluster(i, j);
                    if (cluster.size() > 1) {
                        clusters.add(cluster);
                    }
                }
            }
        }
        
        return clusters;
    }
    
    /**
     * Build a cohesive cluster between start and end indices
     */
    private StatementCluster buildCluster(int start, int end) {
        Set<Integer> clusterIndices = new HashSet<>();
        clusterIndices.add(start);
        clusterIndices.add(end);
        
        // Add all cohesive statements in between
        for (int i = start + 1; i < end; i++) {
            boolean cohesiveWithCluster = false;
            for (int clusterIdx : clusterIndices) {
                if (cohesionMatrix.areCohesive(i, clusterIdx)) {
                    cohesiveWithCluster = true;
                    break;
                }
            }
            if (cohesiveWithCluster) {
                clusterIndices.add(i);
            }
        }
        
        // Convert to continuous cluster
        List<Integer> sortedIndices = new ArrayList<>(clusterIndices);
        Collections.sort(sortedIndices);
        
        return new StatementCluster(sortedIndices.get(0), 
                                    sortedIndices.get(sortedIndices.size() - 1),
                                    statements);
    }
    
    /**
     * Merge overlapping clusters
     */
    private List<StatementCluster> mergeClusters(List<StatementCluster> clusters) {
        if (clusters.isEmpty()) return clusters;
        
        List<StatementCluster> merged = new ArrayList<>();
        List<StatementCluster> sorted = new ArrayList<>(clusters);
        sorted.sort(Comparator.comparingInt(StatementCluster::getStart));
        
        StatementCluster current = sorted.get(0);
        
        for (int i = 1; i < sorted.size(); i++) {
            StatementCluster next = sorted.get(i);
            
            if (current.overlaps(next)) {
                // Merge
                current = current.merge(next);
            } else {
                merged.add(current);
                current = next;
            }
        }
        merged.add(current);
        
        return merged;
    }

    /**
     * Convert to opportunities and validate before adding to candidates
     */
    private void addClustersToCandidates(List<StatementCluster> clusters, List<ExtractOpportunity> candidates) {
        for (StatementCluster cluster : clusters) {
            ExtractOpportunity opportunity = new ExtractOpportunity(
                cluster, method, compilationUnit
            );
            
            if (opportunity.isValid()) {
                candidates.add(opportunity);
            }
        }
    }
    
    /**
     * Step 3: Group similar opportunities
     */
    private List<OpportunityGroup> groupOpportunities(List<ExtractOpportunity> opportunities) {
        List<OpportunityGroup> groups = new ArrayList<>();
        Set<ExtractOpportunity> processed = new HashSet<>();
        
        for (ExtractOpportunity opp : opportunities) {
            if (processed.contains(opp)) continue;
            
            OpportunityGroup group = new OpportunityGroup(opp);
            processed.add(opp);
            
            // Find similar opportunities
            for (ExtractOpportunity other : opportunities) {
                if (processed.contains(other)) continue;
                
                if (areSimilar(opp, other)) {
                    group.addAlternative(other);
                    processed.add(other);
                }
            }
            
            groups.add(group);
        }
        
        return groups;
    }
    
    /**
     * Check if two opportunities are similar enough to group by size difference and overlap
     */
    private boolean areSimilar(ExtractOpportunity a, ExtractOpportunity b) {
        // Check size difference
        int sizeA = a.getStatementCount();
        int sizeB = b.getStatementCount();
        double sizeDiff = Math.abs(sizeA - sizeB) / (double) Math.min(sizeA, sizeB);
        
        if (sizeDiff > maxSizeDifference) {
            return false;
        }
        
        // Check overlap
        double overlap = calculateOverlap(a, b);
        return overlap >= minOverlap;
    }
    
    /**
     * Calculate overlap between two opportunities
     */
    private double calculateOverlap(ExtractOpportunity a, ExtractOpportunity b) {
        int startOverlap = Math.max(a.getStartLine(), b.getStartLine());
        int endOverlap = Math.min(a.getEndLine(), b.getEndLine());
        
        if (startOverlap > endOverlap) {
            return 0.0;
        }
        
        int overlapSize = endOverlap - startOverlap + 1;
        int maxSize = Math.max(a.getStatementCount(), b.getStatementCount());
        
        return (double) overlapSize / maxSize;
    }
    
    /**
     * Rank groups based on LCOM2 benefit
     */
    private void rankGroups(List<OpportunityGroup> groups) {
        for (OpportunityGroup group : groups) {
            group.calculateBenefits();
            group.sortByBenefit(significantDifferenceThreshold);
        }
        
        // Sort groups by primary opportunity benefit
        groups.sort((g1, g2) -> Double.compare(
            g2.getPrimaryOpportunity().getBenefit(),
            g1.getPrimaryOpportunity().getBenefit()
        ));
    }
    
    /**
     * Extract primary opportunities from groups
     */
    private List<ExtractOpportunity> extractPrimaryOpportunities(List<OpportunityGroup> groups) {
        List<ExtractOpportunity> result = new ArrayList<>();
        for (OpportunityGroup group : groups) {
            result.add(group.getPrimaryOpportunity());
        }
        return result;
    }
    
    /**
     * Remove duplicate opportunities
     */
    private List<ExtractOpportunity> removeDuplicates(List<ExtractOpportunity> opportunities) {
        Set<String> seen = new HashSet<>();
        List<ExtractOpportunity> unique = new ArrayList<>();
        
        for (ExtractOpportunity opp : opportunities) {
            String key = opp.getStartLine() + "-" + opp.getEndLine();
            if (!seen.contains(key)) {
                seen.add(key);
                unique.add(opp);
            }
        }
        
        return unique;
    }
    
    /**
     * Extract all statements from method body
     */
    private List<Statement> extractStatements(MethodDeclaration method) {
        List<Statement> result = new ArrayList<>();
        Block body = method.getBody();
        
//        if (body != null) {
//            StatementExtractor extractor = new StatementExtractor();
//            body.accept(extractor);
//            result = extractor.getStatements();
//        }
//
//        return result;
//    }

        if (body == null) return result;
        // print each body itself


        for (Object obj : body.statements()) {
            Statement stmt = (Statement) obj;
            collectSingleLine(stmt, result);
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    private void collectSingleLine(Statement stmt, List<Statement> result) {
        // 2. Block → 不算單行，跳過（不加入 result）
        if (stmt instanceof Block) {
            Block block = (Block) stmt;
            for (Object subObj : block.statements()) {
                collectSingleLine((Statement) subObj, result);
            }
            return;
        }

        // 1. If / For / While / Do / Try / Switch 等複合語句 → 視為單行，不展開
        if (stmt instanceof IfStatement ||
                stmt instanceof ForStatement ||
                stmt instanceof EnhancedForStatement ||
                stmt instanceof WhileStatement ||
                stmt instanceof DoStatement ||
                stmt instanceof TryStatement ||
                stmt instanceof SwitchStatement) {

            System.out.println("Complex statement treated as single line: " + stmt);
            result.add(stmt);   // 整個語句視為單行
            return;
        }

        // 3. 其他（普通語句）→ 視為單行
        result.add(stmt);
    }
    
    private CompilationUnit getCompilationUnit(ASTNode node) {
        while (node != null && !(node instanceof CompilationUnit)) {
            node = node.getParent();
        }
        return (CompilationUnit) node;
    }
}