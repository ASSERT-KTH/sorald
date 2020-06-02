## Contributing

Pull requests are very welcome by the Sorald team!

### Guidelines for all pull-requests

A PR will be accepted and merged when:

- It is minimal in the sense of doing a single thing (for example, an addition of a single new processor and a single bug fix).
- It has a clear explanation of its goal and what was changed.
- It passes in the continuous integration service being used in the project.

### Guidelines for new-processor pull-requests

For adding a new processor in Sorald, please, follow the instructions described below.

1) Find the name for the new processor

The first step is to find the name for the new processor.
To do so, you need to find the name that SonarSource uses for the rule that the new processor targets.
So let's suppose that the new processor targets the rule ["Math operands should be cast before assignment"](https://rules.sonarsource.com/java/type/Bug/RSPEC-2184), rule key 2184.
In [SonarSource for Java](https://github.com/SonarSource/sonar-java/tree/master/java-checks/src/main/java/org/sonar/java/checks), you will find that such a rule is implemented by the class `CastArithmeticOperandCheck`.
So the name of your new processor is `CastArithmeticOperandCheck` replacing "Check" by "Processor", resulting in `CastArithmeticOperandProcessor`.

2) Create the processor

Once you have the name for the new processor, you can create a class using that name in `src/main/java/sorald/processor`.
This new class must extend `SQRAbstractProcessor` and implement the methods `isToBeProcessed` and `process` (check out real examples of processors [here](/src/main/java/sorald/processor)).

3) Create a test class for the processor

Your processor is done, so now it's time to test it.
For that, you should first to create a test class, with the same name as your processor plus the word "Test", and place it in [here](/src/test/java/sorald/processor).
See in this same folder how the tests usually are.
Then, you should create a java file in the [testing resources](/src/test/resources), which is the file you will use in your test class.
Such a resource should contain at least the examples that SonarSource provides, because your processor should at least work on them. 
You can find the examples provided by SonarSource in its [rules' webpage](https://rules.sonarsource.com/java) and in its own [testing resources](https://github.com/SonarSource/sonar-java/tree/master/java-checks-test-sources/src/main/java/checks) (note that different testing cases might exist in both locations, thus both ones should be checked).

4) Update documentation

Your processor is done, it's passing the minimum tests, so now you can update the documentation.
In the `HANDLED_RULES.md` file, you should add the new handled rule in the table of contents, and then a summary of its processor with an example.
You should do it following the same way as the existing handled rules are documented (note that the table of contents and summaries are alphabetically ordered).

5) Open a PR

The processor, the tests, and the documentation being ready, your contribution is also ready to be used in a PR.
Open a PR, with the title "Add <PROCESSOR_NAME> (SonarSource rule <RULE_KEY_NUMBER>)".
For the description of the PR, use the following template:  

This PR adds a processor for the rule [<RULE_DESCRIPTION, which should be the title of the rule in the https://rules.sonarsource.com/java webpage>](LINK TO THE RULE IN THE https://rules.sonarsource.com/java WEBPAGE).

Transformation:

```diff
<Here it goes the diff between your resource file (the one used in the test) and the Sorald generated output for it>
```

And then you can submit your PR!
