package be.ugent.intec.ibcn.dbase.tooling.editor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.bpmn2.EndEvent;
import org.eclipse.bpmn2.ExclusiveGateway;
import org.eclipse.bpmn2.FlowElement;
import org.eclipse.bpmn2.FlowNode;
import org.eclipse.bpmn2.ParallelGateway;
import org.eclipse.bpmn2.Process;
import org.eclipse.bpmn2.SequenceFlow;
import org.eclipse.bpmn2.StartEvent;
import org.eclipse.emf.ecore.util.EcoreUtil;

class ProcessHelper {
	
	public static List<Process> removeChoices(Process process){
		List<FlowElement> flowElements = process.getFlowElements();
		FlowElement startEvent = null;
		for (FlowElement fe : flowElements) {
			if (fe instanceof StartEvent) {
				startEvent = fe;
			}
		}
		
		List<Process> processes = new ArrayList<Process>();
		if(startEvent == null)
			return processes;
		
		List<List<FlowElement>> paden = new ArrayList<List<FlowElement>>();
		List<FlowElement> pad = new ArrayList<FlowElement>();
		paden.add(pad);
		
		FlowElement huidig = startEvent;
		visit(huidig, paden, pad);
		
		for(List<FlowElement> pad1 : paden){
			process.getFlowElements().clear();
			process.getFlowElements().addAll(pad1);
			processes.add(EcoreUtil.copy(process));
		}
		
		return processes;
	}
	
	private static void visit(FlowElement element, List<List<FlowElement>> paden, List<FlowElement> pad){
		pad.add(element);
		if(element instanceof EndEvent){
			return;
		}else if(element instanceof SequenceFlow){
			visit(((SequenceFlow) element).getTargetRef(), paden, pad);
		}else{
			List<SequenceFlow> outgoing =((FlowNode) element).getOutgoing();
			if(element instanceof ExclusiveGateway){
				// split pad als er een keuze is
				// verwijder huidige pad
				paden.remove(pad);
				for(SequenceFlow flow : outgoing){
					// voor elke uitgaande flow, nieuw pad toevoegen
					List<FlowElement> nieuwPad = new ArrayList<FlowElement>();
					nieuwPad.addAll(pad);
					paden.add(nieuwPad);
					visit(flow, paden, nieuwPad);
				}
			}else{
				for(SequenceFlow flow : outgoing){
					visit(flow, paden, pad);
				}
			}
		}
	}

}
