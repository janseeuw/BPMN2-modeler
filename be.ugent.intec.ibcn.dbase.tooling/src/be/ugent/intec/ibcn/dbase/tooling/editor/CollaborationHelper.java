package be.ugent.intec.ibcn.dbase.tooling.editor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;

import org.eclipse.bpmn2.Collaboration;
import org.eclipse.bpmn2.EndEvent;
import org.eclipse.bpmn2.ExclusiveGateway;
import org.eclipse.bpmn2.FlowElement;
import org.eclipse.bpmn2.FlowNode;
import org.eclipse.bpmn2.Gateway;
import org.eclipse.bpmn2.GatewayDirection;
import org.eclipse.bpmn2.MessageFlow;
import org.eclipse.bpmn2.ParallelGateway;
import org.eclipse.bpmn2.Participant;
import org.eclipse.bpmn2.Process;
import org.eclipse.bpmn2.SequenceFlow;
import org.eclipse.bpmn2.StartEvent;
import org.eclipse.bpmn2.impl.StartEventImpl;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

class CollaborationHelper {

	static List<Process> processes;
	static List<MessageFlow> messageFlows;
	static Map<FlowNode, MessageFlow> sending;
	static Map<FlowNode, MessageFlow> receiving;

	// //
	static Map<Process, List<TimeObject>> processVectors;
	static Map<FlowNode, TimeObject> flowNodeTimeObject;
	static Map<FlowNode, List<TimeObject>> flowNodeTimeVectors;
	// //
	
	//
	static Map<Process, Stack<FlowNode>> gateways;
	//

