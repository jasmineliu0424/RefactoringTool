package cmu.detector.ast.visitors;

import org.eclipse.jdt.core.dom.*;
import java.util.ArrayList;
import java.util.List;

public class VisibleInstanceMethodsVisitor extends ASTVisitor {
    
    private List<MethodDeclaration> visibleMethods;
    private boolean visitingTypeDeclaration;
    
    public VisibleInstanceMethodsVisitor() {
        this.visibleMethods = new ArrayList<>();
        this.visitingTypeDeclaration = false;
    }
    
    @Override
    public boolean visit(TypeDeclaration node) {
        if (!visitingTypeDeclaration) {
            visitingTypeDeclaration = true;
            return true;
        }
        // Don't visit inner classes
        return false;
    }
    
    @Override
    public boolean visit(AnonymousClassDeclaration node) {
        // Don't visit anonymous classes
        return false;
    }
    
    @Override
    public boolean visit(MethodDeclaration node) {
        int modifiers = node.getModifiers();
        // Exclude static and private methods
        if (!Modifier.isStatic(modifiers) && !Modifier.isPrivate(modifiers)) {
            visibleMethods.add(node);
        }
        return false;
    }
    
    public List<MethodDeclaration> getVisibleMethods() {
        return visibleMethods;
    }
}