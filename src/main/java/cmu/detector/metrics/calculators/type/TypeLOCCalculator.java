package cmu.detector.metrics.calculators.type;


import cmu.detector.ast.visitors.LineCountingVisitor;
import cmu.detector.metrics.MetricName;
import cmu.detector.metrics.calculators.MetricValueCalculator;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;

/**
 * 	Assumes that the root node is a type declaration and retrieves its compilation unit.
 *  This visitor explores all nodes and identify their lines of code.
 *  As output, it provides the total lines of code (loc) for a class
 *
 * @author Leonardo
 */
public class TypeLOCCalculator extends MetricValueCalculator {
	
	@Override
	protected Double computeValue(ASTNode target) {
		// We need to get the CompilationUnit from this node
		CompilationUnit compilationUnit = getCompilationUnit(target);

		/*
		 * Use the LineCountingVisitor to count lines of code.
		 * Visit the CompilationUnit directly to handle package and imports instead of using the TypeDeclaration
		 */
		LineCountingVisitor visitor = new LineCountingVisitor(compilationUnit);
		compilationUnit.accept(visitor);

		return (double) visitor.getLOC();
	}

	@Override
	public MetricName getMetricName() {
		return MetricName.CLOC;
	}
	
	@Override
	public boolean shouldComputeAggregate() {
		return true;
	}

	private CompilationUnit getCompilationUnit(ASTNode node) {
		ASTNode current = node;

		while (current != null && !(current instanceof CompilationUnit)) {
			current = current.getParent();
		}

		return (CompilationUnit) current;
	}

}
