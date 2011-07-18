package org.eclipselabs.xtext.xtend2.compiler;

import org.eclipse.emf.mwe.core.WorkflowContext;
import org.eclipse.emf.mwe.core.issues.Issues;
import org.eclipse.emf.mwe.core.lib.AbstractWorkflowComponent2;
import org.eclipse.emf.mwe.core.monitor.ProgressMonitor;

public class XtendCompilerComponent extends AbstractWorkflowComponent2 {
	private String modelSlot;
	private String targetDir;
	
	@Override
	protected void invokeInternal(WorkflowContext ctx, ProgressMonitor monitor,
			Issues issues) {
		// TODO Auto-generated method stub
		
	}

	public String getModelSlot() {
		return modelSlot;
	}

	public void setModelSlot(String modelSlot) {
		this.modelSlot = modelSlot;
	}

	public String getTargetDir() {
		return targetDir;
	}

	public void setTargetDir(String targetDir) {
		this.targetDir = targetDir;
	}

}
