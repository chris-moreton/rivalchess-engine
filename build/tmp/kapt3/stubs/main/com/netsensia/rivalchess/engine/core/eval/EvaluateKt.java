package com.netsensia.rivalchess.engine.core.eval;

import java.lang.System;

@kotlin.Metadata(mv = {1, 1, 16}, bv = {1, 0, 3}, k = 2, d1 = {"\u0000Z\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0010\t\n\u0002\b\t\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0018\u0002\n\u0002\b\u0016\n\u0002\u0018\u0002\n\u0002\b\u0011\n\u0002\u0010 \n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b&\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b1\u001a\u0016\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005\u001a\u001e\u0010\u0006\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0007\u001a\u00020\u00012\u0006\u0010\u0004\u001a\u00020\u0005\u001a\u000e\u0010\b\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u0003\u001a\u000e\u0010\t\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u0003\u001a\u000e\u0010\n\u001a\u00020\u000b2\u0006\u0010\u0002\u001a\u00020\u0003\u001a\u000e\u0010\f\u001a\u00020\u000b2\u0006\u0010\u0002\u001a\u00020\u0003\u001a\u0016\u0010\r\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u000e\u001a\u00020\u000f\u001a\u0016\u0010\u0010\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0011\u001a\u00020\u0001\u001a\u000e\u0010\u0012\u001a\u00020\u000b2\u0006\u0010\u0002\u001a\u00020\u0003\u001a\u000e\u0010\u0013\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u0003\u001a\u000e\u0010\u0014\u001a\u00020\u000b2\u0006\u0010\u0002\u001a\u00020\u0003\u001a\u000e\u0010\u0015\u001a\u00020\u000b2\u0006\u0010\u0002\u001a\u00020\u0003\u001a\u000e\u0010\u0016\u001a\u00020\u000b2\u0006\u0010\u0002\u001a\u00020\u0003\u001a\u0010\u0010\u0017\u001a\u00020\u000f2\u0006\u0010\u0018\u001a\u00020\u0019H\u0002\u001a\u000e\u0010\u001a\u001a\u00020\u000b2\u0006\u0010\u0018\u001a\u00020\u0019\u001a\u000e\u0010\u001b\u001a\u00020\u000f2\u0006\u0010\u0018\u001a\u00020\u0019\u001a\u0016\u0010\u001c\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0018\u001a\u00020\u0019\u001a\u0016\u0010\u001d\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0018\u001a\u00020\u0019\u001a\u001e\u0010\u001e\u001a\u00020\u00012\u0006\u0010\u001f\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0018\u001a\u00020\u0019\u001a\u001e\u0010 \u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010!\u001a\u00020\"2\u0006\u0010\u0004\u001a\u00020\u0005\u001a\u000e\u0010#\u001a\u00020\u000b2\u0006\u0010\u0002\u001a\u00020\u0003\u001a\u000e\u0010$\u001a\u00020\u000b2\u0006\u0010\u0002\u001a\u00020\u0003\u001a\u000e\u0010%\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u0003\u001a\u000e\u0010&\u001a\u00020\u000f2\u0006\u0010\u0002\u001a\u00020\u0003\u001a\u0018\u0010\'\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u000e\u001a\u00020\u000fH\u0002\u001a\u0016\u0010(\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010)\u001a\u00020\u0001\u001a\u0018\u0010*\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u000e\u001a\u00020\u000fH\u0002\u001a\u000e\u0010+\u001a\u00020\u00012\u0006\u0010,\u001a\u00020\u0001\u001a\u0016\u0010-\u001a\u00020\u00012\u0006\u0010\u0011\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u0003\u001a\u0016\u0010.\u001a\u00020\u00012\u0006\u0010\u0011\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u0003\u001a\u001e\u0010/\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u001f\u001a\u00020\u00012\u0006\u0010\u0018\u001a\u00020\u0019\u001a\u001e\u00100\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u001f\u001a\u00020\u00012\u0006\u0010\u0018\u001a\u00020\u0019\u001a\u001e\u00101\u001a\u00020\u000f2\u0006\u00102\u001a\u00020\u00012\u0006\u00103\u001a\u00020\u000f2\u0006\u00104\u001a\u00020\u000f\u001a\u001e\u00105\u001a\u00020\u00012\u0006\u00102\u001a\u00020\u00012\u0006\u00103\u001a\u00020\u000f2\u0006\u00104\u001a\u00020\u000f\u001a\u000e\u00106\u001a\u00020\u000b2\u0006\u0010\u0002\u001a\u00020\u0003\u001a>\u00107\u001a\u00020\u00012\u0006\u00108\u001a\u0002092\u0006\u0010:\u001a\u00020\u00012\u0006\u0010;\u001a\u00020\u00012\u0006\u0010\u0004\u001a\u00020\u00052\u0006\u0010<\u001a\u00020\u000f2\u0006\u0010=\u001a\u00020\u000f2\u0006\u0010>\u001a\u000209\u001a\u0016\u0010?\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0011\u001a\u00020\u0001\u001a\u0018\u0010@\u001a\u00020\u00012\u0006\u0010A\u001a\u0002092\u0006\u0010B\u001a\u00020\u0001H\u0002\u001a\u0016\u0010C\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010D\u001a\u00020\u000f\u001a\u0016\u0010E\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010D\u001a\u00020\u000f\u001a\u0018\u0010F\u001a\u00020\u00012\u0006\u0010G\u001a\u00020\u00012\u0006\u0010H\u001a\u00020\u0001H\u0002\u001a\u0014\u0010I\u001a\u00020\u00012\f\u0010J\u001a\b\u0012\u0004\u0012\u00020\u00010K\u001a\u001e\u0010L\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u001f\u001a\u00020\u00012\u0006\u0010\u0018\u001a\u00020\u0019\u001a\u000e\u0010M\u001a\u00020\u00012\u0006\u0010N\u001a\u00020\u0001\u001a\u000e\u0010O\u001a\u00020\u00012\u0006\u0010N\u001a\u00020\u0001\u001a\u000e\u0010P\u001a\u00020\u00012\u0006\u0010Q\u001a\u00020R\u001a\u001c\u0010S\u001a\u00020\u00012\f\u0010T\u001a\b\u0012\u0004\u0012\u00020\u00010K2\u0006\u0010U\u001a\u00020\u0001\u001a\u000e\u0010V\u001a\u00020\u000b2\u0006\u0010\u0011\u001a\u00020\u0001\u001a\u000e\u0010W\u001a\u00020\u000b2\u0006\u0010\u0002\u001a\u00020\u0003\u001a\u001c\u0010X\u001a\u00020\u00012\u0006\u0010Y\u001a\u00020\u000f2\f\u0010!\u001a\b\u0012\u0004\u0012\u00020\u000f0K\u001a&\u0010Z\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010!\u001a\u00020\"2\u0006\u0010Q\u001a\u00020R2\u0006\u0010\u0018\u001a\u00020\u0019\u001a\u0006\u0010[\u001a\u00020\u0001\u001a\u0006\u0010\\\u001a\u00020\u0001\u001a\u001e\u0010]\u001a\u00020\u00012\u0006\u0010^\u001a\u00020\u00012\u0006\u0010[\u001a\u00020\u00012\u0006\u0010\\\u001a\u00020\u0001\u001a.\u0010_\u001a\u00020\u00012\u0006\u0010`\u001a\u00020\u00012\u0006\u0010a\u001a\u00020\u00012\u0006\u0010b\u001a\u00020\u00012\u0006\u0010c\u001a\u00020\u00012\u0006\u0010d\u001a\u00020\u0001\u001a\u000e\u0010e\u001a\u00020\u00012\u0006\u0010\u0004\u001a\u00020\u0005\u001a\u000e\u0010f\u001a\u00020\u00012\u0006\u0010^\u001a\u00020\u0001\u001a\u000e\u0010g\u001a\u00020\u000b2\u0006\u0010\u0002\u001a\u00020\u0003\u001a\u000e\u0010h\u001a\u00020\u000b2\u0006\u0010\u0002\u001a\u00020\u0003\u001a\u000e\u0010i\u001a\u00020\u000b2\u0006\u0010\u0002\u001a\u00020\u0003\u001a\u000e\u0010j\u001a\u00020\u000b2\u0006\u0010\u0002\u001a\u00020\u0003\u001a\u000e\u0010k\u001a\u00020\u000b2\u0006\u0010l\u001a\u00020\u000f\u001a\u0016\u0010m\u001a\u00020\u000f2\u0006\u0010D\u001a\u00020\u000f2\u0006\u0010n\u001a\u00020\u000f\u001a\u000e\u0010o\u001a\u00020\u00012\u0006\u0010m\u001a\u00020\u000f\u001a\u0016\u0010p\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0007\u001a\u00020\u0001\u001a\u0018\u0010q\u001a\u00020\u00012\u0006\u0010A\u001a\u0002092\u0006\u00102\u001a\u00020\u0001H\u0002\u001a>\u0010r\u001a\u00020\u00012\u0006\u0010s\u001a\u00020\u000f2\u0006\u0010t\u001a\u00020\u000f2\u0006\u0010!\u001a\u00020\"2\u0006\u0010\u0004\u001a\u00020\u00052\u0006\u0010:\u001a\u00020\u00012\u0006\u0010;\u001a\u00020\u00012\u0006\u0010>\u001a\u000209\u001a=\u0010u\u001a\u00020\u00012\u0006\u00104\u001a\u00020\u000f2\u0006\u0010v\u001a\u00020\u000f2\u0006\u0010w\u001a\u00020\u000f2\u001d\u0010x\u001a\u0019\u0012\u0004\u0012\u00020\u000f\u0012\u0004\u0012\u00020\u0001\u0012\u0004\u0012\u00020\u000f0y\u00a2\u0006\u0002\bz\u001a\u000e\u0010{\u001a\u00020\u000b2\u0006\u0010\u0002\u001a\u00020\u0003\u001a\u000e\u0010|\u001a\u00020\u000b2\u0006\u0010\u0002\u001a\u00020\u0003\u001a\u000e\u0010}\u001a\u00020\u00012\u0006\u0010~\u001a\u00020\u0001\u001a\u0006\u0010\u007f\u001a\u00020\u0001\u001a\u0019\u0010\u0080\u0001\u001a\u00020\u000b2\u0007\u0010\u0081\u0001\u001a\u00020\u00012\u0007\u0010\u0082\u0001\u001a\u00020\u0001\u001a\u0017\u0010\u0083\u0001\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0007\u001a\u00020\u0001\u001a\u0017\u0010\u0084\u0001\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0007\u001a\u00020\u0001\u001a\u000f\u0010\u0085\u0001\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u0003\u001a\u000f\u0010\u0086\u0001\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u0003\u001a\u000f\u0010\u0087\u0001\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u0003\u001a\u000f\u0010\u0088\u0001\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u0003\u001a\u000f\u0010\u0089\u0001\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u0003\u001a\u000f\u0010\u008a\u0001\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u0003\u001a\u000f\u0010\u008b\u0001\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u0003\u001a\u000f\u0010\u008c\u0001\u001a\u00020\u000b2\u0006\u0010\u0002\u001a\u00020\u0003\u001a\u000f\u0010\u008d\u0001\u001a\u00020\u000b2\u0006\u0010\u0002\u001a\u00020\u0003\u001a\u0018\u0010\u008e\u0001\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0007\u0010\u008f\u0001\u001a\u00020\u000f\u001a\u0017\u0010\u0090\u0001\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0011\u001a\u00020\u0001\u001a\u000f\u0010\u0091\u0001\u001a\u00020\u000b2\u0006\u0010\u0002\u001a\u00020\u0003\u001a\u000f\u0010\u0092\u0001\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u0003\u001a\u000f\u0010\u0093\u0001\u001a\u00020\u000b2\u0006\u0010\u0002\u001a\u00020\u0003\u001a\u000f\u0010\u0094\u0001\u001a\u00020\u000b2\u0006\u0010\u0002\u001a\u00020\u0003\u001a\u000f\u0010\u0095\u0001\u001a\u00020\u000b2\u0006\u0010\u0002\u001a\u00020\u0003\u001a\u0011\u0010\u0096\u0001\u001a\u00020\u000f2\u0006\u0010\u0018\u001a\u00020\u0019H\u0002\u001a\u000f\u0010\u0097\u0001\u001a\u00020\u000b2\u0006\u0010\u0018\u001a\u00020\u0019\u001a\u000f\u0010\u0098\u0001\u001a\u00020\u000f2\u0006\u0010\u0018\u001a\u00020\u0019\u001a\u0017\u0010\u0099\u0001\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0018\u001a\u00020\u0019\u001a\u0017\u0010\u009a\u0001\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0018\u001a\u00020\u0019\u001a\u001f\u0010\u009b\u0001\u001a\u00020\u00012\u0006\u0010\u001f\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0018\u001a\u00020\u0019\u001a\u001f\u0010\u009c\u0001\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010!\u001a\u00020\"2\u0006\u0010\u0004\u001a\u00020\u0005\u001a\u000f\u0010\u009d\u0001\u001a\u00020\u000b2\u0006\u0010\u0002\u001a\u00020\u0003\u001a\u000f\u0010\u009e\u0001\u001a\u00020\u000b2\u0006\u0010\u0002\u001a\u00020\u0003\u001a\u000f\u0010\u009f\u0001\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u0003\u001a\u000f\u0010\u00a0\u0001\u001a\u00020\u000f2\u0006\u0010\u0002\u001a\u00020\u0003\u001a\u001a\u0010\u00a1\u0001\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0007\u0010\u008f\u0001\u001a\u00020\u000fH\u0002\u001a\u0017\u0010\u00a2\u0001\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010)\u001a\u00020\u0001\u001a\u0018\u0010\u00a3\u0001\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0007\u0010\u008f\u0001\u001a\u00020\u000f\u001a\u000f\u0010\u00a4\u0001\u001a\u00020\u00012\u0006\u0010,\u001a\u00020\u0001\u001a\u0017\u0010\u00a5\u0001\u001a\u00020\u00012\u0006\u0010\u0011\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u0003\u001a\u0017\u0010\u00a6\u0001\u001a\u00020\u00012\u0006\u0010\u0011\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u0003\u001a\u001f\u0010\u00a7\u0001\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u001f\u001a\u00020\u00012\u0006\u0010\u0018\u001a\u00020\u0019\u001a\u001f\u0010\u00a8\u0001\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u001f\u001a\u00020\u00012\u0006\u0010\u0018\u001a\u00020\u0019\u001a\u0011\u0010\u00a9\u0001\u001a\u00020\u00012\u0006\u0010H\u001a\u00020\u0001H\u0002\u001a\u0011\u0010\u00aa\u0001\u001a\u00020\u00012\u0006\u0010N\u001a\u00020\u0001H\u0002\u00a8\u0006\u00ab\u0001"}, d2 = {"bishopPairEval", "", "bitboards", "Lcom/netsensia/rivalchess/engine/core/eval/BitboardData;", "materialValues", "Lcom/netsensia/rivalchess/engine/core/eval/MaterialValues;", "bishopScore", "materialDifference", "blackA2TrappedBishopEval", "blackBishopColourCount", "blackBishopDrawOnFileA", "", "blackBishopDrawOnFileH", "blackBishopsEval", "blackPieces", "", "blackCastlingEval", "castlePrivileges", "blackDarkBishopExists", "blackH2TrappedBishopEval", "blackHasInsufficientMaterial", "blackHasOnlyAKnightAndBishop", "blackHasOnlyTwoKnights", "blackKingDangerZone", "kingSquares", "Lcom/netsensia/rivalchess/engine/core/eval/KingSquares;", "blackKingOnFirstTwoRanks", "blackKingShield", "blackKingShieldEval", "blackKingSquareEval", "blackKnightAndBishopVKingEval", "currentScore", "blackKnightsEval", "attacks", "Lcom/netsensia/rivalchess/engine/core/eval/Attacks;", "blackLightBishopExists", "blackMoreThanABishopUpInNonPawns", "blackPawnsEval", "blackPieceBitboard", "blackQueensEval", "blackRookOpenFilesEval", "file", "blackRooksEval", "blackShouldWinWithKnightAndBishopValue", "eval", "blackTimeToCastleKingSide", "blackTimeToCastleQueenSide", "blackWinningEndGameAdjustment", "blackWinningNoWhitePawnsEndGameAdjustment", "blockedKnightLandingSquares", "square", "enemyPawnAttacks", "friendlyPawns", "blockedKnightPenaltyEval", "bothSidesHaveOnlyOneKnightOrBishopEach", "calculateLowMaterialPawnBonus", "lowMaterialColour", "Lcom/netsensia/rivalchess/model/Colour;", "whiteKingSquare", "blackKingSquare", "whitePassedPawnsBitboard", "blackPassedPawnsBitboard", "mover", "castlingEval", "colourAdjustedYRank", "colour", "yRank", "combineBlackKingShieldEval", "kingShield", "combineWhiteKingShieldEval", "difference", "kingX", "it", "doubledRooksEval", "squares", "", "endGameAdjustment", "enemyKingCloseToDarkCornerMateSquareValue", "kingSquare", "enemyKingCloseToLightCornerMateSquareValue", "evaluate", "board", "Lcom/netsensia/rivalchess/engine/core/board/EngineBoard;", "flippedSquareTableScore", "table", "bit", "isAnyCastleAvailable", "isEndGame", "kingAttackCount", "dangerZone", "kingSafetyEval", "kingSquareBonusEndGame", "kingSquareBonusMiddleGame", "kingSquareBonusScaled", "pieceValues", "linearScale", "x", "min", "max", "a", "b", "materialDifferenceEval", "maxCastleValue", "noBlackRooksQueensOrBishops", "noPawnsRemain", "noWhiteRooksQueensOrKnights", "onlyKingsRemain", "onlyOneBitSet", "bitboard", "openFiles", "pawnBitboard", "openFilesKingShieldEval", "oppositeColourBishopsEval", "pawnDistanceFromPromotion", "pawnScore", "whitePawnBitboard", "blackPawnBitboard", "pawnShieldEval", "enemyPawns", "friendlyPawnShield", "shifter", "Lkotlin/Function2;", "Lkotlin/ExtensionFunctionType;", "probableDrawWhenBlackIsWinning", "probablyDrawWhenWhiteIsWinning", "rookEnemyPawnMultiplier", "enemyPawnValues", "rookSquareBonus", "sameFile", "square1", "square2", "tradePawnBonusWhenMoreMaterial", "tradePieceBonusWhenMoreMaterial", "trappedBishopEval", "twoBlackRooksTrappingKingEval", "twoWhiteRooksTrappingKingEval", "uncastledTrappedBlackRookEval", "uncastledTrappedWhiteRookEval", "whiteA7TrappedBishopEval", "whiteBishopColourCount", "whiteBishopDrawOnFileA", "whiteBishopDrawOnFileH", "whiteBishopEval", "whitePieces", "whiteCastlingEval", "whiteDarkBishopExists", "whiteH7TrappedBishopEval", "whiteHasInsufficientMaterial", "whiteHasOnlyAKnightAndBishop", "whiteHasOnlyTwoKnights", "whiteKingDangerZone", "whiteKingOnFirstTwoRanks", "whiteKingShield", "whiteKingShieldEval", "whiteKingSquareEval", "whiteKnightAndBishopVKingEval", "whiteKnightsEval", "whiteLightBishopExists", "whiteMoreThanABishopUpInNonPawns", "whitePawnsEval", "whitePieceBitboard", "whiteQueensEval", "whiteRookOpenFilesEval", "whiteRooksEval", "whiteShouldWinWithKnightAndBishopValue", "whiteTimeToCastleKingSide", "whiteTimeToCastleQueenSide", "whiteWinningEndGameAdjustment", "whiteWinningNoBlackPawnsEndGameAdjustment", "xCoordOfSquare", "yCoordOfSquare", "rivalchess-engine"})
public final class EvaluateKt {
    
