package net.sf.jsqlparser.util;

import java.util.ArrayList;
import java.util.List;

import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.TableFunction;
import net.sf.jsqlparser.statement.truncate.Truncate;

public class TableAndProcedureNameFinder extends TablesNamesFinder {

	private List<Function> listFunctions = new ArrayList<Function>();
	private List<String> listFunctionSignatures = new ArrayList<>();
	private List<String> listTableNames = new ArrayList<>();
	private List<String> additionalTableNames = new ArrayList<>();
	
	@Override
    public void visit(Function function) {
		if (listFunctions.contains(function) == false) {
			listFunctions.add(function);
		}
        ExpressionList exprList = function.getParameters();
        if (exprList != null) {
            visit(exprList);
        }
    }
	
	public void retrieveTablesAndFunctionSignatures(Statement statement) {
		listTableNames = getTableList(statement);
        for (String at : additionalTableNames) {
        	if (listTableNames.contains(at) == false) {
        		listTableNames.add(at);
        	}
        }
		listFunctionSignatures = new ArrayList<String>();
        for (Function f : listFunctions) {
        	listFunctionSignatures.add(f.toString());
        }
	}
	
	@Override
	public void visit(Truncate truncate) {
		Table t = truncate.getTable();
		if (t != null) {
			if (additionalTableNames.contains(t.getFullyQualifiedName()) == false) {
				additionalTableNames.add(t.getFullyQualifiedName());
			}
		}
	}
	
    @Override
    public void visit(TableFunction valuesList) {
    	Function function = valuesList.getFunction();
		if (function != null && listFunctions.contains(function) == false) {
			listFunctions.add(function);
		}
    }

	public List<String> getListTableNames() {
		return listTableNames;
	}

	public List<String> getListFunctionSignatures() {
		return listFunctionSignatures;
	}
	
}