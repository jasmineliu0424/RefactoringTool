package cmu.detector.refactoring.semi;

import org.eclipse.jdt.core.dom.Statement;
import java.util.*;

/**
 * Represents a cluster of consecutive statements
 */
public class StatementCluster {
    private int startIndex;
    private int endIndex;
    private List<Statement> allStatements;
    
    public StatementCluster(int start, int end, List<Statement> allStatements) {
        this.startIndex = start;
        this.endIndex = end;
        this.allStatements = allStatements;
    }
    
    public int getStart() {
        return startIndex;
    }
    
    public int getEnd() {
        return endIndex;
    }
    
    public int size() {
        return endIndex - startIndex + 1;
    }
    
    public List<Statement> getStatements() {
        return allStatements.subList(startIndex, endIndex + 1);
    }
    
    /**
     * Check if this cluster overlaps with another
     */
    public boolean overlaps(StatementCluster other) {
        return !(this.endIndex < other.startIndex || other.endIndex < this.startIndex);
    }
    
    /**
     * Merge with another cluster
     */
    public StatementCluster merge(StatementCluster other) {
        int newStart = Math.min(this.startIndex, other.startIndex);
        int newEnd = Math.max(this.endIndex, other.endIndex);
        return new StatementCluster(newStart, newEnd, allStatements);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof StatementCluster)) return false;
        StatementCluster other = (StatementCluster) obj;
        return this.startIndex == other.startIndex && this.endIndex == other.endIndex;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(startIndex, endIndex);
    }
}