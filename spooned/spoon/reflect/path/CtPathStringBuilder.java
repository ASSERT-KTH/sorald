package spoon.reflect.path;


public class CtPathStringBuilder {
    private final java.util.regex.Pattern pathPattern = java.util.regex.Pattern.compile("([/.#])([^/.#\\[]+)(\\[([^/.#]*)\\])?");

    private final java.util.regex.Pattern argumentPattern = java.util.regex.Pattern.compile("(\\w+)=([^=\\]]+)");

    private java.lang.Class load(java.lang.String name) throws spoon.reflect.path.CtPathException {
        try {
            return java.lang.Class.forName(name);
        } catch (java.lang.ClassNotFoundException ex) {
        }
        try {
            return java.lang.Class.forName(("spoon.reflect.declaration." + name));
        } catch (java.lang.ClassNotFoundException ex) {
        }
        try {
            return java.lang.Class.forName(("spoon.reflect.code." + name));
        } catch (java.lang.ClassNotFoundException ex) {
            throw new spoon.reflect.path.CtPathException(java.lang.String.format("Unable to locate element with type %s in Spoon model", name));
        }
    }

    public spoon.reflect.path.CtPath fromString(java.lang.String pathStr) throws spoon.reflect.path.CtPathException {
        java.util.regex.Matcher matcher = pathPattern.matcher(pathStr);
        spoon.reflect.path.impl.CtPathImpl path = new spoon.reflect.path.impl.CtPathImpl();
        while (matcher.find()) {
            java.lang.String kind = matcher.group(1);
            spoon.reflect.path.impl.CtPathElement pathElement = null;
            if (spoon.reflect.path.impl.CtNamedPathElement.STRING.equals(kind)) {
                pathElement = new spoon.reflect.path.impl.CtNamedPathElement(matcher.group(2));
            }else
                if (spoon.reflect.path.impl.CtTypedNameElement.STRING.equals(kind)) {
                    pathElement = new spoon.reflect.path.impl.CtTypedNameElement(load(matcher.group(2)));
                }else
                    if (spoon.reflect.path.impl.CtRolePathElement.STRING.equals(kind)) {
                        pathElement = new spoon.reflect.path.impl.CtRolePathElement(spoon.reflect.path.CtRole.fromName(matcher.group(2)));
                    }


            java.lang.String args = matcher.group(4);
            if (args != null) {
                for (java.lang.String arg : args.split(";")) {
                    java.util.regex.Matcher argmatcher = argumentPattern.matcher(arg);
                    if (argmatcher.matches()) {
                        pathElement.addArgument(argmatcher.group(1), argmatcher.group(2));
                    }
                }
            }
            path.addLast(pathElement);
        } 
        return path;
    }
}

