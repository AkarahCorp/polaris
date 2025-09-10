package dev.akarah.polaris.script.dsl;

import dev.akarah.polaris.script.exception.SpanData;

public interface DslToken {
    SpanData span();

    record Identifier(String identifier, SpanData span) implements DslToken {

    }

    record StringExpr(String value, SpanData span) implements DslToken {

    }

    record TextExpr(String value, SpanData span) implements DslToken {

    }

    record NamespacedIdentifierExpr(String namespace, String path, SpanData span) implements DslToken {

    }

    record NumberExpr(double value, SpanData span) implements DslToken {

    }

    record Annotation(String annotation, SpanData span) implements DslToken {

    }

    record Comma(SpanData span) implements DslToken {

    }

    record QuestionMark(SpanData span) implements DslToken {

    }

    record Semicolon(SpanData span) implements DslToken {

    }

    record Colon(SpanData span) implements DslToken {

    }

    record OpenParen(SpanData span) implements DslToken {

    }

    record CloseParen(SpanData span) implements DslToken {

    }

    record OpenBrace(SpanData span) implements DslToken {

    }

    record CloseBrace(SpanData span) implements DslToken {

    }

    record OpenBracket(SpanData span) implements DslToken {

    }

    record CloseBracket(SpanData span) implements DslToken {

    }

    record BreakKeyword(SpanData span) implements DslToken {

    }

    record ContinueKeyword(SpanData span) implements DslToken {

    }

    record ReturnKeyword(SpanData span) implements DslToken {

    }

    record SwitchKeyword(SpanData span) implements DslToken {

    }

    record CaseKeyword(SpanData span) implements DslToken {

    }

    record WhereKeyword(SpanData span) implements DslToken {

    }

    record IfKeyword(SpanData span) implements DslToken {

    }

    record UnlessKeyword(SpanData span) implements DslToken {

    }

    record ElseKeyword(SpanData span) implements DslToken {

    }

    record LocalKeyword(SpanData span) implements DslToken {

    }

    record RepeatKeyword(SpanData span) implements DslToken {

    }

    record FunctionKeyword(SpanData span) implements DslToken {

    }

    record StructKeyword(SpanData span) implements DslToken {

    }

    record ForKeyword(SpanData span) implements DslToken {

    }

    record InKeyword(SpanData span) implements DslToken {

    }

    record NewKeyword(SpanData span) implements DslToken {

    }

    record EventKeyword(SpanData span) implements DslToken {

    }

    record AsKeyword(SpanData span) implements DslToken {

    }

    record EqualSymbol(SpanData span) implements DslToken {

    }

    record DoubleEqualSymbol(SpanData span) implements DslToken {

    }

    record NotEqualSymbol(SpanData span) implements DslToken {

    }

    record ArrowSymbol(SpanData span) implements DslToken {

    }

    record PlusSymbol(SpanData span) implements DslToken {

    }

    record MinusSymbol(SpanData span) implements DslToken {

    }

    record StarSymbol(SpanData span) implements DslToken {

    }

    record SlashSymbol(SpanData span) implements DslToken {

    }

    record GreaterThanSymbol(SpanData span) implements DslToken {

    }

    record LessThanSymbol(SpanData span) implements DslToken {

    }

    record GreaterThanOrEqualSymbol(SpanData span) implements DslToken {

    }

    record LessThanOrEqualSymbol(SpanData span) implements DslToken {

    }

    record LogicalAnd(SpanData span) implements DslToken {

    }

    record LogicalOr(SpanData span) implements DslToken {

    }

    record LogicalNot(SpanData span) implements DslToken {

    }

    record Percent(SpanData span) implements DslToken {

    }

    record EOF(SpanData span) implements DslToken {

    }
}
