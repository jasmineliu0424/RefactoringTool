package cmu.detector.ast.visitors;

import org.eclipse.jdt.core.dom.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FieldAccessVisitor extends ASTVisitor {
    
    private Map<MethodDeclaration, Set<IVariableBinding>> methodFieldAccess;
    private Set<IVariableBinding> instanceFields;
    private MethodDeclaration currentMethod;
    
    public FieldAccessVisitor(Set<IVariableBinding> instanceFields) {
        this.instanceFields = instanceFields;
        this.methodFieldAccess = new HashMap<>();
        this.currentMethod = null;
    }
    
    @Override
    public boolean visit(MethodDeclaration node) {
        currentMethod = node;
        methodFieldAccess.put(node, new HashSet<>());
        return true;
    }
    
    @Override
    public void endVisit(MethodDeclaration node) {
        currentMethod = null;
    }
    
    @Override
    public boolean visit(SimpleName node) {
        if (currentMethod == null) {
            return true;
        }
        
        IBinding binding = node.resolveBinding();
        if (binding instanceof IVariableBinding) {
            IVariableBinding varBinding = (IVariableBinding) binding;
            if (varBinding.isField()) {
                for (IVariableBinding field : instanceFields) {
                    if (varBinding.isEqualTo(field)) {
                        methodFieldAccess.get(currentMethod).add(field);
                        break;
                    }
                }
            }
        }
        return true;
    }
    
    /**
     * Get the mapping of methods to the fields they access
     */
    public Map<MethodDeclaration, Set<IVariableBinding>> getMethodFieldAccess() {
        return methodFieldAccess;
    }
}