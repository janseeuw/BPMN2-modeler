package be.ugent.intec.ibcn.dbase.tooling.editor;

import org.eclipse.bpmn2.modeler.core.IBpmn2RuntimeExtension;
import org.eclipse.bpmn2.modeler.core.LifecycleEvent;
import org.eclipse.bpmn2.modeler.core.utils.ModelUtil.Bpmn2DiagramType;
import org.eclipse.bpmn2.modeler.ui.DefaultBpmn2RuntimeExtension.RootElementParser;
import org.eclipse.bpmn2.modeler.ui.wizards.FileService;
import org.eclipse.ui.IEditorInput;
import org.xml.sax.InputSource;

public class Bpmn2RuntimeExtension implements IBpmn2RuntimeExtension {

	@Override
	public String getTargetNamespace(Bpmn2DiagramType arg0) {
		return "http://tooling.dbase.ibcn.intec.ugent.be/bpmn2";
	}

	// test namespaces of bpmn content.
	@Override
	public boolean isContentForRuntime(IEditorInput input) {
		InputSource source = new InputSource(
				FileService.getInputContents(input));
		RootElementParser parser = new RootElementParser(
				"http://tooling.dbase.ibcn.intec.ugent.be/bpmn2");
		parser.parse(source);
		return parser.getResult();
	}

	@Override
	public void notify(LifecycleEvent arg0) {
		// TODO Auto-generated method stub
	}

	public Bpmn2RuntimeExtension() {
		// TODO Auto-generated constructor stub
	}

}
