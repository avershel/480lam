package edu.jmu.decaf;

import java.util.*;

/**
 * @author Samantha Carswell & Austin Vershel
 * @version 8-16-15
 * 
 * Concrete Decaf parser class.
 * 
 * This code complies with the JMU honor code.
 */
class MyDecafParser extends DecafParser
{
	private DecafParser df = new DecafParser();
	public Queue<Token> tokens = new LinkedList<Token>();
	@Override
	/*
	 * Parses queue of tokens and returns new ASTProgram object.
	 * 
	 * @param	tokens is Queue of tokenized input
	 * @throws 	InvalidSyntaxException
	 * @return 	ASTProgram is AST representation of tokens
	 */
    public ASTProgram parse(Queue<Token> tokens) 
    		throws InvalidSyntaxException
    {
        return parseProgram(tokens);
    }

	/*
	 * Parses Token Queue for function and variable statements
	 * and sets it to ASTProgram object.
	 * 
	 * @param	tokens is Queue of tokenized input
	 * @throws	InvalidSyntaxException
	 * @return	ASTProgram that is AST represenation of tokens
	 */
    public ASTProgram parseProgram(Queue<Token> tokens)
            throws InvalidSyntaxException
    {		
    	this.tokens.addAll(tokens);

        SourceInfo src = getCurrentSourceInfo(tokens);
        ASTProgram program = new ASTProgram();
        program.setSourceInfo(src);

        // 
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

    /*
     * Parses for Argument List non-terminal.
     * 
     * @throws	InvalidSyntaxException
     * @return	ASTExpression is discovered argumentList
     */
    public List<ASTExpression> parseArgList() 
    		throws InvalidSyntaxException
    {
    	List<ASTExpression> args = new ArrayList<ASTExpression>();
    	
    	args.add(parseExpr());
    	
    	// check for multiple parameters
    	while(df.isNextTokenSymbol(tokens, ","))
    	{
    		df.matchSymbol(tokens, ",");
    		args.add(parseExpr());
    	}
        return args;
    }

    /*
     * Parses for binary expression non-terminal.
     * 
     * @param	ex is ASTExpression that is left child in binary expression	
     * @param	index is identifier for operand type
     * @throws	InvalidSyntaxException
     * @return	ASTBinaryExpr with corresponding attributes
     */
    public ASTBinaryExpr parseBinaryExpr(ASTExpression ex, int index) 
    		throws InvalidSyntaxException
    {
    	// use index to find BinOp enum type
    	switch(index){
    	case 0:
    		df.matchSymbol(tokens, "*");
    		return new ASTBinaryExpr(ASTBinaryExpr.BinOp.MUL, ex, parseExpr()); 
    	case 1:
    		df.matchSymbol(tokens, "/");
    		return new ASTBinaryExpr(ASTBinaryExpr.BinOp.DIV, ex, parseExpr()); 
    	case 2:
    		df.matchSymbol(tokens, "%");
    		return new ASTBinaryExpr(ASTBinaryExpr.BinOp.MOD, ex, parseExpr()); 
    	case 3:
    		df.matchSymbol(tokens, "+");
    		return new ASTBinaryExpr(ASTBinaryExpr.BinOp.ADD, ex, parseExpr()); 
    	case 4:
    		df.matchSymbol(tokens, "-");
    		return new ASTBinaryExpr(ASTBinaryExpr.BinOp.SUB, ex, parseExpr()); 
    	case 5:
    		df.matchSymbol(tokens, "<");
    		return new ASTBinaryExpr(ASTBinaryExpr.BinOp.LT, ex, parseExpr()); 
    	case 6:
    		df.matchSymbol(tokens, "<=");
    		return new ASTBinaryExpr(ASTBinaryExpr.BinOp.LE, ex, parseExpr()); 
    	case 7:
    		df.matchSymbol(tokens, ">=");
    		return new ASTBinaryExpr(ASTBinaryExpr.BinOp.GE, ex, parseExpr()); 
    	case 8:
			df.matchSymbol(tokens, ">");
			return new ASTBinaryExpr(ASTBinaryExpr.BinOp.GT, ex, parseExpr()); 
    	case 9:
    		df.matchSymbol(tokens, "==");
    		return new ASTBinaryExpr(ASTBinaryExpr.BinOp.EQ, ex, parseExpr()); 
    	case 10:
    		df.matchSymbol(tokens, "!=");
    		return new ASTBinaryExpr(ASTBinaryExpr.BinOp.NE, ex, parseExpr()); 
    	case 11:
    		df.matchSymbol(tokens, "&&");
    		return new ASTBinaryExpr(ASTBinaryExpr.BinOp.AND, ex, parseExpr()); 
    	case 12:
    		df.matchSymbol(tokens, "||");
    		return new ASTBinaryExpr(ASTBinaryExpr.BinOp.OR, ex, parseExpr()); 
    	default:
    		throw new InvalidSyntaxException("Invalid Binary Operator");
    	} 	
    }

    /*
     * Parses for block non-terminal.
     * 
     * @throws	InvalidSyntaxException
     * @return	ASTBlock with appropriate attributes
     */
    public ASTBlock parseBlock()
    		throws InvalidSyntaxException
{
	ASTBlock block = new ASTBlock();
	ArrayList<ASTVariable> vars = new ArrayList<ASTVariable>();
	ArrayList<ASTStatement> stmnts = new ArrayList<ASTStatement>();
	
	df.matchSymbol(tokens, "{");
	
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

    /*
     * Parses for break terminal.
     * 
     * @throws	InvalidSyntaxException
     * @return	ASTBreak with appropriate attributes
     */
    public ASTBreak parseBreak() 
    		throws InvalidSyntaxException
    {
    	ASTBreak b = new ASTBreak();
    	b.setSourceInfo(tokens.peek().source);
		df.matchKeyword(tokens, "break");
		df.matchSymbol(tokens, ";");
		return b;
    }
 
    /*
     * Checks if next token after Expression non-terminal is
     * a binary operand and returns index if found operand.
     * 
     * @param	t is Token being checked
     * @throws	InvalidSyntaxException
     * @return	int value that represents index of found operand
     */
    public int checkForBin(Token t) 
    		throws InvalidSyntaxException
    {
    	int i = 0;
    	String[] binOps = new String[13];
    	
    	binOps[i] = "*";
    	binOps[i+1] = "/";
    	binOps[i+2] = "%";
    	binOps[i+3] = "+";
    	binOps[i+4] = "-";
    	binOps[i+5] = "<";
    	binOps[i+6] = "<=";
    	binOps[i+7] = ">=";
    	binOps[i+8] = ">";
    	binOps[i+9] = "==";
    	binOps[i+10] = "!=";
    	binOps[i+11] = "&&";
    	binOps[i+12] = "||";
    	
    	for (int j = 0; j < binOps.length; j++)
    	{
    		if (tokens.peek().text.equals(binOps[j]))
    		{
    			return j;
    		}
    	}
    	return -1;
    }

    /*
     * Parses for Continue terminal.
     * 
     * @throws	InvalidSyntaException
     * @return	ASTContinue with appropriate attributes
     */
    public ASTContinue parseContinue() 
    		throws InvalidSyntaxException
    {
    	ASTContinue c = new ASTContinue();
    	c.setSourceInfo(tokens.peek().source);
		df.matchKeyword(tokens, "continue");
		df.matchSymbol(tokens, ";");
		return c;
    }
    
    /*
     * Parses for Expression non-terminal.
     * 
     * @throws	InvalidTokenException
     * @return	ASTExpression with appropriate attributes
     */
    public ASTExpression parseExpr() 
    		throws InvalidSyntaxException
    {
    	boolean isBinEx = false;
    	ASTExpression ex;
    	String name;
    	// FUNC CALL | LOC
    	if (df.isNextToken(tokens, Token.Type.ID))
    	{
    		SourceInfo s = tokens.peek().source;
    		name = tokens.poll().text;
        	if(df.isNextTokenSymbol(tokens, ")"))
        	{
        		ex = new ASTLocation(name);
        		// set source info
        		ex.setSourceInfo(s);
        	}
        	else if (df.isNextTokenSymbol(tokens, "("))
    		{
    			ASTFunctionCall f = new ASTFunctionCall(name);
    			df.matchSymbol(tokens, "(");
    			// FUNC CALL WITH ARGS
    			if (!df.isNextTokenSymbol(tokens, ")"))
    			{
    				f.arguments.addAll(parseArgList());
    				df.matchSymbol(tokens, ")");
    				ex = f;
    				// set source info
    				f.setSourceInfo(s);
    			}
    			else 
    			{	// FUNC CALL WITHOUT ARGS
    				df.matchSymbol(tokens, ")");
    				ex = f;
    				// set source info
    				f.setSourceInfo(s);
    			}
    		} 
    		else if(df.isNextTokenSymbol(tokens, ";"))
    		{
    			ex = new ASTLocation(name);
    			ex.setSourceInfo(s);
    		}
    		// LOCATION
    		else 
    		{
    			ex = parseLoc(name);
    			ex.setSourceInfo(s);
   
    		}
        	if (checkForBin(tokens.peek()) != -1)
        	{
        			isBinEx = true;
        	}
    	// LITERAL
    	} else if (df.isNextToken(tokens, Token.Type.DEC) || df.isNextToken(tokens, Token.Type.HEX) 
    			|| df.isNextToken(tokens, Token.Type.STR) || df.isNextTokenKeyword(tokens, "true")
    			|| df.isNextTokenKeyword(tokens, "false"))
    	{   
    		SourceInfo s = tokens.peek().source;
    		ex = parseLit();
    		ex.setSourceInfo(s);
        	if (checkForBin(tokens.peek()) != -1)
        	{
        			isBinEx = true;
        	}

    	} 
    	// UNARY EXPRESSION
    	else if (df.isNextTokenSymbol(tokens, "!") || (df.isNextTokenSymbol(tokens, "-")))
    	{
    		SourceInfo s = tokens.peek().source;
        	ex = parseUnaryExpr();
        	ex.setSourceInfo(s);
            if (checkForBin(tokens.peek()) != -1)
            {
            		isBinEx = true;
            }

    	}
    	// (EXPRESSION)
    	else if (df.isNextTokenSymbol(tokens, "("))
    	{
    		SourceInfo s = tokens.peek().source;
    		df.matchSymbol(tokens, "(");
    		ex = parseExpr();
    		ex.setSourceInfo(s);
    		df.matchSymbol(tokens, ")");
        	if (checkForBin(tokens.peek()) != -1)
        	{
        		isBinEx = true;
        	}

    	}
    	else if (df.isNextTokenSymbol(tokens, ")"))
    	{
    		SourceInfo s = tokens.peek().source;
    		df.matchSymbol(tokens, "(");
    		ex = parseExpr();
    		ex.setSourceInfo(s);
    		df.matchSymbol(tokens, ")");
        	if (checkForBin(tokens.peek()) != -1)
        	{
        			isBinEx = true;
        	}

    	} else 
    	{
        	throw new InvalidSyntaxException("Invalid Expression");
    	}
    	//BINARY EXPRESSION
    	if(isBinEx)
    	{
    		ex = parseBinaryExpr(ex, checkForBin(tokens.peek()));
    	}
    	return ex;
    }

    /*
     * Parses for Function Call non-terminal.
     * 
     * @throws	InvalidTokenException
     * @return	ASTFunction with appropriate attributes
     */
    public ASTFunction parseFunc() 
    		throws InvalidSyntaxException
    {
    	String name;
    	ASTBlock block;
    	ASTNode.DataType type;
    	ASTFunction func;
    	ArrayList<ASTFunction.Parameter> params = new ArrayList<ASTFunction.Parameter>();
    	SourceInfo s = tokens.peek().source;
    	df.matchKeyword(tokens, "def");
    	type = parseType();

    	name = parseID();

    	
    	df.matchSymbol(tokens, "(");
    	
    	if (!tokens.peek().text.equals(")"))
    	{

    		params.add(parseParams());
    		boolean check = !(df.isNextTokenSymbol(tokens, ")"));
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
    	func.setSourceInfo(s);
    	if (params != null)
    	{
    		func.parameters.addAll(params);	
    	}
 
    	return func;
    }

    /*
     * Parses for ID non-terminal.
     * 
     * @throws	InvalidSyntaxException
     * @return	String is name of ID non-terminal
     */
    public String parseID() 
    		throws InvalidSyntaxException
    {
    	String str = tokens.peek().text;
    	df.consumeNextToken(tokens);
    	return str;
    }

    /*
     * Parses for If Conditional and If-else conditional.
     * 
     * @throws	InvalidSyntaxException
     * @return	ASTConditional with appropriate attributes
     */
    public ASTConditional parseIf()
    		throws InvalidSyntaxException
    {
    	ASTConditional ac;
    	SourceInfo s = tokens.peek().source;
    	df.matchKeyword(tokens, "if");
        ASTExpression ex = parseExpr();
    	ASTBlock block = parseBlock();
    	
    	if(df.isNextTokenKeyword(tokens, "else"))
    	{
    		df.matchKeyword(tokens, "else");
    		ASTBlock elseBlock = parseBlock();
    		ac = new ASTConditional(ex, block, elseBlock);
    	}
    	else
    	{
        	ac = new ASTConditional(ex, block);
    	}
    	ac.setSourceInfo(s);
    	return ac;
    }

    /*
     * Parses for Literal non-terminal.
     * 
     * @throws	InvalidSyntaxException
     * @return	ASTConditional with discovered attributes
     */
    public ASTLiteral parseLit() 
    		throws InvalidSyntaxException
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
    		throw new InvalidSyntaxException("Invalid Literal Type");
    	}
    }

    /*
     * Parses for Location non-terminal.
     * 
     * @throws	InvalidSyntaxException
     * @return ASTLocation with discovered attributes
     */
    public ASTLocation parseLoc(String name) 
    		throws InvalidSyntaxException
    {
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
    
    /*
     * Parses for Parameter List non-terminal.
     * 
     * @throws	InvalidSyntaxException
     * @return	ASTFunction.Parameter with discovered attributes
     */
    public ASTFunction.Parameter parseParams() 
    		throws InvalidSyntaxException
    {
    	ASTNode.DataType dt;
    	String name;
    	ASTFunction.Parameter param;
    	dt = parseType();    	
    	name = parseID();
    	
    	param = new ASTFunction.Parameter(name, dt);
    	return param;
    }

    /*
     * Parses for Return non-terminal.
     * 
     * @throws	InvalidSyntaxException
     * @return	ASTReturn with discovered attributes
     */
    public ASTReturn parseReturn() 
    		throws InvalidSyntaxException
    {
    	SourceInfo s = tokens.peek().source;
    	ASTReturn r;
    	df.matchKeyword(tokens, "return");
    	if(df.isNextTokenSymbol(tokens, ";"))
    	{
    		df.matchSymbol(tokens, ";");
    		r = new ASTReturn();
    	}
    	else
    	{
    		ASTExpression ex = parseExpr();

    		df.matchSymbol(tokens, ";");

    		r = new ASTReturn(ex);
    	}
		r.setSourceInfo(s);
		return r;
    }

    /*
     * Parses for Statement non-terminal.
     * 
     * @throws	InvalidSyntaxException	
     * @return	ASTStatement with discovered attributes
     */
    public ASTStatement parseStmnts() 
    		throws InvalidSyntaxException
    {
    	if (df.isNextToken(tokens, Token.Type.ID))
    	{   
    		SourceInfo s = tokens.peek().source;
    		try
    		{
    			// Stmnt -> Void Function Call
    			
    	    	ASTVoidFunctionCall vfc = parseVoidFunc();
    	    	vfc.setSourceInfo(s);
        		return vfc;
    		}
    		catch(InvalidSyntaxException e)
    		{
    			try
    			{
    				ASTLocation loc = parseLoc(tokens.poll().text);
        			
    				df.matchSymbol(tokens, "=");
        			ASTExpression ex = parseExpr();

        			df.matchSymbol(tokens, ";");
        			ASTAssignment at;
        			
        			at = new ASTAssignment(loc, ex);
        			at.setSourceInfo(s);
        			return at;
    			}
        		catch(InvalidSyntaxException ee)
    			{
        			throw new InvalidSyntaxException("Invalid Statement");         			
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
    	throw new InvalidSyntaxException("Invalid Statement");
    }

    /*
     * Parses for Type non-terminal.
     * 
     * @throws	InvalidSyntaxException
     * @return	ASTNode.DataType with discovered attributes
     */
    public ASTNode.DataType parseType() 
    		throws InvalidSyntaxException
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
    	throw new InvalidSyntaxException("Invalid Type");
    }
    
    /*
     * Parses for Unary Expression non-terminal.
     * 
     * @throws	InvalidTokenException
     * @return	ASTUnaryExpr with discovered attributes
     */
    public ASTUnaryExpr parseUnaryExpr() 
    		throws InvalidSyntaxException
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
    		throw new InvalidSyntaxException("Invalid Unary Operator");
    	}
    }

    /*
     * Parses for variable declaration.
     * 
     * @throws	InvalidSyntaxException
     * @return	ASTVariable with discovered attributes
     */
    public ASTVariable parseVar() 
    		throws InvalidSyntaxException
    {
    	String name;
    	ASTNode.DataType type;
    	ASTVariable var;
    	int arrayLen;
    	SourceInfo s = tokens.peek().source;
    	
		if(df.isNextTokenKeyword(tokens, "int") || (df.isNextTokenKeyword(tokens, "bool"))
				|| (df.isNextTokenKeyword(tokens, "void")))
		{    
			type = parseType();
			name = parseID();

			if (df.isNextTokenSymbol(tokens, ";"))
			{
				df.consumeNextToken(tokens);

				var = new ASTVariable(name, type);
				var.setSourceInfo(s);
				return var;
				
			} else if (df.isNextTokenSymbol(tokens, "["))
			{
				df.consumeNextToken(tokens);
				if (df.isNextToken(tokens,Token.Type.DEC))
				{
					arrayLen = Integer.parseInt(tokens.peek().text);
					df.consumeNextToken(tokens);
					df.matchSymbol(tokens, "]");
					df.matchSymbol(tokens, ";");
					var = new ASTVariable(name, type, arrayLen);
					var.setSourceInfo(s);
					return var;
				}
			}
		}		
		throw new InvalidSyntaxException("Invalid Variable");
    }
 
    /*
     * Parses for Void Function Call non-terminal.
     * 
     * @throws 	InvalidTokenException
     * @return	ASTVoidFunctionCall with discovered attributes
     */
    public ASTVoidFunctionCall parseVoidFunc() 
    		throws InvalidSyntaxException
    { 
    	Queue<Token> tkns = new LinkedList<Token>();
    	tkns.addAll(tokens);
    	String name;
    	name = parseID();

		ASTVoidFunctionCall vfc = new ASTVoidFunctionCall(name);
		if(df.isNextTokenSymbol(tokens, "("))
		{
			df.matchSymbol(tokens, "(");
			
			if (!df.isNextTokenSymbol(tokens, ")"))
			{	
				vfc.arguments.addAll(parseArgList());
			}
			
			df.matchSymbol(tokens, ")");
			df.matchSymbol(tokens, ";");

			return vfc;
		}
		else
		{
			tokens.clear();
			tokens.addAll(tkns);
			throw new InvalidSyntaxException("Invalid void function call");
		}

		
    }
    
    /*
     * Parses for While Loop non-terminal.
     * 
     * @throws	InvalidTokenException
     * @return	ASTWhileLoop with discovered attributes
     */
    public ASTWhileLoop parseWhileLoop()
    		throws InvalidSyntaxException
    {
    	ASTWhileLoop wl;
    	ASTExpression ex;
    	ASTBlock block;
    	SourceInfo s = tokens.peek().source;
    	// Expr -> while (Expr)
    	df.matchKeyword(tokens,"while");
    	df.matchSymbol(tokens, "(");
    	ex = parseExpr();
    	df.matchSymbol(tokens, ")");
    	block = parseBlock();
    	
    	wl = new ASTWhileLoop(ex, block);
    	wl.setSourceInfo(s);
    	return wl;
    }      
}
