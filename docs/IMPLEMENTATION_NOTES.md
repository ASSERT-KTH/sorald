## Implementation notes

Both processors for code smells and one bug (Resources should be closed) are written in a different way from the others, using the plugin Sonarjava for localising bugs. The processors and their implementations have not been maintained and your milage may vary.
The remaining processors leverage the capabilities of Spoon for both localising and repairing violations.

#### This only applies to the 2 Code Smells and Bug S2095

Sonarjava had to be changed in order to return the appropriate issue information for offline-repair. The changes are at https://github.com/kth-tcs/sonar-java/pull/1 . Now I(Ashutosh) don't think that doing offline-repair is a good idea and we should stick to using the web api. I don't know of a way to offline detect issues which have their components in more than one file.