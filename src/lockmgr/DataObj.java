package lockmgr;

public class DataObj extends TrxnObj {
    // The data members inherited are
    // XObj:: protected int xid;
    // TrxnObj:: protected String strData;
    // TrxnObj:: protected int lockType;
    // TrxnObj:: public static final int READ = 0;
    // TrxnObj:: public static final int WRITE = 1;

    DataObj() {
        super();
    }

    DataObj(int xid, String strData, int lockType) {
        super(xid, strData, lockType);
    }

    public int hashCode() {
        return strData.hashCode();
    }

    public int key() {
        return strData.hashCode();
    }

    public Object clone() {
        DataObj d = new DataObj(this.xid, this.strData, this.lockType);
        return d;
    }
}