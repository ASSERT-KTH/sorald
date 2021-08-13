## Contributing

Pull requests are very welcome by the Sorald team!

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

0) Familiarize yourself with the [`sorald.util` utility package](#reusable-utility-classes-and-methods)

This is to ensure that you do not recreate functionality that we already have a
solution for.

1) Find the name for the new processor

The first step is to find the name for the new processor.
To do so, you need to find the name that SonarSource uses for the rule that the new processor targets.
So let's suppose that the new processor targets the rule ["Math operands should be cast before assignment"](https://rules.sonarsource.com/java/type/Bug/RSPEC-2184), rule key 2184.
In [SonarSource for Java](https://github.com/SonarSource/sonar-java/tree/master/java-checks/src/main/java/org/sonar/java/checks), you will find that such a rule is implemented by the class `CastArithmeticOperandCheck`.
So the name of your new processor is `CastArithmeticOperandCheck` replacing "Check" by "Processor", resulting in `CastArithmeticOperandProcessor`.

2) Create the processor

Once you have the name for the new processor, you can create a class using that
name in `src/main/java/sorald/processor`.  This new class must extend
`SoraldAbstractProcessor` and implement the abstract methods, and be annotated
with `ProcessorAnnotation`.

> See the documentation for the abstract methods in
> [SoraldAbstractProcessor](src/main/java/sorald/processor/SoraldAbstractProcessor.java),
> and check out [the existing implementations
> here](/src/main/java/sorald/processor) for guidance.

As soon as you have created a skeleton for the processor, with the
`ProcessorAnnotation` properly filled in, you should update the `Processors`
class. You do this by running the main method of `sorald.CodeGenerator`, which
you can either do with your favorite IDE, or by running the following shell
commands in `bash`:

```bash
$ mvn clean compile dependency:build-classpath -Dmdep.outputFile=cp.txt
$ java -cp "$(cat cp.txt):./target/classes" sorald.CodeGenerator
$ mvn spotless:apply
```

After that, you can start developing your processor for real! Note that if you
update the annotations of your processor, you must update the `Processors`
class again as described above.

> **Complicated?:** The procedure for adding a processor is a little bit
> contrived. It used to be automatic through processor annotations, which
> worked excellently with Maven, but we experienced so many issues with IDE and
> editor integration that we eventually gave up on it.

3) Add at least one test file with expected output for your processor

See [Guidelines for testing processors](#guidelines-for-testing-processors) for
details.

4) Ensure compliance with code style

We have an automatically enforced code style in sorald, and your code will not
pass CI if it does not adhere to it. To ensure that your code does adhere to it,
run `mvn spotless:apply`, and then commit any changes the formatter makes.

5) Update documentation

Your processor is done, it's passing the minimum tests, so now you can update the documentation.
Write a summary of the processor supporting it with an example in <PROCESSOR_NAME>.md file and place it inside the
`sorald.processor` package. An example name of such file could be `CastArithmeticOperandProcessor.md` if your
processor's name is `CastArithmeticOperandProcessor`.

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

### Reusable utility classes and methods

In the [sorald.util](/src/main/java/sorald/util) package we collect utility classes
and methods for use throughout Sorald. It's a good idea to have a look through
it before writing a processor. For example, the
[Transformations](/src/main/java/sorald/util/Transformations.java) class contains
high-level transformations that may be of use to you.

### Guidelines for testing processors

Tests for the processors are automatically generated based on a set of test
files in
[src/test/resources/processor_test_files](/src/test/resources/processor_test_files).
To add test files for a processor, you must find or create a subdirectory called
`<RULE_KEY>_<RULE_NAME>`, were you substitute `<RULE_KEY>` for the key of the
rule your processor is related to, and `<RULE_NAME>` for the name of the
processor without to `Processor` suffix. In this subdirectory, you add _test
files_ and _expected files_ for the processor, which are described in more
detail below.

#### Test files

