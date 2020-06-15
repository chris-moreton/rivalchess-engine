package com.netsensia.rivalchess.engine.core.search;

import java.lang.System;

@kotlin.Metadata(mv = {1, 1, 16}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000\u00a8\u0001\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\b\n\u0002\b\n\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010\u0015\n\u0000\n\u0002\u0010!\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0011\n\u0002\b\u0014\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0012\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u001c\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0018\u0002\n\u0002\b\u0013\n\u0002\u0018\u0002\n\u0002\b/\u0018\u00002\u00020\u0001B\u000f\b\u0016\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004B\u001b\b\u0007\u0012\b\b\u0002\u0010\u0005\u001a\u00020\u0006\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0007J\b\u0010_\u001a\u00020\tH\u0002J\u0010\u0010`\u001a\u00020a2\u0006\u0010b\u001a\u00020\u0016H\u0002J\u0010\u0010c\u001a\u00020\u000b2\u0006\u0010d\u001a\u00020\u000bH\u0002J\u0018\u0010e\u001a\u00020f2\u0006\u0010g\u001a\u00020\u000b2\u0006\u0010h\u001a\u00020iH\u0002J\b\u0010j\u001a\u00020\"H\u0002J\u0018\u0010k\u001a\u00020\u000b2\u0006\u0010l\u001a\u00020\u000b2\u0006\u0010m\u001a\u00020\tH\u0002J\u0006\u0010n\u001a\u00020aJ\b\u0010o\u001a\u00020aH\u0002J0\u0010l\u001a\u00020\u000b2\u0006\u0010p\u001a\u00020\u000b2\u0006\u0010q\u001a\u00020\u000b2\u0006\u0010r\u001a\u00020\u000b2\u0006\u0010s\u001a\u00020\u000b2\u0006\u0010t\u001a\u00020\u000bH\u0002J0\u0010u\u001a\u00020\u00162\u0006\u0010\u0002\u001a\u00020(2\u0006\u0010v\u001a\u00020\u000b2\u0006\u0010w\u001a\u00020\u000b2\u0006\u0010x\u001a\u00020\u000b2\u0006\u0010m\u001a\u00020\tH\u0002J\u0018\u0010y\u001a\u00020\u000b2\u0006\u0010m\u001a\u00020\t2\u0006\u0010z\u001a\u00020\tH\u0002J\u0006\u0010{\u001a\u00020\u001eJ \u0010|\u001a\u00020\u000b2\u0006\u0010\u0002\u001a\u00020(2\u0006\u0010v\u001a\u00020\u000b2\u0006\u0010}\u001a\u00020\u000bH\u0002J\u000e\u0010~\u001a\u00020\u000b2\u0006\u0010\u007f\u001a\u00020\"JD\u0010\u0080\u0001\u001a\u00020\u00162\u0007\u0010\u0081\u0001\u001a\u00020\u000b2\u0007\u0010\u0082\u0001\u001a\u00020\t2\u0006\u0010g\u001a\u00020\u000b2\u0006\u0010v\u001a\u00020\u000b2\u0007\u0010\u0083\u0001\u001a\u00020i2\u0006\u0010l\u001a\u00020\u000b2\u0006\u0010m\u001a\u00020\tH\u0002J\u0007\u0010\u0084\u0001\u001a\u00020aJ-\u0010\u0085\u0001\u001a\u00030\u0086\u00012\u0006\u0010\u0002\u001a\u00020(2\u0007\u0010\u0087\u0001\u001a\u00020\u000b2\u0007\u0010\u0083\u0001\u001a\u00020i2\u0007\u0010\u0088\u0001\u001a\u00020\u0016H\u0002J$\u0010\u0089\u0001\u001a\u00020\t2\u0007\u0010\u008a\u0001\u001a\u00020\u000b2\u0007\u0010\u008b\u0001\u001a\u00020\u000b2\u0007\u0010\u0083\u0001\u001a\u00020iH\u0002JT\u0010\u008c\u0001\u001a\u00020\u000b2\u0006\u0010\u0002\u001a\u00020(2\u0006\u0010}\u001a\u00020\u000b2\u0007\u0010\u0087\u0001\u001a\u00020\u000b2\u0006\u0010g\u001a\u00020\u000b2\u0006\u0010v\u001a\u00020\u000b2\u0007\u0010\u0083\u0001\u001a\u00020i2\u0006\u0010l\u001a\u00020\u000b2\u0007\u0010\u008d\u0001\u001a\u00020\u000b2\u0006\u0010m\u001a\u00020\tH\u0002J)\u0010\u008e\u0001\u001a\t\u0012\u0004\u0012\u00020\u000b0\u008f\u00012\u0006\u0010\u0002\u001a\u00020(2\u0006\u0010v\u001a\u00020\u000b2\u0007\u0010\u008c\u0001\u001a\u00020\u000bH\u0002J$\u0010\u0090\u0001\u001a\u00020\u000b2\u0007\u0010\u0091\u0001\u001a\u00020\t2\u0007\u0010\u0092\u0001\u001a\u00020\u000b2\u0007\u0010\u0093\u0001\u001a\u00020\u000bH\u0002J\t\u0010\u0094\u0001\u001a\u00020aH\u0002J\t\u0010\u0095\u0001\u001a\u00020\tH\u0002J\u0011\u0010\u0096\u0001\u001a\u00020\t2\u0006\u0010v\u001a\u00020\u000bH\u0002J\u0007\u0010\u0097\u0001\u001a\u00020\tJ\u0010\u0010\u0098\u0001\u001a\u00020a2\u0007\u0010\u0099\u0001\u001a\u00020\u000bJ\u0011\u0010\u009a\u0001\u001a\u00020\u000b2\u0006\u0010v\u001a\u00020\u000bH\u0002J\u0007\u0010\u009b\u0001\u001a\u00020aJ\u0012\u0010\u009c\u0001\u001a\u00020\t2\u0007\u0010\u009d\u0001\u001a\u00020\u000bH\u0002J\u0019\u0010\u009e\u0001\u001a\u00020\u000b2\u0006\u0010l\u001a\u00020\u000b2\u0006\u0010\u0002\u001a\u00020(H\u0002J\"\u0010\u009f\u0001\u001a\u00020\t2\u0006\u0010\u0002\u001a\u00020(2\u0007\u0010\u0087\u0001\u001a\u00020\u000b2\u0006\u0010m\u001a\u00020\tH\u0002J@\u0010\u00a0\u0001\u001a\u00020\u00162\u0006\u0010\u0002\u001a\u00020(2\u0006\u0010g\u001a\u00020\u000b2\u0006\u0010v\u001a\u00020\u000b2\u0007\u0010\u00a1\u0001\u001a\u00020\u000b2\u0006\u0010w\u001a\u00020\u000b2\u0006\u0010x\u001a\u00020\u000b2\u0006\u0010m\u001a\u00020\tJ\u0006\u0010R\u001a\u00020aJ>\u0010\u00a2\u0001\u001a\u00030\u00a3\u00012\u0006\u0010l\u001a\u00020\u000b2\u0007\u0010\u00a4\u0001\u001a\u00020\u000b2\u0007\u0010\u00a5\u0001\u001a\u00020\u000b2\u0006\u0010\u0002\u001a\u00020(2\u0007\u0010\u0081\u0001\u001a\u00020\u000b2\u0007\u0010\u008d\u0001\u001a\u00020\u000bH\u0002J\t\u0010\u00a6\u0001\u001a\u00020aH\u0002J\t\u0010\u00a7\u0001\u001a\u00020aH\u0016J\u0019\u0010\u00a8\u0001\u001a\u00020\u000b2\u0006\u0010\u0002\u001a\u00020(2\u0006\u0010v\u001a\u00020\u000bH\u0002J\u0019\u0010\u00a9\u0001\u001a\u00020a2\u0006\u0010\u0002\u001a\u00020(2\u0006\u0010v\u001a\u00020\u000bH\u0002J,\u0010\u00aa\u0001\u001a\u00020\u000b2\u0006\u0010\u0002\u001a\u00020(2\u0007\u0010\u008b\u0001\u001a\u00020\u000b2\u0007\u0010\u00ab\u0001\u001a\u00020\u000b2\u0007\u0010\u00ac\u0001\u001a\u00020\u000bH\u0002J#\u0010\u00ad\u0001\u001a\u00020\u000b2\u0006\u0010v\u001a\u00020\u000b2\u0007\u0010\u00ae\u0001\u001a\u00020\u000b2\u0007\u0010\u00af\u0001\u001a\u00020\"H\u0002J=\u0010\u00b0\u0001\u001a\u00020\u000b2\u0006\u0010\u0002\u001a\u00020(2\u0006\u0010v\u001a\u00020\u000b2\u0007\u0010\u00ae\u0001\u001a\u00020\u000b2\u0007\u0010\u008b\u0001\u001a\u00020\u000b2\u0007\u0010\u00af\u0001\u001a\u00020\"2\u0007\u0010\u00ac\u0001\u001a\u00020\u000bH\u0002J\"\u0010\u00b1\u0001\u001a\u00020\u000b2\u0006\u0010v\u001a\u00020\u000b2\u0007\u0010\u00ae\u0001\u001a\u00020\u000b2\u0006\u0010\u0002\u001a\u00020(H\u0002J#\u0010\u00b2\u0001\u001a\u00020\u000b2\u0006\u0010\u0002\u001a\u00020(2\u0007\u0010\u00ab\u0001\u001a\u00020\u000b2\u0007\u0010\u00ac\u0001\u001a\u00020\u000bH\u0002J\"\u0010\u00b3\u0001\u001a\u00020a2\u0006\u0010\u0002\u001a\u00020(2\u0006\u0010v\u001a\u00020\u000b2\u0007\u0010\u00b4\u0001\u001a\u00020\tH\u0002JN\u0010\u0082\u0001\u001a\u00020\u00162\u0007\u0010\u00b5\u0001\u001a\u00020\t2\u0006\u0010g\u001a\u00020\u000b2\u0006\u0010v\u001a\u00020\u000b2\u0007\u0010\u0083\u0001\u001a\u00020i2\u0007\u0010\u00b6\u0001\u001a\u00020\u000b2\u0007\u0010\u00b7\u0001\u001a\u00020\u000b2\u0007\u0010\u00b8\u0001\u001a\u00020\t2\u0006\u0010\u0002\u001a\u00020(H\u0002JA\u0010\u00b9\u0001\u001a\u00020\u00162\u0006\u0010\u0002\u001a\u00020(2\u0006\u0010g\u001a\u00020\u000b2\u0006\u0010v\u001a\u00020\u000b2\u0007\u0010\u0083\u0001\u001a\u00020i2\u0006\u0010l\u001a\u00020\u000b2\u0007\u0010\u008d\u0001\u001a\u00020\u000b2\u0006\u0010m\u001a\u00020\tJ;\u0010\u00ba\u0001\u001a\u00020\u00162\u0006\u0010\u0002\u001a\u00020(2\u0006\u0010g\u001a\u00020\u000b2\u0007\u0010\u00bb\u0001\u001a\u00020\u000b2\u0006\u0010v\u001a\u00020\u000b2\u0007\u0010\u0083\u0001\u001a\u00020i2\u0006\u0010l\u001a\u00020\u000bH\u0002J(\u0010\u00bc\u0001\u001a\u00020\u00162\u0006\u0010\u0002\u001a\u00020(2\u0006\u0010g\u001a\u00020\u000b2\u0006\u0010v\u001a\u00020\u000b2\u0007\u0010\u0083\u0001\u001a\u00020iJ\u000f\u0010\u00bd\u0001\u001a\u00020a2\u0006\u0010\u0002\u001a\u00020\u0003J\u0010\u0010\u00be\u0001\u001a\u00020a2\u0007\u0010\u00bf\u0001\u001a\u00020\u000bJ\u000f\u0010\u00c0\u0001\u001a\u00020a2\u0006\u0010C\u001a\u00020\u000bJ\u000f\u0010\u00c1\u0001\u001a\u00020a2\u0006\u0010P\u001a\u00020\u000bJ*\u0010\u00c2\u0001\u001a\u00020a2\u0006\u0010m\u001a\u00020\t2\u0006\u0010v\u001a\u00020\u000b2\u0006\u0010\u0002\u001a\u00020(2\u0007\u0010\u00a1\u0001\u001a\u00020\u000bH\u0002J\u0011\u0010\u00c3\u0001\u001a\u00020\"2\u0006\u0010v\u001a\u00020\u000bH\u0002J\u0007\u0010\u00c4\u0001\u001a\u00020aJ\u0010\u0010\u00c5\u0001\u001a\u00020a2\u0007\u0010\u00c6\u0001\u001a\u00020\u000bJ\t\u0010\u00c7\u0001\u001a\u00020aH\u0002J\t\u0010\u00c8\u0001\u001a\u00020aH\u0002J\u0007\u0010\u00c9\u0001\u001a\u00020aJ\u0007\u0010\u00ca\u0001\u001a\u00020aJ\u0019\u0010\u00cb\u0001\u001a\u00020\u000b2\u0006\u0010b\u001a\u00020\u00162\u0006\u0010l\u001a\u00020\u000bH\u0002J\u001b\u0010\u00cc\u0001\u001a\u00020a2\u0007\u0010\u0081\u0001\u001a\u00020\u000b2\u0007\u0010\u00cd\u0001\u001a\u00020\u000bH\u0002J,\u0010\u00ce\u0001\u001a\u00020a2\u0006\u0010G\u001a\u00020H2\u0007\u0010\u0081\u0001\u001a\u00020\u000b2\u0007\u0010\u0087\u0001\u001a\u00020\u000b2\u0007\u0010\u00cf\u0001\u001a\u00020\tH\u0002J+\u0010\u00d0\u0001\u001a\u00020a2\u0007\u0010\u00d1\u0001\u001a\u00020%2\u0007\u0010\u0081\u0001\u001a\u00020\u000b2\u0006\u0010v\u001a\u00020\u000b2\u0006\u0010b\u001a\u00020\u0016H\u0002R\u0012\u0010\b\u001a\u00020\t8\u0006@\u0006X\u0087\u000e\u00a2\u0006\u0002\n\u0000R\u001a\u0010\n\u001a\u00020\u000bX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\f\u0010\r\"\u0004\b\u000e\u0010\u000fR\u001a\u0010\u0010\u001a\u00020\u000bX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0011\u0010\r\"\u0004\b\u0012\u0010\u000fR\u0011\u0010\u0013\u001a\u00020\u000b8F\u00a2\u0006\u0006\u001a\u0004\b\u0014\u0010\rR\u001a\u0010\u0015\u001a\u00020\u0016X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0017\u0010\u0018\"\u0004\b\u0019\u0010\u001aR\u0011\u0010\u001b\u001a\u00020\u000b8F\u00a2\u0006\u0006\u001a\u0004\b\u001c\u0010\rR\u0011\u0010\u001d\u001a\u00020\u001e8F\u00a2\u0006\u0006\u001a\u0004\b\u001f\u0010 R\u000e\u0010!\u001a\u00020\"X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001a\u0010#\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020%0$0$X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010&\u001a\b\u0012\u0004\u0012\u00020\u000b0$X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\'\u001a\u00020(X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001e\u0010+\u001a\u00020*2\u0006\u0010)\u001a\u00020*@BX\u0086\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b,\u0010-R\u000e\u0010.\u001a\u00020\u000bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001c\u0010/\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\"0000X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u00101R\u001c\u00102\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\"0000X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u00101R\u0011\u00103\u001a\u00020\t8F\u00a2\u0006\u0006\u001a\u0004\b3\u00104R\u001a\u00105\u001a\u00020\tX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b5\u00104\"\u0004\b6\u00107R\u001a\u00108\u001a\u00020\u000bX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b9\u0010\r\"\u0004\b:\u0010\u000fR\u0016\u0010;\u001a\b\u0012\u0004\u0012\u00020\"00X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010<R\u0014\u0010=\u001a\b\u0012\u0004\u0012\u00020\u000b0$X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010>\u001a\u00020%X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b?\u0010@\"\u0004\bA\u0010BR\u000e\u0010C\u001a\u00020\u000bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0018\u0010D\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010E00X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010FR\u0011\u0010G\u001a\u00020H8F\u00a2\u0006\u0006\u001a\u0004\bI\u0010JR\u001a\u0010K\u001a\u00020\u000bX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\bL\u0010\r\"\u0004\bM\u0010\u000fR\u0011\u0010N\u001a\u00020\u000b8F\u00a2\u0006\u0006\u001a\u0004\bO\u0010\rR\u000e\u0010P\u001a\u00020\u000bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0016\u0010Q\u001a\b\u0012\u0004\u0012\u00020\"00X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010<R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010R\u001a\u00020\tX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0011\u0010S\u001a\u00020%8F\u00a2\u0006\u0006\u001a\u0004\bT\u0010@R\u000e\u0010U\u001a\u00020%X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0016\u0010V\u001a\b\u0012\u0004\u0012\u00020\u001600X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010WR\u000e\u0010X\u001a\u00020%X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010Y\u001a\u00020%X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010Z\u001a\u00020[X\u0082\u0004\u00a2\u0006\u0002\n\u0000R$\u0010\\\u001a\u00020\t2\u0006\u0010\\\u001a\u00020\t@FX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b]\u00104\"\u0004\b^\u00107\u00a8\u0006\u00d2\u0001"}, d2 = {"Lcom/netsensia/rivalchess/engine/core/search/Search;", "Ljava/lang/Runnable;", "board", "Lcom/netsensia/rivalchess/model/Board;", "(Lcom/netsensia/rivalchess/model/Board;)V", "printStream", "Ljava/io/PrintStream;", "(Ljava/io/PrintStream;Lcom/netsensia/rivalchess/model/Board;)V", "abortingSearch", "", "currentDepthZeroMove", "", "getCurrentDepthZeroMove", "()I", "setCurrentDepthZeroMove", "(I)V", "currentDepthZeroMoveNumber", "getCurrentDepthZeroMoveNumber", "setCurrentDepthZeroMoveNumber", "currentMove", "getCurrentMove", "currentPath", "Lcom/netsensia/rivalchess/engine/core/search/SearchPath;", "getCurrentPath", "()Lcom/netsensia/rivalchess/engine/core/search/SearchPath;", "setCurrentPath", "(Lcom/netsensia/rivalchess/engine/core/search/SearchPath;)V", "currentScore", "getCurrentScore", "currentScoreHuman", "", "getCurrentScoreHuman", "()Ljava/lang/String;", "depthZeroMoveScores", "", "drawnPositionsAtRoot", "", "", "drawnPositionsAtRootCount", "engineBoard", "Lcom/netsensia/rivalchess/engine/core/board/EngineBoard;", "<set-?>", "Lcom/netsensia/rivalchess/enums/SearchState;", "engineState", "getEngineState", "()Lcom/netsensia/rivalchess/enums/SearchState;", "finalDepthToSearch", "historyMovesFail", "", "[[[I", "historyMovesSuccess", "isSearching", "()Z", "isUciMode", "setUciMode", "(Z)V", "iterativeDeepeningDepth", "getIterativeDeepeningDepth", "setIterativeDeepeningDepth", "killerMoves", "[[I", "mateKiller", "millisSetByEngineMonitor", "getMillisSetByEngineMonitor", "()J", "setMillisSetByEngineMonitor", "(J)V", "millisToThink", "moveOrderStatus", "Lcom/netsensia/rivalchess/enums/MoveOrder;", "[Lcom/netsensia/rivalchess/enums/MoveOrder;", "mover", "Lcom/netsensia/rivalchess/model/Colour;", "getMover", "()Lcom/netsensia/rivalchess/model/Colour;", "nodes", "getNodes", "setNodes", "nodesPerSecond", "getNodesPerSecond", "nodesToSearch", "orderedMoves", "quit", "searchDuration", "getSearchDuration", "searchEndTime", "searchPath", "[Lcom/netsensia/rivalchess/engine/core/search/SearchPath;", "searchStartTime", "searchTargetEndTime", "staticExchangeEvaluator", "Lcom/netsensia/rivalchess/engine/core/eval/see/StaticExchangeEvaluator;", "useOpeningBook", "getUseOpeningBook", "setUseOpeningBook", "abortIfTimeIsUp", "adjustScoreForMateDepth", "", "newPath", "adjustedSee", "see", "aspirationSearch", "Lcom/netsensia/rivalchess/engine/core/search/AspirationSearchResult;", "depth", "aspirationWindow", "Lcom/netsensia/rivalchess/engine/core/search/Window;", "boardMoves", "checkExtension", "extensions", "isCheck", "clearHash", "determineDrawnPositionsAndGenerateDepthZeroMoves", "checkExtend", "threatExtend", "recaptureExtend", "pawnExtend", "maxNewExtensionsInThisPart", "finalPath", "ply", "low", "high", "getExtensions", "wasPawnPush", "getFen", "getHighScoreMove", "hashMove", "getHighestScoringMoveFromArray", "theseMoves", "getPathFromSearch", "move", "scoutSearch", "window", "go", "hashProbe", "Lcom/netsensia/rivalchess/engine/core/search/HashProbeResult;", "depthRemaining", "bestPath", "hashProbeResult", "flag", "score", "highRankingMove", "recaptureSquare", "highScoreMoveSequence", "Lkotlin/sequences/Sequence;", "historyScore", "isWhite", "from", "to", "initSearchVariables", "isBookMoveAvailable", "isDrawnAtRoot", "isOkToSendInfo", "makeMove", "engineMove", "maxExtensionsForPly", "newGame", "onlyOneMoveAndNotOnFixedTime", "numLegalMoves", "pawnExtensions", "performNullMove", "quiesce", "quiescePly", "recaptureExtensions", "Lcom/netsensia/rivalchess/engine/core/search/RecaptureExtensionResponse;", "targetPiece", "movePiece", "reorderDepthZeroMoves", "run", "scoreFullWidthCaptures", "scoreFullWidthMoves", "scoreHistoryHeuristic", "fromSquare", "toSquare", "scoreKillerMoves", "i", "movesForSorting", "scoreLosingCapturesWithWinningHistory", "scoreMove", "scorePieceSquareValues", "scoreQuiesceMoves", "includeChecks", "useScoutSearch", "newExtensions", "newRecaptureSquare", "localIsCheck", "search", "searchNullMove", "nullMoveReduceDepth", "searchZero", "setBoard", "setHashSizeMB", "hashSizeMB", "setMillisToThink", "setNodesToSearch", "setOrderedMovesArrayForQuiesce", "setPlyMoves", "setSearchComplete", "setSearchDepth", "searchDepth", "setupHistoryMoveTable", "setupMateAndKillerMoveTables", "startSearch", "stopSearch", "threatExtensions", "updateCurrentDepthZeroMove", "arrayIndex", "updateHistoryMoves", "success", "updateKillerMoves", "enemyBitboard", "rivalchess-engine"})
public final class Search implements java.lang.Runnable {
    private final java.io.PrintStream printStream = null;
    private final com.netsensia.rivalchess.engine.core.board.EngineBoard engineBoard = null;
    private final com.netsensia.rivalchess.engine.core.eval.see.StaticExchangeEvaluator staticExchangeEvaluator = null;
    private final com.netsensia.rivalchess.enums.MoveOrder[] moveOrderStatus = null;
    private final java.util.List<java.util.List<java.lang.Long>> drawnPositionsAtRoot = null;
    private final java.util.List<java.lang.Integer> drawnPositionsAtRootCount = null;
    private final java.util.List<java.lang.Integer> mateKiller = null;
    private final int[][] killerMoves = null;
    private final int[][][] historyMovesSuccess = null;
    private final int[][][] historyMovesFail = null;
    private final int[][] orderedMoves = null;
    private final com.netsensia.rivalchess.engine.core.search.SearchPath[] searchPath = null;
    private int[] depthZeroMoveScores;
    @org.jetbrains.annotations.NotNull()
    private com.netsensia.rivalchess.enums.SearchState engineState;
    private boolean quit = false;
    private int nodes = 0;
    private long millisSetByEngineMonitor = 0L;
    private int millisToThink = 0;
    private int nodesToSearch = 2147483647;
    public boolean abortingSearch = true;
    private long searchStartTime = -1L;
    private long searchTargetEndTime = 0L;
    private long searchEndTime = 0L;
    private int finalDepthToSearch = 1;
    private int iterativeDeepeningDepth = 1;
    private int currentDepthZeroMove = 0;
    private int currentDepthZeroMoveNumber = 0;
    @org.jetbrains.annotations.NotNull()
    private com.netsensia.rivalchess.engine.core.search.SearchPath currentPath;
    private boolean isUciMode = false;
    private boolean useOpeningBook;
    
    @org.jetbrains.annotations.NotNull()
    public final com.netsensia.rivalchess.enums.SearchState getEngineState() {
        return null;
    }
    
    public final int getNodes() {
        return 0;
    }
    
    public final void setNodes(int p0) {
    }
    
    public final long getMillisSetByEngineMonitor() {
        return 0L;
    }
    
    public final void setMillisSetByEngineMonitor(long p0) {
    }
    
    public final int getIterativeDeepeningDepth() {
        return 0;
    }
    
    public final void setIterativeDeepeningDepth(int p0) {
    }
    
    public final int getCurrentDepthZeroMove() {
        return 0;
    }
    
    public final void setCurrentDepthZeroMove(int p0) {
    }
    
    public final int getCurrentDepthZeroMoveNumber() {
        return 0;
    }
    
    public final void setCurrentDepthZeroMoveNumber(int p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.netsensia.rivalchess.engine.core.search.SearchPath getCurrentPath() {
        return null;
    }
    
    public final void setCurrentPath(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.search.SearchPath p0) {
    }
    
    public final boolean isUciMode() {
        return false;
    }
    
    public final void setUciMode(boolean p0) {
    }
    
    public final boolean getUseOpeningBook() {
        return false;
    }
    
    public final void setUseOpeningBook(boolean useOpeningBook) {
    }
    
    public final void go() {
    }
    
    private final com.netsensia.rivalchess.engine.core.search.AspirationSearchResult aspirationSearch(int depth, com.netsensia.rivalchess.engine.core.search.Window aspirationWindow) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.netsensia.rivalchess.engine.core.search.SearchPath searchZero(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.board.EngineBoard board, int depth, int ply, @org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.search.Window window) throws com.netsensia.rivalchess.exception.InvalidMoveException {
        return null;
    }
    
    private final boolean onlyOneMoveAndNotOnFixedTime(int numLegalMoves) {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.netsensia.rivalchess.engine.core.search.SearchPath search(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.board.EngineBoard board, int depth, int ply, @org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.search.Window window, int extensions, int recaptureSquare, boolean isCheck) throws com.netsensia.rivalchess.exception.InvalidMoveException {
        return null;
    }
    
    private final kotlin.sequences.Sequence<java.lang.Integer> highScoreMoveSequence(com.netsensia.rivalchess.engine.core.board.EngineBoard board, int ply, int highRankingMove) {
        return null;
    }
    
    private final int pawnExtensions(int extensions, com.netsensia.rivalchess.engine.core.board.EngineBoard board) {
        return 0;
    }
    
    private final int maxExtensionsForPly(int ply) {
        return 0;
    }
    
    private final void updateKillerMoves(long enemyBitboard, int move, int ply, com.netsensia.rivalchess.engine.core.search.SearchPath newPath) {
    }
    
    private final com.netsensia.rivalchess.engine.core.search.RecaptureExtensionResponse recaptureExtensions(int extensions, int targetPiece, int movePiece, com.netsensia.rivalchess.engine.core.board.EngineBoard board, int move, int recaptureSquare) {
        return null;
    }
    
    private final com.netsensia.rivalchess.engine.core.search.SearchPath scoutSearch(boolean useScoutSearch, int depth, int ply, com.netsensia.rivalchess.engine.core.search.Window window, int newExtensions, int newRecaptureSquare, boolean localIsCheck, com.netsensia.rivalchess.engine.core.board.EngineBoard board) {
        return null;
    }
    
    private final int highRankingMove(com.netsensia.rivalchess.engine.core.board.EngineBoard board, int hashMove, int depthRemaining, int depth, int ply, com.netsensia.rivalchess.engine.core.search.Window window, int extensions, int recaptureSquare, boolean isCheck) {
        return 0;
    }
    
    private final int extensions(int checkExtend, int threatExtend, int recaptureExtend, int pawnExtend, int maxNewExtensionsInThisPart) {
        return 0;
    }
    
    private final void updateHistoryMoves(com.netsensia.rivalchess.model.Colour mover, int move, int depthRemaining, boolean success) {
    }
    
    private final void updateCurrentDepthZeroMove(int move, int arrayIndex) {
    }
    
    private final int getExtensions(boolean isCheck, boolean wasPawnPush) {
        return 0;
    }
    
    private final com.netsensia.rivalchess.engine.core.search.SearchPath getPathFromSearch(int move, boolean scoutSearch, int depth, int ply, com.netsensia.rivalchess.engine.core.search.Window window, int extensions, boolean isCheck) {
        return null;
    }
    
    private final com.netsensia.rivalchess.engine.core.search.HashProbeResult hashProbe(com.netsensia.rivalchess.engine.core.board.EngineBoard board, int depthRemaining, com.netsensia.rivalchess.engine.core.search.Window window, com.netsensia.rivalchess.engine.core.search.SearchPath bestPath) {
        return null;
    }
    
    private final boolean hashProbeResult(int flag, int score, com.netsensia.rivalchess.engine.core.search.Window window) {
        return false;
    }
    
    private final com.netsensia.rivalchess.engine.core.search.SearchPath finalPath(com.netsensia.rivalchess.engine.core.board.EngineBoard board, int ply, int low, int high, boolean isCheck) {
        return null;
    }
    
    private final boolean abortIfTimeIsUp() {
        return false;
    }
    
    private final boolean performNullMove(com.netsensia.rivalchess.engine.core.board.EngineBoard board, int depthRemaining, boolean isCheck) {
        return false;
    }
    
    private final com.netsensia.rivalchess.engine.core.search.SearchPath searchNullMove(com.netsensia.rivalchess.engine.core.board.EngineBoard board, int depth, int nullMoveReduceDepth, int ply, com.netsensia.rivalchess.engine.core.search.Window window, int extensions) {
        return null;
    }
    
    private final int threatExtensions(com.netsensia.rivalchess.engine.core.search.SearchPath newPath, int extensions) {
        return 0;
    }
    
    private final int checkExtension(int extensions, boolean isCheck) {
        return 0;
    }
    
    private final void adjustScoreForMateDepth(com.netsensia.rivalchess.engine.core.search.SearchPath newPath) {
    }
    
    private final void reorderDepthZeroMoves() {
    }
    
    private final void determineDrawnPositionsAndGenerateDepthZeroMoves() {
    }
    
    private final int[] setPlyMoves(int ply) {
        return null;
    }
    
    private final int[] boardMoves() {
        return null;
    }
    
    private final boolean isBookMoveAvailable() {
        return false;
    }
    
    public final void setHashSizeMB(int hashSizeMB) {
    }
    
    public final void setBoard(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.model.Board board) {
    }
    
    public final void clearHash() {
    }
    
    public final void newGame() {
    }
    
    private final void scoreQuiesceMoves(com.netsensia.rivalchess.engine.core.board.EngineBoard board, int ply, boolean includeChecks) throws com.netsensia.rivalchess.exception.InvalidMoveException {
    }
    
    private final int getHighScoreMove(com.netsensia.rivalchess.engine.core.board.EngineBoard board, int ply, int hashMove) throws com.netsensia.rivalchess.exception.InvalidMoveException {
        return 0;
    }
    
    public final int getHighestScoringMoveFromArray(@org.jetbrains.annotations.NotNull()
    int[] theseMoves) {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.netsensia.rivalchess.engine.core.search.SearchPath quiesce(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.board.EngineBoard board, int depth, int ply, int quiescePly, int low, int high, boolean isCheck) throws com.netsensia.rivalchess.exception.InvalidMoveException {
        return null;
    }
    
    private final void setOrderedMovesArrayForQuiesce(boolean isCheck, int ply, com.netsensia.rivalchess.engine.core.board.EngineBoard board, int quiescePly) {
    }
    
    private final int adjustedSee(int see) {
        return 0;
    }
    
    private final int scoreFullWidthCaptures(com.netsensia.rivalchess.engine.core.board.EngineBoard board, int ply) throws com.netsensia.rivalchess.exception.InvalidMoveException {
        return 0;
    }
    
    private final int scoreMove(int ply, int i, com.netsensia.rivalchess.engine.core.board.EngineBoard board) {
        return 0;
    }
    
    private final int scoreLosingCapturesWithWinningHistory(com.netsensia.rivalchess.engine.core.board.EngineBoard board, int ply, int i, int score, int[] movesForSorting, int toSquare) {
        return 0;
    }
    
    private final int historyScore(boolean isWhite, int from, int to) {
        return 0;
    }
    
    private final void scoreFullWidthMoves(com.netsensia.rivalchess.engine.core.board.EngineBoard board, int ply) {
    }
    
    private final int scorePieceSquareValues(com.netsensia.rivalchess.engine.core.board.EngineBoard board, int fromSquare, int toSquare) {
        return 0;
    }
    
    private final int scoreHistoryHeuristic(com.netsensia.rivalchess.engine.core.board.EngineBoard board, int score, int fromSquare, int toSquare) {
        return 0;
    }
    
    private final int scoreKillerMoves(int ply, int i, int[] movesForSorting) {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.netsensia.rivalchess.model.Colour getMover() {
        return null;
    }
    
    public final void makeMove(int engineMove) throws com.netsensia.rivalchess.exception.InvalidMoveException {
    }
    
    private final void initSearchVariables() {
    }
    
    private final void setupMateAndKillerMoveTables() {
    }
    
    private final void setupHistoryMoveTable() {
    }
    
    public final void setMillisToThink(int millisToThink) {
    }
    
    public final void setNodesToSearch(int nodesToSearch) {
    }
    
    public final void setSearchDepth(int searchDepth) {
    }
    
    public final boolean isSearching() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getCurrentScoreHuman() {
        return null;
    }
    
    public final int getCurrentScore() {
        return 0;
    }
    
    public final long getSearchDuration() {
        return 0L;
    }
    
    public final int getNodesPerSecond() {
        return 0;
    }
    
    private final boolean isDrawnAtRoot(int ply) {
        return false;
    }
    
    public final int getCurrentMove() {
        return 0;
    }
    
    public final void startSearch() {
    }
    
    public final void stopSearch() {
    }
    
    public final void setSearchComplete() {
    }
    
    public final void quit() {
    }
    
    @java.lang.Override()
    public void run() {
    }
    
    public final boolean isOkToSendInfo() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getFen() {
        return null;
    }
    
    public Search(@org.jetbrains.annotations.NotNull()
    java.io.PrintStream printStream, @org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.model.Board board) {
        super();
    }
    
    public Search(@org.jetbrains.annotations.NotNull()
    java.io.PrintStream printStream) {
        super();
    }
    
    public Search() {
        super();
    }
    
    public Search(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.model.Board board) {
        super();
    }
}