package cmu.detector.refactoring.semi;

import org.eclipse.jdt.core.dom.*;
import java.util.*;

/**
 * Analyzes return type for method extraction
 */
public class ReturnTypeAnalyzer {
    
    private StatementCluster cluster;
    private MethodDeclaration originalMethod;
    
    public ReturnTypeAnalyzer(StatementCluster cluster, MethodDeclaration method) {
        this.cluster = cluster;
        this.originalMethod = method;
    }
    
    /**
     * Analyze and determine return type
     */
    public ITypeBinding analyze() {
        // Find all variables defined in cluster
        Set<IVariableBinding> definedInCluster = findDefinedVariables();
        
        // Find all variables used outside cluster
        Set<IVariableBinding> usedOutside = findVariablesUsedOutside();
        
        // Return variables = defined in cluster AND used outside
        Set<IVariableBinding> returnVars = new HashSet<>(definedInCluster);
        returnVars.retainAll(usedOutside);
        
        // Analyze return type based on return variables
        return determineReturnType(returnVars);
    }
    
    /**
     * Find variables defined in cluster
     */
    private Set<IVariableBinding> findDefinedVariables() {
        Set<IVariableBinding> defined = new HashSet<>();
        
        for (Statement stmt : cluster.getStatements()) {
            VariableDefinitionVisitor visitor = new VariableDefinitionVisitor();
            stmt.accept(visitor);
            defined.addAll(visitor.getDefinedVariables());
        }
        
        // Also include variables that are assigned (not just declared)
        for (Statement stmt : cluster.getStatements()) {
            AssignmentVisitor visitor = new AssignmentVisitor();
            stmt.accept(visitor);
            defined.addAll(visitor.getAssignedVariables());
        }
        
        return defined;
    }
    
    /**
     * Find variables used outside cluster
     */
    private Set<IVariableBinding> findVariablesUsedOutside() {
        Set<IVariableBinding> usedOutside = new HashSet<>();
        
        Block body = originalMethod.getBody();
        if (body == null) return usedOutside;
        
        List<Statement> outsideStatements = new ArrayList<>();
        for (Object obj : body.statements()) {
            Statement stmt = (Statement) obj;
            if (!cluster.getStatements().contains(stmt)) {
                outsideStatements.add(stmt);
            }
        }
        
        for (Statement stmt : outsideStatements) {
            VariableUsageVisitor visitor = new VariableUsageVisitor();
            stmt.accept(visitor);
            usedOutside.addAll(visitor.getUsedVariables());
        }
        
        return usedOutside;
    }
    
    /**
     * Determine return type based on return variables
     */
    private ITypeBinding determineReturnType(Set<IVariableBinding> returnVars) {
        AST ast = originalMethod.getAST();
        
        if (returnVars.isEmpty()) {
            // No return value needed - void
            return ast.resolveWellKnownType("void");
        }
        
        if (returnVars.size() == 1) {
            // Single return value
            return returnVars.iterator().next().getType();
        }
        
        // Multiple return values - need to create a wrapper class or return array
        // For now, return null to indicate complex case
        // In practice, you might want to create a Result class
        return null;
    }
    
    /**
     * Visitor to find variable definitions
     */
    private static class VariableDefinitionVisitor extends ASTVisitor {
        private Set<IVariableBinding> defined = new HashSet<>();
        
        @Override
        public boolean visit(VariableDeclarationFragment node) {
            IVariableBinding binding = node.resolveBinding();
            if (binding != null && !binding.isField()) {
                defined.add(binding);
            }
            return true;
        }
        
        public Set<IVariableBinding> getDefinedVariables() {
            return defined;
        }
    }
    
    /**
     * Visitor to find variable assignments
     */
    private static class AssignmentVisitor extends ASTVisitor {
        private Set<IVariableBinding> assigned = new HashSet<>();
        
        @Override
        public boolean visit(Assignment node) {
            Expression lhs = node.getLeftHandSide();
            if (lhs instanceof SimpleName) {
                SimpleName name = (SimpleName) lhs;
                IBinding binding = name.resolveBinding();
                if (binding instanceof IVariableBinding) {
                    IVariableBinding varBinding = (IVariableBinding) binding;
                    if (!varBinding.isField()) {
                        assigned.add(varBinding);
                    }
                }
            }
            return true;
        }
        
        public Set<IVariableBinding> getAssignedVariables() {
            return assigned;
        }
    }
    
    /**
     * Visitor to find variable usage
     */
    private static class VariableUsageVisitor extends ASTVisitor {
        private Set<IVariableBinding> used = new HashSet<>();
        
        @Override
        public boolean visit(SimpleName node) {
            IBinding binding = node.resolveBinding();
            if (binding instanceof IVariableBinding) {
                IVariableBinding varBinding = (IVariableBinding) binding;
                if (!varBinding.isField()) {
                    used.add(varBinding);
                }
            }
            return true;
        }
        
        public Set<IVariableBinding> getUsedVariables() {
            return used;
        }
    }
}