    public static final int evaluate(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.board.EngineBoard board) {
        return 0;
    }
    
    public static final int materialDifferenceEval(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.MaterialValues materialValues) {
        return 0;
    }
    
    public static final boolean onlyOneBitSet(long bitboard) {
        return false;
    }
    
    public static final boolean onlyKingsRemain(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.BitboardData bitboards) {
        return false;
    }
    
    public static final int whiteKingSquareEval(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.BitboardData bitboards, @org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.KingSquares kingSquares) {
        return 0;
    }
    
    public static final int blackKingSquareEval(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.BitboardData bitboards, @org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.KingSquares kingSquares) {
        return 0;
    }
    
    public static final int linearScale(int x, int min, int max, int a, int b) {
        return 0;
    }
    
    public static final int twoWhiteRooksTrappingKingEval(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.BitboardData bitboards) {
        return 0;
    }
    
    public static final int twoBlackRooksTrappingKingEval(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.BitboardData bitboards) {
        return 0;
    }
    
    public static final int whiteRookOpenFilesEval(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.BitboardData bitboards, int file) {
        return 0;
    }
    
    public static final int blackRookOpenFilesEval(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.BitboardData bitboards, int file) {
        return 0;
    }
    
    public static final int rookEnemyPawnMultiplier(int enemyPawnValues) {
        return 0;
    }
    
