package lockmgr;

public class TrxnObj extends XObj {
    public static final int READ = 0;

    public static final int WRITE = 1;

    protected String strData = null;

    protected int lockType = -1;

    // The data members inherited are
    // XObj::protected int xid = 0;

    TrxnObj() {
        super();
        this.strData = null;
        this.lockType = -1;
    }

    TrxnObj(int xid, String strData, int lockType) {
        super(xid);
        this.strData = new String(strData);

        if ((lockType == TrxnObj.READ) || (lockType == TrxnObj.WRITE)) {
            this.lockType = lockType;
        } else {
            this.lockType = -1; // invalid lock type.
        }
    }

    public String toString() {
        String outString = new String(super.toString() + "::strData(" + this.strData + ")::lockType(" + this.lockType
                + ")");
        return outString;
    }

    public boolean equals(Object t) {
        if (t == null) {
            return false;
        }

        if (t instanceof TrxnObj) {
            if (this.xid == ((TrxnObj) t).getXId()) {
                if (this.strData.equals(((TrxnObj) t).getDataName())) {
                    if (this.lockType == ((TrxnObj) t).getLockType()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public Object clone() {
        TrxnObj t = new TrxnObj(this.xid, this.strData, this.lockType);
        return t;
    }

    public void setDataName(String strData) {
        this.strData = new String(strData);
    }

    public String getDataName() {
        String strData = new String(this.strData);
        return strData;
    }

    public void setLockType(int lockType) {
        if ((lockType == TrxnObj.READ) || (lockType == TrxnObj.WRITE)) {
            this.lockType = lockType;
        }
    }

    public int getLockType() {
        return this.lockType;
    }
}