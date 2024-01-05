package de.jlo.talend.model.parser.sql;

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
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.TableFunction;
import net.sf.jsqlparser.statement.select.WithItem;
import net.sf.jsqlparser.statement.truncate.Truncate;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.upsert.Upsert;
import net.sf.jsqlparser.util.TablesNamesFinder;

public class TableAndProcedureNameFinder extends TablesNamesFinder {

	private List<Function> listFunctions = null;
	private List<String> listFunctionSignatures = null;
	private List<String> listTableNamesInput = null;
	private List<String> listTableNamesOutput = null;
	private List<String> listTableNamesCreate = null;
	private Statement currentStatement = null;
    private List<String> otherItemNames = null;
    private List<String> listTempTableNames = null;

	
	@Override
    public void visit(Function function) {
		if (listFunctions.contains(function) == false) {
			listFunctions.add(function);
		}
        ExpressionList<?> exprList = function.getParameters();
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
		currentStatement = null;
	    otherItemNames = new ArrayList<>();
	    listTempTableNames = new ArrayList<>();
	}
	
	/**
	 * Entry method to analyze a Statement
	 * @param statement
	 */
	public void analyse(Statement statement) {
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
		super.visit(truncate);
	}
	
	@Override
	public void visit(Select stat) {
		currentStatement = stat;
		super.visit(stat);
	}
	
    @Override
    public void visit(PlainSelect plainSelect) {
    	currentStatement = plainSelect;
    	super.visit(plainSelect);
    }

	@Override
	public void visit(CreateTable stat) {
		boolean isTemporaryTable = false;
		List<String> options = stat.getCreateOptionsStrings();
		if (options != null && options.size() > 0) {
			for (String option : options) {
				if (option.toLowerCase().contains("temporary")) {
					isTemporaryTable = true;
				}
			}
		}
		if (isTemporaryTable == false) {
			// we do not collect temporary tables
			currentStatement = stat;
		} else {
			currentStatement = null;
			listTempTableNames.add(extractTableName(stat.getTable()));
		}
		super.visit(stat);
	}
	
	@Override
	public void visit(CreateView stat) {
		currentStatement = stat;
		stat.getView().accept(this);
		stat.getSelect().accept((SelectVisitor) this);
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
        otherItemNames.add(withItem.getAlias().getName().toLowerCase());
        withItem.getSelect().accept((SelectVisitor) this);
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
        } else if (currentStatement instanceof Insert || 
        		currentStatement instanceof Update || 
        		currentStatement instanceof Upsert || 
        		currentStatement instanceof Truncate || 
        		currentStatement instanceof Delete) {
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

	public List<String> getListTableNamesCreate() {
		return listTableNamesCreate;
	}

	public List<String> getListTableNamesTemp() {
		return listTempTableNames;
	}

	public List<String> getListFunctionSignatures() {
		return listFunctionSignatures;
	}
	
}