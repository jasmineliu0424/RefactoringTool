package cmu.detector.refactoring.semi;

import org.eclipse.jdt.core.dom.*;
import java.util.*;

/**
 * Calculator for LCOM2 (Lack of Cohesion of Methods 2) metric
 */
public class LCOM2Calculator {
    
    /**
     * Calculate LCOM2 for a method
     */
    public double calculate(MethodDeclaration method) {
        Block body = method.getBody();
        if (body == null) return 0.0;
        
        List<Statement> statements = new ArrayList<>();
        for (Object obj : body.statements()) {
            statements.add((Statement) obj);
        }
        
        return calculate(statements);
    }
    
    /**
     * Calculate LCOM2 for a list of statements
     */
    public double calculate(List<Statement> statements) {
        if (statements.isEmpty()) return 0.0;
        
        // Extract variables used by each statement
        Map<Statement, Set<IVariableBinding>> statementVars = new HashMap<>();
        Set<IVariableBinding> allVars = new HashSet<>();
        
        for (Statement stmt : statements) {
            VariableCollector collector = new VariableCollector();
            stmt.accept(collector);
            Set<IVariableBinding> vars = collector.getVariables();
            statementVars.put(stmt, vars);
            allVars.addAll(vars);
        }
        
        int m = statements.size();
        int a = allVars.size();
        
        // If no variables or statements, LCOM2 = 0
        if (m == 0 || a == 0) {
            return 0.0;
        }
        
        // Calculate sum: for each variable, count how many statements use it
        int sum = 0;
        for (IVariableBinding var : allVars) {
            int count = 0;
            for (Statement stmt : statements) {
                if (statementVars.get(stmt).contains(var)) {
                    count++;
                }
            }
            sum += count;
        }
        
        // LCOM2 = 1 - (sum / (m * a))
        double lcom2 = 1.0 - ((double) sum / (m * a));
        
        return Math.max(0.0, lcom2); // Ensure non-negative
    }
    
    /**
     * Visitor to collect all variables used in statements
     */
    private static class VariableCollector extends ASTVisitor {
        private Set<IVariableBinding> variables = new HashSet<>();
        
        @Override
        public boolean visit(SimpleName node) {
            IBinding binding = node.resolveBinding();
            if (binding instanceof IVariableBinding) {
                IVariableBinding varBinding = (IVariableBinding) binding;
                // Include all variables (fields, locals, parameters)
                variables.add(varBinding);
            }
            return true;
        }
        
        public Set<IVariableBinding> getVariables() {
            return variables;
        }
    }
}