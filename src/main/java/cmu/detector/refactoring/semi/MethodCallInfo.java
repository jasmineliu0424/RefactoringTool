package cmu.detector.refactoring.semi;

import org.eclipse.jdt.core.dom.*;

/**
 * Information about a method call
 */
public class MethodCallInfo {
    private IMethodBinding methodBinding;
    private IVariableBinding receiverBinding;
    private ITypeBinding receiverType;
    
    public MethodCallInfo(MethodInvocation invocation) {
        this.methodBinding = invocation.resolveMethodBinding();
        
        Expression expr = invocation.getExpression();
        if (expr != null) {
            if (expr instanceof SimpleName) {
                SimpleName name = (SimpleName) expr;
                IBinding binding = name.resolveBinding();
                if (binding instanceof IVariableBinding) {
                    this.receiverBinding = (IVariableBinding) binding;
                    this.receiverType = receiverBinding.getType();
                }
            } else {
                ITypeBinding type = expr.resolveTypeBinding();
                this.receiverType = type;
            }
        }
    }
    
    /**
     * Check if two calls have the same receiver object
     */
    public boolean isSameReceiver(MethodCallInfo other) {
        if (this.receiverBinding == null || other.receiverBinding == null) {
            return false;
        }
        return this.receiverBinding.isEqualTo(other.receiverBinding);
    }
    
    /**
     * Check if two calls are to the same method on different objects of same type
     */
    public boolean isSameMethodDifferentReceiver(MethodCallInfo other) {
        // Different receivers
        if (this.receiverBinding != null && other.receiverBinding != null) {
            if (this.receiverBinding.isEqualTo(other.receiverBinding)) {
                return false;
            }
        }
        
        // Same method
        if (this.methodBinding == null || other.methodBinding == null) {
            return false;
        }
        
        if (!this.methodBinding.getName().equals(other.methodBinding.getName())) {
            return false;
        }
        
        // Same receiver type
        if (this.receiverType == null || other.receiverType == null) {
            return false;
        }
        
        return this.receiverType.isEqualTo(other.receiverType);
    }
}