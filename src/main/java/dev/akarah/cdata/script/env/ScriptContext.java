package dev.akarah.cdata.script.env;

public class ScriptContext {
    Selection defaultSelection = Selection.empty();

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

    public Selection defaultSelection() {
        return this.defaultSelection;
    }
}
