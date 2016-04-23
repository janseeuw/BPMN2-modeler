package be.ugent.intec.ibcn.dbase.tooling.editor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.bpmn2.Bpmn2Factory;
import org.eclipse.bpmn2.Definitions;
import org.eclipse.bpmn2.EndEvent;
import org.eclipse.bpmn2.FlowElement;
import org.eclipse.bpmn2.FlowNode;
import org.eclipse.bpmn2.Gateway;
import org.eclipse.bpmn2.IntermediateThrowEvent;
import org.eclipse.bpmn2.ParallelGateway;
import org.eclipse.bpmn2.Process;
import org.eclipse.bpmn2.SequenceFlow;
import org.eclipse.bpmn2.StartEvent;
import org.eclipse.bpmn2.modeler.core.model.ModelDecorator;
import org.eclipse.bpmn2.modeler.core.validation.validators.AbstractBpmn2ElementValidator;
import org.eclipse.bpmn2.modeler.core.validation.validators.BaseElementValidator;
import org.eclipse.bpmn2.modeler.core.validation.validators.FlowElementsContainerValidator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emf.validation.IValidationContext;

public class ProcessValidator extends AbstractBpmn2ElementValidator<Process> {

	/**
	 * @param ctx
	 */
	public ProcessValidator(IValidationContext ctx) {
		super(ctx);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.bpmn2.modeler.core.validation.validators.
	 * AbstractBpmn2ElementValidator#validate(org.eclipse.bpmn2.BaseElement)
	 */
	@Override
	public IStatus validate(Process object) {
	
		List<Process> paden = ProcessHelper.removeChoices(object);

		// save pad
		for(Process pad1 : paden){
			//savePad(object, pad1.getFlowElements());
		}

		new BaseElementValidator(this).validate(object);		

		return getResult();
	}
	
	
	public void savePad(Process proc, List<FlowElement> pad){
		proc.getFlowElements().clear();
		proc.getFlowElements().addAll(pad);
		
		Resource.Factory.Registry reg = Resource.Factory.Registry.INSTANCE;
		Map<String, Object> m = reg.getExtensionToFactoryMap();
		m.put("key", new XMIResourceFactoryImpl());
		ResourceSet resSet = new ResourceSetImpl();
		Resource resource = resSet.createResource(URI.createFileURI("C:\\Users\\janseeuw\\Documents\\" + pad.hashCode() + ".bpmn"));
		resource.getContents().add(proc.eContainer().eContainer());
		try {
			resource.save(Collections.EMPTY_MAP);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}