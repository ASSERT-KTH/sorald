## Contributing

Pull requests are very welcome by the Sorald team!

### A note on using an IDE to develop Sorald

Due to a somewhat unconventional annotation processor setup that includes a
precompile step to compile the processor, IDEs sometimes have trouble building
Sorald. There are currently two workarounds for this.

1. If your IDE supports it, you may delegate building of the project to Maven
2. Simply build with Maven from the command line
    - `mvn test-compile`
    - You can then run the tests with the IDE as per usual

In addition, you must ensure that `target/generated-sources` is marked as a
source root within your IDE. Otherwise, it may complain about the `Processors`
class not existing. The IDE should pick this up from `pom.xml`, but it has
happened that it doesn't do so correctly.

### Guidelines for all pull-requests

You may open a PR at any time when working on something. Prefix the title of
the PR with `wip:` if it is not ready to be merged. A PR will be accepted and
merged when:

- The title of the PR is prefixed with `<CATEGORY>:`, where `<CATEGORY>` is the
  most applicable of:
    - `feat`: A change to user-facing features
    - `refactor`: A refactoring
    - `fix`: A bugfix
    - `chore`: A change to the CI setup or other supporting files (e.g. `pom.xml`)
    - `test`: A change to the test suite
    - `doc`: A change to the documentation
    - Having trouble picking a category? Open your PR anyway and a maintainer will
    help you!
- It is minimal in the sense of doing a single thing (for example, an addition of a single new processor and a single bug fix).
- It has a clear explanation of its goal and what was changed.
- It passes in the continuous integration service being used in the project.
- If the PR relates to an issue, the first line in the body says `Fix
  #<ISSUE_NUMBER>` if it resolves the issue, or just `#<ISSUE_NUMBER>` if it is
  related but does not resolve the issue.
    - See the [PR that introduced this very line](https://github.com/SpoonLabs/sorald/pull/239) for an example
    - Contributions such as touch-ups to documentation, new test cases and pure
      refactorings do not need to be related to an issue

> **Note:** _Change_ in this context means any of _addition_, _removal_ or
> _modification_.

### Guidelines for maintainers

When merging a PR, maintainers should take care to:

* Squash the merge
* Ensure that the squash message starts with the `<CATEGORY>:` prefix
* Ensure that the squash message ends with `(#<PR_NUMBER>)`
* Ensure that any related issue is properly referenced in the PR body

### Guidelines for new-processor pull-requests

For adding a new processor in Sorald, please, follow the instructions described below.

1) Find the name for the new processor

The first step is to find the name for the new processor.
To do so, you need to find the name that SonarSource uses for the rule that the new processor targets.
So let's suppose that the new processor targets the rule ["Math operands should be cast before assignment"](https://rules.sonarsource.com/java/type/Bug/RSPEC-2184), rule key 2184.
In [SonarSource for Java](https://github.com/SonarSource/sonar-java/tree/master/java-checks/src/main/java/org/sonar/java/checks), you will find that such a rule is implemented by the class `CastArithmeticOperandCheck`.
So the name of your new processor is `CastArithmeticOperandCheck` replacing "Check" by "Processor", resulting in `CastArithmeticOperandProcessor`.

2) Create the processor

Once you have the name for the new processor, you can create a class using that
name in `src/main/java/sorald/processor`.  This new class must extend
`SoraldAbstractProcessor` and implement the abstract methods.

> See the documentation for the abstract methods in
> [SoraldAbstractProcessor](src/main/java/sorald/processor/SoraldAbstractProcessor.java),
> and check out [the existing implementations
> here](/src/main/java/sorald/processor) for guidance.

When you have created your processor, you must also add the check class to one
of the four categories of check classes in the static code block in
[Checks.java](/src/main/java/sorald/sonar/Checks.java), if it is not already
present. To find out which category to add your check to, look it up on the
[SonarSource website](https://rules.sonarsource.com/java), the category is
listed just below the rule title.

3) Add at least one test file with expected output for your processor

Tests for the processors are automatically generated based on a set of test
files in
[src/test/resources/processor_test_files](/src/test/resources/processor_test_files).
To add test files for your new processor, you must first add a new subdirectory
called `<RULE_KEY>_<RULE_NAME>`, were you substitute `<RULE_KEY>` for the key
of the rule your processor is related to, and `<RULE_NAME>` for the name of the
processor without to `Processor` suffix. Then, you put at least one Java file
with at least one violation of the rule you're working on into the new
directory. For each line in the file that violates the considered rule, put an
inline comment saying `Noncompliant` at the end of the line like so:

```java
"a" == "b" // Noncompliant
```

For each noncompliant input file `SomeFile.java`, you should also add an
expected output file called `SomeFile.java.expected` in the same directory.
Each such "expected" file should contain the expected output from processing
its corresponding noncompliant file with your new processor. For a concrete
excample, see the noncompliant file
[ArrayHashCodeAndToString.java](/src/test/resources/processor_test_files/2116_ArrayHashCodeAndToString/ArrayHashCodeAndToString.java)
and its expected output
[ArrayHashCodeAndToString.java.expected](/src/test/resources/processor_test_files/2116_ArrayHashCodeAndToString/ArrayHashCodeAndToString.java.expected).

The precise names of the Java source files do not matter, as long as the
noncompliant files are suffixed with `.java` and each expected file
is suffixed with `.expected` and has a matching noncompliant file.
For more examples, see
[src/test/resources/processor_test_files](/src/test/resources/processor_test_files).

> See
> [src/test/java/sorald/processor/ProcessorTest.java](/src/test/java/sorald/processor/ProcessorTest.java)
> if you are curious as to how tests are generated from the test files.

4) Ensure compliance with code style

We have an automatically enforced code style in sorald, and your code will not
pass CI if it does not adhere to it. To ensure that your code does adhere to it,
run `mvn spotless:apply`, and then commit any changes the formatter makes.

5) Update documentation

Your processor is done, it's passing the minimum tests, so now you can update the documentation.
In the `HANDLED_RULES.md` file, you should add the new handled rule in the table of contents, and then a summary of its processor with an example.
You should do it following the same way as the existing handled rules are documented (note that the table of contents and summaries are alphabetically ordered).

6) Open a PR

The processor, the tests, and the documentation being ready, your contribution is also ready to be used in a PR.
Open a PR, with the title "feat: Add <PROCESSOR_NAME> (SonarSource rule <RULE_KEY_NUMBER>)".
For the description of the PR, use the following template:  

This PR adds a processor for the rule [<RULE_DESCRIPTION, which should be the title of the rule in the https://rules.sonarsource.com/java webpage>](LINK TO THE RULE IN THE https://rules.sonarsource.com/java WEBPAGE).

Transformation:

```diff
<Here it goes the diff between your resource file (the one used in the test) and the Sorald generated output for it>
```

And then you can submit your PR!