	public static boolean validate(Collaboration original,
			Collaboration collaboration) {
		// messageflows --> sending/receiving
		messageFlows = collaboration.getMessageFlows();
		sending = new HashMap<FlowNode, MessageFlow>();
		receiving = new HashMap<FlowNode, MessageFlow>();
		for (MessageFlow messageFlow : messageFlows) {
			FlowNode source = (FlowNode) messageFlow.getSourceRef();
			FlowNode target = (FlowNode) messageFlow.getTargetRef();
			sending.put(source, messageFlow);
			receiving.put(target, messageFlow);
		}

		processes = new ArrayList<Process>();
		for (Participant participant : collaboration.getParticipants()) {
			processes.add(participant.getProcessRef());
		}
		

		// //
		processVectors = new HashMap<Process, List<TimeObject>>();
		gateways = new HashMap<Process, Stack<FlowNode>>();

		for (Process process : processes) {
			List<TimeObject> vector = new ArrayList<TimeObject>();
			for (int i = 0; i < processes.size(); i++) {
				vector.add(new TimeObject());
			}
			processVectors.put(process, vector);
			gateways.put(process, new Stack<FlowNode>());
		}
		// //

		// start pointers per process
		// assign time objects
		List<FlowNode> pointers = new ArrayList<FlowNode>();
		flowNodeTimeObject = new HashMap<FlowNode, TimeObject>();
		flowNodeTimeVectors = new HashMap<FlowNode, List<TimeObject>>();
		for (Process process : processes) {
			if (process != null) {
				List<FlowElement> flowElements = process.getFlowElements();
				for (FlowElement flowElement : flowElements) {
					if (flowElement instanceof StartEvent) {
						pointers.add((FlowNode) flowElement);

						// //
						int i = processes.indexOf(process);
						TimeObject timeObject = processVectors.get(process)
								.get(i);
						flowNodeTimeObject.put((FlowNode) flowElement,
								TimeObject.clone(timeObject));
						List<TimeObject> timeVector = processVectors.get(process);
						flowNodeTimeVectors.put((FlowNode) flowElement, TimeObject.clone(timeVector));
						
						printTimeObject((FlowNode) flowElement);
						printTimeVector((FlowNode) flowElement);
						// //
					}
				}
			}
		}

		// move pointers and set vector clocks per node
		while (pointers.size() != 0) {

			// move pointers as long as they are not sending or receiving and
			// not on gateway (diverging/converging)
			// while incrementing vector clocks per node
			List<FlowNode> updatedPointers = new ArrayList<FlowNode>();
			for (FlowNode pointer : pointers) {
				while (pointer != null && hasNext(pointer)
						&& !isReceiving(pointer) && !isSending(pointer)
						&& !isConverging(pointer) && !isDiverging(pointer)) {
					
					// // Hold current time
					TimeObject timeObject = flowNodeTimeObject.get(pointer);
					List<TimeObject> timeVector = flowNodeTimeVectors.get(pointer);
					// //
					
					pointer = moveNext(pointer);
					if (pointer != null && !isConverging(pointer)) {
						
						// // Assign to next flow element
						TimeObject to = TimeObject.clone(timeObject);
						List<TimeObject> tv = TimeObject.clone(timeVector);
						flowNodeTimeObject.put(pointer, to);
						flowNodeTimeVectors.put(pointer, tv);
						printTimeObject(pointer);
						printTimeVector(pointer);
						// //

						if (!(pointer instanceof StartEvent
								|| pointer instanceof EndEvent
								|| pointer instanceof Gateway
								|| isSending(pointer) || isReceiving(pointer))) {

							// // increment if not startevent or gateway
							to.increment();
							tv.get(processes.indexOf(pointer.eContainer())).increment();

							printTimeObject(pointer);
							printTimeVector(pointer);
						}
					}

				}
				if (pointer != null) {
					updatedPointers.add(pointer);
				}
			}
			pointers = updatedPointers;

			// validate messageflows + fork/join
			List<FlowNode> updatedPointers2 = new ArrayList<FlowNode>();
			for (FlowNode pointer : pointers) {
				if (isSending(pointer) || isReceiving(pointer)) {
					// validate messageflow

					FlowNode sendingNode = null;
					FlowNode receivingNode = null;
					if (isSending(pointer)) {
						sendingNode = pointer;
						receivingNode = (FlowNode) sending.get(pointer)
								.getTargetRef();
					} else if (isReceiving(pointer)) {
						receivingNode = pointer;
						sendingNode = (FlowNode) receiving.get(pointer)
								.getSourceRef();
					}

					boolean sendingNodeReached = false;
					boolean receivingNodeReached = false;
					for (FlowNode ptr : pointers) {
						if (ptr.equals(sendingNode)) {
							sendingNodeReached = true;
						}
						if (ptr.equals(receivingNode)) {
							receivingNodeReached = true;
						}
					}

					if (sendingNodeReached == true
							&& receivingNodeReached == true) {
						// check valid
						boolean valid = validateMessageFlow(sendingNode, receivingNode);
						if (!valid) {
							//return false;
						}

						// increment clocks
						flowNodeTimeObject.get(sendingNode).increment();
						flowNodeTimeVectors.get(sendingNode).get(processes.indexOf(sendingNode.eContainer())).increment();
						printTimeObject(sendingNode);
						printTimeVector(sendingNode);
						flowNodeTimeObject.get(receivingNode).increment();
						flowNodeTimeVectors.get(receivingNode).get(processes.indexOf(receivingNode.eContainer())).increment();
						
						// update receiving process knowledge about sending
						// process
						List<TimeObject> receivingNodeTimeVector = flowNodeTimeVectors.get(receivingNode);
						List<TimeObject> sendingNodeTimeVector = flowNodeTimeVectors.get(sendingNode);
						TimeObject.supremumVectorClock(receivingNodeTimeVector, sendingNodeTimeVector);
						
						printTimeObject(receivingNode);
						printTimeVector(receivingNode);

						// move both pointers
						List<FlowNode> nodes = new ArrayList<FlowNode>();
						nodes.add(sendingNode);
						nodes.add(receivingNode);

						for (FlowNode pointer1 : nodes) {
							
							// // Hold current time
							TimeObject timeObject = flowNodeTimeObject.get(pointer1);
							List<TimeObject> timeVector = flowNodeTimeVectors.get(pointer1);
							// //			
							
							pointer1 = moveNext(pointer1);
							if (pointer1 != null && !isConverging(pointer1)) {

								// // Assign to next flow element
								TimeObject to = TimeObject.clone(timeObject);
								List<TimeObject> tv = TimeObject.clone(timeVector);
								flowNodeTimeObject.put(pointer1, to);
								flowNodeTimeVectors.put(pointer1, tv);
								printTimeObject(pointer1);
								printTimeVector(pointer1);
								// //

								if (!(pointer1 instanceof StartEvent
										|| pointer1 instanceof EndEvent
										|| pointer1 instanceof Gateway
										|| isSending(pointer1) || isReceiving(pointer1))) {

									// // increment if not start/end event,
									// gateway or receiving or sending
									to.increment();
									tv.get(processes.indexOf(pointer1.eContainer())).increment();
									printTimeObject(pointer1);
									printTimeVector(pointer1);

								}
								
							}

							updatedPointers2.add(pointer1);
						}
						
						// remove sending+receiving (when checking counterpart,
						// no need to fix messageflow twice)
						sending.remove(sendingNode);
						receiving.remove(receivingNode);

					} else {
						// keep waiting
						updatedPointers2.add(pointer);
					}

				} else if (isConverging(pointer)) {
					// join (indien nog niet gebeurt (geen timeobject voor
					// gateway)
					if (!flowNodeTimeObject.containsKey(pointer)) {
					
						// pas als paden volledig behandeld zijn.
						// evenveel pointers wijzen naar gateway, als er
						// inkomende paden zijn.
						int aantal = 0;
						for (FlowNode ptr : pointers) {
							if (ptr.getId().equals(pointer.getId())) {
								aantal++;
							}
						}
						List<SequenceFlow> incoming = pointer.getIncoming();
						if (aantal == incoming.size()) {
							// kijken als parallel zijn
							FlowNode firstGateway = gateways.get(pointer.eContainer()).pop();
							System.out.println(firstGateway.getName() + " - " + pointer.getName());
																			
							// first elements
							List<FlowNode> firstNodes = new ArrayList<FlowNode>();
							getFirstNodes(firstGateway, firstNodes);
							
							// last elements
							List<FlowNode> lastNodes = new ArrayList<FlowNode>();
							getLastNodes(pointer, lastNodes);
							
							// compare
							boolean parallel = false;
							for(FlowNode n1 : firstNodes){
								List<TimeObject> timeVectorN1 = flowNodeTimeVectors.get(n1);
								int k1 = timeVectorN1.get(processes.indexOf(n1.eContainer())).getK();
								for(FlowNode n2 : lastNodes){
									List<TimeObject> timeVectorN2 = flowNodeTimeVectors.get(n2);
									int k2 = timeVectorN2.get(processes.indexOf(n2.eContainer())).getK();
									// if zelfde k-waarde
									if(k1 != k2){
										System.out.println(n1.getName() + " " + TimeObject.printTimeVector(timeVectorN1) + " || " + n2.getName() + " " + TimeObject.printTimeVector(timeVectorN2) );
										for(int i=0; i<processes.size(); i++){
											TimeObject to1 = TimeObject.clone(timeVectorN1.get(i));
											while(to1.getParent() != null){
												to1 = to1.getParent();
											}
											TimeObject to2 = TimeObject.clone(timeVectorN2.get(i));
											while(to2.getParent() != null){
												to2 = to2.getParent();
											}
											if(to1.isParallel(to2)){
												parallel = true;
											}
										}
										if(!parallel){
											return false;
										}
									}
								}
								
							}
							for(FlowNode n1 : firstNodes){
								List<TimeObject> timeVectorN1 = flowNodeTimeVectors.get(n1);
								for(FlowNode n2 : firstNodes){
									if(!n1.equals(n2)){
										List<TimeObject> timeVectorN2 = flowNodeTimeVectors.get(n2);
										System.out.println(n1.getName() + " " + TimeObject.printTimeVector(timeVectorN1) + " || " + n2.getName() + " " + TimeObject.printTimeVector(timeVectorN2) );
										for(int i=0; i<processes.size(); i++){
											TimeObject to1 = TimeObject.clone(timeVectorN1.get(i));
											while(to1.getParent() != null){
												to1 = to1.getParent();
											}
											TimeObject to2 = TimeObject.clone(timeVectorN2.get(i));
											while(to2.getParent() != null){
												to2 = to2.getParent();
											}
											if(to1.isParallel(to2)){
												parallel = true;
											}
										}
										if(!parallel){
											return false;
										}
									}
								}
								
							}
							for(FlowNode n1 : lastNodes){
								List<TimeObject> timeVectorN1 = flowNodeTimeVectors.get(n1);
								for(FlowNode n2 : lastNodes){
									if(!n1.equals(n2)){
										List<TimeObject> timeVectorN2 = flowNodeTimeVectors.get(n2);
										System.out.println(n1.getName() + " " + TimeObject.printTimeVector(timeVectorN1) + " || " + n2.getName() + " " + TimeObject.printTimeVector(timeVectorN2) );
										for(int i=0; i<processes.size(); i++){
											TimeObject to1 = TimeObject.clone(timeVectorN1.get(i));
											while(to1.getParent() != null){
												to1 = to1.getParent();
											}
											TimeObject to2 = TimeObject.clone(timeVectorN2.get(i));
											while(to2.getParent() != null){
												to2 = to2.getParent();
											}
											if(to1.isParallel(to2)){
												parallel = true;
											}
										}
										if(!parallel){
											return false;
										}
									}
								}
							}

							
							// //
							
							List<TimeObject> timeObjects = new ArrayList<TimeObject>();
							List<List<TimeObject>> timeVectors = new ArrayList<List<TimeObject>>();
							for (SequenceFlow sequenceFlow : incoming) {
								FlowNode node = sequenceFlow.getSourceRef();
								if (flowNodeTimeObject.containsKey(node)) {
									TimeObject timeObject = flowNodeTimeObject.get(node);
									timeObjects.add(timeObject);
								}
								if (flowNodeTimeVectors.containsKey(node)){
									List<TimeObject> timeVector = flowNodeTimeVectors.get(node);
									timeVectors.add(timeVector);
								}
							}

							TimeObject newTime = TimeObject.supremum(timeObjects);
							List<TimeObject> newTimeVector = new ArrayList<TimeObject>();
							int huidig = processes.indexOf(pointer.eContainer());
							for(int i=0; i<processes.size(); i++){
								if(i == huidig){
									newTimeVector.add(i, newTime);
								}else{
									// grootste TimeObject van alle inkomende verbindingen hun TimeVector
									TimeObject greatestTimeObject = new TimeObject();
									for(List<TimeObject> vector : timeVectors){
										TimeObject to = TimeObject.clone(vector.get(i));
										while(to.getParent() != null){
											to = to.getParent();
										}
										/*if(greatestTimeObject.lessOrEqualThan(to)){
											greatestTimeObject = to;
										}*/
										TimeObject.sup(greatestTimeObject, to);
									}
									newTimeVector.add(i, greatestTimeObject);
									
								}
							}
							
							// //

							// set timeObject on gateway (prevents checking
							// gateway twice)
							TimeObject to = TimeObject.clone(newTime);
							List<TimeObject> tv = TimeObject.clone(newTimeVector);
							flowNodeTimeObject.put(pointer, to);
							flowNodeTimeVectors.put(pointer, tv);
							printTimeObject(pointer);
							printTimeVector(pointer);

							// move pointer
							FlowNode pointer1 = pointer.getOutgoing().get(0).getTargetRef();
							// set pointer
							flowNodeTimeObject.put(pointer1, to);
							flowNodeTimeVectors.put(pointer1, tv);
							printTimeObject(pointer1);
							printTimeVector(pointer1);

							// increment if not startevent or gateway
							if (!(pointer1 instanceof StartEvent
									|| pointer1 instanceof EndEvent
									|| pointer1 instanceof Gateway
									|| isSending(pointer1) || isReceiving(pointer1))) {

								// // increment if not startevent or gateway
								to.increment();
								tv.get(processes.indexOf(pointer1.eContainer())).increment();
								
								printTimeObject(pointer1);
								printTimeVector(pointer1);

							}

							updatedPointers2.add(pointer1);
						} else {
							// keep waiting
							updatedPointers2.add(pointer);
						}
					}

				} else if (isDiverging(pointer)) {
					// fork
					gateways.get(pointer.eContainer()).push(pointer);
					
					// //
					TimeObject timeObject = flowNodeTimeObject.get(pointer);
					List<TimeObject> timeVector = flowNodeTimeVectors.get(pointer);

					List<SequenceFlow> outgoing = pointer.getOutgoing();
					List<TimeObject> tos = new ArrayList<TimeObject>();
					for (int i = 0; i < outgoing.size(); i++){
						TimeObject to = timeObject.addTimeObject();
						tos.add(to);
					}
					TimeObject newTimeObject = TimeObject.clone(tos.get(0).getParent());
					List<TimeObject> newTimeVector = TimeObject.clone(timeVector);
					flowNodeTimeObject.put(pointer, newTimeObject);
					newTimeVector.set(processes.indexOf(pointer.eContainer()), newTimeObject);
					flowNodeTimeVectors.put(pointer, newTimeVector);
					printTimeObject(pointer);
					printTimeVector(pointer);
					for (int i = 0; i < outgoing.size(); i++) {
						// hold current time object
						TimeObject to = TimeObject.clone(tos.get(i));
						// move pointer
						FlowNode pointer1 = outgoing.get(i).getTargetRef();

						// set pointer (if not converging gateway)
						if (!isConverging(pointer1)) {
							TimeObject toc = TimeObject.clone(to);
							List<TimeObject> tv = TimeObject.clone(newTimeVector);
							TimeObject tocc = TimeObject.clone(toc);
							tv.set(processes.indexOf(pointer.eContainer()), tocc);
							flowNodeTimeObject.put(pointer1, toc);
							flowNodeTimeVectors.put(pointer1, tv);
							printTimeObject(pointer1);
							printTimeVector(pointer1);
							// increment if not startevent or gateway
							if (!(pointer1 instanceof StartEvent
									|| pointer1 instanceof EndEvent
									|| pointer1 instanceof Gateway
									|| isSending(pointer1) || isReceiving(pointer1))) {

								// // increment if not startevent or gateway
								toc.increment();
								tocc.increment();
								printTimeObject(pointer1);
								printTimeVector(pointer1);

							}
						}

						updatedPointers2.add(pointer1);

					}
					// //

				}
			}
			pointers = updatedPointers2;

		}

		// saveCollaboration(original, collaboration);
		// printTimeVectors();

		return true;
	}

