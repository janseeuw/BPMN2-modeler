package be.ugent.intec.ibcn.dbase.tooling;

import org.eclipse.bpmn2.FlowNode;

public class MonitoringPoint {
	
    private FlowNode flowNode;
    private Position position;
    
	public MonitoringPoint(FlowNode flowNode) {
		this.flowNode = flowNode;
	}
	
	public MonitoringPoint(FlowNode flowNode, Position position){
		this.flowNode = flowNode;
		this.setPosition(position);
	}

	public FlowNode getFlowNode() {
		return flowNode;
	}

	public void setFlowNode(FlowNode flowNode) {
		this.flowNode = flowNode;
	}

	public Position getPosition() {
		return position;
	}

	public void setPosition(Position position) {
		this.position = position;
	}

}