    public static final boolean sameFile(int square1, int square2) {
        return false;
    }
    
    public static final int doubledRooksEval(@org.jetbrains.annotations.NotNull()
    java.util.List<java.lang.Integer> squares) {
        return 0;
    }
    
    public static final int flippedSquareTableScore(@org.jetbrains.annotations.NotNull()
    java.util.List<java.lang.Integer> table, int bit) {
        return 0;
    }
    
    public static final int kingAttackCount(long dangerZone, @org.jetbrains.annotations.NotNull()
    java.util.List<java.lang.Long> attacks) {
        return 0;
    }
    
    public static final int tradePieceBonusWhenMoreMaterial(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.BitboardData bitboards, int materialDifference) {
        return 0;
    }
    
    public static final int tradePawnBonusWhenMoreMaterial(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.BitboardData bitboards, int materialDifference) {
        return 0;
    }
    
    public static final int bishopScore(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.BitboardData bitboards, int materialDifference, @org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.MaterialValues materialValues) {
        return 0;
    }
    
    public static final boolean whiteLightBishopExists(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.BitboardData bitboards) {
        return false;
    }
    
    public static final boolean whiteDarkBishopExists(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.BitboardData bitboards) {
        return false;
    }
    
    public static final boolean blackLightBishopExists(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.BitboardData bitboards) {
        return false;
    }
    