A _test file_ is a `.java` source file with at least one violation of the
considered rule. Sorald's test suite automatically converts each test file
into a test case for the processor. Each violation in a test file _must_ be
marked with an inline comment `// Noncompliant`, like so.

```java
"a" == "b" // Noncompliant
```

> Note the capital `N` in `Noncompliant`!

##### A test file is a test case

As each test file generates a test case, it is good practice to create one test
file per specific case you want to test, and document it with a header. To avoid
an explosion of test files, we typically create one test file for the general
operation of a processor, and then individual files for edge cases.

The name of the test file should ideally reflect what the test case is about,
but there are otherwise no particular naming conventions.

> See
> [src/test/java/sorald/processor/ProcessorTest.java](/src/test/java/sorald/processor/ProcessorTest.java)
> if you are curious as to how tests are generated from the test files.

##### Test files should be standalone compilable

In general, a test file must be _standalone compilable_ given only the standard
library. In other words, it must be syntactically correct, can't reference types
that are not part of the standard library, etc. You can easily test this by
simply running `javac` on your test file.

> **Important:** Note that this means that test files must abide by Java
> conventions of having the name of the primary type declaration in the class
> match the name of the file.

In some cases, the point of the test case is that the test file does not
compile, typically due types or packages that aren't available being referenced.
To signify this, the test file name (as well as the class name) should be
prefixed with `NOCOMPILE`, which will cause it to be ignored by tests that
require compilation.

> **Important:** Don't forget to also prefix the primary type's name with
> `NOCOMPILE`, such that the file name matches the type name.

If a test file should be ignored completely, prefix it with `IGNORE` instead.
This is useful for keeping test files that Sorald currently cannot handle.

#### Expected files

An _expected file_ is a `.java` source file that contains the expected output
after processing a test file, marked by having the suffix `.expected` tacked on
to the end. For example, given a test file `A.java` the corresponding expected
file would be called `A.java.expected`. After processing `A.java` with the
appropriate processor, the test suite compares the results to `A.java.expected`
with an AST comparison, meaning that most formatting differences are **not**
captured (but e.g. method order still matters).

When you add a test file, you should always add a corresponding expected file.
Otherwise, the test suite can verify that the violations are removed, but not
that the output is actually correct, making for weak tests.

For a concrete example of a test file and expected file pair, see
[ArrayHashCodeAndToString.java](/src/test/resources/processor_test_files/2116_ArrayHashCodeAndToString/ArrayHashCodeAndToString.java)
and its expected output
[ArrayHashCodeAndToString.java.expected](/src/test/resources/processor_test_files/2116_ArrayHashCodeAndToString/ArrayHashCodeAndToString.java.expected).
For more examples, see
[src/test/resources/processor_test_files](/src/test/resources/processor_test_files).

#### Exact match files

An _exact match file_ contains one or more code snippets that is expected to be
present after Sorald has processed a test file, and is marked by having the
suffix `.exact`. For example, given a test file `A.java`, the corresponding
exact match file is `A.java.exact`. Whereas the expected files are matched with
an AST comparison, the code snippets of exact match files are asserted to be
literal substrings of the output file contents, in order.

A simple exact match file contains only a single code snippet to be matched as a
substring. _Do keep in mind that indentation matters!_. For an example, see
[DeadVariableWithMultipleDeadStores.java.exact](src/test/resources/processor_test_files/1854_DeadStore/DeadVariableWithMultipleDeadStores.java.exact).

An exact match file can contain multiple code snippets, where each snippet
except for the last one is terminated with a line containing only `###`. See
[UpperClass.java.exact](src/test/resources/processor_test_files/2057_SerialVersionUidCheck/UpperClass.java.exact)
for an example.

> **Important:** Exact match files should be used sparingly, and should only
> capture formatting that's of absolute relevance to Sorald itself and its
> transformations. Overspecifying exact matches leads to unnecessary work down
> the line.
