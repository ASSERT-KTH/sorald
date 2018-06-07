package spoon.support.template;


public abstract class Parameters {
    private Parameters() {
    }

    protected static final java.lang.String fieldPrefix = "_FIELD_";

    @java.lang.SuppressWarnings("unchecked")
    public static java.lang.Integer getIndex(spoon.reflect.code.CtExpression<?> e) {
        if ((e.getParent()) instanceof spoon.reflect.code.CtArrayAccess) {
            spoon.reflect.code.CtExpression<java.lang.Integer> indexExpression = ((spoon.reflect.code.CtArrayAccess<?, spoon.reflect.code.CtExpression<java.lang.Integer>>) (e.getParent())).getIndexExpression();
            return ((spoon.reflect.code.CtLiteral<java.lang.Integer>) (indexExpression)).getValue();
        }
        return null;
    }

    public static java.lang.Object getValue(spoon.template.Template<?> template, java.lang.String parameterName, java.lang.Integer index) {
        java.lang.reflect.Field rtField = null;
        try {
            for (java.lang.reflect.Field f : spoon.support.util.RtHelper.getAllFields(template.getClass())) {
                if (spoon.support.template.Parameters.isParameterSource(f)) {
                    if (parameterName.equals(spoon.support.template.Parameters.getParameterName(f))) {
                        rtField = f;
                        break;
                    }
                }
            }
        } catch (java.lang.Exception e) {
            throw new spoon.support.template.UndefinedParameterException(e);
        }
        java.lang.Object tparamValue = spoon.support.template.Parameters.getValue(template, parameterName, rtField);
        if ((rtField.getType().isArray()) && (index != null)) {
            tparamValue = ((java.lang.Object[]) (tparamValue))[index];
        }
        return tparamValue;
    }

    private static java.lang.Object getValue(spoon.template.Template<?> template, java.lang.String parameterName, java.lang.reflect.Field rtField) {
        if (rtField == null) {
            throw new spoon.support.template.UndefinedParameterException();
        }
        try {
            if (java.lang.reflect.Modifier.isFinal(rtField.getModifiers())) {
                java.util.Map<java.lang.String, java.lang.Object> m = spoon.support.template.Parameters.finals.get(template);
                if (m == null) {
                    return null;
                }
                return m.get(parameterName);
            }
            rtField.setAccessible(true);
            return rtField.get(template);
        } catch (java.lang.Exception e) {
            throw new spoon.support.template.UndefinedParameterException(e);
        }
    }

    static java.util.Map<spoon.template.Template<?>, java.util.Map<java.lang.String, java.lang.Object>> finals = new java.util.HashMap<>();

    public static spoon.reflect.declaration.CtField<?> getParameterField(spoon.reflect.declaration.CtClass<? extends spoon.template.Template<?>> templateClass, java.lang.String parameterName) {
        for (spoon.reflect.declaration.CtTypeMember typeMember : templateClass.getTypeMembers()) {
            if (!(typeMember instanceof spoon.reflect.declaration.CtField)) {
                continue;
            }
            spoon.reflect.declaration.CtField<?> f = ((spoon.reflect.declaration.CtField<?>) (typeMember));
            spoon.template.Parameter p = f.getAnnotation(spoon.template.Parameter.class);
            if (p == null) {
                continue;
            }
            if (f.getSimpleName().equals(parameterName)) {
                return f;
            }
            if (parameterName.equals(p.value())) {
                return f;
            }
        }
        return null;
    }

    @java.lang.SuppressWarnings("null")
    public static void setValue(spoon.template.Template<?> template, java.lang.String parameterName, java.lang.Integer index, java.lang.Object value) {
        java.lang.Object tparamValue = null;
        try {
            java.lang.reflect.Field rtField = null;
            for (java.lang.reflect.Field f : spoon.support.util.RtHelper.getAllFields(template.getClass())) {
                if (spoon.support.template.Parameters.isParameterSource(f)) {
                    if (parameterName.equals(spoon.support.template.Parameters.getParameterName(f))) {
                        rtField = f;
                        break;
                    }
                }
            }
            if (rtField == null) {
                return;
            }
            if (java.lang.reflect.Modifier.isFinal(rtField.getModifiers())) {
                java.util.Map<java.lang.String, java.lang.Object> m = spoon.support.template.Parameters.finals.get(template);
                if (m == null) {
                    spoon.support.template.Parameters.finals.put(template, (m = new java.util.HashMap<>()));
                }
                m.put(parameterName, value);
                return;
            }
            rtField.setAccessible(true);
            rtField.set(template, value);
            if (rtField.getType().isArray()) {
                tparamValue = ((java.lang.Object[]) (tparamValue))[index];
            }
        } catch (java.lang.Exception e) {
            throw new spoon.support.template.UndefinedParameterException();
        }
    }

    private static java.lang.String getParameterName(java.lang.reflect.Field f) {
        java.lang.String name = f.getName();
        spoon.template.Parameter p = f.getAnnotation(spoon.template.Parameter.class);
        if ((p != null) && (!(p.value().equals("")))) {
            name = p.value();
        }
        return name;
    }

    private static java.lang.String getParameterName(spoon.reflect.reference.CtFieldReference<?> f) {
        java.lang.String name = f.getSimpleName();
        spoon.template.Parameter p = f.getDeclaration().getAnnotation(spoon.template.Parameter.class);
        if ((p != null) && (!(p.value().equals("")))) {
            name = p.value();
        }
        return name;
    }

