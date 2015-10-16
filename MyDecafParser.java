package edu.jmu.decaf;

import java.util.*;


/**
 * Concrete Decaf parser class.
 */
class MyDecafParser extends DecafParser
{
	private DecafParser df = new DecafParser();
	public Queue<Token> tokens = new LinkedList<Token>();
	@Override
    public ASTProgram parse(Queue<Token> tokens) throws InvalidSyntaxException
    {
        return parseProgram(tokens);
    }

    public ASTProgram parseProgram(Queue<Token> tokens)
            throws InvalidSyntaxException
    {		
    	this.tokens.addAll(tokens);

        SourceInfo src = getCurrentSourceInfo(tokens);
        ASTProgram program = new ASTProgram();
        program.setSourceInfo(src);

        // TODO: parse variable and function definitions
        while(!this.tokens.isEmpty())
        {
        	System.out.println("TOKENS.SIZE == " + this.tokens.size());
        	if(df.isNextTokenKeyword(this.tokens, "def"))
        	{
        		program.functions.add(parseFunc());
        	}
        	else
        	{
        		program.variables.add(parseVar());

        	}
        }
        return program;
    }
    
    public ASTVariable parseVar() throws InvalidSyntaxException
    {
    	String name;
    	ASTNode.DataType type;
    	int arrayLen;
    	
		if(df.isNextTokenKeyword(tokens, "int") || (df.isNextTokenKeyword(tokens, "bool"))
				|| (df.isNextTokenKeyword(tokens, "void")))
		{    

			type = parseType();

			name = parseID();

			if (df.isNextTokenSymbol(tokens, ";"))
			{
				df.consumeNextToken(tokens);

				System.out.println("TYPE = " + type);
				System.out.println("NAME = " + name + "\n");

				System.out.println("VARIABLE = " + new ASTVariable(name,type));

				return new ASTVariable(name, type);
				
			} else if (df.isNextTokenSymbol(tokens, "["))
			{
				df.consumeNextToken(tokens);
				if (df.isNextToken(tokens,Token.Type.DEC))
				{
					arrayLen = Integer.parseInt(tokens.peek().text);
					df.consumeNextToken(tokens);
					df.matchSymbol(tokens, "]");
					df.matchSymbol(tokens, ";");
					return new ASTVariable(name, type, arrayLen);
				}
			}
		}		

		throw new InvalidSyntaxException("");

    }
    
    public ASTNode.DataType parseType() throws InvalidSyntaxException
    {
    	if(df.isNextTokenKeyword(tokens, "int"))
    	{
    		df.consumeNextToken(tokens);
    		return ASTNode.DataType.INT;
    	}
    	else if(df.isNextTokenKeyword(tokens, "bool"))
    	{
    		df.consumeNextToken(tokens);

    		return ASTNode.DataType.BOOL;

    	}
    	else if(df.isNextTokenKeyword(tokens, "void"))
    	{
    		df.consumeNextToken(tokens);

    		return ASTNode.DataType.VOID;

    	}
    	throw new InvalidSyntaxException("");
    }
    
    public String parseID() throws InvalidSyntaxException
    {
    	String str = tokens.peek().text;
    	df.consumeNextToken(tokens);
    	return str;
    }

    public ASTFunction parseFunc() throws InvalidSyntaxException
    {
    	String name;
    	ASTBlock block;
    	ASTNode.DataType type;
    	ASTFunction func;
    	ArrayList<ASTFunction.Parameter> params = new ArrayList<ASTFunction.Parameter>();
    	
    	df.matchKeyword(tokens, "def");
    	type = parseType();

    	
    	// check that the next word is and id or throw an error?
    	name = parseID();

    	df.matchSymbol(tokens, "(");

    	
    	if (!tokens.peek().text.equals(")"))
    	{
    		params.add(parseParams());
    		boolean check = true;
    		while(check)
    		{
    			df.matchSymbol(tokens, ",");
    			//df.consumeNextToken(tokens);
    			params.add(parseParams());	
    			if(df.isNextTokenSymbol(tokens, ")"))
    			{
    				check = false;
    			}
    		}
    		
    	}
    	
    	df.matchSymbol(tokens, ")");

    	block = parseBlock();

    	
    	func = new ASTFunction(name, type, block);
    	if (params != null)
    	{
    		func.parameters.addAll(params);	
    	}
    	
    	
    	return func;
    }
    
    public ASTFunction.Parameter parseParams() throws InvalidSyntaxException
    {
    	ASTNode.DataType dt;
    	String name;
    	ASTFunction.Parameter param;
    	
    	name = parseID();
    	dt = parseType();
    	
    	param = new ASTFunction.Parameter(name, dt);
    	return param;
    }

    public ASTBlock parseBlock() throws InvalidSyntaxException
{
	ASTBlock block = new ASTBlock();
	ArrayList<ASTVariable> vars = new ArrayList<ASTVariable>();
	ArrayList<ASTStatement> stmnts = new ArrayList<ASTStatement>();
	
	df.matchSymbol(tokens, "{");
	
	// doesn't ensure that variables are not followed by statements
	while (!df.isNextTokenSymbol(tokens, "}") && !tokens.isEmpty())
	{
		if(df.isNextTokenKeyword(tokens, "int") || (df.isNextTokenKeyword(tokens, "bool"))
				|| (df.isNextTokenKeyword(tokens, "void")))
		{
			vars.add(parseVar());	
		} else
		{
    		stmnts.add(parseStmnts());	
		}
	}
	
	df.matchSymbol(tokens, "}");
	
	block.variables.addAll(vars);
	block.statements.addAll(stmnts);
	
	return block;
}

