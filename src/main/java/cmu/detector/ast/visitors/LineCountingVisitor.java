package cmu.detector.ast.visitors;

import org.eclipse.jdt.core.dom.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Visits a type or method body in order to count the lines of code
 *
 * @author Leonardo
 */
public class LineCountingVisitor extends ASTVisitor {
    private CompilationUnit compilationUnit;
    private Set<Integer> lineNumbers;
    int lineCount = 0;

    public LineCountingVisitor(CompilationUnit compilationUnit) {
        this.compilationUnit = compilationUnit;
        this.lineNumbers = new HashSet<>();
    }

    @Override
    public void preVisit(ASTNode node) {
        int nodeStartPosition = node.getStartPosition();
        int startLine = compilationUnit.getLineNumber(nodeStartPosition);

        lineNumbers.add(startLine);

        if(checkIfNodeStartsWithComment(nodeStartPosition)){
            lineNumbers.remove(startLine);
        } else {
            addClosingBraceLineNumber(node);
        }

    }

    private boolean checkIfNodeStartsWithComment(int nodeStartPosition) {
        List<Comment> comments = (List<Comment>) compilationUnit.getCommentList();

        for (Comment comment : comments) {
            if (comment instanceof LineComment || comment instanceof BlockComment || comment instanceof Javadoc) {
                int commentStartPosition = comment.getStartPosition();
                int commentEndPosition = commentStartPosition + comment.getLength();

                // Check if the comment is before or at the same position as the node
                if (commentStartPosition <= nodeStartPosition && commentEndPosition > nodeStartPosition) {
                    // This means the node starts with a comment
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public void endVisit(TypeDeclaration node) {
        addClosingBraceLineNumber(node);
    }

    @Override
    public void endVisit(MethodDeclaration node) {
        addClosingBraceLineNumber(node);
    }

    @Override
    public void endVisit(Block node) {
        addClosingBraceLineNumber(node);
    }

    @Override
    public void endVisit(ForStatement node) {
        addClosingBraceLineNumber(node);
    }

    @Override
    public void endVisit(WhileStatement node) {
        addClosingBraceLineNumber(node);
    }

    @Override
    public void endVisit(DoStatement node) {
        addClosingBraceLineNumber(node);
    }

    @Override
    public void endVisit(SwitchStatement node) {
        addClosingBraceLineNumber(node);
    }

    @Override
    public void endVisit(TryStatement node) {
        addClosingBraceLineNumber(node);
    }

    @Override
    public void endVisit(CatchClause node) {
        addClosingBraceLineNumber(node);
    }

    @Override
    public void endVisit(SynchronizedStatement node) {
        addClosingBraceLineNumber(node);
    }

    @SuppressWarnings("unchecked")
    private void addClosingBraceLineNumber(ASTNode node) {
        // 1. Find the deepest "real content" line inside this node
        int closingBraceLineNumber = getLastContentLine(node);

        // Edge case: single-line declarations like "{}"
        // In that case, node has basically no children and getLastContentLine(node)
        // will return the node's own end line; then +1 would overshoot.
        // We correct by not going past the node's own end line.
        int nodeEndPos = node.getStartPosition() + node.getLength() - 1;
        int nodeEndLine = compilationUnit.getLineNumber(nodeEndPos);

        if (closingBraceLineNumber > nodeEndLine) {
            closingBraceLineNumber = nodeEndLine;
        }

        // 3. Preserve your filtering logic
        int closingBraceApproxPos = compilationUnit.getPosition(closingBraceLineNumber, 0);
        // column 0 of that line; this gives us *a* position on that line
        // (we just need something stable to pass into the check)
        if (!checkIfNodeStartsWithComment(closingBraceApproxPos)) {
            lineNumbers.add(closingBraceLineNumber);
        }
    }

    /**
     * Returns the maximum line number of any meaningful descendant of `node`.
     * Falls back to node's own end line if it has no children.
     */
    @SuppressWarnings("unchecked")
    private int getLastContentLine(ASTNode node) {
        int maxLine = -1;

        // Walk all structural properties of this node and inspect children
        for (Object propObj : node.structuralPropertiesForType()) {
            StructuralPropertyDescriptor prop = (StructuralPropertyDescriptor) propObj;

            if (prop.isChildProperty()) {
                Object child = node.getStructuralProperty(prop);
                if (child instanceof ASTNode) {
                    maxLine = Math.max(maxLine, getNodeEndLine((ASTNode) child, compilationUnit));
                }

            } else if (prop.isChildListProperty()) {
                Object childList = node.getStructuralProperty(prop);
                if (childList instanceof List<?>) {
                    for (Object o : (List<?>) childList) {
                        if (o instanceof ASTNode) {
                            maxLine = Math.max(maxLine, getNodeEndLine((ASTNode) o, compilationUnit));
                        }
                    }
                }
            }
        }

        // If no children were found (e.g. empty class {} or empty method body {}),
        // fallback to this node's own end line
        if (maxLine == -1) {
            return getNodeEndLine(node, compilationUnit);
        }

        return maxLine;
    }

    private int getNodeEndLine(ASTNode n, CompilationUnit cu) {
        int endPos = n.getStartPosition() + n.getLength() - 1;
        return cu.getLineNumber(endPos);
    }


    /**
     * It returns the lines of code (LOC) of a class
     *
     * @return LOC
     */
    public int getLOC() {
        return lineNumbers.size();
    }
}