	private static void getLastNodes(FlowNode node, List<FlowNode> nodes) {
		if(isConverging(node)){
			List<SequenceFlow> incoming = node.getIncoming();
			for(SequenceFlow sf : incoming){
				FlowNode node1 = sf.getSourceRef();
				getLastNodes(node1, nodes);
			}
		}else{
			nodes.add(node);
		}
	}

	private static void getFirstNodes(FlowNode firstGateway,
			List<FlowNode> firstNodes) {
		if(isDiverging(firstGateway)){
			List<SequenceFlow> outgoing = firstGateway.getOutgoing();
			for(SequenceFlow sf : outgoing){
				FlowNode node = sf.getTargetRef();
				getFirstNodes(node, firstNodes);
			}
		}else{
			firstNodes.add(firstGateway);
		}
	}

	private static void printTimeVectors() {
		// TODO Auto-generated method stub
		Iterator it = flowNodeTimeVectors.entrySet().iterator();
	    while (it.hasNext()) {
	    	Map.Entry<FlowNode, List<TimeObject>> pair = (Entry<FlowNode, List<TimeObject>>)it.next();
	        printTimeVector(pair.getKey());
	        it.remove(); // avoids a ConcurrentModificationException
	    }
	}

	private static void printTimeVector(FlowNode pointer) {
		// change name
		List<TimeObject> timeVector = flowNodeTimeVectors.get(pointer);
		String output = pointer.getName();
		output += "\t";
		for(TimeObject timeObject : timeVector){
			while (timeObject.getParent() != null) {
				timeObject = timeObject.getParent();
			}
			output += timeObject.toString();
		}
		System.out.println(output);
	}

