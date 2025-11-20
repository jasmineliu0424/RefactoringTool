package cmu.detector.refactoring.semi;

import org.eclipse.jdt.core.dom.*;
import java.util.*;

/**
 * Validates if an extract opportunity is valid
 */
public class OpportunityValidator {
    
    private ExtractOpportunity opportunity;
    
    public OpportunityValidator(ExtractOpportunity opportunity) {
        this.opportunity = opportunity;
    }
    
    /**
     * Validate the opportunity
     */
    public boolean validate() {
        return validateSyntax() && validateSemantics() && validateBehavior();
    }
    
    /**
     * Validate syntactic preconditions (complete blocks)
     */
    private boolean validateSyntax() {
        List<Statement> statements = opportunity.getStatements();
        
        // Check if all blocks are complete
        Stack<ASTNode> blockStack = new Stack<>();
        
        for (Statement stmt : statements) {
            BlockChecker checker = new BlockChecker();
            stmt.accept(checker);
            
            // If statement opens a block, it must close it within the cluster
            if (checker.opensBlock() && !checker.closesBlock()) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Validate semantic preconditions
     */
    private boolean validateSemantics() {
        // Check for control flow issues
        for (Statement stmt : opportunity.getStatements()) {
            if (stmt instanceof ReturnStatement || 
                stmt instanceof BreakStatement || 
                stmt instanceof ContinueStatement) {
                // These require special handling
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Validate behavioral preconditions
     */
    private boolean validateBehavior() {
        // Check for multiple primitive returns
        List<VariableInfo> params = opportunity.getParameters();
        ITypeBinding returnType = opportunity.getReturnType();
        
        // Find variables that need to be returned
        Set<IVariableBinding> returnVars = findReturnVariables();
        
        // Count primitive return variables
        int primitiveReturns = 0;
        for (IVariableBinding var : returnVars) {
            if (var.getType().isPrimitive()) {
                primitiveReturns++;
            }
        }
        
        // Cannot return more than one primitive value
        if (primitiveReturns > 1) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Find variables that need to be returned
     */
    private Set<IVariableBinding> findReturnVariables() {
        ReturnTypeAnalyzer analyzer = new ReturnTypeAnalyzer(
            new StatementCluster(0, opportunity.getStatements().size() - 1, 
                               opportunity.getStatements()),
            opportunity.getStatements().get(0).getParent() instanceof MethodDeclaration ?
                (MethodDeclaration) opportunity.getStatements().get(0).getParent() : null
        );
        
        // This is a simplified version - the actual implementation would need
        // to track variables defined in cluster and used outside
        return new HashSet<>();
    }
    
    /**
     * Helper to check if statement opens/closes blocks
     */
    private static class BlockChecker extends ASTVisitor {
        private boolean opensBlock = false;
        private boolean closesBlock = false;
        
        @Override
        public boolean visit(IfStatement node) {
            opensBlock = true;
            closesBlock = node.getElseStatement() == null || 
                         hasCompleteBlock(node.getThenStatement());
            return false;
        }
        
        @Override
        public boolean visit(ForStatement node) {
            opensBlock = true;
            closesBlock = hasCompleteBlock(node.getBody());
            return false;
        }
        
        @Override
        public boolean visit(WhileStatement node) {
            opensBlock = true;
            closesBlock = hasCompleteBlock(node.getBody());
            return false;
        }
        
        @Override
        public boolean visit(TryStatement node) {
            opensBlock = true;
            closesBlock = !node.catchClauses().isEmpty() || 
                         node.getFinally() != null;
            return false;
        }
        
        private boolean hasCompleteBlock(Statement stmt) {
            return stmt instanceof Block;
        }
        
        public boolean opensBlock() {
            return opensBlock;
        }
        
        public boolean closesBlock() {
            return closesBlock;
        }
    }
}