    public static final boolean blackDarkBishopExists(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.BitboardData bitboards) {
        return false;
    }
    
    public static final int whiteBishopColourCount(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.BitboardData bitboards) {
        return 0;
    }
    
    public static final int blackBishopColourCount(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.BitboardData bitboards) {
        return 0;
    }
    
    public static final int oppositeColourBishopsEval(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.BitboardData bitboards, int materialDifference) {
        return 0;
    }
    
    public static final int bishopPairEval(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.BitboardData bitboards, @org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.MaterialValues materialValues) {
        return 0;
    }
    
    public static final int trappedBishopEval(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.BitboardData bitboards) {
        return 0;
    }
    
    public static final int blackH2TrappedBishopEval(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.BitboardData bitboards) {
        return 0;
    }
    
    public static final int blackA2TrappedBishopEval(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.BitboardData bitboards) {
        return 0;
    }
    
    public static final int whiteH7TrappedBishopEval(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.BitboardData bitboards) {
        return 0;
    }
    
    public static final int whiteA7TrappedBishopEval(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.BitboardData bitboards) {
        return 0;
    }
    
    public static final long blackPieceBitboard(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.BitboardData bitboards) {
        return 0L;
    }
    
    public static final long whitePieceBitboard(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.BitboardData bitboards) {
        return 0L;
    }
    
