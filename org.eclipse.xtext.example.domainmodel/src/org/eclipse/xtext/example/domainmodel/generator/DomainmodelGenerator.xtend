/*******************************************************************************
 * Copyright (c) 2011 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtext.example.domainmodel.generator

import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.xtext.generator.IGenerator
import org.eclipse.xtext.generator.IFileSystemAccess
import org.eclipse.xtext.xbase.compiler.*
import org.eclipse.xtext.xbase.*
import org.eclipse.xtext.example.domainmodel.domainmodel.*
import org.eclipse.xtext.common.types.*
import java.util.*
import com.google.inject.Inject
import static extension org.eclipse.xtext.xtend2.lib.ResourceExtensions.*
import static extension org.eclipse.xtext.xtend2.lib.ResourceSetExtensions.*
import org.eclipse.xtext.resource.IContainer
import org.eclipse.emf.ecore.resource.ResourceSet
import org.eclipse.xtext.resource.IEObjectDescription
import org.eclipse.xtext.scoping.IScopeProvider
import org.eclipse.xtext.EcoreUtil2
import org.eclipse.emf.ecore.util.EcoreUtil
import org.eclipse.xtext.generator.IGenerator2

class DomainmodelGenerator implements IGenerator2 {
	
	@Inject extension GeneratorExtensions generatorExtensions
	
	@Inject DomainmodelCompiler domainmodelCompiler
	
	@Inject IScopeProvider scopeProvider
	
	override void doGenerate(Resource resource, IFileSystemAccess fsa) {
		for(entity: resource.allContentsIterable.filter(typeof(Entity))) {
			fsa.generateFile(entity.fileName, entity.compile)
		}
	}
	
	override void doGenerate(ResourceSet rs, IFileSystemAccess fsa) {
		val Iterable<Entity> entities = rs.resources.map(r|r.allContentsIterable.filter(typeof(Entity))).flatten
		
		fsa.generateFile("entities.txt", entities.compile)
	}
	
	def compile (Iterable<Entity> entities) '''
		«FOR e : entities SEPARATOR ", "»«e.name»«ENDFOR»
	'''
		
	def compile(Entity e) ''' 
		«val importManager = new ImportManager(true)»
		«/* first evaluate the body in order to collect the used types for the import section */
		val body = body(e, importManager)»
		«IF !(e.packageName.isNullOrEmpty)»
			package «e.packageName»;
			
		«ENDIF»
		«FOR i:importManager.imports»
			import «i»;
		«ENDFOR»
		
		«body»
	'''
	
	def body(Entity e, ImportManager importManager) '''
		public class «e.name» «e.superTypeClause(importManager)»{
			«FOR f:e.features»
				«feature(f, importManager)»
			«ENDFOR»
		}
	'''   
	
	def superTypeClause(Entity e, ImportManager importManager) {
		if(e.superType != null)
			(if (e.superType.isInterface) 
				'implements ' 
			else 
				'extends ') + e.superType.shortName(importManager) + " "
		else ""    
	}
	
	def dispatch feature(Property p, ImportManager importManager) '''
		private «p.type.shortName(importManager)» «p.name»;
			
		public «p.type.shortName(importManager)» get«p.name.toFirstUpper»() {
			return «p.name»;
		}
		
		public void set«p.name.toFirstUpper»(«p.type.shortName(importManager)» «p.name») {
			this.«p.name» = «p.name»;
		}
	'''

	def dispatch feature(Operation o, ImportManager importManager) '''
		public «o.type.shortName(importManager)» «o.name»(«o.parameterList(importManager)») {
			«domainmodelCompiler.compile(o, importManager)» 
		}
	'''
	
}
