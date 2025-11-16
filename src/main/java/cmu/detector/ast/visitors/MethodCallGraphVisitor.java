package cmu.detector.ast.visitors;

import org.eclipse.jdt.core.dom.*;
import java.util.*;

public class MethodCallGraphVisitor extends ASTVisitor {
    
    private Map<MethodDeclaration, Set<SimpleName>> methodCalls;
    private MethodDeclaration currentMethod;
    
    public MethodCallGraphVisitor() {
        this.methodCalls = new HashMap<>();
        this.currentMethod = null;
    }
    
    @Override
    public boolean visit(MethodDeclaration node) {
        currentMethod = node;
        methodCalls.put(node, new HashSet<>());
        return true;
    }
    
    @Override
    public void endVisit(MethodDeclaration node) {
        currentMethod = null;
    }
    
    @Override
    public boolean visit(MethodInvocation node) {
        if (currentMethod == null) {
            return true;
        }
        
        // Only consider calls without explicit receiver or with 'this' receiver
        Expression expr = node.getExpression();
        if (expr == null || expr instanceof ThisExpression) {
            methodCalls.get(currentMethod).add(node.getName());
        }
        
        return true;
    }
    
    /**
     * Get the mapping of methods to the method names they call
     */
    public Map<MethodDeclaration, Set<SimpleName>> getMethodCalls() {
        return methodCalls;
    }
}