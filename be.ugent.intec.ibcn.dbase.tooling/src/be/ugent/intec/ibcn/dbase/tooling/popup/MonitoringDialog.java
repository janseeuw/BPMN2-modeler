package be.ugent.intec.ibcn.dbase.tooling.popup;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.bpmn2.Bpmn2Factory;
import org.eclipse.bpmn2.Definitions;
import org.eclipse.bpmn2.DocumentRoot;
import org.eclipse.bpmn2.FlowElement;
import org.eclipse.bpmn2.Monitoring;
import org.eclipse.bpmn2.Process;
import org.eclipse.bpmn2.SequenceFlow;
import org.eclipse.bpmn2.Task;
import org.eclipse.bpmn2.Import;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.command.BasicCommandStack;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.impl.AdapterImpl;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
import org.eclipse.emf.edit.ui.provider.AdapterFactoryContentProvider;
import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import be.ugent.intec.ibcn.dbase.tooling.MonitoringPointAdder;

public class MonitoringDialog extends Dialog {
	
	private static final int _ADDMONITORINGPOINT = 1001;
	
	private DocumentRoot documentRoot;
	private ComposedAdapterFactory composedAdapterFactory;

	private Resource resource;
	private AdapterFactoryLabelProvider adapterFactoryLabelProvider;
	private AdapterFactoryContentProvider adapterFactoryContentProvider;
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite root = (Composite) super.createDialogArea(parent);
		root.setLayout(new FillLayout(SWT.VERTICAL));
		TreeViewer treeViewer = new TreeViewer(root, SWT.SINGLE | SWT.H_SCROLL
				| SWT.V_SCROLL);
		initializeTreeviewer(treeViewer);
		root.layout();
		parent.pack();
				
		return parent;
	}

	protected void loadContent(IFile file) throws IOException {
		AdapterFactoryEditingDomain editingDomain = new AdapterFactoryEditingDomain(
				getAdapterFactory(), new BasicCommandStack());
		resource = editingDomain.createResource(file.getFullPath().toString());
		resource.load(null);
		EObject eObject = resource.getContents().get(0);
		setDocumentRoot((DocumentRoot) eObject);
	}
	
	public DocumentRoot getDocumentRoot() {
		return this.documentRoot;
	}

	public void setDocumentRoot(DocumentRoot documentRoot) {
		this.documentRoot = documentRoot;
	}


	protected void save() throws IOException {
		resource.save(null);
	}

	protected void addMonitoring() {
		EditingDomain editingDomain = AdapterFactoryEditingDomain
				.getEditingDomainFor(getDocumentRoot());
		Collection<?> children = editingDomain.getChildren(getDocumentRoot());
		Iterator<?> it = children.iterator();
		if (it.hasNext()) {
			Object type = it.next();
			Definitions definitions = (Definitions) type;

			Process process = (Process) definitions.getRootElements().get(0);
			
			MonitoringPointAdder.start(process);

		}
	}

	@Override
	protected void buttonPressed(int buttonId) {
		switch (buttonId) {
		case _ADDMONITORINGPOINT:
			addMonitoring();
			break;
		default:
			super.buttonPressed(buttonId);
		}
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, _ADDMONITORINGPOINT, "Add Monitoring", true);
		super.createButtonsForButtonBar(parent);
	}

	@Override
	protected void okPressed() {
		try {
			save();
		} catch (IOException e) {
			Status status = new Status(IStatus.ERROR,
					"be.ugent.intec.ibcn.dbase.tooling", 0, e.getMessage(),
					null);
			ErrorDialog.openError(this.getShell(), "Error on save",
					"Something went wrong on save", status);
		}
		super.okPressed();
	}

	protected void initializeTreeviewer(TreeViewer treeViewer) {
		adapterFactoryLabelProvider = new AdapterFactoryLabelProvider(
				getAdapterFactory());
		treeViewer.setLabelProvider(adapterFactoryLabelProvider);
		adapterFactoryContentProvider = new AdapterFactoryContentProvider(
				getAdapterFactory());
		treeViewer.setContentProvider(adapterFactoryContentProvider);
		treeViewer.setInput(getDocumentRoot());
	}

	@Override
	public boolean close() {
		adapterFactoryLabelProvider.dispose();
		adapterFactoryContentProvider.dispose();
		return super.close();
	}

	protected MonitoringDialog(Shell parentShell) {
		super(parentShell);
	}
	
	/**
	 * Return an ComposedAdapterFactory for all registered modesl
	 * 
	 * @return a ComposedAdapterFactory
	 */
	protected AdapterFactory getAdapterFactory() {
		if (composedAdapterFactory == null) {
			composedAdapterFactory = new ComposedAdapterFactory(
					ComposedAdapterFactory.Descriptor.Registry.INSTANCE);
		}
		return composedAdapterFactory;
	}
	
	@Override
	protected boolean isResizable()  {
	  return true;
	}

}
