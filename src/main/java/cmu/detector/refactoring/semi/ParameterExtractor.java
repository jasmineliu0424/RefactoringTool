package cmu.detector.refactoring.semi;

import org.eclipse.jdt.core.dom.*;
import java.util.*;

/**
 * Extracts parameters needed for method extraction
 */
public class ParameterExtractor {
    
    private StatementCluster cluster;
    private MethodDeclaration originalMethod;
    
    public ParameterExtractor(StatementCluster cluster, MethodDeclaration method) {
        this.cluster = cluster;
        this.originalMethod = method;
    }
    
    /**
     * Extract all parameters needed
     */
    public List<VariableInfo> extract() {
        // Find all variables used in the cluster
        Set<IVariableBinding> usedVariables = findUsedVariables();
        
        // Find all variables defined in the cluster
        Set<IVariableBinding> definedVariables = findDefinedVariables();
        
        // Parameters = used but not defined in cluster
        Set<IVariableBinding> parameters = new HashSet<>(usedVariables);
        parameters.removeAll(definedVariables);
        
        // Convert to VariableInfo
        List<VariableInfo> result = new ArrayList<>();
        for (IVariableBinding var : parameters) {
            result.add(new VariableInfo(var));
        }
        
        // Sort by name for consistency
        result.sort(Comparator.comparing(VariableInfo::getName));
        
        return result;
    }
    
    /**
     * Find all variables used in cluster
     */
    private Set<IVariableBinding> findUsedVariables() {
        Set<IVariableBinding> used = new HashSet<>();
        
        for (Statement stmt : cluster.getStatements()) {
            VariableUsageVisitor visitor = new VariableUsageVisitor();
            stmt.accept(visitor);
            used.addAll(visitor.getUsedVariables());
        }
        
        return used;
    }
    
    /**
     * Find all variables defined in cluster
     */
    private Set<IVariableBinding> findDefinedVariables() {
        Set<IVariableBinding> defined = new HashSet<>();
        
        for (Statement stmt : cluster.getStatements()) {
            VariableDefinitionVisitor visitor = new VariableDefinitionVisitor();
            stmt.accept(visitor);
            defined.addAll(visitor.getDefinedVariables());
        }
        
        return defined;
    }
    
    /**
     * Visitor to find used variables
     */
    private static class VariableUsageVisitor extends ASTVisitor {
        private Set<IVariableBinding> usedVariables = new HashSet<>();
        
        @Override
        public boolean visit(SimpleName node) {
            IBinding binding = node.resolveBinding();
            if (binding instanceof IVariableBinding) {
                IVariableBinding varBinding = (IVariableBinding) binding;
                // Only include local variables and parameters
                if (!varBinding.isField()) {
                    usedVariables.add(varBinding);
                }
            }
            return true;
        }
        
        public Set<IVariableBinding> getUsedVariables() {
            return usedVariables;
        }
    }
    
    /**
     * Visitor to find defined variables
     */
    private static class VariableDefinitionVisitor extends ASTVisitor {
        private Set<IVariableBinding> definedVariables = new HashSet<>();
        
        @Override
        public boolean visit(VariableDeclarationFragment node) {
            IVariableBinding binding = node.resolveBinding();
            if (binding != null) {
                definedVariables.add(binding);
            }
            return true;
        }
        
        public Set<IVariableBinding> getDefinedVariables() {
            return definedVariables;
        }
    }
}

/**
 * Variable information
 */
class VariableInfo {
    private IVariableBinding binding;
    
    public VariableInfo(IVariableBinding binding) {
        this.binding = binding;
    }
    
    public String getName() {
        return binding.getName();
    }
    
    public ITypeBinding getType() {
        return binding.getType();
    }
    
    public String getTypeName() {
        return binding.getType().getName();
    }
    
    public IVariableBinding getBinding() {
        return binding;
    }
    
    @Override
    public String toString() {
        return getTypeName() + " " + getName();
    }
}