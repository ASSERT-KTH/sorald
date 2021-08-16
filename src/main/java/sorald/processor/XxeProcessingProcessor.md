**Note:** This processor is a work in progress!

This repair is a work in progress. On a high level, it aims to make XML parsing
safe against XXE attacks by disabling features such as external schema and DTD
support.

Currently, we target the following types:

* `DocumentBuilderFactory`
* `TransformerFactory`
* `XMLInputFactory`

The transformation is highly similar regardless of type, and consists of replacing
factory creation with a call to a helper method that creates a "safe" factory. For
`DocumentBuilderFactory`, it looks like the following:

```diff
         // somewhere in a method body
-        DocumentBuilder builder = DocumentBuilderFactory.newInstance().createDocumentBuilder();
+        DocumentBuilder builder = createDocumentBuilderFactory().createDocumentBuilder();
         [...]

         // somewhere in a method body
-        DocumentBuilderFactory df = DocumentBuilderFactory.newInstance();
+        DocumentBuilderFactory df = createDocumentBuilderFactory();
         [...]

+    private static DocumentBuilderFactory createDocumentBuilderFactory() {
+         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
+         factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
+         factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
+         return factory;
+     }
```

The precise attributes set in `createTYPEFACTORY` depends on the particular
factory used. For example, with `TransformerFactory`, `ACCESS_EXTERNAL_SCHEMA`
is replaced with `ACCESS_EXTERNAL_STYLESHEET`. The method name to set attributes
also varies, but is typically either `setAttribute` or `setProperty`.

This is just a small part of rule 2755, and we are working on adding support
for other cases. The repair currently cannot handle builders and factories in
fields, as Sonar does not appear to issue warnings for them.
