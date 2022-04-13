package sorald.qodana;

import com.contrastsecurity.sarif.Result;
import com.google.auto.service.AutoService;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import sorald.rule.Rule;
import sorald.rule.RuleViolation;
import sorald.rule.StaticAnalyzer;
// TODO: why does qodana need a minimal maven build???

@AutoService(StaticAnalyzer.class)
public class Qodana implements StaticAnalyzer {

    @Override
    public Collection<RuleViolation> findViolations(
            File projectRoot, List<File> files, List<Rule> rule, List<String> classpath) {
        QodanaRunner.Builder builder = new QodanaRunner.Builder();
        List<Result> results = builder.build().runQodana(projectRoot.toPath());
        results.removeIf(v -> v.getLocations().isEmpty());
        // TODO: remove result with wrong locations and rules.
        List<RuleViolation> violations = new ArrayList<>();
        for (Result result : results) {
            RuleViolation violation = new QodanaRuleViolation(result, projectRoot);
            violations.add(violation);
        }
        return violations;
    }
}
