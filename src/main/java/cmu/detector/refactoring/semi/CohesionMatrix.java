package cmu.detector.refactoring.semi;

import org.eclipse.jdt.core.dom.*;
import java.util.*;

/**
 * Cohesion matrix to track relationships between statements
 */
public class CohesionMatrix {
    
    private List<Statement> statements;
    private CompilationUnit compilationUnit;
    private Map<Statement, Set<IVariableBinding>> statementVariables;
    private Map<Statement, Set<MethodCallInfo>> statementMethodCalls;
    private boolean[][] cohesionMatrix;
    
    public CohesionMatrix(List<Statement> statements, CompilationUnit compilationUnit) {
        this.statements = statements;
        this.compilationUnit = compilationUnit;
        this.statementVariables = new HashMap<>();
        this.statementMethodCalls = new HashMap<>();
        this.cohesionMatrix = new boolean[statements.size()][statements.size()];
    }
    
    /**
     * Build the cohesion matrix
     */
    public void build() {
        // Step 1: Extract variables and method calls for each statement
        for (Statement stmt : statements) {
            extractStatementInfo(stmt);
        }
        
        // Step 2: Build cohesion matrix
        for (int i = 0; i < statements.size(); i++) {
            for (int j = i + 1; j < statements.size(); j++) {
                cohesionMatrix[i][j] = calculateCohesion(
                    statements.get(i), 
                    statements.get(j)
                );
                cohesionMatrix[j][i] = cohesionMatrix[i][j];
            }
            cohesionMatrix[i][i] = true;
        }
        System.out.println("Cohesion matrix built with " + statements.size() + " statements.");
//        for (int i = 0; i < statements.size(); i++) {
//            for (int j = i + 1; j < statements.size(); j++) {
//                System.out.println("  Statements " + i + " and " + j +
//                        (cohesionMatrix[i][j] ? " are " : " are NOT ") + "cohesive.");
//            }
//        }
    }
    
    /**
     * Extract variables and method calls from a statement
     */
    private void extractStatementInfo(Statement stmt) {
        StatementInfoExtractor extractor = new StatementInfoExtractor();
        stmt.accept(extractor);
        
        statementVariables.put(stmt, extractor.getVariables());
        statementMethodCalls.put(stmt, extractor.getMethodCalls());
    }
    
    /**
     * Calculate if two statements are cohesive
     */
    private boolean calculateCohesion(Statement s1, Statement s2) {
        // Rule 1: Access same variable
        if (shareVariables(s1, s2)) {
            return true;
        }
        
        // Rule 2: Call method on same object
        if (callMethodOnSameObject(s1, s2)) {
            return true;
        }
        
        // Rule 3: Call same method on different objects of same type
        if (callSameMethodDifferentObjects(s1, s2)) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Check if statements share variables
     */
    private boolean shareVariables(Statement s1, Statement s2) {
        Set<IVariableBinding> vars1 = statementVariables.get(s1);
        Set<IVariableBinding> vars2 = statementVariables.get(s2);
        
        if (vars1 == null || vars2 == null) return false;
        
        for (IVariableBinding var : vars1) {
            if (vars2.contains(var)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Check if statements call method on same object
     */
    private boolean callMethodOnSameObject(Statement s1, Statement s2) {
        Set<MethodCallInfo> calls1 = statementMethodCalls.get(s1);
        Set<MethodCallInfo> calls2 = statementMethodCalls.get(s2);
        
        if (calls1 == null || calls2 == null) return false;
        
        for (MethodCallInfo call1 : calls1) {
            for (MethodCallInfo call2 : calls2) {
                if (call1.isSameReceiver(call2)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Check if statements call same method on different objects of same type
     */
    private boolean callSameMethodDifferentObjects(Statement s1, Statement s2) {
        Set<MethodCallInfo> calls1 = statementMethodCalls.get(s1);
        Set<MethodCallInfo> calls2 = statementMethodCalls.get(s2);
        
        if (calls1 == null || calls2 == null) return false;
        
        for (MethodCallInfo call1 : calls1) {
            for (MethodCallInfo call2 : calls2) {
                if (call1.isSameMethodDifferentReceiver(call2)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    public boolean areCohesive(int i, int j) {
        if (i < 0 || i >= statements.size() || j < 0 || j >= statements.size()) {
            return false;
        }
        return cohesionMatrix[i][j];
    }
    
    /**
     * Helper class to extract variables and method calls from a statement
     */
    private static class StatementInfoExtractor extends ASTVisitor {
        private Set<IVariableBinding> variables = new HashSet<>();
        private Set<MethodCallInfo> methodCalls = new HashSet<>();
        
        @Override
        public boolean visit(SimpleName node) {
            IBinding binding = node.resolveBinding();
            if (binding instanceof IVariableBinding) {
                variables.add((IVariableBinding) binding);
            }
            return true;
        }
        
        @Override
        public boolean visit(MethodInvocation node) {
            methodCalls.add(new MethodCallInfo(node));
            return true;
        }
        
        public Set<IVariableBinding> getVariables() {
            return variables;
        }
        
        public Set<MethodCallInfo> getMethodCalls() {
            return methodCalls;
        }
    }
}