    public static final boolean isEndGame(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.BitboardData bitboards) {
        return false;
    }
    
    public static final int kingSafetyEval(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.BitboardData bitboards, @org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.Attacks attacks, @org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.board.EngineBoard board, @org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.KingSquares kingSquares) {
        return 0;
    }
    
    private static final long blackKingDangerZone(com.netsensia.rivalchess.engine.core.eval.KingSquares kingSquares) {
        return 0L;
    }
    
    private static final long whiteKingDangerZone(com.netsensia.rivalchess.engine.core.eval.KingSquares kingSquares) {
        return 0L;
    }
    
    public static final int uncastledTrappedWhiteRookEval(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.BitboardData bitboards) {
        return 0;
    }
    
    public static final int pawnShieldEval(long friendlyPawns, long enemyPawns, long friendlyPawnShield, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super java.lang.Long, ? super java.lang.Integer, java.lang.Long> shifter) {
        return 0;
    }
    
    public static final int uncastledTrappedBlackRookEval(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.BitboardData bitboards) {
        return 0;
    }
    
    public static final long openFiles(long kingShield, long pawnBitboard) {
        return 0L;
    }
    
    public static final int whiteKingShieldEval(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.BitboardData bitboards, @org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.KingSquares kingSquares) {
        return 0;
    }
    
