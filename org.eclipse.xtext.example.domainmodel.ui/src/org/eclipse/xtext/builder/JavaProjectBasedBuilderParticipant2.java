/*******************************************************************************
 * Copyright (c) 2011 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtext.builder;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.builder.JavaProjectBasedBuilderParticipant;
import org.eclipse.xtext.generator.IFileSystemAccess;
import org.eclipse.xtext.generator.IGenerator2;
import org.eclipse.xtext.resource.IContainer;
import org.eclipse.xtext.resource.IResourceDescription;
import org.eclipse.xtext.resource.IResourceDescription.Delta;
import org.eclipse.xtext.resource.IResourceDescriptions;
import org.eclipse.xtext.resource.impl.ResourceDescriptionsProvider;

import com.google.inject.Inject;

/**
 * @author thoms - Initial contribution and API
 */
public class JavaProjectBasedBuilderParticipant2 extends JavaProjectBasedBuilderParticipant {
	@Inject
	private ResourceDescriptionsProvider resourceDescriptionsProvider;
	
	@Inject
	private IContainer.Manager containerManager;
	
	@Inject (optional=true)
	private IGenerator2 generator;
	
	protected ThreadLocal<Boolean> buildSemaphor = new ThreadLocal<Boolean>();
	
	@Override
	public void build(IBuildContext context, IProgressMonitor monitor) throws CoreException {
		buildSemaphor.set(false);
		super.build(context, monitor);
	}
	
	@Override
	protected void handleChangedContents(Delta delta, IBuildContext context, IFileSystemAccess fileSystemAccess) {
		super.handleChangedContents(delta, context, fileSystemAccess);
		if (!buildSemaphor.get() && generator != null) {
			invokeGenerator(delta, context, fileSystemAccess);
		}
	}
	
	@Override
	protected void handleDeletion(Delta delta, IBuildContext context, IFileSystemAccess fileSystemAccess) {
		super.handleDeletion(delta, context, fileSystemAccess);
		if (!buildSemaphor.get() && generator != null) {
			invokeGenerator(delta, context, fileSystemAccess);
		}
	}
	
	private void invokeGenerator (Delta delta, IBuildContext context, IFileSystemAccess fileSystemAccess) {
		buildSemaphor.set(true);
		Resource resource = context.getResourceSet().getResource(delta.getUri(), true);
		if (shouldGenerate(resource, context)) {
			IResourceDescriptions index = resourceDescriptionsProvider.createResourceDescriptions();
			IResourceDescription resDesc = index.getResourceDescription(resource.getURI());
			List<IContainer> visibleContainers = containerManager.getVisibleContainers(resDesc, index);
			for (IContainer c : visibleContainers) {
				for (IResourceDescription rd : c.getResourceDescriptions()) {
					context.getResourceSet().getResource(rd.getURI(), true);
				}
			}
			
			generator.doGenerate(context.getResourceSet(), fileSystemAccess);
		}
	}
}