	private static void printTimeObject(FlowNode pointer) {
		// change name
		/*TimeObject timeObject = flowNodeTimeObject.get(pointer);
		while (timeObject.getParent() != null) {
			timeObject = timeObject.getParent();
		}
		//pointer.setName(timeObject.toString());
		System.out.println(pointer.getName() + "\t" + timeObject);*/
		// //
	}

	private static void saveCollaboration(Collaboration original,
			Collaboration collaboration) {
		original.getParticipants()
				.get(0)
				.setProcessRef(
						collaboration.getParticipants().get(0).getProcessRef());

		Resource.Factory.Registry reg = Resource.Factory.Registry.INSTANCE;
		Map<String, Object> m = reg.getExtensionToFactoryMap();
		m.put("key", new XMIResourceFactoryImpl());
		ResourceSet resSet = new ResourceSetImpl();
		Resource resource = resSet.createResource(URI
				.createFileURI("C:\\Users\\janseeuw\\Documents\\"
						+ original.hashCode() + ".bpmn"));
		resource.getContents().add(original.eContainer().eContainer());
		try {
			resource.save(Collections.EMPTY_MAP);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static boolean isSending(FlowNode node) {
		return sending.containsKey(node);
	}

	private static boolean isReceiving(FlowNode node) {
		return receiving.containsKey(node);
	}

	private static boolean isConverging(FlowNode node) {
		if (node instanceof Gateway) {
			Gateway gateway = (Gateway) node;
			return gateway instanceof ParallelGateway
					&& gateway.getGatewayDirection() == GatewayDirection.CONVERGING;
		} else {
			return false;
		}
	}

	private static boolean isDiverging(FlowNode node) {
		if (node instanceof Gateway) {
			Gateway gateway = (Gateway) node;
			return gateway instanceof ParallelGateway
					&& gateway.getGatewayDirection() == GatewayDirection.DIVERGING;
		} else {
			return false;
		}
	}

	private static boolean hasNext(FlowNode node) {
		List<SequenceFlow> taskOutgoing = node.getOutgoing();
		return taskOutgoing.size() != 0;
	}

	private static FlowNode moveNext(FlowNode node) {
		List<SequenceFlow> taskOutgoing = node.getOutgoing();
		if (taskOutgoing.size() != 0) {
			// follow (first) sequenceflow
			node = taskOutgoing.get(0).getTargetRef();
		} else {
			// op het einde
			node = null;
		}
		return node;
	}

	private static boolean validateMessageFlow(FlowNode sendingNode,
			FlowNode receivingNode) {
		
		List<TimeObject> sendingNodeTimeVector = flowNodeTimeVectors.get(sendingNode);
		List<TimeObject> receivingNodeTimeVector = flowNodeTimeVectors.get(receivingNode);
		
		Process receivingProcess = (Process) receivingNode.eContainer();
		int index = processes.indexOf(receivingProcess); 

		Process sendingProcess = (Process) sendingNode.eContainer();
		
		// clone omdat we aanpassingen gaan doen
		TimeObject sendingNodeTimeObject = TimeObject.clone(sendingNodeTimeVector.get(index));
		TimeObject receivingNodeTimeObject = TimeObject.clone(receivingNodeTimeVector.get(index));

		// op zelfde niveau brengen
		if(sendingNodeTimeObject.getDepth() < receivingNodeTimeObject.getDepth()){
			while(sendingNodeTimeObject.getDepth() != receivingNodeTimeObject.getDepth()){
				receivingNodeTimeObject = TimeObject.supremum(receivingNodeTimeObject.getParent().getTimeObjects());
			}
		}else if(sendingNodeTimeObject.getDepth() > receivingNodeTimeObject.getDepth()){
			while(sendingNodeTimeObject.getDepth() != receivingNodeTimeObject.getDepth()){
				sendingNodeTimeObject = TimeObject.supremum(sendingNodeTimeObject.getParent().getTimeObjects());
			}
		}		
		
		return sendingNodeTimeObject.getI() == receivingNodeTimeObject.getI();
	}

}
