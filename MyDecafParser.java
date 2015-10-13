package edu.jmu.decaf;

import java.util.*;

/**
 * Concrete Decaf parser class.
 */
class MyDecafParser extends DecafParser
{
	private DecafParser df = new DecafParser();
	
    @Override
    public ASTProgram parse(Queue<Token> tokens) throws InvalidSyntaxException
    {
        return parseProgram(tokens);
    }

    public ASTProgram parseProgram(Queue<Token> tokens)
            throws InvalidSyntaxException
    {
        SourceInfo src = getCurrentSourceInfo(tokens);
        ASTProgram program = new ASTProgram();
        program.setSourceInfo(src);
        Token t1;
        Token t2;

        // TODO: parse variable and function definitions
        while (!tokens.isEmpty())
        {
        	t1 = tokens.poll();
        	t2 = tokens.poll();
        }
        
        return program;
    }
    
    public void classChecker(Queue<Token> tokens)
    {
    	int openChecker = 1;
    	int closedChecker = 0;

    	Queue<Token> tokenHolder = new LinkedList<Token>();
    	Token t1;
        if (df.isNextTokenKeyword(tokens, "class"))
        {
        	tokens.poll();
        	if(df.isNextToken(tokens, Token.Type.ID))
        	{
        		tokens.poll();
        		if (df.isNextTokenSymbol(tokens, "{"))
        		{
			    	openChecker = 1;
			    	closedChecker = 0;
					t1 = tokens.poll();
					// CHECK HERE
					tokenHolder.add(t1);
					while (!tokens.isEmpty())
					{
						t1 = tokens.poll();
						tokenHolder.add(t1);
						
						if(df.isNextTokenSymbol(tokens, "{"))
						{
							openChecker++;
						} else if (df.isNextTokenSymbol(tokens, "}"))
						{
							closedChecker++;
							
							if(openChecker == closedChecker)
							{
								//end of method
								parseMethod(tokenHolder);
							}
						}
					}
        		}
        	}
        }
    	
    }
    
    public void methodChecker(Queue<Token> tokens)
    {
    	Token t1;
    	//Token t2;
    	int openChecker = 1;
    	int closedChecker = 0;

    	Queue<Token> tokenHolder = new LinkedList<Token>();

    	if (df.isNextTokenKeyword(tokens, "def"))
    	{
    		t1 = tokens.poll();
    		// next term must be literal type
    		if ((df.isNextTokenKeyword(tokens, "int")) || (df.isNextTokenKeyword(tokens, "bool")) 
    				|| (df.isNextTokenKeyword(tokens, "string")) || (df.isNextTokenKeyword(tokens, "double"))
    				|| (df.isNextTokenKeyword(tokens, "float")))
    		{
    			t1 = tokens.poll();
    			if (df.isNextToken(tokens, Token.Type.ID))
    			{
    				t1 = tokens.poll();
    				if (df.isNextTokenSymbol(tokens, "("))
    				{
    					tokens.poll();
    					if (df.isNextTokenSymbol(tokens, ")"))
    					{
    						tokens.poll();
    						if (df.isNextTokenSymbol(tokens, "{"))
    						{
						    	openChecker = 1;
						    	closedChecker = 0;
								t1 = tokens.poll();
								// CHECK HERE
								tokenHolder.add(t1);
    							while (!tokens.isEmpty())
    							{
    								t1 = tokens.poll();
    								tokenHolder.add(t1);
    								
    								if(df.isNextTokenSymbol(tokens, "{"))
    								{
    									openChecker++;
    								} else if (df.isNextTokenSymbol(tokens, "}"))
    								{
    									closedChecker++;
    									
    									if(openChecker == closedChecker)
    									{
    										//end of method
    										parseMethod(tokenHolder);
    									}
    								}
    							}
    						}
    						
    					} else if ((df.isNextTokenKeyword(tokens, "int")) || (df.isNextTokenKeyword(tokens, "bool")) 
    							|| (df.isNextTokenKeyword(tokens, "string")) || (df.isNextTokenKeyword(tokens, "double"))
    							|| (df.isNextTokenKeyword(tokens, "float")))
    					{
    						tokens.poll();
    						
    						if (df.isNextToken(tokens, Token.Type.ID))
    						{
    							tokens.poll();
    							if ((df.isNextTokenKeyword(tokens, "int")) || (df.isNextTokenKeyword(tokens, "bool")) 
    	    							|| (df.isNextTokenKeyword(tokens, "string")) || (df.isNextTokenKeyword(tokens, "double"))
    	    							|| (df.isNextTokenKeyword(tokens, "float")))
    	    					{
    								tokens.poll();
    	    						if (df.isNextToken(tokens, Token.Type.ID))
    	    						{
    	    							tokens.poll();
    	    							if (df.isNextTokenSymbol(tokens, ")"))
    	    							{
    	    								tokens.poll();
    	    								if (df.isNextTokenSymbol(tokens, "{"))
    	    								{
    	    							    	openChecker = 1;
    	    							    	closedChecker = 0;
    	    									t1 = tokens.poll();
    	    									// CHECK HERE
    	    									tokenHolder.add(t1);
    	    	    							while (!tokens.isEmpty())
    	    	    							{
    	    	    								t1 = tokens.poll();
    	    	    								tokenHolder.add(t1);
    	    	    								
    	    	    								if(df.isNextTokenSymbol(tokens, "{"))
    	    	    								{
    	    	    									openChecker++;
    	    	    								} else if (df.isNextTokenSymbol(tokens, "}"))
    	    	    								{
    	    	    									closedChecker++;
    	    	    									
    	    	    									if(openChecker == closedChecker)
    	    	    									{
    	    	    										//end of method
    	    	    										parseMethod(tokenHolder);
    	    	    									}
    	    	    								}
    	    	    							}
    	    								}
    	    							}
    	    						}	
    	    					}
    						}
    					}
    				}
    			}
    		}
    	} 
    }
    
    public void parseMethod(Queue<Token> tokens)
    {
    }
    
    public void parseClass(Queue<Token> tokens)
    {
    	
    }
    

}

