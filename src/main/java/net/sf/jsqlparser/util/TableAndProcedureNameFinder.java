package net.sf.jsqlparser.util;

import java.util.ArrayList;
import java.util.List;

import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.view.CreateView;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.TableFunction;
import net.sf.jsqlparser.statement.select.WithItem;
import net.sf.jsqlparser.statement.truncate.Truncate;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.upsert.Upsert;

public class TableAndProcedureNameFinder extends TablesNamesFinder {

	private List<Function> listFunctions = null;
	private List<String> listFunctionSignatures = null;
	private List<String> listTableNamesInput = null;
	private List<String> listTableNamesOutput = null;
	private List<String> listTableNamesCreate = null;
	private List<String> additionalTableNames = null;
	private Statement currentStatement = null; 
    private List<String> otherItemNames = null;

	
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
	
	private void init() {
		listFunctions = new ArrayList<Function>();
		listFunctionSignatures = new ArrayList<>();
		listTableNamesInput = new ArrayList<>();
		listTableNamesOutput = new ArrayList<>();
		listTableNamesCreate = new ArrayList<>();
		additionalTableNames = new ArrayList<>();
		currentStatement = null; 
	    otherItemNames = new ArrayList<>();
	}
	
	public void retrieveTablesAndFunctionSignatures(Statement statement) {
		init();
		statement.accept(this);
		listFunctionSignatures = new ArrayList<String>();
        for (Function f : listFunctions) {
        	listFunctionSignatures.add(f.toString());
        }
	}
	
	@Override
	public void visit(Truncate truncate) {
		currentStatement = truncate;
		Table t = truncate.getTable();
		if (t != null) {
			if (additionalTableNames.contains(t.getFullyQualifiedName()) == false) {
				additionalTableNames.add(t.getFullyQualifiedName());
			}
		}
	}
	
	@Override
	public void visit(Select stat) {
		currentStatement = stat;
		super.visit(stat);
	}
	
	@Override
	public void visit(CreateTable stat) {
		currentStatement = stat;
		super.visit(stat);
	}
	
	@Override
	public void visit(CreateView stat) {
		currentStatement = stat;
		super.visit(stat);
	}
	
	@Override
	public void visit(Insert stat) {
		currentStatement = stat;
		super.visit(stat);
	}

	@Override
	public void visit(Update stat) {
		currentStatement = stat;
		super.visit(stat);
	}

	@Override
	public void visit(Upsert stat) {
		currentStatement = stat;
		super.visit(stat);
	}

	@Override
	public void visit(Delete stat) {
		currentStatement = stat;
		super.visit(stat);
	}

    @Override
    public void visit(WithItem withItem) {
        otherItemNames.add(withItem.getName().toLowerCase());
        withItem.getSelectBody().accept(this);
    }

    @Override
    public void visit(Table tableName) {
        String tableWholeName = extractTableName(tableName);
        if (currentStatement instanceof Select) {
            if (!otherItemNames.contains(tableWholeName.toLowerCase()) && !listTableNamesInput.contains(tableWholeName)) {
            	listTableNamesInput.add(tableWholeName);
            }
        } else if (currentStatement instanceof CreateTable || currentStatement instanceof CreateView) {
            if (!listTableNamesCreate.contains(tableWholeName)) {
            	listTableNamesCreate.add(tableWholeName);
            }
        } else if (currentStatement instanceof Insert || currentStatement instanceof Update || currentStatement instanceof Upsert || currentStatement instanceof Delete) {
            if (!otherItemNames.contains(tableWholeName.toLowerCase()) && !listTableNamesOutput.contains(tableWholeName)) {
            	listTableNamesOutput.add(tableWholeName);
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
    
    

	public List<String> getListTableNamesInput() {
		return listTableNamesInput;
	}

	public List<String> getListTableNamesOutput() {
		return listTableNamesOutput;
	}

	public List<String> getListFunctionSignatures() {
		return listFunctionSignatures;
	}
	
}