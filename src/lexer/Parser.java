/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lexer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 *
 * @author Ryan.Solnik
 */
public class Parser {

    Token currentToken;
    String nodeName;
    ArrayList<Token> tokens;
    HashMap<String, Node> createdNodes = new HashMap<>();
    
    int tokenIndex;

    public Parser() {

        Lexer lex = new Lexer();
        this.tokens = lex.execute();
        tokenIndex = 0;
        
        this.currentToken = tokens.get(0);
//    System.out.println(currentToken);

    }

    public void parseInput() {
        parseStrict();
        parseGraphType();
    }

    public void parseGraphType() {
        if (currentToken.tokenValue.equals("digraph")) {
            accept(currentToken, "DIGRAPH");
            accept(currentToken, "ID");
            accept(currentToken, "LEFT_CB");
            parseStatementList();
            accept(currentToken, "RIGHT_CB");

        } else if (currentToken.tokenValue.equals("graph")) {
            accept(currentToken, "GRAPH");
            accept(currentToken, "ID");
            accept(currentToken, "LEFT_CB");
            parseStatementList();
            accept(currentToken, "RIGHT_CB");
        } else {
            System.err.println("Something is wrong on line " + currentToken.lineNumber);
            System.exit(1);
        }

    }

    private void parseStatementList() {

        while (!(currentToken.tokenType.equals("RIGHT_CB"))) {
            parseStatement();
        }
    }

    private void parseStatement() {
        if (currentToken.tokenType.equals("NODE")) {
            parseNodeStatement();
        } 
        
        else if (currentToken.tokenType.equals("ID")) {
            nodeName = currentToken.getValue();
            accept(currentToken, "ID");
            if (currentToken.tokenType.equals("D_EDGEOP") || (currentToken.tokenType.equals("U_EDGEOP"))) {
                parseEdgeStatement(nodeName);
                parseSemicolon();
            }
            else if(currentToken.tokenType.equals("LEFT_BR")){
                parseNodeCreation(nodeName);
            }
            
            else if(currentToken.tokenType.equals("EQUALS")){
                parseSingleAssign();
            }
        } 
        else if (currentToken.tokenType.equals("SUBGRAPH")) {
            parseSubgraph();

        } else {
            System.err.println("Something is wrong on line " + currentToken.lineNumber);
            System.exit(1);
        }
    }
 
    private void parseSubgraph(){
        if(currentToken.tokenType.equals("SUBGRAPH"))
        {
            accept(currentToken, "SUBGRAPH");
            parseID();
            accept(currentToken,"LEFT_CB");
            parseStatementList();
            accept(currentToken,"RIGHT_CB");
        }
    }
    
    private void parseSingleAssign(){
        if(currentToken.tokenType.equals("EQUALS")){
            accept(currentToken,"EQUALS");
            accept(currentToken,"ID");
            parseSemicolon();
        }
    }
    
    private void parseNodeCreation(String nodeName){
        
        addNode(nodeName);
        if(currentToken.tokenType.equals("LEFT_BR")){
            accept(currentToken, "LEFT_BR");
            parseAssignmentList();
            accept(currentToken, "RIGHT_BR");
            parseSemicolon();
        }
        else
        {
            System.err.println("Something is wrong on line " + currentToken.lineNumber);
            System.exit(1);
        }
    }
    
    private void parseNodeStatement() {
        if (currentToken.tokenType.equals("NODE")) {
            accept(currentToken, "NODE");
            parseAttributeList();
            parseSemicolon();
        }

    } 

    private void parseAttributeList() {
        if (currentToken.tokenType.equals("LEFT_BR")) {
            accept(currentToken, "LEFT_BR");
            parseAssignmentList();
            accept(currentToken, "RIGHT_BR");
        }

    }

    private void parseAssignmentList() {
        parseAssignment();

        if (currentToken.tokenType.equals("COMMA")) {
            accept(currentToken, "COMMA");
            parseAssignmentList();
        }
        if (currentToken.tokenType.equals("SEMICOLON")) {
            accept(currentToken, "SEMICOLON");
            parseAssignmentList();
        }
    }

    private void parseAssignment() {
        accept(currentToken, "ID");
        accept(currentToken, "EQUALS");
        accept(currentToken, "ID");

    }

    private void parseStrict() {
        if (currentToken.getValue().equals("strict")) {
            accept(currentToken, "ID");
        }
    }

    private void parseEdgeStatement(String nodeName) {
        parseEdgeType();

        if (currentToken.tokenType.equals("ID")) {
            addParentChild(nodeName,currentToken.getValue());
            accept(currentToken, "ID");
            parseAttributeList();
            

        } else {
            System.err.println("Something is wrong on line " + currentToken.lineNumber);
            System.exit(1);
        }

    }
    
    private void parseEdgeType() {
        if (currentToken.tokenType.equals("D_EDGEOP")) {
            accept(currentToken, "D_EDGEOP");
        } else if (currentToken.tokenType.equals("E_EDGEOP")) {
            accept(currentToken, "E_EDGEOP");
        } else {
            System.err.println("Something is amiss @" + currentToken.lineNumber);
            System.exit(1);
        }
    }

    private void parseSemicolon() {
        if (currentToken.tokenType.equals("SEMICOLON")) {
            accept(currentToken, "SEMICOLON");
        }
    }

    private void parseID() {
        if (currentToken.tokenType.equals("ID")) {
            accept(currentToken, "ID");
        }
    }

    private void nextToken() {

        if (tokens.get(tokenIndex).tokenType.equals("EOF")) {
            System.exit(1);
        } else {
            tokenIndex += 1;
            currentToken = tokens.get(tokenIndex);

        }
    }

    private void accept(Token currentToken, String tokenType) {
        if (currentToken.getType().equals(tokenType)) {
//            System.out.println("Accepted " + currentToken.tokenValue);
            nextToken();

        } else {
            System.err.println("Something is amiss @Line " + currentToken.lineNumber);
            System.exit(1);
        }

    }

    private void addNode(String nodeName){
        
        if(!(createdNodes.containsKey((nodeName))))
        {
          createdNodes.put(nodeName, new Node(nodeName));
          
        }
    }
    
    private void addParentChild(String parent, String child){
        addNode(parent);
        addNode(child);
        
        //
        if(!(createdNodes.get(parent).children.containsKey(child))){
            createdNodes.get(parent).children.put(child, createdNodes.get(child));
//            createdNodes.get(parent).addChild(child, createdNodes.get(child));
        }
    }
    
    public void printNodes(){
        Set<String> nodes = createdNodes.keySet();
        
        //iterates through each created node
        for(String node : nodes){
            
            //prints node name
            System.out.println("Node Name : "+ node);
            
           
            //gets the list of children from the node
            Set<String> kids = createdNodes.get(node).children.keySet();
            
            //iterates through each created child
            for(String kid : kids){
                System.out.println("\tChildren : "+ kid);
            }
        }
    }
}
