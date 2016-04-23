package be.ugent.intec.ibcn.dbase.tooling.editor;

import org.eclipse.bpmn2.BaseElement;
import org.eclipse.bpmn2.IntermediateThrowEvent;
import org.eclipse.bpmn2.modeler.core.features.CustomShapeFeatureContainer;
import org.eclipse.bpmn2.modeler.core.features.MultiUpdateFeature;
import org.eclipse.bpmn2.modeler.core.features.label.UpdateLabelFeature;
import org.eclipse.bpmn2.modeler.core.model.ModelDecorator;
import org.eclipse.bpmn2.modeler.core.utils.BusinessObjectUtil;
import org.eclipse.bpmn2.modeler.core.utils.StyleUtil;
import org.eclipse.bpmn2.modeler.ui.features.event.IntermediateThrowEventFeatureContainer;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.graphiti.features.IAddFeature;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.IUpdateFeature;
import org.eclipse.graphiti.features.context.IAddContext;
import org.eclipse.graphiti.mm.algorithms.Polygon;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.Shape;
import org.eclipse.graphiti.services.Graphiti;

public class MonitoringPointFeatureContainer extends
		CustomShapeFeatureContainer {

	// these values must match what's in the plugin.xml
	private final static String TYPE_VALUE = "MonitoringPoint";
	private final static String CUSTOM_TASK_ID = "be.ugent.intec.ibcn.dbase.tooling.monitoringPoint";

	public MonitoringPointFeatureContainer() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getId(EObject object) {
		// This is where we inspect the object to determine what its custom task
		// ID should be.
		// In this case, the "type" attribute will have a value of
		// "MonitoringPoint".
		// If found, return the CUSTOM_TASK_ID string.
		//
		// Note that the object inspection can be arbitrarily complex and may
		// include several
		// object features. This simple case just demonstrates what needs to
		// happen here.
		EStructuralFeature f = ModelDecorator.getAnyAttribute(object, "type");
		if (f != null) {
			Object id = object.eGet(f);
			if (TYPE_VALUE.equals(id))
				return CUSTOM_TASK_ID;
		}

		return null;
	}

	@Override
	protected IntermediateThrowEventFeatureContainer createFeatureContainer(
			IFeatureProvider fp) {
		return new IntermediateThrowEventFeatureContainer() {
			@Override
			public IUpdateFeature getUpdateFeature(IFeatureProvider fp) {
				MultiUpdateFeature multiUpdate = new MultiUpdateFeature(fp);
				multiUpdate.addFeature(new UpdateLabelFeature(fp) {
					@Override
					protected String getLabelString(BaseElement element) {
						String l = super.getLabelString(element);

						// change the label string here!
						return "";
					}
				});
				return multiUpdate;
			}

			@Override
			public IAddFeature getAddFeature(IFeatureProvider fp) {
				return new AddIntermediateThrowEventFeature(fp) {
					@Override
					protected void decorateShape(IAddContext context,
							ContainerShape containerShape,
							IntermediateThrowEvent businessObject) {
						super.decorateShape(context, containerShape,
								businessObject);
						IntermediateThrowEvent ta = BusinessObjectUtil
								.getFirstElementOfType(containerShape,
										IntermediateThrowEvent.class);
						if (ta != null) {
							Shape shape = Graphiti.getPeService().createShape(
									containerShape, false);

							final int eventHeight = shape.getContainer()
									.getGraphicsAlgorithm().getHeight();
							final int eventWidth = shape.getContainer()
									.getGraphicsAlgorithm().getWidth();

							final float heightRatio = calculateRatio(
									eventHeight, Float.valueOf(36));
							final float widthRatio = calculateRatio(eventWidth,
									Float.valueOf(36));

							Polygon polygon = gaService.createPolygon(shape, 
									new int[] { generateRatioPointValue(3, widthRatio), generateRatioPointValue(18, heightRatio),
												generateRatioPointValue(7, widthRatio), generateRatioPointValue(18, heightRatio),
												generateRatioPointValue(10, widthRatio), generateRatioPointValue(10, heightRatio),
												generateRatioPointValue(13, widthRatio), generateRatioPointValue(26, heightRatio),
												generateRatioPointValue(16, widthRatio), generateRatioPointValue(14, heightRatio),
												generateRatioPointValue(19, widthRatio), generateRatioPointValue(22, heightRatio),
												generateRatioPointValue(22, widthRatio), generateRatioPointValue(10, heightRatio),
												generateRatioPointValue(25, widthRatio), generateRatioPointValue(24, heightRatio),
												generateRatioPointValue(28, widthRatio), generateRatioPointValue(18, heightRatio),
												generateRatioPointValue(31, widthRatio), generateRatioPointValue(18, heightRatio),
												generateRatioPointValue(31, widthRatio), generateRatioPointValue(18, heightRatio),
												generateRatioPointValue(28, widthRatio), generateRatioPointValue(18, heightRatio),
												generateRatioPointValue(25, widthRatio), generateRatioPointValue(24, heightRatio),
												generateRatioPointValue(22, widthRatio), generateRatioPointValue(10, heightRatio),
												generateRatioPointValue(19, widthRatio), generateRatioPointValue(22, heightRatio),
												generateRatioPointValue(16, widthRatio), generateRatioPointValue(14, heightRatio),
												generateRatioPointValue(13, widthRatio), generateRatioPointValue(26, heightRatio),
												generateRatioPointValue(10, widthRatio), generateRatioPointValue(10, heightRatio),
												generateRatioPointValue(7, widthRatio), generateRatioPointValue(18, heightRatio),
												generateRatioPointValue(3, widthRatio), generateRatioPointValue(18, heightRatio)
											 });
							polygon.setLineWidth(2);
							StyleUtil.applyStyle(polygon, ta);
						}
					}

					private int generateRatioPointValue(float originalPointValue, float widthRatio) {
						return Math.round(Float.valueOf(originalPointValue
								* widthRatio));
					}

					private float calculateRatio(float x, Float y) {
						return x / y;
					}

				};
			}

		};

	}

}
