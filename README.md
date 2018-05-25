# sonarqube-repair

This is the the first draft of the repair system.

ParseAPI class makes a get request to sonarqube and parses them to generate a JSONArray.

Bug.java has description of objects of type bugs. For each instance of a bug, a Bug object will be created. It has fields like lineNumber and bugName.
It also contains a static method createSetOfBugs which returns a hashSet when given a JSONArray having many individual bugs.

NullDereferenceProcessor.java contains the actual processor for repairing instances where nullable objects have been dereferenced.
It has a constructor with a JSONArray as a parameter. It creates a SetOfBugs using createSetOfBugs method of the Bug class.
It is parsing over all CtExecutable references and checking their names and positions. This part is not complete yet.
Once it finds the bugs, it will repair them(to be implemented).