    public static final int combineWhiteKingShieldEval(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.BitboardData bitboards, long kingShield) {
        return 0;
    }
    
    public static final int openFilesKingShieldEval(long openFiles) {
        return 0;
    }
    
    public static final int blackKingShieldEval(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.BitboardData bitboards, @org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.KingSquares kingSquares) {
        return 0;
    }
    
    public static final int combineBlackKingShieldEval(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.BitboardData bitboards, long kingShield) {
        return 0;
    }
    
    public static final boolean whiteKingOnFirstTwoRanks(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.KingSquares kingSquares) {
        return false;
    }
    
    public static final boolean blackKingOnFirstTwoRanks(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.KingSquares kingSquares) {
        return false;
    }
    
    public static final long blackKingShield(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.KingSquares kingSquares) {
        return 0L;
    }
    
    public static final long whiteKingShield(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.KingSquares kingSquares) {
        return 0L;
    }
    
    public static final int whiteCastlingEval(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.BitboardData bitboards, int castlePrivileges) {
        return 0;
    }
    
    public static final int whiteTimeToCastleQueenSide(int castlePrivileges, @org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.BitboardData bitboards) {
        return 0;
    }
    
    public static final int whiteTimeToCastleKingSide(int castlePrivileges, @org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.BitboardData bitboards) {
        return 0;
    }
    
