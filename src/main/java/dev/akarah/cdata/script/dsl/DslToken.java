package dev.akarah.cdata.script.dsl;

public interface DslToken {
    record Identifier(String identifier) implements DslToken {

    }

    record StringExpr(String value) implements DslToken {

    }

    record TextExpr(String value) implements DslToken {

    }

    record NumberExpr(double value) implements DslToken {

    }

    record Comma() implements DslToken {

    }

    record Semicolon() implements DslToken {

    }

    record OpenParen() implements DslToken {

    }

    record CloseParen() implements DslToken {

    }

    record OpenBrace() implements DslToken {

    }

    record CloseBrace() implements DslToken {

    }

    record IfKeyword() implements DslToken {

    }

    record ElseKeyword() implements DslToken {

    }

    record LocalKeyword() implements DslToken {

    }

    record RepeatKeyword() implements DslToken {

    }

    record EqualSymbol() implements DslToken {

    }

    record ArrowSymbol() implements DslToken {

    }

    record MinusSymbol() implements DslToken {

    }

    record EOF() implements DslToken {

    }
}
