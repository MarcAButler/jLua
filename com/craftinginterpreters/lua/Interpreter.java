package com.craftinginterpreters.lua;

import java.util.List;
import java.util.Scanner;

class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Object> 
{
    private Environment globalEnvironment = new Environment();
    private Environment environment = globalEnvironment;

    // Handle Input
    // Create a scanner object
    // Note that this scanner is from the standard Java library
    // not our scanner
    Scanner inputScanner = new Scanner(System.in);

    void interpret(List<Stmt> statements)
    {
        try
        {
           for (Stmt statement : statements)
           {
               execute(statement);
           }
        }
        catch (RuntimeError error)
        {
            Lua.runtimeError(error);
        }
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr)
    {
        return expr.value;
    }

    @Override
    public Object visitLogicalExpr(Expr.Logical expr)
    {
        Object left = evaluate(expr.left);

        if (expr.operator.type == TokenType.OR)
        {
            if (isTruthy(left)) return left;
        }
        else
        {
            if (!isTruthy(left)) return left;
        }

        return evaluate(expr.right);
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr)
    {
        return evaluate(expr.expression);
    }

    private Object evaluate(Expr expr)
    {
        return expr.accept(this);
    }

    private void execute(Stmt stmt)
    {
        stmt.accept(this);
    }

    private void executeBlock(List<Stmt> statements, Environment environment)
    {
        Environment previous = this.environment;
        try
        {
            this.environment = environment;

            for (Stmt statement : statements)
            {
                // If a variable is a local variable, then the current environment
                // is set to the scope of the local variable
                // [!] if (statement)

                execute(statement);
            }
        }
        finally
        {
            this.environment = previous;
        }
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt)
    {
        executeBlock(stmt.statements, new Environment(environment));
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt)
    {
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt)
    {
        if (isTruthy(evaluate(stmt.condition)))
        {
            execute(stmt.thenBranch);
        }
        else if (stmt.elseBranch != null)
        {
            execute(stmt.elseBranch);
        }
        return null;
    }

    @Override
    public String visitInputExpr(Expr.Input expr)
    {
        // Read user input
        String input = inputScanner.nextLine();
        return input;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt)
    {
        Object value = evaluate(stmt.expression);
        System.out.println(stringify(value));
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt)
    {
        Object value = null;
        if (stmt.initializer != null)
        {
            value = evaluate(stmt.initializer);
        }

        globalEnvironment.define(stmt.name.lexeme, value);
        return null;
    }

    @Override
    public Void visitLocalVarStmt(Stmt.LocalVar stmt)
    {
        Object value = null;
        if (stmt.initializer != null)
        {
            value = evaluate(stmt.initializer);
        }

        environment.define(stmt.name.lexeme, value);
        return null;
    }

    @Override
    public Void visitRepeatStmt(Stmt.Repeat stmt)
    {
        do
        {
            execute(stmt.body);
        }
        // Until is the opposite of while so in order to make while until we must invert with a !
        while(!isTruthy(evaluate(stmt.condition)));

        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt)
    {
        while(isTruthy(evaluate(stmt.condition)))
        {
            execute(stmt.body);
        }
        return null;
    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr)
    {
        Object value = evaluate(expr.value);
        environment.assign(expr.name, value);
        return value;
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr)
    {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.operator.type)
        {
            case GREATER:
                checkNumberOperands(expr.operator, left, right);
                return (double)left > (double)right;
            case GREATER_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double)left >= (double)right;
            case LESS:
                checkNumberOperands(expr.operator, left, right);
                return (double)left < (double)right;
            case LESS_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double)left <= (double)right;
            case TILDE_EQUAL:
                return !isEqual(left, right);
            case EQUAL_EQUAL:
                return isEqual(left, right);
            case HYPHEN:
                checkNumberOperands(expr.operator, left, right);
                return (double)left - (double)right;
            case PLUS:
                checkNumberOperands(expr.operator, left, right);
                return (double)left + (double)right;
            case SLASH:
                checkNumberOperands(expr.operator, left, right);
                return (double)left / (double)right;
            case STAR:
                checkNumberOperands(expr.operator, left, right);
                return (double)left * (double)right;
            case EXPONENT:
                checkNumberOperands(expr.operator, left, right);
                return Math.pow((double)left, (double)right);
            // For concatenation
            case DOT_DOT:
                if (left instanceof String && right instanceof String)
                    return (String)left + (String)right;
                throw new RuntimeError(
                    expr.operator, "Operands must be two numbers or two strings.");
        }

        // Unreachable
        return null;
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr)
    {
        Object right = evaluate(expr.right);

        switch (expr.operator.type)
        {
            case TILDE:
                return !isTruthy(right);
            case HYPHEN:
                checkNumberOperand(expr.operator, right);
                return -(double)right;
        }

        // Unreachable
        return null;
    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr)
    {
        return environment.get(expr.name);
    }

    private void checkNumberOperand(Token operator, Object operand)
    {
        if (operand instanceof Double) return;
        throw new RuntimeError(operator, "Operand must be a number.");
    }

    private void checkNumberOperands(Token operator, Object left, Object right)
    {
        if (left instanceof Double && right instanceof Double) return;

        throw new RuntimeError(operator, "Operand must be numbers");
    }

    private boolean isTruthy(Object object)
    {
        if (object == null) return false;
        if (object instanceof Boolean) return (boolean)object;
        return true;
    }

    private boolean isEqual(Object a, Object b)
    {
        if (a == null && b == null) return true;
        if (a == null) return false;

        return a.equals(b);
    }

    private String stringify(Object object)
    {
        if (object == null) return "nil";

        if (object instanceof Double)
        {
            String text = object.toString();
            if (text.endsWith(".0"))
            {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }
        return object.toString();
    }
}