    public static final int blackTimeToCastleQueenSide(int castlePrivileges, @org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.BitboardData bitboards) {
        return 0;
    }
    
    public static final int blackTimeToCastleKingSide(int castlePrivileges, @org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.BitboardData bitboards) {
        return 0;
    }
    
    public static final int blackCastlingEval(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.BitboardData bitboards, int castlePrivileges) {
        return 0;
    }
    
    public static final int rookSquareBonus() {
        return 0;
    }
    
    public static final int kingSquareBonusEndGame() {
        return 0;
    }
    
    public static final int kingSquareBonusMiddleGame() {
        return 0;
    }
    
    public static final int castlingEval(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.BitboardData bitboards, int castlePrivileges) {
        return 0;
    }
    
    public static final int maxCastleValue(int pieceValues) {
        return 0;
    }
    
    public static final int kingSquareBonusScaled(int pieceValues, int kingSquareBonusEndGame, int kingSquareBonusMiddleGame) {
        return 0;
    }
    
    public static final boolean isAnyCastleAvailable(int castlePrivileges) {
        return false;
    }
    
    public static final int endGameAdjustment(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.BitboardData bitboards, int currentScore, @org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.KingSquares kingSquares) {
        return 0;
    }
    
    public static final int blackWinningEndGameAdjustment(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.BitboardData bitboards, int currentScore, @org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.KingSquares kingSquares) {
        return 0;
    }
    
    public static final int blackWinningNoWhitePawnsEndGameAdjustment(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.BitboardData bitboards, int currentScore, @org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.KingSquares kingSquares) {
        return 0;
    }
    
    public static final int blackKnightAndBishopVKingEval(int currentScore, @org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.BitboardData bitboards, @org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.KingSquares kingSquares) {
        return 0;
    }
    
    public static final int whiteWinningEndGameAdjustment(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.BitboardData bitboards, int currentScore, @org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.KingSquares kingSquares) {
        return 0;
    }
    
    public static final int whiteWinningNoBlackPawnsEndGameAdjustment(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.BitboardData bitboards, int currentScore, @org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.KingSquares kingSquares) {
        return 0;
    }
    
    public static final int whiteKnightAndBishopVKingEval(int currentScore, @org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.BitboardData bitboards, @org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.KingSquares kingSquares) {
        return 0;
    }
    
    public static final int enemyKingCloseToDarkCornerMateSquareValue(int kingSquare) {
        return 0;
    }
    
    public static final int enemyKingCloseToLightCornerMateSquareValue(int kingSquare) {
        return 0;
    }
    
    public static final int blackShouldWinWithKnightAndBishopValue(int eval) {
        return 0;
    }
    
    public static final int whiteShouldWinWithKnightAndBishopValue(int eval) {
        return 0;
    }
    
    public static final boolean whiteHasOnlyAKnightAndBishop(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.BitboardData bitboards) {
        return false;
    }
    
    public static final boolean blackHasOnlyAKnightAndBishop(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.BitboardData bitboards) {
        return false;
    }
    
    public static final boolean whiteHasOnlyTwoKnights(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.BitboardData bitboards) {
        return false;
    }
    
    public static final boolean blackHasOnlyTwoKnights(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.BitboardData bitboards) {
        return false;
    }
    
    public static final boolean blackMoreThanABishopUpInNonPawns(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.BitboardData bitboards) {
        return false;
    }
    