    public ASTStatement parseStmnts() throws InvalidSyntaxException
    {
    	ASTVoidFunctionCall vfc;
    	String name;
    	// statement = loc OR FunctionCall
    	if (df.isNextToken(tokens, Token.Type.ID))
    	{
    
    		try
    		{
        		vfc = parseVoidFunc();
        		return vfc;

    		}
    		catch(InvalidSyntaxException e)
    		{
    			try
    			{
        			ASTLocation loc = parseLoc();
        			df.matchSymbol(tokens, "=");
        			ASTExpression ex = parseExpr();
        			df.matchSymbol(tokens, ";");
        			return new ASTAssignment(loc, ex);
    			}
        		catch(InvalidSyntaxException ee)
    			{
        			throw new InvalidSyntaxException("");         			
    			}
    		}
    	} 
    	// stmnt -> if | while ( Expr)
    	// CONDITIONAL
    	else if (df.isNextTokenKeyword(tokens, "if"))
    	{
    		return parseIf();
    	} 
    	else if (df.isNextTokenKeyword(tokens, "while"))
    	{
    		return parseWhileLoop();
    	}
    	else if (df.isNextTokenKeyword(tokens, "return"))
    	{
    		return parseReturn();
    		// break;
    		// BREAK STATEMENT
    	} else if (df.isNextTokenKeyword(tokens, "break"))
    	{
    		return parseBreak();
    		// continue;
    		// CONTINUE STATEMENT
    	} else if (df.isNextTokenKeyword(tokens, "continue"))
    	{
    		df.consumeNextToken(tokens);
    		df.matchSymbol(tokens, ";");
    		return new ASTContinue();
    	}
    	throw new InvalidSyntaxException("");
    }
    
    public List<ASTExpression> parseArgList() throws InvalidSyntaxException
    {
    	List<ASTExpression> args = new ArrayList<ASTExpression>();
    	if(df.isNextToken(tokens, Token.Type.ID))
    	{
    		args.add(new ASTLocation(tokens.poll().text));
    		df.consumeNextToken(tokens);
    		
    		while(df.isNextTokenSymbol(tokens, ","))
    		{
        		df.consumeNextToken(tokens);
            	if(df.isNextToken(tokens, Token.Type.ID))
            	{
            		args.add(new ASTLocation(tokens.poll().text));
            		df.consumeNextToken(tokens);
            	}
            	else
            	{
                	throw new InvalidSyntaxException("");

            	}
    		}
    	}
    	else
    	{
        	throw new InvalidSyntaxException("");
    	}
        return args;

    }
    
    public ASTVoidFunctionCall parseVoidFunc() throws InvalidSyntaxException
    { 
    	String name;
    	name = parseID();
		ASTVoidFunctionCall vfc = new ASTVoidFunctionCall(name);
		
			df.matchSymbol(tokens, "(");
			df.matchSymbol(tokens, ")");
			df.matchSymbol(tokens, ";");


			return vfc;

		
    }
    
    public ASTReturn parseReturn() throws InvalidSyntaxException
    {
    	df.matchKeyword(tokens, "return");
    	if(df.isNextTokenSymbol(tokens, ";"))
    	{
    		df.matchSymbol(tokens, ";");
    		return new ASTReturn();
    	}
    	else
    	{
    		ASTExpression ex = parseExpr();
    		df.matchSymbol(tokens, ";");
    		return new ASTReturn(ex);
    		return null;
    	}
    }
    
    public ASTWhileLoop parseWhileLoop() throws InvalidSyntaxException
    {
    	ASTExpression ex;
    	ASTBlock block;
    	
    	df.matchKeyword(tokens,"while");
    	df.matchSymbol(tokens, "(");
    	ex = parseExpr();
    	df.matchSymbol(tokens, ")");
    	block = parseBlock();
    	
    	return new ASTWhileLoop(ex, block);
    }
    
    public ASTLocation parseLoc() throws InvalidSyntaxException
    {
    	if(df.isNextToken(tokens, Token.Type.ID))
    	{
    		String name = tokens.peek().text;
    		df.consumeNextToken(tokens);
    		if(df.isNextTokenSymbol(tokens, "["))
    		{
    			df.matchSymbol(tokens, "[");
    			ASTExpression ex = parseExpr();
    			df.matchSymbol(tokens, "]");
    			return new ASTLocation(name, ex);
    		}
    		else
    		{
    			return new ASTLocation(name);
    		}
    	}
    }
    
    public ASTConditional parseIf() throws InvalidSyntaxException
    {
    	df.matchKeyword(tokens, "if");
    	df.matchSymbol(tokens, "(");
    	ASTExpression ex = parseExpr();
    	df.matchSymbol(tokens, ")");
    	ASTBlock block = parseBlock();
    	if(df.isNextTokenKeyword(tokens, "else"))
    	{
    		df.matchKeyword(tokens, "else");
    		ASTBlock elseBlock = parseBlock();
    		return new ASTConditional(ex, block, elseBlock);
    	}
    	else
    	{
        	return new ASTConditional(ex, block);

    	}
    }

    public ASTBreak parseBreak() throws InvalidSyntaxException
    {
		df.matchKeyword(tokens, "break");
		df.matchSymbol(tokens, ";");
		return new ASTBreak();
    }

    public ASTContinue parseContinue() throws InvalidSyntaxException
    {
		df.matchKeyword(tokens, "continue");
		df.matchSymbol(tokens, ";");
		return new ASTContinue();
    }

}
