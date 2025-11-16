package cmu.detector.metrics;

import cmu.detector.metrics.calculators.type.LackOfCohesion1Calculator;
import cmu.detector.metrics.calculators.type.LackOfCohesion2Calculator;
import cmu.detector.metrics.calculators.type.LackOfCohesion3Calculator;
import cmu.detector.metrics.calculators.type.LackOfCohesion4Calculator;
import cmu.detector.metrics.calculators.type.PublicFieldCountCalculator;
import cmu.detector.metrics.calculators.type.TCCMetricValueCalculator;

public class TypeMetricValueCollector extends MetricValueCollector {

	public TypeMetricValueCollector() {
		addCalculator(new PublicFieldCountCalculator());
		addCalculator(new LackOfCohesion1Calculator());
		addCalculator(new LackOfCohesion2Calculator());
		addCalculator(new LackOfCohesion3Calculator());
		addCalculator(new LackOfCohesion4Calculator());
		addCalculator(new TCCMetricValueCalculator());
	}

}
