package blah;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.List;
import java.util.Set;

public class TestAp extends AbstractProcessor {
	static {
		System.out.println("TestAp initalized !!! woo yeah");
	}
	
	{
		System.out.println("TestAp CONSTRUCTED !!! woo yeah");
	}
	
	@Override
	public Set<String> getSupportedAnnotationTypes() {
		return Set.of("*");
	}
	
	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latestSupported();
	}
	
	@Override
	public void init(ProcessingEnvironment env) {
		super.init(env);
		env.getMessager().printMessage(Diagnostic.Kind.OTHER, "HELLO FROM TestAp THIS IS MY NOTE");
	}
	
	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		ProcessingEnvironment env = processingEnv;
		
		env.getMessager().printMessage(Diagnostic.Kind.OTHER, "FOOOOoO");
		for(Element root : roundEnv.getRootElements()) {
			env.getMessager().printMessage(Diagnostic.Kind.OTHER, "got ROOT: " + root);
			for(AnnotationMirror ann : root.getAnnotationMirrors()) {
				env.getMessager().printMessage(Diagnostic.Kind.OTHER, "\\-> annotated with " + ann);
			}
		}
		
		return false;
	}
}
