public class Main {

    /**
     *
     * @param args string array. Give either 0, 1 or 2 arguments. first argument is sonarqube rule-number which you can get from https://rules.sonarsource.com/java/type/Bug
     *             second argument is the projectKey for the sonarqube analysis of source files. for  example "fr.inria.gforge.spoon:spoon-core"
     */
    public static void main(String[] args) throws Exception
    {

        String projectKey="fr.inria.gforge.spoon:spoon-core";
        int rulenumber = 2116;
        if(args.length>0)
        {
            rulenumber = Integer.parseInt(args[0]);

            if(args.length==1)
            {
                projectKey = "fr.inria.gforge.spoon:spoon-core";
                System.out.println("One argument given. Applying " + TestHelp.getProcessor(rulenumber).getName() + " on " + projectKey);
            }
            else if(args.length==2)
            {
                projectKey = args[1];
                System.out.println("Two argument given. Applying "+TestHelp.getProcessor(rulenumber).getName()+ " on "+projectKey);
            }
            else
            {
                throw new IllegalArgumentException("Enter less than three arguments");
            }
        }
        else //no arguments given
        {
            System.out.println("No arguments given. Using "+ TestHelp.getProcessor(rulenumber).getName()+ " by default on "+projectKey);
        }
        TestHelp.normalRepair("./source/act/",projectKey,rulenumber);
        System.out.println("done");
	}
}
/*
        rule.putIfAbsent(1854, DeadStoreProcessor.class);
        rule.putIfAbsent(1948, SerializableFieldProcessor.class);
        rule.putIfAbsent(2055, NonSerializableSuperClassProcessor.class);
        rule.putIfAbsent(2095, ResourceCloseProcessor.class);
        rule.putIfAbsent(2259, NullDereferenceProcessor.class);

 */
