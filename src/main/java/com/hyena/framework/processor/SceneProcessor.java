package com.hyena.framework.processor;

import com.google.auto.service.AutoService;
import com.hyena.framework.annotation.Scene;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.*;

/**
 * Created by yangzc on 17/11/23.
 */
@AutoService(Processor.class)
public class SceneProcessor extends AbstractProcessor {

	private static final String APPLICATION_ID = "applicationId";

	private Filer filer;
//	private Elements elementUtils;
	private Messager messager;

	private String applicationId = "com.hyena.ids";
	private final HashMap<String, String> mSceneMap = new HashMap<>();

	@Override
	public synchronized void init(ProcessingEnvironment processingEnvironment) {
		super.init(processingEnvironment);
		this.filer = processingEnvironment.getFiler();
//		this.elementUtils = processingEnvironment.getElementUtils();
		this.messager = processingEnvironment.getMessager();

		messager.printMessage(Diagnostic.Kind.NOTE, "init!!!");
		if (processingEnvironment.getOptions() != null) {
			Map<String, String> options = processingEnvironment.getOptions();
			String applicationId = options.get(APPLICATION_ID);
			if (applicationId != null && !"".equals(applicationId)) {
				this.applicationId = applicationId;
				messager.printMessage(Diagnostic.Kind.NOTE, "applicationId: " + applicationId);
			}
		}
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		if (roundEnv.getRootElements().isEmpty() || isIgnore(roundEnv)) {
			return true;
		}
		messager.printMessage(Diagnostic.Kind.NOTE, "process!!!");
		mSceneMap.clear();
		for (Element e : roundEnv.getElementsAnnotatedWith(Scene.class)) {
			Scene scene = e.getAnnotation(Scene.class);
			mSceneMap.put(scene.value(), e.toString());
		}
		TypeSpec.Builder javaClassBuilder = TypeSpec.classBuilder("SceneIds").addModifiers(Modifier.PUBLIC);
		List<String> methodLine = new ArrayList<>();
		if (!mSceneMap.isEmpty()) {
			//添加静态变量
			for (String sceneId : mSceneMap.keySet()) {
				String className = mSceneMap.get(sceneId);
				FieldSpec fieldSpec = FieldSpec.builder(String.class, sceneId)
						.addModifiers(Modifier.PUBLIC, Modifier.FINAL, Modifier.STATIC)
						.initializer("$S", sceneId).build();
				javaClassBuilder.addField(fieldSpec);
				methodLine.add("sceneMap.put(" + sceneId + ", \"" + className + "\")");
			}
		}

		//添加getSceneMaps方法
		MethodSpec.Builder methodGetSceneMapSpecBuild = MethodSpec.methodBuilder("getSceneMaps")
				.returns(HashMap.class)
				.addModifiers(Modifier.STATIC, Modifier.PUBLIC)
				.addStatement("$T sceneMap = new $T()", HashMap.class, HashMap.class);
		for (String s : methodLine) {
			methodGetSceneMapSpecBuild.addStatement(s);
		}
		methodGetSceneMapSpecBuild.addStatement("return sceneMap");
		javaClassBuilder.addMethod(methodGetSceneMapSpecBuild.build());
		TypeSpec javaClass = javaClassBuilder.build();
		JavaFile javaFile = JavaFile.builder(applicationId, javaClass).build();

		try {
			javaFile.writeTo(filer);
		} catch (IOException e) {
			messager.printMessage(Diagnostic.Kind.NOTE, e.getMessage());
		}
		return true;
	}

	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latestSupported();
	}

	@Override
	public Set<String> getSupportedAnnotationTypes() {
    	Set<String> types = new HashSet<String>();
		types.add(Scene.class.getCanonicalName());
    	return types;
	}

	@Override
	public Set<String> getSupportedOptions() {
		return Collections.singleton(APPLICATION_ID);
	}

	private boolean isIgnore(RoundEnvironment roundEnv) {
		Set<? extends Element> rootElements = roundEnv.getRootElements();
		if (rootElements.isEmpty())
			return true;
		for (Element element: rootElements) {
			String name = element.getSimpleName().toString();
			if ("SceneIds".equals(name))
				return true;
		}
		return false;
	}
}
