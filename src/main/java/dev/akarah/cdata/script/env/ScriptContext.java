package dev.akarah.cdata.script.env;

public class ScriptContext {
    Selection defaultSelection = Selection.empty();
    VariableContainer localVariables = VariableContainer.empty();

    private ScriptContext() {

    }

    private ScriptContext(Selection defaultSelection) {
        this.defaultSelection = defaultSelection;
    }

    public static ScriptContext empty() {
        return new ScriptContext();
    }

    public static ScriptContext of(Selection defaultSelection) {
        return new ScriptContext(defaultSelection);
    }

    public VariableContainer localVariables() {
        return this.localVariables;
    }

    public Selection defaultSelection() {
        return this.defaultSelection;
    }
}
