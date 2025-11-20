package cmu.detector.refactoring.semi;

import org.eclipse.jdt.core.dom.*;
import java.util.*;

/**
 * Represents an extract method opportunity
 */
public class ExtractOpportunity {
    private StatementCluster cluster;
    private MethodDeclaration originalMethod;
    private CompilationUnit compilationUnit;
    private double benefit;
    private List<VariableInfo> parameters;
    private ITypeBinding returnType;
    
    public ExtractOpportunity(StatementCluster cluster, MethodDeclaration method, 
                             CompilationUnit cu) {
        this.cluster = cluster;
        this.originalMethod = method;
        this.compilationUnit = cu;
        this.benefit = 0.0;
    }
    
    /**
     * Check if this opportunity is valid for extraction
     */
    public boolean isValid() {
        return new OpportunityValidator(this).validate();
    }
    
    /**
     * Calculate LCOM2 benefit
     */
    public void calculateBenefit() {
        LCOM2Calculator calculator = new LCOM2Calculator();
        
        // Calculate LCOM2 for original method
        double originalLCOM2 = calculator.calculate(originalMethod);
        
        // Calculate LCOM2 for extracted method
        double extractedLCOM2 = calculator.calculate(cluster.getStatements());
        
        // Calculate LCOM2 for remaining method
        List<Statement> remaining = getRemainingStatements();
        double remainingLCOM2 = calculator.calculate(remaining);
        
        // Benefit = original - max(extracted, remaining)
        this.benefit = originalLCOM2 - Math.max(extractedLCOM2, remainingLCOM2);
    }
    
    /**
     * Get parameters needed for extraction
     */
    public List<VariableInfo> getParameters() {
        if (parameters == null) {
            parameters = new ParameterExtractor(cluster, originalMethod).extract();
        }
        return parameters;
    }
    
    /**
     * Get return type for extraction
     */
    public ITypeBinding getReturnType() {
        if (returnType == null) {
            returnType = new ReturnTypeAnalyzer(cluster, originalMethod).analyze();
        }
        return returnType;
    }
    
    public int getStartLine() {
        return compilationUnit.getLineNumber(
            cluster.getStatements().get(0).getStartPosition()
        );
    }
    
    public int getEndLine() {
        Statement last = cluster.getStatements().get(cluster.getStatements().size() - 1);
        return compilationUnit.getLineNumber(
            last.getStartPosition() + last.getLength()
        );
    }
    
    public int getStatementCount() {
        return cluster.size();
    }
    
    public double getBenefit() {
        return benefit;
    }
    
    public List<Statement> getStatements() {
        return cluster.getStatements();
    }
    
    private List<Statement> getRemainingStatements() {
        Block body = originalMethod.getBody();
        if (body == null) return new ArrayList<>();
        
        List<Statement> all = new ArrayList<>();
        for (Object obj : body.statements()) {
            all.add((Statement) obj);
        }
        
        all.removeAll(cluster.getStatements());
        return all;
    }
}