    public static final boolean whiteMoreThanABishopUpInNonPawns(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.BitboardData bitboards) {
        return false;
    }
    
    public static final boolean noBlackRooksQueensOrBishops(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.BitboardData bitboards) {
        return false;
    }
    
    public static final boolean bothSidesHaveOnlyOneKnightOrBishopEach(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.BitboardData bitboards) {
        return false;
    }
    
    public static final boolean noPawnsRemain(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.BitboardData bitboards) {
        return false;
    }
    
    public static final boolean noWhiteRooksQueensOrKnights(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.BitboardData bitboards) {
        return false;
    }
    
    public static final boolean blackBishopDrawOnFileH(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.BitboardData bitboards) {
        return false;
    }
    
    public static final boolean blackBishopDrawOnFileA(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.BitboardData bitboards) {
        return false;
    }
    
    public static final boolean whiteBishopDrawOnFileA(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.BitboardData bitboards) {
        return false;
    }
    
    public static final boolean whiteBishopDrawOnFileH(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.BitboardData bitboards) {
        return false;
    }
    
    public static final boolean probableDrawWhenBlackIsWinning(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.BitboardData bitboards) {
        return false;
    }
    
    public static final boolean probablyDrawWhenWhiteIsWinning(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.BitboardData bitboards) {
        return false;
    }
    
    public static final boolean blackHasInsufficientMaterial(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.BitboardData bitboards) {
        return false;
    }
    
    public static final boolean whiteHasInsufficientMaterial(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.BitboardData bitboards) {
        return false;
    }
    
    public static final int blockedKnightPenaltyEval(int square, long enemyPawnAttacks, long friendlyPawns) {
        return 0;
    }
    
    public static final long blockedKnightLandingSquares(int square, long enemyPawnAttacks, long friendlyPawns) {
        return 0L;
    }
    
    public static final int whitePawnsEval(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.BitboardData bitboards) {
        return 0;
    }
    
    public static final int blackPawnsEval(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.BitboardData bitboards) {
        return 0;
    }
    
    public static final int blackKnightsEval(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.BitboardData bitboards, @org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.Attacks attacks, @org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.MaterialValues materialValues) {
        return 0;
    }
    
    public static final int whiteKnightsEval(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.BitboardData bitboards, @org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.Attacks attacks, @org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.MaterialValues materialValues) {
        return 0;
    }
    
    public static final int blackBishopsEval(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.BitboardData bitboards, long blackPieces) {
        return 0;
    }
    
    public static final int whiteBishopEval(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.BitboardData bitboards, long whitePieces) {
        return 0;
    }
    
    private static final int blackQueensEval(com.netsensia.rivalchess.engine.core.eval.BitboardData bitboards, long blackPieces) {
        return 0;
    }
    
    private static final int whiteQueensEval(com.netsensia.rivalchess.engine.core.eval.BitboardData bitboards, long whitePieces) {
        return 0;
    }
    
    private static final int blackRooksEval(com.netsensia.rivalchess.engine.core.eval.BitboardData bitboards, long blackPieces) {
        return 0;
    }
    
    public static final int whiteRooksEval(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.BitboardData bitboards, long whitePieces) {
        return 0;
    }
    
    public static final int pawnScore(long whitePawnBitboard, long blackPawnBitboard, @org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.Attacks attacks, @org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.MaterialValues materialValues, int whiteKingSquare, int blackKingSquare, @org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.model.Colour mover) {
        return 0;
    }
    
    public static final int calculateLowMaterialPawnBonus(@org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.model.Colour lowMaterialColour, int whiteKingSquare, int blackKingSquare, @org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.engine.core.eval.MaterialValues materialValues, long whitePassedPawnsBitboard, long blackPassedPawnsBitboard, @org.jetbrains.annotations.NotNull()
    com.netsensia.rivalchess.model.Colour mover) {
        return 0;
    }
    
    private static final int colourAdjustedYRank(com.netsensia.rivalchess.model.Colour colour, int yRank) {
        return 0;
    }
    
    private static final int difference(int kingX, int it) {
        return 0;
    }
    
    private static final int pawnDistanceFromPromotion(com.netsensia.rivalchess.model.Colour colour, int square) {
        return 0;
    }
    
    private static final int xCoordOfSquare(int it) {
        return 0;
    }
    
    private static final int yCoordOfSquare(int kingSquare) {
        return 0;
    }
}