package be.ugent.intec.ibcn.dbase.tooling.editor;

import org.eclipse.bpmn2.Collaboration;
import org.eclipse.bpmn2.Process;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.validation.AbstractModelConstraint;
import org.eclipse.emf.validation.IValidationContext;

public class constraint extends AbstractModelConstraint {

	@Override
	public IStatus validate(IValidationContext ctx) {
		EObject object = ctx.getTarget();
		
		/*if (object instanceof Process) {
			return new ProcessValidator(ctx).validate((Process)object);
		}*/
		if(object instanceof Collaboration){
			return new CollaborationValidator(ctx).validate((Collaboration)object);
		}

		return ctx.createSuccessStatus();
	}

}