    public static java.util.List<java.lang.String> getNames(spoon.reflect.declaration.CtClass<? extends spoon.template.Template<?>> templateType) {
        java.util.List<java.lang.String> params = new java.util.ArrayList<>();
        try {
            for (spoon.reflect.reference.CtFieldReference<?> f : templateType.getAllFields()) {
                if (spoon.support.template.Parameters.isParameterSource(f)) {
                    params.add(spoon.support.template.Parameters.getParameterName(f));
                }
            }
        } catch (java.lang.Exception e) {
            throw new spoon.SpoonException("Getting of template parameters failed", e);
        }
        return params;
    }

    public static java.util.Map<java.lang.String, java.lang.Object> getNamesToValues(spoon.template.Template<?> template, spoon.reflect.declaration.CtClass<? extends spoon.template.Template<?>> templateType) {
        java.util.Map<java.lang.String, java.lang.Object> params = new java.util.LinkedHashMap<>();
        try {
            for (spoon.reflect.reference.CtFieldReference<?> f : templateType.getAllFields()) {
                if (spoon.support.template.Parameters.isParameterSource(f)) {
                    java.lang.String parameterName = spoon.support.template.Parameters.getParameterName(f);
                    params.put(parameterName, spoon.support.template.Parameters.getValue(template, parameterName, ((java.lang.reflect.Field) (f.getActualField()))));
                }
            }
        } catch (java.lang.Exception e) {
            throw new spoon.SpoonException("Getting of template parameters failed", e);
        }
        return params;
    }

    public static java.util.Map<java.lang.String, java.lang.Object> getTemplateParametersAsMap(spoon.reflect.factory.Factory f, spoon.reflect.declaration.CtType<?> targetType, spoon.template.Template<?> template) {
        java.util.Map<java.lang.String, java.lang.Object> params = new java.util.HashMap(spoon.support.template.Parameters.getNamesToValues(template, ((spoon.reflect.declaration.CtClass) (f.Class().get(template.getClass())))));
        if (targetType != null) {
            params.put(template.getClass().getSimpleName(), targetType.getReference());
        }
        return params;
    }

    public static boolean isParameterSource(spoon.reflect.reference.CtFieldReference<?> ref) {
        spoon.reflect.declaration.CtField<?> field = ref.getDeclaration();
        if (field == null) {
            return false;
        }
        if ((field.getAnnotation(spoon.template.Parameter.class)) != null) {
            return true;
        }
        if ((ref.getType()) instanceof spoon.reflect.reference.CtTypeParameterReference) {
            return false;
        }
        if (ref.getSimpleName().equals("this")) {
            return false;
        }
        if (ref.getType().isSubtypeOf(spoon.support.template.Parameters.getTemplateParameterType(ref.getFactory()))) {
            return true;
        }
        return false;
    }

    public static boolean isParameterSource(java.lang.reflect.Field field) {
        return ((field.getAnnotation(spoon.template.Parameter.class)) != null) || (spoon.template.TemplateParameter.class.isAssignableFrom(field.getType()));
    }

    static spoon.reflect.reference.CtTypeReference<spoon.template.TemplateParameter<?>> templateParameterType;

    @java.lang.SuppressWarnings({ "rawtypes", "unchecked" })
    private static synchronized spoon.reflect.reference.CtTypeReference<spoon.template.TemplateParameter<?>> getTemplateParameterType(spoon.reflect.factory.Factory factory) {
        if ((spoon.support.template.Parameters.templateParameterType) == null) {
            spoon.support.template.Parameters.templateParameterType = ((spoon.reflect.reference.CtTypeReference) (factory.Type().createReference(spoon.template.TemplateParameter.class)));
        }
        return spoon.support.template.Parameters.templateParameterType;
    }

    @java.lang.SuppressWarnings("unchecked")
    public static <T> spoon.template.TemplateParameter<T> NIL(java.lang.Class<? extends T> type) {
        if (java.lang.Number.class.isAssignableFrom(type)) {
            return ((spoon.template.TemplateParameter<T>) (new spoon.template.TemplateParameter<java.lang.Number>() {
                public java.lang.Number S() {
                    return 0;
                }
            }));
        }
        return new spoon.template.TemplateParameter<T>() {
            public T S() {
                return null;
            }
        };
    }

    public static java.util.List<java.lang.reflect.Field> getAllTemplateParameterFields(java.lang.Class<? extends spoon.template.Template> clazz) {
        if (!(spoon.template.Template.class.isAssignableFrom(clazz))) {
            throw new java.lang.IllegalArgumentException();
        }
        java.util.List<java.lang.reflect.Field> result = new java.util.ArrayList<>();
        for (java.lang.reflect.Field f : spoon.support.util.RtHelper.getAllFields(clazz)) {
            if (spoon.support.template.Parameters.isParameterSource(f)) {
                result.add(f);
            }
        }
        return result;
    }

    public static java.util.List<spoon.reflect.declaration.CtField<?>> getAllTemplateParameterFields(java.lang.Class<? extends spoon.template.Template<?>> clazz, spoon.reflect.factory.Factory factory) {
        spoon.reflect.declaration.CtClass<?> c = factory.Class().get(clazz);
        if (c == null) {
            throw new java.lang.IllegalArgumentException("Template not in template classpath");
        }
        java.util.List<spoon.reflect.declaration.CtField<?>> result = new java.util.ArrayList<>();
        for (java.lang.reflect.Field f : spoon.support.template.Parameters.getAllTemplateParameterFields(clazz)) {
            result.add(c.getField(f.getName()));
        }
        return result;
    }
}

