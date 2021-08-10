import java.io.File;
import java.io.FileNotFoundException;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import spoon.Launcher;
import spoon.compiler.Environment;
import spoon.compiler.SpoonResource;
import spoon.compiler.SpoonResourceHelper;
import spoon.reflect.CtModel;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.declaration.CtPackage;
import spoon.reflect.declaration.CtType;

public class GetKeyAndDescription {

    public static void main(String[] args) throws FileNotFoundException {
        CtPackage processorPackage = getProcessorPackage(new File(args[0]));
        Set<CtType<?>> allProcessors = processorPackage.getTypes();
        JsonArray rules = new JsonArray();
        allProcessors.stream().forEach(processor -> {
            // Skips SoraldAbstractProcessor
            if (processor.isAbstract()) return;

            JsonObject rule = new JsonObject();

            // Remove 'S' prefix from rule key
            CtLiteral<String> ruleKey = processor.getAnnotations().stream()
                    .filter(annotation -> annotation.getType().getSimpleName().equals("ProcessorAnnotation"))
                    .findFirst().get().getValue("key");
            Integer ruleKeyID = Integer.parseInt(ruleKey.getValue().substring(1));

            rule.add("rule_key", new JsonPrimitive(ruleKeyID));
            rule.add("repair_description", new JsonPrimitive(processor.getDocComment()));

            rules.add(rule);
        });
        System.out.print(rules);
    }

    private static CtPackage getProcessorPackage(File file) throws FileNotFoundException {
        final SpoonResource resource = SpoonResourceHelper.createResource(file);
        final Launcher launcher = new Launcher();
        Environment env = launcher.getEnvironment();
        launcher.addInputResource(resource);
        CtModel model = launcher.buildModel();
        return model.getRootPackage().getPackage("sorald").getPackage("processor");
    }
}
