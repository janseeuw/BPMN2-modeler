package be.ugent.intec.ibcn.dbase.tooling;

import java.util.List;

import org.eclipse.bpmn2.Bpmn2Factory;
import org.eclipse.bpmn2.FlowElement;
import org.eclipse.bpmn2.FlowNode;
import org.eclipse.bpmn2.IntermediateThrowEvent;
import org.eclipse.bpmn2.Monitoring;
import org.eclipse.bpmn2.Process;
import org.eclipse.bpmn2.SequenceFlow;
import org.eclipse.bpmn2.Task;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.impl.EAttributeImpl;
import org.eclipse.emf.ecore.impl.EStructuralFeatureImpl.SimpleFeatureMapEntry;
import org.eclipse.emf.ecore.util.ExtendedMetaData;
import org.eclipse.emf.ecore.util.FeatureMap;
import org.eclipse.emf.ecore.util.FeatureMap.Entry;
import org.kie.api.io.ResourceType;
import org.kie.internal.KnowledgeBase;
import org.kie.internal.builder.KnowledgeBuilder;
import org.kie.internal.builder.KnowledgeBuilderFactory;
import org.kie.internal.io.ResourceFactory;
import org.kie.internal.runtime.StatefulKnowledgeSession;

import be.ugent.intec.ibcn.dbase.tooling.test.DroolsTest.Message;

public class MonitoringPointAdder {
	
	public static void start(Process process){
		try {
        	KnowledgeBase kbase = readKnowledgeBase();
            StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();
           
            ksession.insert(process); 
             
            ksession.fireAllRules();

        } catch (Throwable t) {
            t.printStackTrace();
        }
	}
	
	private static KnowledgeBase readKnowledgeBase() {
    	KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();

        kbuilder.add(ResourceFactory.newClassPathResource("Monitoring.dsl"), ResourceType.DSL);
        kbuilder.add(ResourceFactory.newClassPathResource("Monitoring.drl"), ResourceType.DRL);
        kbuilder.add(ResourceFactory.newClassPathResource("Monitoring.dslr"), ResourceType.DSLR);

    	
        if (kbuilder.hasErrors()) {
            throw new RuntimeException(kbuilder.getErrors()
            .toString());
        }
        KnowledgeBase kbase = kbuilder.newKnowledgeBase();
        kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
        return kbase;

	}
	
	public static void addMonitoringPoint(FlowNode flowNode, Position position) {
		switch (position) {
		case BEFORE:
			addMonitoringPointBeforeTask(flowNode);
			break;
		case AFTER:
			addMonitoringPointAfterTask(flowNode);
			break;
		default:
			Monitoring monitoring = Bpmn2Factory.eINSTANCE.createMonitoring();
			flowNode.setMonitoring(monitoring);
			break;
		}
	}
	
	private static void addMonitoringPointBeforeTask(FlowNode flowNode){
		Process process = (Process) flowNode.eContainer();

		IntermediateThrowEvent monitoringPoint = createMonitoringPoint();
		monitoringPoint.setId(flowNode.getId() + "_mpa");
		
		SequenceFlow[] taskIncoming = (SequenceFlow[]) flowNode.getIncoming().toArray();
		for(SequenceFlow sequenceFlow : taskIncoming){
			sequenceFlow.setTargetRef(monitoringPoint);
		}
	
		process.getFlowElements().add(monitoringPoint);
		
		SequenceFlow sequenceFlow = Bpmn2Factory.eINSTANCE.createSequenceFlow();
		sequenceFlow.setId(flowNode.getId() + "_mpa_sf");
		process.getFlowElements().add(sequenceFlow);
		
		sequenceFlow.setSourceRef(monitoringPoint);
		sequenceFlow.setTargetRef(flowNode);
	
	}
	
	private static void addMonitoringPointAfterTask(FlowNode flowNode){
		Process process = (Process) flowNode.eContainer();
		
		IntermediateThrowEvent monitoringPoint = createMonitoringPoint();
		monitoringPoint.setId(flowNode.getId() + "_mpb");
		
		SequenceFlow[] taskOutgoing = (SequenceFlow[]) flowNode.getOutgoing().toArray();
		for(SequenceFlow sequenceFlow : taskOutgoing){
			sequenceFlow.setSourceRef(monitoringPoint);
		}
	
		process.getFlowElements().add(monitoringPoint);
		SequenceFlow sequenceFlow = Bpmn2Factory.eINSTANCE.createSequenceFlow();
		sequenceFlow.setId(flowNode.getId() + "_mpb_sf");
		process.getFlowElements().add(sequenceFlow);
		
		sequenceFlow.setSourceRef(flowNode);
		sequenceFlow.setTargetRef(monitoringPoint);
	
	}
	
	private static IntermediateThrowEvent createMonitoringPoint(){
		IntermediateThrowEvent monitoringPoint = Bpmn2Factory.eINSTANCE.createIntermediateThrowEvent();
		ExtendedMetaData metadata=ExtendedMetaData.INSTANCE;
		EAttributeImpl extensionAttribute=(EAttributeImpl)metadata.demandFeature("http://tooling.dbase.ibcn.intec.ugent.be/bpmn2","type",false,false);
		SimpleFeatureMapEntry extensionEntry=new SimpleFeatureMapEntry(extensionAttribute,"MonitoringPoint");
		monitoringPoint.getAnyAttribute().add(extensionEntry);
		
		return monitoringPoint;
	}
}
