import java.util.*;

public class ScopeAnalyzer {
    private Set<String> globalScope;
    private Stack<Set<String>> localScopes;

    public ScopeAnalyzer() {
        this.globalScope = new HashSet<>();
        this.localScopes = new Stack<>();
    }

    public void declareVariable(String varName) {
        if (!localScopes.isEmpty()) {
            localScopes.peek().add(varName);
        } else {
            globalScope.add(varName);
        }
    }

    public void declareFunction(String funcName) {
        globalScope.add(funcName);
    }

    public boolean isVariableDeclared(String varName) {
        if (!localScopes.isEmpty() && localScopes.peek().contains(varName)) {
            return true;
        }
        return globalScope.contains(varName);
    }

    public boolean isFunctionDeclared(String funcName) {
        return globalScope.contains(funcName);
    }

    public void enterScope() {
        localScopes.push(new HashSet<>());
    }

    public void exitScope() {
        if (!localScopes.isEmpty()) {
            localScopes.pop();
        }
    }
}
