package be.ugent.intec.ibcn.dbase.tooling.editor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

import org.eclipse.bpmn2.BaseElement;
import org.eclipse.bpmn2.Bpmn2Factory;
import org.eclipse.bpmn2.Collaboration;
import org.eclipse.bpmn2.FlowElement;
import org.eclipse.bpmn2.FlowNode;
import org.eclipse.bpmn2.Gateway;
import org.eclipse.bpmn2.InteractionNode;
import org.eclipse.bpmn2.IntermediateThrowEvent;
import org.eclipse.bpmn2.MessageFlow;
import org.eclipse.bpmn2.Participant;
import org.eclipse.bpmn2.Process;
import org.eclipse.bpmn2.SequenceFlow;
import org.eclipse.bpmn2.StartEvent;
import org.eclipse.bpmn2.impl.InclusiveGatewayImpl;
import org.eclipse.bpmn2.modeler.core.validation.validators.AbstractBpmn2ElementValidator;
import org.eclipse.bpmn2.modeler.core.validation.validators.BaseElementValidator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.validation.IValidationContext;

public class CollaborationValidator extends AbstractBpmn2ElementValidator<Collaboration> {
	
	/**
	 * @param ctx
	 */
	public CollaborationValidator(IValidationContext ctx) {
		super(ctx);
	}
	
	@Override
	public IStatus validate(Collaboration object) {			
		// participants --> processes
		List<Participant> participants;
		List<List<Process>> processCombinations;
		participants = object.getParticipants();
		processCombinations = new ArrayList<List<Process>>();
		for(Participant p : participants){
			// processes --> remove choices --> more processes
			// door process op te splitsen zullen referenties uit messageflows niet meer kloppen
			// manueel fixen door op IDs te vergelijken
			List<Process> proc = ProcessHelper.removeChoices(p.getProcessRef());
			processCombinations.add(proc);
		}
				
		// participants --> processes => collaborations --> participant --> process 
		// brute force alle mogelijke combinaties
		processCombinations = Helper.getCross(processCombinations);
		List<Collaboration> collaborations = new ArrayList<Collaboration>();
		for(List<Process> processCombination : processCombinations){
			Collaboration collaboration = Bpmn2Factory.eINSTANCE.createCollaboration();
			for(Process process : processCombination){
				Participant participant = Bpmn2Factory.eINSTANCE.createParticipant();
				participant.setProcessRef(process);
				collaboration.getParticipants().add(participant);
			}
			collaborations.add(collaboration);
		}
		
		// plaatsen van messageflows in collaborations
		// verwijder invalide collaborations als messageflow niet past
		List<MessageFlow> messageFlows = object.getMessageFlows();
		Set<Collaboration> invalid = new HashSet<Collaboration>();
		for(MessageFlow messageFlow : messageFlows){
			String sourceId = ((BaseElement) messageFlow.getSourceRef()).getId();
			String targetId = ((BaseElement) messageFlow.getTargetRef()).getId();
			for(Collaboration collaboration : collaborations){
				InteractionNode source = null;
				InteractionNode target = null;
				for(Participant participant : collaboration.getParticipants()){
					Process process = participant.getProcessRef();
					for(FlowElement flowElement : process.getFlowElements()){
						if(flowElement.getId().equals(sourceId)){
							source = (InteractionNode) flowElement;
						}else if(flowElement.getId().equals(targetId)){
							target = (InteractionNode) flowElement;
						}
					}
				}
				if(source != null && target != null){
					MessageFlow newMessageFlow = Bpmn2Factory.eINSTANCE.createMessageFlow();
					newMessageFlow.setSourceRef(source);
					newMessageFlow.setTargetRef(target);
					collaboration.getMessageFlows().add(newMessageFlow);
				}else if(source != null && target == null){
					invalid.add(collaboration);
				}else if(target != null && source == null){
					invalid.add(collaboration);
				}
			}
		}
		collaborations.removeAll(invalid);
		
		// check of alle paden wel degelijk voorkomen in een collaboration
		Set<Process> processes = new HashSet<Process>();
		for(List<Process> processCombination : processCombinations){
			for(Process process : processCombination){
				processes.add(process);
			}
		}
		for(Collaboration collaboration : collaborations){
			for(Participant participant : collaboration.getParticipants()){
				processes.remove(participant.getProcessRef());
			}
		}
		if(!processes.isEmpty()){
			// niet alle paden worden gebruikt
			addStatus(object, "error", Status.ERROR, "fout tijdens removeChoices", object.getName(), object.getId()); //$NON-NLS-1$
		}
		
		// per COLLABORATION	
		for(Collaboration collaboration : collaborations){
			boolean valid = CollaborationHelper.validate(object, collaboration);
			if(!valid){
				addStatus(object, "error", Status.ERROR, "fout tijdens validatie", object.getName(), object.getId()); //$NON-NLS-1$
			}
		}
		
		new BaseElementValidator(this).validate(object);
		
		return getResult();
	}
	



}
