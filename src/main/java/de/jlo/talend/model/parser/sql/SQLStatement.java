/**
 * Copyright 2015 Jan Lolling jan.lolling@gmail.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.jlo.talend.model.parser.sql;

import java.io.Serializable;

/**
 * Eintrag in der Historie
 * Klasse kapselt ausgeführte SQL-Statements mit Zusatzinformationen
 */
public class SQLStatement implements Serializable {

	private static final long serialVersionUID = 1L;
	static final public int    OTHER                      = 0;
    static final public int    QUERY                      = 1;
    static final public int    UPDATE                     = 2;
    static final public int    START                      = 3;
    static final public int    EXPLAIN                    = 4;
    static int                 lastIndex                  = -1;
    private int                index;
    protected String           sql;
    protected String           sql_temp;                                 // das eigentliche SQL-Statement
    private long               executedAt                 = 0;           // wann ausgeführt
    private long               durationExec               = 0;
    private long               durationGet                = 0;
    private boolean            successful                 = false;       // erfolgreich ausgeführt ?
    private int                type;
    private int                startPos;
    private int                endPos;
    private int                lineNumber		          = 0;
    private String             message                    = "";
    protected boolean          hidden                     = false;
    public static final String START_SEQUENCE_FOR_SUMMARY = "-[";
    public static final String SQL_PARAM_DELIMITER        = "\u0000";
    public static final String PARAM_PARAM_DELIMITER      = "\u0001";
    private boolean            isPrepared                 = false;
    private boolean            hasNamedParams             = false;
    private boolean            sqlCodeValid               = true; // falls bei der Erstellung erkannt wird, dass der SQLCode fehlerhaft sein muss !
    private String             currentFile                = null;
    private boolean isExecuting = false;
    private boolean isGettingData = false;
    private String currentUrl;
    private String currentUser;
    
    public SQLStatement(String sql) {
        this.sql = sql.replace('\r', ' ');
        type = checkType();
        index = ++lastIndex;
    }

    public SQLStatement() {}

    public String getSQL() {
        return sql;
    }

    public void setSQL(String sql_loc) {
        this.sql = sql_loc.replace('\r', ' ');
        type = checkType();
    }

    public void setStartTime() {
        isExecuting = true;
        this.executedAt = System.currentTimeMillis();
    }

    public void setExecStopTime() {
        isExecuting = false;
        if (type == QUERY) {
            isGettingData = true;
        }
        durationExec = System.currentTimeMillis() - executedAt;
    }

    public void setGetStopTime() {
        isGettingData = false;
        durationGet = (System.currentTimeMillis() - executedAt) + durationExec;
    }

    public long getDurationExec() {
        return durationExec;
    }

    public long getDurationGet() {
        return durationGet;
    }

    public java.util.Date getExecutionDate() {
        return new java.util.Date(executedAt);
    }

    /**
     * Status der Ausführung
     * @return true wenn Statement ausgeführt wurde, false wenn noch nicht gestartet
     */
    public boolean isStarted() {
        if (executedAt == 0) {
            return false;
        } else {
            return true;
        }
    }
    
    public boolean isRunning() {
        return isExecuting || isGettingData;
    }

    public void setTextRange(int startPos_loc, int endPos_loc) {
        this.startPos = startPos_loc;
        this.endPos = endPos_loc;
    }

    public int getStartPos() {
        return startPos;
    }

    public int getEndPos() {
        return endPos;
    }

    public int getIndex() {
        return index;
    }

    static public void resetIndex() {
        lastIndex = -1;
    }

    public void setSuccessful(boolean successful_loc) {
        isGettingData = false;
        isExecuting = false;
        this.successful = successful_loc;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public void setMessage(String message_loc) {
        this.message = message_loc;
    }

    public String getMessage() {
        return message;
    }
    
    @Override
    public boolean equals(Object o) {
        if (o instanceof SQLStatement) {
            final SQLStatement sqlStat = ((SQLStatement) o);
            if ((((getSQL()).trim()).toLowerCase()).equals(((sqlStat.getSQL()).trim()).toLowerCase())) {
                return true;
            } else {
                return false;
            }
        } else {
        	return false;
        }
    }

    public int getType() {
        return type;
    }

    private int checkType() {
        int sqlType;
        sql_temp = sql.trim().toLowerCase();
        if ((sql_temp.startsWith("insert") || sql_temp.startsWith("update")) || sql_temp.startsWith("delete")) {
            sqlType = UPDATE;
        } else if (sql_temp.startsWith("select")) {
            sqlType = QUERY;
        } else if (sql_temp.startsWith("start") || sql_temp.startsWith("@")) {
            sqlType = START;
        } else {
            sqlType = OTHER;
        }
        return sqlType;
    }

    public void setHidden(boolean hidden_loc) {
        this.hidden = hidden_loc;
    }

    public boolean isHidden() {
        return hidden;
    }

    public String getSummaryStr() {
        return START_SEQUENCE_FOR_SUMMARY
                + String.valueOf(index)
                + "|"
                + String.valueOf(successful)
                + "|"
                + String.valueOf(executedAt)
                + "|"
                + String.valueOf(durationExec)
                + "|"
                + String.valueOf(durationGet)
                + "]";
    }

    public void parseSummaryStr(String param) {
        // index
        final int i1 = param.indexOf('|');
        index = Integer.parseInt(param.substring(0, i1));
        // flag successful
        final int i2 = param.indexOf('|', i1 + 2);
        if (((param.substring(i1 + 1, i2)).trim()).equals("true")) {
            successful = true;
        } else {
            successful = false;
        }
        // executedAt
        final int i3 = param.indexOf('|', i2 + 1);
        executedAt = Long.parseLong(param.substring(i2 + 1, i3));
        // durationExec
        final int i4 = param.indexOf('|', i3 + 1);
        durationExec = Long.parseLong(param.substring(i3 + 1, i4));
        // durationGet
        final int i5 = param.indexOf('|', i4 + 1);
        if (i5 != -1) {
            durationGet = Long.parseLong(param.substring(i4 + 1, i5));
            currentFile = param.substring(i5 + 1, param.length());
        } else {
            durationGet = Long.parseLong(param.substring(i4 + 1, param.length()));
        }
    }

    @Override
    public int hashCode() {
        return sql.hashCode();
    }

    public boolean isSqlCodeValid() {
        return sqlCodeValid;
    }
    
    public void setSqlCodeValid(boolean sqlCodeWrong) {
        this.sqlCodeValid = sqlCodeWrong;
    }

	public boolean isHasNamedParams() {
		return hasNamedParams;
	}

	public void setHasNamedParams(boolean hasNamedParams) {
		this.hasNamedParams = hasNamedParams;
	}

	public void setFile(String file) {
		this.currentFile = file;
	}

	public String getFile() {
		return currentFile;
	}

	public boolean isStartStatement() {
		return type == START;
	}

	public String getCurrentUrl() {
		return currentUrl;
	}

	public void setCurrentUrl(String currentUrl) {
		this.currentUrl = currentUrl;
	}

	public String getCurrentUser() {
		return currentUser;
	}

	public void setCurrentUser(String currentUser) {
		this.currentUser = currentUser;
	}
	
	public void setIsExplainStatement() {
		type = EXPLAIN;
	}
    
	public boolean isExplainStatement() {
		return type == EXPLAIN;
	}

	public void setPrepared(boolean b) {
		isPrepared = b;
	}

	public boolean isPrepared() {
		return isPrepared;
	}

	public int getStartLineNumber() {
		return lineNumber;
	}

	public void setStartLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
	}
}
