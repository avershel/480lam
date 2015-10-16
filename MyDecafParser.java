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
    	if (df.isNextToken(tokens, Token.Type.ID))
    	{
    
    		try
    		{
    	    	ASTVoidFunctionCall vfc = parseVoidFunc();
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

    	} else if (df.isNextTokenKeyword(tokens, "break"))
    	{
    		return parseBreak();

    	} else if (df.isNextTokenKeyword(tokens, "continue"))
    	{
    		return parseContinue();
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
    		//return null;
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
    		String name = tokens.poll().text;
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
    	throw new InvalidSyntaxException("");
    }
    
    public ASTConditional parseIf() throws InvalidSyntaxException
    {
    	df.matchKeyword(tokens, "if");

        ASTExpression ex = parseExpr();
        System.out.println("EX IN IF ====> " + ex);

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

    public ASTLiteral parseLit() throws InvalidSyntaxException
    {
    	String str = "";
    	if(df.isNextToken(tokens, Token.Type.DEC) || df.isNextToken(tokens, Token.Type.HEX))
    	{
    		str = tokens.peek().text;
    		df.consumeNextToken(tokens);
    		return new ASTLiteral(ASTNode.DataType.INT, str);
    	}
    	else if(df.isNextToken(tokens, Token.Type.STR))
    	{
    		str = tokens.peek().text;
    		df.consumeNextToken(tokens);
    		return new ASTLiteral(ASTNode.DataType.STR, str);
    	}
    	else     	if(df.isNextTokenKeyword(tokens, "true") || df.isNextTokenKeyword(tokens, "false"))
    	{
    		str = tokens.peek().text;
    		df.consumeNextToken(tokens);
    		return new ASTLiteral(ASTNode.DataType.BOOL, str);
    	}
    	else
    	{
    		throw new InvalidSyntaxException("");
    	}
    }

    public ASTExpression parseExpr() throws InvalidSyntaxException
    {
    	ASTExpression ex;
    	String name;
    	if (df.isNextToken(tokens, Token.Type.ID))
    	{
    		name = tokens.poll().text;
    		//df.consumeNextToken(tokens);
    		// FUNC CALL
        	if(df.isNextTokenSymbol(tokens, ")"))
        	{
        		return new ASTLocation(name);
        	}
    		if (df.isNextTokenSymbol(tokens, "("))
    		{

    			ASTFunctionCall f = new ASTFunctionCall(name);

    			df.matchSymbol(tokens, "(");
    			if (!df.isNextTokenSymbol(tokens, ")"))
    			{
    				f.arguments.addAll(parseArgList());
    				df.matchSymbol(tokens, ")");
    				ex = f;
    			}
    			else 
    			{
    				// VOID FUNC CALL
    				df.matchSymbol(tokens, ")");
    				ex = f;
    			}
    			return ex;
    		} 
    		// LOCATION
    		else 
    		{

    			return parseLoc();
    		}
    		// LITERAL
    	} else if (df.isNextToken(tokens, Token.Type.DEC) || df.isNextToken(tokens, Token.Type.HEX) 
    			|| df.isNextToken(tokens, Token.Type.STR) || df.isNextTokenKeyword(tokens, "true")
    			|| df.isNextTokenKeyword(tokens, "false"))
    	{    		
    		return parseLit();
    	} 
    	// UNARY EXPRESSION
    	else if (df.isNextTokenSymbol(tokens, "!") || (df.isNextTokenSymbol(tokens, "-")))
    	{
    		try
    		{
    			System.out.println("in UNARY - EXP");
        		return parseUnaryExpr();

    		}
    		catch(InvalidSyntaxException e)
    		{
    			System.out.println("ERROR In unary(parse expr)");
    		}
    		//return parseUnaryExpr();
    	}
    	// (EXPRESSION)
    	else if (df.isNextTokenSymbol(tokens, "("))
    	{
    		System.out.println("IN EXPRESSION - EXP");
    		df.matchSymbol(tokens, "(");
    		System.out.println("matched (");
    		ex = parseExpr();
    		df.matchSymbol(tokens, ")");
    		System.out.println("matched )");
    		System.out.println("EX => " + ex);
    		return ex;
    	}
    	else if (df.isNextTokenSymbol(tokens, ")"))
    	{
    		System.out.println("IN EXPRESSION - EXP - CLOSED PAR");
    		df.matchSymbol(tokens, "(");
    		ex = parseExpr();
    		df.matchSymbol(tokens, ")");
    		System.out.println("U EX => " + ex);

    		return ex;
    	}

//    	
//    	if (df.isNextToken(tokens, Token.Type.SYM) && (!df.isNextTokenSymbol(tokens, ";")))
//    	{
//    		return parseBinaryExpr(tokens, ex);
//    	}
    	throw new InvalidSyntaxException("");
    }

    public ASTUnaryExpr parseUnaryExpr() throws InvalidSyntaxException
    {
    	if (df.isNextTokenSymbol(tokens, "!"))
    	{
    		df.matchSymbol(tokens, "!");

        	ASTExpression ex = parseExpr();

    		//ASTExpression ex = parseExpr();
    		return new ASTUnaryExpr(ASTUnaryExpr.UnaryOp.NOT, ex);
    	} else if (df.isNextTokenSymbol(tokens, "-"))
    		
    	{
    		df.matchSymbol(tokens, "-");

    		return new ASTUnaryExpr(ASTUnaryExpr.UnaryOp.NEG, parseExpr());
    	}
    	else
    	{
    		throw new InvalidSyntaxException("");
    	}
    }


}
