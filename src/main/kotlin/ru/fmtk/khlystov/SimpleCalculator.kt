package ru.fmtk.khlystov

import java.util.*

fun main() {
    val sc = Scanner(System.`in`)
    println(eval(Tokenizer.tokenize(sc.nextLine())))
}

interface Expr
data class Num(val value: Int) : Expr
data class Sum(val left: Expr, val right: Expr) : Expr
data class Sub(val left: Expr, val right: Expr) : Expr
data class Mul(val left: Expr, val right: Expr) : Expr

fun eval(expr: Expr): Int = when (expr) {
    is Num -> expr.value
    is Sum -> (eval(expr.left) + eval(expr.right))
    is Sub -> (eval(expr.left) - eval(expr.right))
    is Mul -> (eval(expr.left) * eval(expr.right))
    else -> throw IllegalArgumentException("Unknown expression")
}

typealias ExprCreator = (Expr, Expr) -> Expr

class Tokenizer private constructor() {
    private var state: TokenizerState = TokenizerState.FIRST
    private var expr1: Expr = Num(0)
    private var op1: String = ""
    private var expr2: Expr = Num(0)
    private var op2: String = ""

    companion object {
        fun tokenize(s: String): Expr {
            val tokenizer = Tokenizer()
            s.split(" ").forEach(tokenizer::processSubstring)
            tokenizer.endProcess()
            return tokenizer.expr1
        }
    }

    private fun processSubstring(c: String) {
        if (state == TokenizerState.STOP) {
            return
        }
        if (state == TokenizerState.FIRST) {
            expr1 = Num(c.toInt())
            state = TokenizerState.OPERATION1
        } else if (state == TokenizerState.OPERATION1) {
            op1 = c
            state = TokenizerState.NEXT_NUM1
        } else if (state == TokenizerState.NEXT_NUM1) {
            expr2 = Num(c.toInt())
            if (op1 == "+" || op1 == "-") {
                state =
                    TokenizerState.OPERATION2
            } else if (op1 == "*") {
                expr1 = Mul(expr1, expr2)
                state =
                    TokenizerState.OPERATION1
            } else {
                throw IllegalArgumentException("Unknown operation $op1")
            }
        } else if (state == TokenizerState.OPERATION2) {
            op2 = c
            state = TokenizerState.NEXT_NUM2
        } else if (state == TokenizerState.NEXT_NUM2) {
            val num = Num(c.toInt())
            if (op2 == "*") {
                expr2 = Mul(expr2, num)
                state =
                    TokenizerState.OPERATION2
            } else if (op2 == "+" || op2 == "-") {
                foldOperation1()
                op1 = op2
                expr2 = num
                state = TokenizerState.OPERATION2
            } else {
                throw IllegalArgumentException("Unknown operation $op2")
            }
        }
    }

    private fun foldOperation1() {
        val ctor = op1.toExprCreator()
        expr1 = ctor(expr1, expr2)
    }

    private fun endProcess() {
        if (state != TokenizerState.STOP) {
            if (state == TokenizerState.OPERATION2) {
                foldOperation1()
            } else if (state != TokenizerState.OPERATION1) {
                throw IllegalArgumentException("Bad end of expression")
            }
            state = TokenizerState.STOP
        }
    }

    enum class TokenizerState { FIRST, OPERATION1, NEXT_NUM1, OPERATION2, NEXT_NUM2, STOP }

    private fun String.toExprCreator(): ExprCreator = when (this) {
        "+" -> ::Sum
        "-" -> ::Sub
        "*" -> ::Mul
        else -> throw IllegalArgumentException("Unknown operation $this")
    }
}
