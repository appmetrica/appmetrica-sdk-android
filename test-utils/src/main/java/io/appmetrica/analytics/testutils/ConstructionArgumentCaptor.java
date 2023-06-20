package io.appmetrica.analytics.testutils;

import java.util.ArrayList;
import java.util.List;
import org.mockito.MockedConstruction;

public class ConstructionArgumentCaptor<T> implements MockedConstruction.MockInitializer<T> {

    private final List<List<Object>> arguments = new ArrayList<>();

    private final MockedConstruction.MockInitializer<T> initializer;

    public ConstructionArgumentCaptor() {
        this(null);
    }

    public ConstructionArgumentCaptor(MockedConstruction.MockInitializer<T> initializer) {
        this.initializer = initializer;
    }

    public List<List<Object>> getArguments() {
        return arguments;
    }

    public List<Object> flatArguments() {
        List<Object> out = new ArrayList<>();
        for (List<Object> args: arguments) {
            out.addAll(args);
        }
        return out;
    }

    @Override
    public void prepare(T mock, MockedConstruction.Context context) throws Throwable {
        if (initializer != null) {
            initializer.prepare(mock, context);
        }
        arguments.add(new ArrayList<>(context.arguments()));
    }
}
