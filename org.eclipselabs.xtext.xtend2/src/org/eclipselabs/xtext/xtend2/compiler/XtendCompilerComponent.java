package org.eclipselabs.xtext.xtend2.compiler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.emf.mwe.core.WorkflowContext;
import org.eclipse.emf.mwe.core.issues.Issues;
import org.eclipse.emf.mwe.core.lib.AbstractWorkflowComponent2;
import org.eclipse.emf.mwe.core.monitor.ProgressMonitor;
import org.eclipse.xtext.xtend2.Xtend2StandaloneSetup;
import org.eclipse.xtext.xtend2.compiler.Xtend2Compiler;
import org.eclipse.xtext.xtend2.resource.Xtend2Resource;
import org.eclipse.xtext.xtend2.xtend2.XtendFile;

import com.google.common.collect.Iterables;
import com.google.inject.Injector;

/**
 * MWE Workflow Component to compile Xtend2 files.
 * @author thoms
 *
 */
@SuppressWarnings("restriction")
public class XtendCompilerComponent extends AbstractWorkflowComponent2 {
	private static final Log LOG = LogFactory.getLog(XtendCompilerComponent.class);
	private String modelSlot;
	private File targetDirectory = new File("xtend-gen");
	/**
	 * Just for information purposes.
	 */
	private File baseDir;
	
	@Override
	protected void checkConfigurationInternal(Issues issues) {
		checkRequiredConfigProperty("modelSlot", modelSlot, issues);
	}

	@Override
	protected void invokeInternal(WorkflowContext ctx, ProgressMonitor monitor,
			Issues issues) {
		LOG.info("Invoking Xtend2 Compiler");
		LOG.debug("modelSlot: "+modelSlot);
		LOG.debug("targetDir: "+targetDirectory.getAbsolutePath());
		if (baseDir != null) {
			LOG.debug("baseDir: "+baseDir.getAbsolutePath());
		}
		
		Injector xtendInjector = new Xtend2StandaloneSetup().createInjectorAndDoEMFRegistration();
		
		if (!targetDirectory.exists()) {
			targetDirectory.mkdirs();
		}
		Object slotContents = ctx.get(modelSlot);
		if (slotContents == null) {
			throw new IllegalStateException("Slot '"+modelSlot+"' is empty");
		}
		if (! (slotContents instanceof List<?>)) {
			throw new IllegalStateException("Expected a List in slot '"+modelSlot+"', but actually found an instance of " + slotContents.getClass().getName());
		}
		Iterable<Xtend2Resource> xtendResources = Iterables.filter((List<?>) slotContents, Xtend2Resource.class);
		Xtend2Compiler compiler = xtendInjector.getInstance(Xtend2Compiler.class);

		int count = 0;
		int errors = 0;
		for (Xtend2Resource r : xtendResources) {
			String srcFile = r.getURI().toFileString();
			if (baseDir != null && srcFile.startsWith(baseDir.getAbsolutePath())) {
				srcFile = srcFile.substring(baseDir.getAbsolutePath().length()+1);
			}
			LOG.debug("compiling "+srcFile);
			XtendFile xtendFile = (XtendFile) r.getContents().get(0);
			File packageDir = new File(targetDirectory, xtendFile.getPackage().replace(".", "/"));
			if (!packageDir.exists()) {
				packageDir.mkdirs();
			}
			File targetFile = new File(packageDir, r.getURI().lastSegment().replace(".xtend", ".java"));
			try {
				compiler.compile(xtendFile, new FileWriter(targetFile));
				count++;
			} catch (IOException e) {
				LOG.warn("IOException occurred when writing target file: "+e.getMessage());
				errors++;
			} catch (RuntimeException e) {
				LOG.warn("Could not compile "+r.getURI().lastSegment()+": "+e.getClass().getSimpleName() + " " + e.getMessage());
				errors++;
			}
		}
		if (count==0 && errors==0) {
			LOG.warn("No Xtend files were compiled.");
		} else if (count==0 && errors>0) {
			LOG.error("All Xtend "+errors+" files could not be compiled.");
		} else if (count>0 && errors>0) {
			LOG.warn(String.format("Compiled %s Xtend files. %s files failed with compilation problems. Enable debug logging to see more details.", count, errors));
		} else {
			LOG.info(String.format("Compiled %s Xtend files without errors.", count));
		}
	}
	
	public void setModelSlot(String modelSlot) {
		this.modelSlot = modelSlot;
	}

	public void setTargetDir(String targetDir) {
		this.targetDirectory = new File(targetDir);
	}
	
	public void setBaseDir(String baseDir) {
		this.baseDir = new File(baseDir);
	}

}
