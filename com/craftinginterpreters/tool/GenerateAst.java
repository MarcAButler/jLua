package com.craftinginterpreters.tool;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

public class GenerateAst
{
    public static void main(String[] args) throws IOException
    {
        if (args.length != 1)
        {
            System.err.println("Usage: generate_ast <output directory>");
            System.exit(64);
        }
        String outputDir = args[0];

        defineAst(outputDir, "Expr", Arrays.asList
        (
            "Assign : Token name, Expr value",
            "Binary : Expr left, Token operator, Expr right",
            "Grouping : Expr expression",
            "Literal : Object value",
            "Logical : Expr left, Token operator, Expr right",
            "Unary : Token operator, Expr right",
            "Variable : Token name",
            // Put input expr
            "Input : "
        ));

        defineAst(outputDir, "Stmt", Arrays.asList(
            "Block        : List<Stmt> statements",
                  "Expression   : Expr expression",
                  "If           : Expr condition, Stmt thenBranch," +
                                " Stmt elseBranch",
                  "Print        : Expr expression",
                  "Var          : Token name, Expr initializer",
                  "LocalVar     : Token name, Expr initializer",
                  "Repeat       : Stmt body, Expr condition",
                  "While        : Expr condition, Stmt body"
        ));
    }

    private static void defineAst(
        String outputDir, String baseName, List<String> types)
        throws IOException
    {
        String path = outputDir + "/" + baseName + ".java";
        PrintWriter writer = new PrintWriter(path, "UTF-8");

        writer.println("package com.craftinginterpreters.lua;");
        writer.println();
        writer.println("import java.util.List;");
        writer.println();
        writer.println("abstract class " + baseName);
        writer.println("{");

        defineVisitor(writer, baseName, types);

        // The AST classes

        for (String type : types)
        {
            String className = type.split(":")[0].trim();
            String fields = type.split(":")[1].trim();
            defineType(writer, baseName, className, fields);
        }

        // The base accept method
        writer.println();
        writer.println("  abstract <R> R accept(Visitor<R> visitor);");

        writer.println("}");
        writer.close();
    }

    private static void defineVisitor(
        PrintWriter writer, String baseName, List<String> types)
    {
        writer.println("  interface Visitor<R>");
        writer.println("  {");
        for (String type : types)
        {
            String typeName = type.split(":")[0].trim();
            writer.println("    R visit" + typeName + baseName + "(" + 
                typeName + " " + baseName.toLowerCase() + ");");
        }

        writer.print("  }");
    }

    private static void defineType(
        PrintWriter writer, String baseName,
        String className, String fieldList)
    {
        writer.println("  static class " + className + " extends " + baseName);
        writer.println("  {");

        // Constructor
        writer.println("    " + className + "(" + fieldList + ")");
        writer.println("    {");

        // Store parameters in the fields
        String[] fields = fieldList.split(", ");

        // If no parameters / empty string we do not split nor loop
        if (fieldList.length() != 0) 
        {

            for (String field : fields)
            {
                String name = field.split(" ")[1];
                writer.println("      this." + name + " = " + name + ";");
            }
        }

        writer.println("    }");

        // Visitor pattern
        writer.println();
        writer.println("    @Override");
        writer.println("    <R> R accept(Visitor<R> visitor)");
        writer.println("    {");
        writer.println("      return visitor.visit" + className + baseName + "(this);");
        writer.println("    }");
        
        if (fieldList.length() != 0)
        {
            // Fields
            writer.println();
            for (String field : fields)
            {
                writer.println("    final " + field + ";");
            }
        }

        writer.println("  }");

    }
}
